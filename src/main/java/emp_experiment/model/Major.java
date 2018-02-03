package emp_experiment.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import emp_experiment.driver.MajorDriver;
import emp_experiment.utils.CopyFilesException;
import emp_experiment.utils.MutationException;
import emp_experiment.utils.Utils;

public class Major implements MutationSystem {

	private static final String MAJOR_LOG_FILE = "mutants.log";
	private static final String MUTANTS_DIR = "/mutants/";
	private static final String CLASSES_DIR = "/classes/";
	private static final String SRC_DIR = "/src/";
	private static final String MAJOR_DIR = "/major/";
	private static final String MUTATION_TESTING_TOOL = "major";

	private Session session;

	private File sourcesDir;
	private File classesDir;
	private File mutantsDir;

	private int totalMutants;

	public Major(Session s) {
		super();
		this.session = s;
	}

	@Override
	public File setupStructure(File testName) throws CopyFilesException {
		File majordir = new File(getSession().getPath() + "/" + testName.getName() + MAJOR_DIR);
		if (!majordir.exists()) {
			majordir.mkdirs();
		}

		// Cria a estrutura de diretorios que o MuJava precisa
		sourcesDir = new File(majordir.getAbsolutePath() + SRC_DIR);
		classesDir = new File(majordir.getAbsolutePath() + CLASSES_DIR);
		mutantsDir = new File(majordir.getAbsolutePath() + MUTANTS_DIR);
		sourcesDir.mkdirs();
		classesDir.mkdirs();
		mutantsDir.mkdirs();

		try {
			copyOriginalFilesToMujavaDir(testName);
		} catch (IOException e) {
			throw new CopyFilesException("Exception to copy from Original dirrectory:" + e.getMessage());
		}

		return majordir;
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
		File originalClassFolder = new File(getSession().getClassesDir() + "/" + testName.getName());
		FileUtils.copyDirectory(originalClassFolder, classesDir);
	}

	private File getPackageBetween(File classFile, File testName) {
		String lastFolder = testName.getName();
		File startPackage = new File(classFile.getAbsolutePath());
		boolean continueWalk = true;
		while (continueWalk) {
			if (startPackage.getName().equals(lastFolder)) {
				continueWalk = false;
			} else {
				startPackage = startPackage.getParentFile();
			}
		}
		return startPackage;
	}

	@Override
	public void mutate(File testName) throws MutationException {
		// invoca o Major para gerar os mutantes (dentro da pasta result/ criada
		// acima.)

		try {
			List<File> javaFiles = Utils.listFilesAndFilesSubDirectories(testName.getAbsolutePath(), ".java");
			MajorDriver majorDriver = new MajorDriver();
			// Caso queira especificar, basta separar por virgula
			// Ex.(AOR,LOR,ROR,...)
			String operators = "ALL";
			majorDriver.generateMutants(javaFiles, mutantsDir.getAbsolutePath(), operators);

			// Apos gerar os mutantes. (O Major colocar os mutantes em pastas
			// numeradas).Precisa renomear as pastas numeradas com os operadores de
			// mutação usados. Ler o arquivo de confirguração e para cada linha,
			// alteramos a pasta. E logamos as informações do mutante
			File logSource = new File(MAJOR_LOG_FILE);
			FileUtils.deleteQuietly(new File(mutantsDir.getAbsolutePath() + "/" + MAJOR_LOG_FILE));
			FileUtils.moveFileToDirectory(logSource, mutantsDir, false);

			// List<File> originalJavaFiles =
			// Utils.listFilesAndFilesSubDirectories(sourcesDir.getAbsolutePath(), ".java");

			List<String> lines = Files
					.readAllLines(new File(mutantsDir.getAbsolutePath() + "/" + MAJOR_LOG_FILE).toPath());
			for (String line : lines) {
				// 11:ROR:<=(int,int):<(int,int):Triangle@classify:18:a <= 0 |==> a
				// < 0 (line example)
				String[] mutantInfo = line.split(":");
				String number = mutantInfo[0];
				String operator = mutantInfo[1];
				File source = new File(mutantsDir.getAbsolutePath() + "/" + number);
				File target = new File(mutantsDir.getAbsolutePath() + "/" + operator + "_" + number);

				if (target.exists()) {
					FileUtils.deleteDirectory(target);
				}

				// for (File originalJavaFile : originalJavaFiles) {
				// FileUtils.copyFileToDirectory(originalJavaFile, target);
				// }

				FileUtils.moveDirectory(source, target);
				totalMutants++;
			}
		} catch (Exception e) {
			throw new MutationException(
					"Exception to generate mutants in " + MUTATION_TESTING_TOOL + ": " + e.getMessage());
		}
	}

	@Override
	public List<Mutant> getMutants() {
		// Apos gerar. List os mutantes que passaremos para o
		// equivalentMutantDetection
		List<Mutant> majorMutants = new ArrayList<Mutant>();
		List<File> mutantDirs = Utils.listDirectories(mutantsDir.getAbsolutePath());
		for (File mutantDirectory : mutantDirs) {
			Mutant mutant = new Mutant();
			mutant.setOriginalDir(sourcesDir.getParentFile());
			mutant.setMutantDir(mutantDirectory);
			mutant.setMutationTestingTool(MUTATION_TESTING_TOOL);
			mutant.setNeedCompile(true);

			List<File> mutantFiles = Utils.listFilesAndFilesSubDirectories(mutantDirectory.getAbsolutePath(), ".java");
			if (mutantFiles != null && mutantFiles.size() > 0) {
				mutant.setMutatedFile(mutantFiles.get(0));
			}

			String mutationOperator = mutantDirectory.getName();
			mutationOperator = mutationOperator.substring(0, mutationOperator.lastIndexOf("_"));
			mutant.setMutationOperator(mutationOperator);
			majorMutants.add(mutant);
		}
		return majorMutants;
	}

	private String getPackageFrom(File mutantFile) throws IOException {
		String packageName = Utils.getPackageName(mutantFile);
		packageName = packageName.replaceAll("\\.", "/");
		return packageName;
	}

	@Override
	public List<String> getLogInfo() {
		List<String> lines = new ArrayList<String>();
		lines.add("Total Mutants Major: " + getTotalMutants());
		return lines;
	}

	public Session getSession() {
		return session;
	}

	public int getTotalMutants() {
		return totalMutants;
	}

	@Override
	public File getSourcesDir() {
		return this.sourcesDir;
	}

	@Override
	public File getClassesDir() {
		return this.classesDir;
	}

}
