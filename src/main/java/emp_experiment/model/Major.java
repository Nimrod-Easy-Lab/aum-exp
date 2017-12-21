package emp_experiment.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import emp_experiment.driver.MajorDriver;
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
	public File setupStructure(File testName) throws IOException {
		File majordir = new File(getSession().getPath() + "/" + testName.getName() + MAJOR_DIR);
		if (!majordir.exists()) {
			majordir.mkdirs();
		}

		// Cria a estrutura de diretorios que o MuJava precisa
		sourcesDir = new File(majordir.getAbsolutePath() + SRC_DIR + testName.getName());
		classesDir = new File(majordir.getAbsolutePath() + CLASSES_DIR + testName.getName());
		mutantsDir = new File(majordir.getAbsolutePath() + MUTANTS_DIR);
		sourcesDir.mkdirs();
		classesDir.mkdirs();
		mutantsDir.mkdirs();

		// copio o programa gerado para a pasta src do mujava
		FileUtils.copyDirectory(testName, sourcesDir);

		return majordir;
	}

	@Override
	public void mutate(File testName) throws Exception {
		// invoca o Major para gerar os mutantes (dentro da pasta result/ criada
		// acima.)

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
		
		List<String> lines = Files.readAllLines(new File(mutantsDir.getAbsolutePath() + "/" + MAJOR_LOG_FILE).toPath());
		for (String line : lines) {
			// 11:ROR:<=(int,int):<(int,int):Triangle@classify:18:a <= 0 |==> a
			// < 0 (line example)
			String[] mutantInfo = line.split(":");
			String number = mutantInfo[0];
			String operator = mutantInfo[1];
			File source = new File(mutantsDir.getAbsolutePath() + "/" + number);
			File target = new File(mutantsDir.getAbsolutePath() + "/" + operator + "_" + number);
			
			FileUtils.moveDirectory(source, target);
			totalMutants++;
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

}
