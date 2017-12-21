package emp_experiment.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import emp_experiment.driver.PITDriver;
import emp_experiment.driver.SafeRefactorDriver;
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

	private String getJavaClassName(File file) {

		boolean stop = false;
		String result = file.getName().replaceAll(".class", "");
		File testDir = file.getParentFile();
		while (!stop) {
			result = testDir.getName() + "." + result;
			if (result.contains("test")) { // until get diretory testXXX
				stop = true;
			} else {
				testDir = testDir.getParentFile();
			}
		}
		return result;
	}

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
	public File setupStructure(File testDir) throws IOException {
		// Cria o diretorio do pitest
		File pitestDir = new File(getSession().getPath() + "/" + testDir.getName() + PITEST_DIR);
		if (!pitestDir.exists()) {
			pitestDir.mkdirs();
		}

		// Cria a estrutura de diretorios que o MuJava precisa
		sourcesDir = new File(pitestDir.getAbsolutePath() + SRC_DIR + testDir.getName());
		classesDir = new File(pitestDir.getAbsolutePath() + CLASSES_DIR + testDir.getName());
		mutantsDir = new File(pitestDir.getAbsolutePath() + MUTANTS_DIR);
		sourcesDir.mkdirs();
		classesDir.mkdirs();
		mutantsDir.mkdirs();

		try {
			// copio o programa gerado para a pasta src do mujava
			FileUtils.copyDirectory(testDir, sourcesDir);
			// Copia tb os .class
			File originalClassFolder = new File(getSession().getClassesDir() + "/" + testDir.getName());
			FileUtils.copyDirectory(originalClassFolder, classesDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return pitestDir;
	}

	@Override
	public void mutate(File testName) throws Exception {
		// invoca o pitest para gerar os mutantes (dentro da pasta mutants/
		// criada acima.)
		String pathToClassDir = getSession().getClassesDir() + "/" + testName.getName();
		List<File> classFiles = Utils.listFilesAndFilesSubDirectories(pathToClassDir, ".class");
		int aux = 0;
		for (File classFile : classFiles) {
			String className = getJavaClassName(classFile);
			PITDriver pitest = new PITDriver(mutantsDir.getAbsolutePath(), getSession());
			int total = pitest.mutate(className, Integer.toString(aux));
			totalMutants += total;
			aux++;
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
			
			List<File> mutantFiles = Utils.listFilesAndFilesSubDirectories(mutantDirectory.getAbsolutePath(),".class");
			if(mutantFiles != null && mutantFiles.size()>0){
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
