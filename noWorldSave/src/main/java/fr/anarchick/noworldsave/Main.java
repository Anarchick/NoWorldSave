package fr.anarchick.noworldsave;

import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import fr.anarchick.agent.Agent;
import fr.anarchick.agent.AgentJar;
import fr.anarchick.agent.AgentLoader;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class Main extends JavaPlugin {

	private static Main instance;
	private static String NMS_CLASS = "net.minecraft.server.level.ChunkProviderServer";
	private static String NMS_SAVE_METHOD = "save";
	private static String PATCH_METHOD_BODY = "{ System.out.println(\"Trying to save chunk!\"); }";
	private static Agent agent = null;
	
	@Override
	public void onEnable() {
		if(instance != null) {
			throw new IllegalStateException("Plugin initialized twice.");
		}
		instance = this;
		//AgentJar.createAgentJar();
		//AgentLoader.loadAgent();
		BukkitScheduler scheduker = Bukkit.getScheduler();
		agent = Agent.getAgent();
		scheduker.runTaskLater(instance, () -> {
			Logging.info("Main loader : " + getClass().getClassLoader());
			Logging.info("Agent in Main loader : " + Agent.class.getClassLoader());
			Logging.info("Agent ? " + agent);
		}, 1L);
		patch();
	}
	
	static public Main getInstance( ) {
		return instance;
	}
	
	public static void patch() {
		ClassPool cp = ClassPool.getDefault();
		try {
			Class<?> clz = Class.forName(NMS_CLASS);
			CtClass ctClass = cp.get(NMS_CLASS);
			CtMethod saveMethod = getMethod(ctClass, NMS_SAVE_METHOD);
			saveMethod.setBody(PATCH_METHOD_BODY);
			redefineClasses(clz, ctClass);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static CtMethod getMethod(CtClass ctClass, String name) {
        return Arrays.stream(ctClass.getMethods())
                .filter(method -> method.getName().equals(name))
                .findAny()
                .orElse(null);
    }
	
	public static boolean isLoaded(String className) {
		Object test = null;
		try {
			Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[] { String.class });
			m.setAccessible(true);
	        ClassLoader cl = ClassLoader.getSystemClassLoader();
	        test = m.invoke(cl, className);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return test != null;
	}
	
	@Override
	public void onDisable() {
		Agent.killAgent();
	}
	
	private static void redefineClasses(Class<?> oldClass, CtClass newClass) {
		try {
			System.out.println("try to redefine class '"+oldClass.getName()+"'");
            Agent.getInstrumentation().redefineClasses(new ClassDefinition(oldClass, newClass.toBytecode()));
        } catch (Exception e) {
        	System.out.println("Failed to redefine class!");
        	e.printStackTrace();
        }
	}
	
}
