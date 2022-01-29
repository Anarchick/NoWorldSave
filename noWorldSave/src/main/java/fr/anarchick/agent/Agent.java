package fr.anarchick.agent;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import fr.anarchick.noworldsave.Patches;
import javassist.ClassPool;
import javassist.CtClass;

public class Agent implements ClassFileTransformer  {

	static {
		System.out.println("Try to load agent...");
	}
	
	private static Instrumentation instrumentation = null;
    private static Agent transformer;
    
    public static void premain(String args, Instrumentation instrument) {
    	agentmain(args, instrument);
	}
    
    /**
     * Call when the java agent is loaded by the JVM
     * @param args
     * @param instrument
     */
	public static void agentmain(String args, Instrumentation instrument) {
		System.out.println("Agent loaded! ");
        // initialization code:
        transformer = new Agent();
        instrumentation = instrument;
        instrumentation.addTransformer(transformer);
    }
	
	public static Agent getAgent() {
		return transformer;
	}
	
	public static Instrumentation getInstrumentation() {
		return instrumentation;
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
    	if (!Patches.isPatched && className.equals("net/minecraft/server/level/ChunkProviderServer")) {
    		System.out.println("111111111111111111111111111111111111111");
    		try {
				instrumentation.redefineClasses(new ClassDefinition(classBeingRedefined, Patches.getPatch()));
			} catch (ClassNotFoundException | UnmodifiableClassException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return classfileBuffer;
    }
    
    private byte[] redefine(String className) {
    	try {
    		CtClass original = ClassPool.getDefault().get(className.replace('/', '.'));
			byte[] bytes = original.toBytecode();
			original.detach();
			return bytes;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
}
