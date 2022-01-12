package fr.anarchick.agent;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import fr.anarchick.noworldsave.Logging;
import fr.anarchick.noworldsave.Main;
import javassist.ClassPool;
import javassist.CtClass;

public class Agent implements ClassFileTransformer  {

	private static Instrumentation instrumentation = null;
    private static Agent transformer;
    
    /**
     * Call when the java agent is loaded by the JVM
     * @param args
     * @param instrument
     */
	public static void agentmain(String args, Instrumentation instrument) {
		Logging.info("Agent loaded!");
        // initialization code:
        transformer = new Agent();
        instrumentation = instrument;
        instrumentation.addTransformer(transformer);
    }
	
	public static Agent getAgent() {
		return transformer;
	}
	
	public void redefineClasses(Class<?> oldClass, CtClass newClass) {
		try {
            instrumentation.redefineClasses(new ClassDefinition(oldClass, newClass.toBytecode()));
        } catch (Exception e) {
        	Logging.severe("Failed to redefine class!");
        	e.printStackTrace();
        }
	}

    /**
     * Kills this agent
     */
    public static void killAgent() {
    	if (instrumentation != null)
    			instrumentation.removeTransformer(transformer);
    }
    
    @Override
    public byte[] transform(ClassLoader loader,
    						String className,
    						Class<?> classBeingRedefined,
    						ProtectionDomain protectionDomain,
    						byte[] classfileBuffer) throws IllegalClassFormatException {
		if (!className.startsWith("net/minecraft/")) {
			return classfileBuffer;
		}
    	// We can only profile classes that we can see. If a class uses a custom
		// ClassLoader we will not be able to see it and crash if we try to
		// profile it.
		if (loader != Main.class.getClassLoader()) {
		    return classfileBuffer;
		}
		// Don't profile yourself, otherwise you'll die in a StackOverflow.
		if (className.contains("Agent")) {
		    return classfileBuffer;
		}
		
		//Logging.info("instrumentation of " + className);
		try {
			CtClass original = ClassPool.getDefault().get(classBeingRedefined.getName());
			byte[] bytes = original.toBytecode();
			original.detach();
			return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
    	return classfileBuffer;
    }
	
	private static String unqualify(String clazz) {
        return clazz.replace('.', '/');
    }
	
}
