package fr.anarchick.noworldsave;

import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Method;
import java.util.Arrays;

import fr.anarchick.agent.Agent;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class Patches {

	private static String NMS_CLASS = "net.minecraft.server.level.ChunkProviderServer";
	private static String NMS_SAVE_METHOD = "save";
	private static String PATCH_METHOD_BODY = "{ System.out.println(\"Trying to save chunk!\"); }";
	
	public static boolean isPatched = false;
	
	public static void patch() {
		isPatched = true;
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
	
	public static byte[] getPatch() {
		ClassPool cp = ClassPool.getDefault();
		byte[] bytes = null;
		try {
			Class<?> clz = Class.forName(NMS_CLASS);
			CtClass ctClass = cp.get(NMS_CLASS);
			CtMethod saveMethod = getMethod(ctClass, NMS_SAVE_METHOD);
			saveMethod.setBody(PATCH_METHOD_BODY);
			bytes = ctClass.toBytecode();
			ctClass.detach();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
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
