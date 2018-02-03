package emp_experiment.driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.DefaultMutationEngineConfiguration;
import org.pitest.mutationtest.engine.gregor.config.Mutator;
import org.pitest.mutationtest.engine.gregor.inlinedcode.NoInlinedCodeDetection;

import emp_experiment.model.Session;
import emp_experiment.utils.Utils;

/**
 * This class was created as an example of how PIT's mutation engine can be used
 * directly to output mutations of a mutatee to file.
 * 
 * @author Joel van den Berg
 */
public class PITDriver {

	private ClassPathByteArraySource byteSource = new ClassPathByteArraySource();
	private String targetDir;
	private Mutater mutater;
	private Session session;

	/**
	 * Creates a PITMutate object with the default mutators. To change the
	 * mutators another constructor can be made or the mutators collection can
	 * be adapted. All possible mutators are in the {@link Mutator} class. If
	 * the output directory doesn't yet exist, it will be created.
	 * 
	 * @param outputDir
	 *            The directory where to output the mutants.
	 * @param session
	 * @throws Exception
	 */
	public PITDriver(File classesDir, String output, Session s) throws Exception {
		if (output.isEmpty())
			throw new Exception("Output directory can't be <empty>");

		File odir = new File(output);
		if (!odir.exists())
			odir.mkdir();

		targetDir = output;
		session = s;

		//Select ALL mutators
		final Collection<MethodMutatorFactory> mutators = Mutator.all();
		
		
		final DefaultMutationEngineConfiguration engConfig = new DefaultMutationEngineConfiguration(True.<MethodInfo>all(),
				Collections.<String>emptyList(), mutators, new NoInlinedCodeDetection());

		GregorMutationEngine eng = new GregorMutationEngine(engConfig);
//		MutationConfig config = new MutationConfig(eng, Collections.<String>emptyList());		
		
		Collection<File> files = ClassPath.getClassPathElementsAsFiles();
		files.add(classesDir);
		byteSource = new ClassPathByteArraySource(new ClassPath(files));
		mutater = eng.createMutator(byteSource);
		
	}

	

	/**
	 * Mutate the class with the associated class name. The class needs to be on
	 * the classpath before it can be mutated. Multiple mutants will be created
	 * (depending on the mutators that are chosen) and returned in an ArrayList.
	 * Each mutant in the ArrayList can then be written to file with
	 * {@link PITDriver#writeMutantClassToFile(String, byte[], String)} by using
	 * the {@link Mutant#getBytes()} method.
	 * 
	 * @param className
	 *            The name of the class that needs to be mutated.
	 * @return An ArrayList of byte array that represent the mutant.
	 */
	public ArrayList<Mutant> mutateClass(String className) {

//		Set<String> toBeMutated = Collections.singleton(className);
		
		Collection<MutationDetails> details = mutater.findMutations(new ClassName(className));
		Iterator<MutationDetails> iterator = details.iterator();
		ArrayList<Mutant> mutants = new ArrayList<Mutant>();
		while (iterator.hasNext()) {			
			Mutant mutant;
			try {
				mutant = mutater.getMutation(iterator.next().getId());
				mutants.add(mutant);
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
			}			
		}
		return mutants;
	}
	
	
	
	/**
	 * Writes a mutant's byte array to a file. The class name which contains the
	 * package name will be used to create a folder structure in the target
	 * directory. The dir_pre parameter is used to create a directory in the
	 * target directory so that mutants with same class names can be written to
	 * the target directory in different directories. E.g. a class with name
	 * "foo.Bar", when dir_pre is defined as "pre" will end up in
	 * "_targetDir_/pre/foo/Bar.class"
	 * 
	 * @param className
	 *            The name of the class that is written to file.
	 * @param mutant
	 *            The byte array representing the mutant
	 * @param dirPre
	 *            A directory that can be made to put the mutant in, which makes
	 *            it easier to write multiple mutants in the same directory
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void writeMutantClassToFile(String className, byte[] mutant, String dirPre)
			throws FileNotFoundException, IOException {

		String[] dirs = className.split("\\.");
		String reldir = this.targetDir;
		if (dirPre != null && dirPre != "") {
			File dir = new File(reldir, dirPre);
			dir.mkdir();
			reldir = dir.getAbsolutePath();
		}
		for (int i = 0; i < dirs.length - 1; i++) {
			File dir = new File(reldir, dirs[i]);
			if (!dir.exists()) {
				dir.mkdir();
			}
			reldir += File.separatorChar + dir.getName();
		}

		File file = new File(reldir, dirs[dirs.length - 1] + ".class");

		if (!file.exists())
			file.createNewFile();

		FileOutputStream fstream = new FileOutputStream(file);
		fstream.write(mutant);
		fstream.close();
	}

	/**
	 * As a test, this class is mutated and its mutations are written to a file,
	 * which shows the purpose of the dirPre parameter of
	 * {@link PITDriver#writeMutantClassToFile(String, byte[], String)}. The
	 * output directory isn't cleaned beforehand, so that must be done manually
	 * or a recursive method can be written, but that's outside the scope of
	 * this example.
	 * 
	 * @param args
	 */
//	public static void main(String[] args) {
//		String outputDir = "output";
//		Class<?> mutatee = PITDriver.class;
//
//		String name = "MyUnit";
//		
//		try {
//			PITDriver pit = new PITDriver(outputDir);
//			ArrayList<Mutant> mutations = pit.mutateClass(name);
//			int dirPre = 0;
//			for (Mutant mutant : mutations) {
//				pit.writeMutantClassToFile(name, mutant.getBytes(), "pre" + dirPre);
//				dirPre++;
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	
	/**
	 * API to mutate a class file.
	 * The class needs to be on the classpath before it can be mutated(so, use all class name. Eg. "org.package.Class01").
	 * It generates all possible PIT mutants
	 * 
	 * @param className
	 */
	public int mutate(String className, String aux) {
		int totalMutants = 0;
		try {
			ArrayList<Mutant> mutations = this.mutateClass(className);
			int pred = 0;
			totalMutants += mutations.size();
			for (Mutant mutant : mutations) {
				String suffix = "_" + aux + pred;
				this.writeMutantClassToFile(className, mutant.getBytes(), 
						getMutatorName(mutant.getDetails().getMutator()) + suffix);
				logInfo(mutant, className, suffix);
				pred++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalMutants;
	}
	
	private String getMutatorName(String className){
		String[] names = className.split("\\.");
		if(names != null && names.length>0){
			return names[names.length-1];
		} else {
			return className;
		}
	}

	private void logInfo(Mutant mutant, String testName, String aux){
		String line = "File:" + testName;
		line += ". Mutation:" + getMutatorName(mutant.getDetails().getMutator()) + aux;
		line += ". Line:" + mutant.getDetails().getLineNumber();
		line += ". Description:" + mutant.getDetails().getDescription();
		List<String> lines = new ArrayList<String>();
		lines.add(line);
		try {
			Utils.logAppend(session.getPath(), "PITEST_MUTANTS", lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

}
