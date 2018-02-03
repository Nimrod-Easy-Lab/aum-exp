package emp_experiment.model;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import emp_experiment.driver.PITDriver;
import emp_experiment.utils.ClassPathHack;
import emp_experiment.utils.CopyFilesException;
import emp_experiment.utils.MutationException;
import emp_experiment.utils.Utils;

public class Pitest implements MutationSystem {

	private static final String MUTATION_TESTING_TOOL = "pitest";
	private static final String MUTANTS_DIR = "/mutants/";
	private static final String CLASSES_DIR = "/classes/";
	private static final String SRC_DIR = "/src/";
	private static final String PITEST_DIR = "/pitest/";
	private static final String PITEST_OUTPUT = "/result/";
	private Session session;
	private String output;

	private File sourcesDir;
	private File classesDir;
	private File mutantsDir;

	private int totalMutants;

	public Pitest(Session s) {
		super();
		this.session = s;
		this.output = session.getPath() + PITEST_OUTPUT;
	}

	// @Override
	// public void mutate() throws Exception {
	// for (String key : getDolly().getClassFiles().keySet()) {
	// List<File> files = getDolly().getClassFiles().get(key);
	// mutate(files);
	// }
	// }
	//
	// @Override
	// public void mutate(File file) throws Exception {
	// PITDriver pitDriver = new PITDriver(this.getOutput());
	// pitDriver.mutate(file.getAbsolutePath(), getOutput());
	// }
	//
	// @Override
	// public void mutate(List<File> files) throws Exception {
	// PITDriver pitDriver = new PITDriver(this.getOutput());
	// for (File file : files) {
	// String className = getJavaClassName(file);
	// pitDriver.mutate(className, getOutput());
	// }
	// }

	

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getOutput() {
		return output;
	}

	public File getSourcesDir() {
		return sourcesDir;
	}

	public File getClassesDir() {
		return classesDir;
	}

	public File getMutantsDir() {
		return mutantsDir;
	}

	@Override
	public File setupStructure(File testDir) throws CopyFilesException  {
		// Cria o diretorio do pitest
		File pitestDir = new File(getSession().getPath() + "/" + testDir.getName() + PITEST_DIR);
		if (!pitestDir.exists()) {
			pitestDir.mkdirs();
		}
		// Cria a estrutura de diretorios que o MuJava precisa
		sourcesDir = new File(pitestDir.getAbsolutePath() + SRC_DIR);
		classesDir = new File(pitestDir.getAbsolutePath() + CLASSES_DIR);
		mutantsDir = new File(pitestDir.getAbsolutePath() + MUTANTS_DIR);
		sourcesDir.mkdirs();
		classesDir.mkdirs();
		mutantsDir.mkdirs();

		try {
			copyOriginalFilesToPitestDir(testDir);
		} catch (IOException e) {
			throw new CopyFilesException("Exception to copy from Original dirrectory:" + e.getMessage());
		}

		return pitestDir;
	}
	
	private void copyOriginalFilesToPitestDir(File testName) throws IOException {
//		List<File> files = Utils.listFilesAndFilesSubDirectories(testName.getAbsolutePath(), "java");
//		for (File tmpFile : files) {
//			String pack = getPackageFrom(tmpFile);
//			File dest = new File(sourcesDir + "/" + pack);
//			if(!dest.exists()) {
//				dest.mkdirs();
//			}
//			FileUtils.copyFileToDirectory(tmpFile, dest);
//		}
		// Copia tb os .class
		File originalClassFolder = new File(getSession().getClassesDir());
		FileUtils.copyDirectory(originalClassFolder, classesDir);
	}
	
	private File getPackageBetween(File classFile, File testName) {
		String lastFolder = testName.getName();
		File startPackage = new File(classFile.getAbsolutePath());
		boolean continueWalk = true;
		while(continueWalk) {
			if(startPackage.getName().equals(lastFolder)) {
				continueWalk = false;
			} else {
				startPackage = startPackage.getParentFile();
			}
		}
		return startPackage;
	}

	@Override
	public void mutate(File testName) throws MutationException {
		// invoca o pitest para gerar os mutantes (dentro da pasta mutants/
		// criada acima.)
		// String pathToClassDir = getSession().getClassesDir() + "/" +
		// testName.getName();
		// List<File> classFiles = Utils.listFilesAndFilesSubDirectories(pathToClassDir,
		// ".class");
		List<File> classFiles = Utils.listFilesAndFilesSubDirectories(classesDir.getAbsolutePath(), "class");
		int aux = 0;
		for (File classFile : classFiles) {
//			ClassPathHack.addFile(this.getClassesDir());
			String className = Utils.getJavaClassNameFromClassFile(classFile);
			
			PITDriver pitest;
			try {
				pitest = new PITDriver(classesDir, mutantsDir.getAbsolutePath(), getSession());
				int total = pitest.mutate(className, Integer.toString(aux));
				totalMutants += total;
				aux++;
			} catch (Exception e) {
				throw new MutationException(
						"Exception to generate mutants in " + MUTATION_TESTING_TOOL + ": " + e.getMessage());
			}
		}
	}

	@Override
	public List<Mutant> getMutants() {
		// Apos gerar. List os mutantes que passaremos para o
		// equivalentMutantDetection
		List<Mutant> pitestMutants = new ArrayList<Mutant>();
		List<File> mutantDirs = Utils.listDirectories(mutantsDir.getAbsolutePath());
		for (File mutantDirectory : mutantDirs) {
			Mutant mutant = new Mutant();
			mutant.setOriginalDir(classesDir.getParentFile());
			mutant.setMutantDir(mutantDirectory);
			mutant.setMutationTestingTool(MUTATION_TESTING_TOOL);
			mutant.setNeedCompile(false);

			List<File> mutantFiles = Utils.listFilesAndFilesSubDirectories(mutantDirectory.getAbsolutePath(), ".class");
			if (mutantFiles != null && mutantFiles.size() > 0) {
				mutant.setMutatedFile(mutantFiles.get(0));
			}

			String mutationOperator = mutantDirectory.getName();
			mutationOperator = mutationOperator.substring(0, mutationOperator.lastIndexOf("_"));
			mutant.setMutationOperator(mutationOperator);

			pitestMutants.add(mutant);
		}
		return pitestMutants;
	}

	public int getTotalMutants() {
		return totalMutants;
	}

	@Override
	public List<String> getLogInfo() {
		List<String> lines = new ArrayList<String>();
		lines.add("Total Mutants Pitest: " + getTotalMutants());
		return lines;
	}

}
