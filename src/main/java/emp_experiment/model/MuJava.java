package emp_experiment.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import emp_experiment.driver.MuJavaDriver;
import emp_experiment.utils.CopyFilesException;
import emp_experiment.utils.MutationException;
import emp_experiment.utils.Utils;

public class MuJava implements MutationSystem {

	private static final String MUTANTS_DIR = "/mutants/";

	private static final String RESULT_DIR = "/result/";

	private static final String CLASSES_DIR = "/classes/";

	private static final String SRC_DIR = "/src/";

	private static final String MUJAVA_DIR = "/mujava/";

	private static final String MUTATION_TESTING_TOOL = "mujava";

	private Session session;

	private File sourcesDir;
	private File classesDir;
	private File resultDir;
	private File mutantsDir;

	private int totalMutants;

	public MuJava(Session s) {
		super();
		this.session = s;

	}

	@Override
	public File setupStructure(File testName) throws CopyFilesException {
		File mujavadir = new File(getSession().getPath() + "/" + testName.getName() + MUJAVA_DIR);
		if (!mujavadir.exists()) {
			mujavadir.mkdirs();
		}

		// Cria a estrutura de diretorios que o MuJava precisa
		sourcesDir = new File(mujavadir.getAbsolutePath() + SRC_DIR);
		classesDir = new File(mujavadir.getAbsolutePath() + CLASSES_DIR);
		resultDir = new File(mujavadir.getAbsolutePath() + RESULT_DIR);
		mutantsDir = new File(mujavadir.getAbsolutePath() + MUTANTS_DIR);
		sourcesDir.mkdirs();
		classesDir.mkdirs();
		resultDir.mkdirs();
		mutantsDir.mkdirs();

		try {
			copyOriginalFilesToMujavaDir(testName);
		} catch (IOException e) {
			throw new CopyFilesException("Exception to copy from Original dirrectory:" + e.getMessage());
		}
		return mujavadir;
	}

	private void copyOriginalFilesToMujavaDir(File testName) throws IOException {
		// copio o programa gerado para a pasta src do mujava
		// O Mujava exige que a mesma estrutura de pacotes eja usada.
		List<File> files = Utils.listFilesAndFilesSubDirectories(testName.getAbsolutePath(), "java");
		for (File tmpFile : files) {
			String pack = getPackageFrom(tmpFile);
			File dest = new File(sourcesDir + "/" + pack);
			if (!dest.exists()) {
				dest.mkdirs();
			}
			FileUtils.copyFileToDirectory(tmpFile, dest);
		}
		// Copia tb os .class
		File originalClassFolder = new File(getSession().getClassesDir());
		FileUtils.copyDirectory(originalClassFolder, classesDir);
	}

	@Override
	public void mutate(File testName) throws MutationException {
		// invoca o MuJava para gerar os mutantes (dentro da pasta result/
		// criada acima.)
		MuJavaDriver mujava = new MuJavaDriver();
		try {
			mujava.makeMujavaConfigFile(getSession().getPath() + "/" + testName.getName());

			// Only class-level mutants
			// String[] args = { "-IHD" ,"-IOD" ,"-IOP" ,"-IOR" ,"-ISI"
			// ,"-ISD" ,"-IPC" ,"-PNC" ,"-PMD" ,"-PPD" ,"-PCI"
			// ,"-PCC" ,"-PCD" ,"-PRV" ,"-OMR" ,"-OMD" ,"-OAN"
			// ,"-JTI" ,"-JTD" ,"-JSI" ,"-JSD" ,"-JID" ,"-JDC"
			// ,"-EOA" ,"-EOC" ,"-EAM" ,"-EMM", MUTATION_TESTING_TOOL };

			// ALLALL mutants
			String[] args = { "-ALLALL", MUTATION_TESTING_TOOL };
			// String[] args = { "-ALL", MUTATION_TESTING_TOOL };

			// String[] args = { "-AOIS", MUTATION_TESTING_TOOL };
			mujava.generateMutants(args);

			// Apos gerar os mutantes. Precisa copiar os mesmos para uma pasta a
			// parte. Então copiamos de result/ e colocamos em mutants/
			copyFilesToMutantsDir();

		} catch (IOException e) {
			throw new MutationException(
					"Exception to generate mutants in " + MUTATION_TESTING_TOOL + ": " + e.getMessage());
		} catch (Exception e) {
			throw new MutationException(
					"Exception to generate mutants in " + MUTATION_TESTING_TOOL + ": " + e.getMessage());
		}
	}

	private void copyFilesToMutantsDir() throws IOException {
		List<File> mutantJavaFiles = Utils.listFilesAndFilesSubDirectories(resultDir.getAbsolutePath(), ".java");
		for (File mutantFile : mutantJavaFiles) {
			// Pega a pasta pai, que tem o nome do operador de mutação.
			String className = mutantFile.getName().replaceAll(".java", "");
			String mutationOperator = mutantFile.getParentFile().getName();
			String packageName = getPackageFrom(mutantFile);
			File destination = new File(
					mutantsDir.getAbsolutePath() + "/" + className + "/" + mutationOperator + "/" + packageName);

			if (mutantFile.getAbsolutePath().contains("class_mutants")
					|| (mutantFile.getAbsolutePath().contains("traditional_mutants"))) {

				FileUtils.copyFileToDirectory(mutantFile, destination);
				// Also get the class file
				List<File> classFiles = Utils
						.listFilesAndFilesSubDirectories(mutantFile.getParentFile().getAbsolutePath(), ".class");
				if (classFiles != null && classFiles.size() > 0) {
					for (File classfile : classFiles) {
						FileUtils.copyFileToDirectory(classfile, destination);
					}
				}
				this.totalMutants++;
			}
		}
		System.out.println("Teste");
	}

	private String getPackageFrom(File mutantFile) throws IOException {
		String packageName = Utils.getPackageName(mutantFile);
		packageName = packageName.replaceAll("\\.", "/");
		return packageName;
	}

	@Override
	public List<Mutant> getMutants() {
		// Apos gerar os mutantes. Lista os mutantes que passaremos para o
		// EquivalentMutantDetection
		List<Mutant> mujavaMutants = new ArrayList<Mutant>();
		List<File> mutantClassesDir = Utils.listDirectories(mutantsDir.getAbsolutePath());
		for (File mutantClassDir : mutantClassesDir) {
			List<File> mutantsDir = Utils.listDirectories(mutantClassDir.getAbsolutePath());
			for (File mutantDirectory : mutantsDir) {
				Mutant mutant = new Mutant();
				mutant.setOriginalDir(sourcesDir.getParentFile());
				mutant.setMutantDir(mutantDirectory);
				mutant.setMutationTestingTool(MUTATION_TESTING_TOOL);
				mutant.setNeedCompile(false);
				List<File> mutantFiles = Utils.listFilesAndFilesSubDirectories(mutantDirectory.getAbsolutePath(),
						".java");
				if (mutantFiles != null && mutantFiles.size() > 0) {
					mutant.setMutatedFile(mutantFiles.get(0));
				}

				String mutationOperator = mutantDirectory.getName();
				mutationOperator = mutationOperator.substring(0, mutationOperator.lastIndexOf("_"));
				mutant.setMutationOperator(mutationOperator);

				mujavaMutants.add(mutant);
			}
		}
		return mujavaMutants;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public File getSourcesDir() {
		return sourcesDir;
	}

	public File getClassesDir() {
		return classesDir;
	}

	public File getResultDir() {
		return resultDir;
	}

	public File getTestsDir() {
		return mutantsDir;
	}

	public int getTotalMutants() {
		return totalMutants;
	}

	@Override
	public List<String> getLogInfo() {
		List<String> lines = new ArrayList<String>();
		lines.add("Total Mutants MuJava: " + getTotalMutants());
		return lines;
	}

}
