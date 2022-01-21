package fr.anarchick.agent;

import static fr.anarchick.agent.Tools.getBytesFromStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;

public class AgentJar {

	private static Class[] CLASSES = {ClassPool.class, CtClass.class, CtMethod.class, CtMember.class,
			CtBehavior.class, NotFoundException.class, CannotCompileException.class};
    
	/**
	 * Init java agent file
	 * @return true if loaded
	 */
	public static void createAgentJar() {
		try {
            generateAgentJar(Agent.class, CLASSES);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    /**
     * Generates an agent file to be loaded.
     *
     * @param agent     The main agent class.
     * @param resources Array of classes to be included with agent.
     * @return Returns a temporary jar file with the specified classes included.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static File generateAgentJar(Class agent, Class... resources) throws IOException {
        
    	File jarFile = new File("agent.jar");

        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        // Create manifest stating that agent is allowed to transform classes
        mainAttributes.put(Name.MANIFEST_VERSION, "1.0");
        mainAttributes.put(new Name("Premain-Class"), agent.getName());
        //mainAttributes.put(new Name("Agent-Class"), agent.getName());
        mainAttributes.put(new Name("Can-Retransform-Classes"), "true");
        mainAttributes.put(new Name("Can-Redefine-Classes"), "true");

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile), manifest);

        jos.putNextEntry(new JarEntry(agent.getName().replace('.', '/') + ".class"));

        jos.write(getBytesFromStream(agent.getClassLoader().getResourceAsStream(unqualify(agent))));
        jos.closeEntry();

        for (Class clazz : resources) {
            String name = unqualify(clazz);
            jos.putNextEntry(new JarEntry(name));
            jos.write(getBytesFromStream(clazz.getClassLoader().getResourceAsStream(name)));
            jos.closeEntry();
        }

        jos.close();
        return jarFile;
    }
    
    private static String unqualify(Class clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }
    
}
