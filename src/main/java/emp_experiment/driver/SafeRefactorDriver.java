package emp_experiment.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import emp_experiment.model.Mutant;
import emp_experiment.utils.Utils;
import saferefactor.core.NimrodImpl;
import saferefactor.core.Parameters;
import saferefactor.core.Report;
import saferefactor.core.SafeRefactor;
import saferefactor.core.SafeRefactorException;
import saferefactor.core.SafeRefactorImp;
import saferefactor.core.generation.TestGeneratorType;
import saferefactor.core.util.Project;
import saferefactor.ui.Main;

public class SafeRefactorDriver {

	private static String srcPath = "";
	private static String binPath = "";
	private static String libPath = "";
	private static String source = "";
	// private static String target = "";
	private static List<String> targets;
	// private static String timeout = "3";
	private static boolean quiet = false;

	private static final String SAFEREFACTOR_MAIN = " saferefactor.ui.Main";
	public static final String SAFEREFACTOR_MUTANTS_DIR = "/saferefactor/mutants/";
	public static final String SAFEREFACTOR_ORIGINAL_DIR = "/saferefactor/original/";

	private emp_experiment.model.SafeRefactor safeRefactorModel;

	private static Logger logger = Logger.getLogger(Utils.LOGGER_NAME);

	private Map<String, List<String>> mutants = new HashMap<String, List<String>>();

	public void execute(String[] args, File classesDir, List<Mutant> mutants,
			emp_experiment.model.SafeRefactor safeRefactor) {

		safeRefactorModel = safeRefactor;

		ArrayList<String> argsToNimrod = new ArrayList<String>();
		argsToNimrod.add("-original");
		argsToNimrod.add(args[0]);
		argsToNimrod.add("-mutants");
		String param = "";
		for (int i = 1; i < args.length; i++) {
			if (i == 1) {
				param = args[i];
			} else {
				param = param + ":" + args[i];
			}
		}
		argsToNimrod.add(param);
		argsToNimrod.add("-testGenerator");
		argsToNimrod.add("evo_suite"); // evo_suite | randoop_ant
		argsToNimrod.add("-timeout");
		argsToNimrod.add(safeRefactorModel.getSession().getTimeoutTestGeneration());
		argsToNimrod.add("-output");
		argsToNimrod.add(classesDir.getParentFile().getAbsolutePath());

		// Verifica se precisa compilar o projeto
		if (mutants.get(0).isNeedCompile()) {
			// Caso eu queira executar apenas com .class
			argsToNimrod.add("-compile");
		}

		String path = System.getProperty("user.dir");
		// Imprimir o comando para o Nimrod
		String comando = "java -cp " + path + "/target/nimrod-0.0.1-SNAPSHOT.jar:" + args[0] + ":" + path
				+ "/lib/* saferefactor.ui.Main ";
		for (String temp : argsToNimrod) {
			comando = comando + temp + " ";
		}
		// System.out.println(comando);
		ArrayList<String> lines = new ArrayList<String>();
		lines.add(comando);
		try {
			Utils.logWrite(classesDir.getParentFile().getAbsolutePath(), "NimrodCommand.sh", lines);
		} catch (IOException e) {
			System.out.println("Error to generate Nimrod command to:" + classesDir.getParentFile().getAbsolutePath());
			e.printStackTrace();
		}

		// try {
		//
		// NimrodImpl sr = (NimrodImpl) Main.startAnalysis(argsToNimrod.toArray(new
		// String[argsToNimrod.size()]));
		//
		// List<String> equivalents = sr.getEquivalents();
		// List<String> duplicateds = sr.getDuplicateds();
		//
		// System.out.println("Finished analysis of " + classesDir.getParent());
		// System.out.println("Tool: " + mutants.get(0).getMutationTestingTool());
		// System.out.println("Equivalents: " + equivalents.size());
		// System.out.println("Duplicateds: " + duplicateds.size());
		// System.out.println("-------------------------------------------------");
		//
		// // Log duplicateds
		// List<String> duplicated_lines = new ArrayList<String>();
		// for (String dup : duplicateds) {
		// String line = args[0] + ":";
		// line += dup;
		// duplicated_lines.add(line);
		// }
		//
		// if (duplicated_lines.size() > 0) {
		// Utils.logAppend(safeRefactorModel.getSession().getPath(), "duplicated",
		// duplicated_lines);
		// }
		//
		// // Log equivalents
		// List<String> equivalent_lines = new ArrayList<String>();
		// for (String pathToEquivalent : equivalents) {
		// for (Mutant mutant : mutants) {
		// if (mutant.getMutantDir().getAbsolutePath().equals(pathToEquivalent)) {
		// mutant.setEquivalent(true);
		// }
		// }
		// String line = args[0] + ":";
		// line += pathToEquivalent;
		// equivalent_lines.add(line);
		// }
		// if (equivalent_lines.size() > 0) {
		// Utils.logAppend(safeRefactorModel.getSession().getPath(), "equivalents_path",
		// equivalent_lines);
		// }
		//
		// reAnalysisDuplicated(sr, args[0]);
		//
		// } catch (Throwable e) {
		// // Se gerar algum erro, tb deve ser logado como falha.
		// safeRefactorModel.addOneToTotalFailedAnalysis();
		// List<String> line = new ArrayList<String>();
		// String textToLog = "";
		// textToLog = "Test " + classesDir.getParent() + " failed analysis.";
		// textToLog += "Result: EXCEPTION";
		// textToLog += "Reason: " + e.getMessage();
		// line.add(textToLog);
		// try {
		// Utils.logAppend(safeRefactorModel.getSession().getPath(), "failed_analysis",
		// line);
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		//
		// System.err.println(e.getMessage());
		// e.printStackTrace();
		// }

	}

	public void execute(String[] args, File classesDir, List<Mutant> mutants,
			emp_experiment.model.SafeRefactor safeRefactor, int x) {

		safeRefactorModel = safeRefactor;

		parseArguments(args);

		File sourceFile = new File(source);
		// File targetFile = new File(target);
		try {
			if (!sourceFile.exists())
				throw new Throwable("Directory not found:" + sourceFile.getAbsolutePath());
			// if (!targetFile.exists())
			// throw new Throwable("Directory not found:" +
			// targetFile.getAbsolutePath());

			File binSource = new File(sourceFile, binPath);
			File srcSource = new File(sourceFile, srcPath);
			File libSource = new File(sourceFile, libPath);

			Project sourceProject = new Project();
			sourceProject.setProjectFolder(sourceFile.getAbsoluteFile());
			sourceProject.setSrcFolder(srcSource);
			sourceProject.setBuildFolder(binSource);
			sourceProject.setLibFolder(libSource);

			List<Project> targetProjects = new ArrayList<Project>();
			for (String target : targets) {
				File targetFile = new File(target);
				File binTarget = new File(targetFile, binPath);
				File srcTarget = new File(targetFile, srcPath);
				File libTarget = new File(targetFile, libPath);

				Project targetProject = new Project();
				targetProject.setProjectFolder(targetFile);
				targetProject.setBuildFolder(binTarget);
				targetProject.setSrcFolder(srcTarget);
				targetProject.setLibFolder(libTarget);

				targetProjects.add(targetProject);
			}

			Parameters parameters = new Parameters();
			// parameters.setKind_of_analysis(Parameters.SAFIRA_ANALYSIS);
			// //Ativando a análise de impacto Leo adicionou esta linha
			parameters.setTimeLimit(Integer.parseInt(safeRefactorModel.getSession().getTimeoutTestGeneration()));
			// Verifica se precisa compilar o projeto
			if (!mutants.get(0).isNeedCompile()) {
				// Caso eu queira executar apenas com .class
				parameters.setCompileProjects(false);
			}

			// SafeRefactor sr = new SafeRefactorImp(sourceProject ,
			// targetProject, parameters );
			NimrodImpl sr = new NimrodImpl(sourceProject, targetProjects, parameters, TestGeneratorType.EVO_SUITE);
			// sr.checkTransformation();
			if (parameters.isCompileProjects()) {
				sr.compileTargets(); 
			}
			sr.checkTransformations(targetProjects);
//			sr.logEquivalents(path); reativar estas linhas
//			sr.logDuplicated(path);

			Report report = sr.getReport();

			List<String> equivalents = sr.getEquivalents();
			List<String> duplicateds = sr.getDuplicateds();

			System.out.println("Finished analysis of " + classesDir.getParent());
			System.out.println("Tool: " + mutants.get(0).getMutationTestingTool());
			System.out.println("Equivalents: " + equivalents.size());
			System.out.println("Duplicateds: " + duplicateds.size());
			System.out.println("-------------------------------------------------");

			// Log duplicateds
			List<String> duplicated_lines = new ArrayList<String>();
			for (String dup : duplicateds) {
				String line = sourceProject.getSrcFolder().getAbsolutePath() + ":";
				line += dup;
				duplicated_lines.add(line);
			}

			if (duplicated_lines.size() > 0) {
				Utils.logAppend(safeRefactorModel.getSession().getPath(), "duplicated", duplicated_lines);
			}

			// Log equivalents
			List<String> equivalent_lines = new ArrayList<String>();
			for (String pathToEquivalent : equivalents) {
				for (Mutant mutant : mutants) {
					if (mutant.getMutantDir().getAbsolutePath().equals(pathToEquivalent)) {
						mutant.setEquivalent(true);
					}
				}
				String line = sourceProject.getSrcFolder().getAbsolutePath() + ":";
				line += pathToEquivalent;
				equivalent_lines.add(line);
			}
			if (equivalent_lines.size() > 0) {
				Utils.logAppend(safeRefactorModel.getSession().getPath(), "equivalents_path", equivalent_lines);
			}

			reAnalysisDuplicated(sr, sourceProject.getSrcFolder().getAbsolutePath());

		} catch (Throwable e) {
			// Se gerar algum erro, tb deve ser logado como falha.
			safeRefactorModel.addOneToTotalFailedAnalysis();
			List<String> line = new ArrayList<String>();
			String textToLog = "";
			textToLog = "Test " + classesDir.getParent() + " failed analysis.";
			textToLog += "Result: EXCEPTION";
			textToLog += "Reason: " + e.getMessage();
			line.add(textToLog);
			try {
				Utils.logAppend(safeRefactorModel.getSession().getPath(), "failed_analysis", line);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			System.err.println(e.getMessage());
			e.printStackTrace();
		}

	}

	private void reAnalysisDuplicated(NimrodImpl sr, String sourceFolder)
			throws Exception, SafeRefactorException, IOException {
		List<String> duplicateds = sr.getDuplicateds();
		System.out.println("Checking false positives in Duplicated Mutants...");
		System.out.println("Total duplicateds before re-analysis: " + duplicateds.size());
		int totalDuplicateds = 0;
		List<String> duplicated_lines_reanalysis = new ArrayList<String>();
		for (String duplicated : duplicateds) {
			String[] programs = duplicated.split(":");

			File binSourceDup = new File(programs[0], binPath);
			File srcSourceDup = new File(programs[0], srcPath);
			File libSourceDup = new File(programs[0], libPath);

			File binTargetDup = new File(programs[1], binPath);
			File srcTargetDup = new File(programs[1], srcPath);
			File libTargetDup = new File(programs[1], libPath);

			Project sourceProjectDup = new Project();
			sourceProjectDup.setProjectFolder(new File(programs[0]));
			sourceProjectDup.setSrcFolder(srcSourceDup);
			sourceProjectDup.setBuildFolder(binSourceDup);
			sourceProjectDup.setLibFolder(libSourceDup);

			Project targetProjectDup = new Project();
			targetProjectDup.setProjectFolder(new File(programs[1]));
			targetProjectDup.setBuildFolder(binTargetDup);
			targetProjectDup.setSrcFolder(srcTargetDup);
			targetProjectDup.setLibFolder(libTargetDup);

			Parameters parametersDup = new Parameters();
			// parameters.setKind_of_analysis(Parameters.SAFIRA_ANALYSIS);
			parametersDup.setTimeLimit(Integer.parseInt(safeRefactorModel.getSession().getTimeoutTestGeneration()));
			parametersDup.setCompileProjects(false);

			SafeRefactor srDuplicateds = new SafeRefactorImp(sourceProjectDup, targetProjectDup, parametersDup,
					TestGeneratorType.EVO_SUITE);
			srDuplicateds.checkTransformation();
			Report report = srDuplicateds.getReport();
			if (report.isRefactoring()) {
				System.out.println(programs[0] + " == " + programs[1]);
				totalDuplicateds++;
				String line = sourceFolder + ":";
				line += sourceProjectDup.getProjectFolder().getAbsolutePath() + ":"
						+ targetProjectDup.getProjectFolder().getAbsolutePath();
				duplicated_lines_reanalysis.add(line);
			}
		}
		Utils.logAppend(safeRefactorModel.getSession().getPath(), "duplicated_reanalysis", duplicated_lines_reanalysis);
		System.out.println("Total duplicateds after re-analysis: " + totalDuplicateds);
	}

	private static void parseArguments(String[] args) {
		boolean vflag = false;
		String arg;
		int i = 0;
		while (i < args.length && args[i].startsWith("-")) {
			arg = args[i++];

			if (arg.equals("-src")) {
				if (i < args.length)
					srcPath = args[i++];
				else
					System.err.println("-src requires a path");
				if (vflag)
					System.out.println("src path= " + srcPath);
			} else if (arg.equals("-bin")) {
				if (i < args.length)
					binPath = args[i++];
				else
					System.err.println("-bin requires a path");
				if (vflag)
					System.out.println("bin path= " + binPath);
			} else if (arg.equals("-lib")) {
				if (i < args.length)
					libPath = args[i++];
				else
					System.err.println("-lib requires a path");
				if (vflag)
					System.out.println("lib path= " + libPath);
				// } else if (arg.equals("-timeout")) {
				// if (i < args.length)
				// timeout = args[i++];
				// else
				// System.err.println("-timeout requires a time");
				// if (vflag)
				// System.out.println("timeout= " + libPath);
			}
		}

		if (i == args.length || i + 1 == args.length)
			System.err.println(
					"Usage: Main [-src path] [-bin path] [-lib path] [-timeout t] original_project_path refactored_project_path");

		source = args[i];
		targets = new ArrayList<String>();
		for (int j = ++i; j < args.length; j++) {
			targets.add(args[j]);
		}
		// target = args[i + 1];
	}

	// Necessário deletar a pasta de diretorios temporarios do SafeRefactor
	// Deleta exemplo: /tmp/SafeRefactor99/
	private void deleteSRTempFolders() throws IOException {
		List<File> sfDirs = Utils.listDirectories("/tmp");
		for (File folder : sfDirs) {
			if (folder.exists() && folder.getName().contains("SafeRefactor")) {
				FileUtils.deleteDirectory(folder);
			}
		}

	}

	/**
	 * Organização dos arquivos por Arquivo Gerado. Ex. Mutant:
	 * ./saferefactor/ClassId_1/mutants/SDL_1/ClassId_1.java Original:
	 * ./saferefactor/ClassId_1/original/ClassId_1.java
	 * 
	 * @param mujavaSession
	 * @param saferefactorDir
	 * @param mutantOperators
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void executeByFile(String mujavaSession, String saferefactorDir, List<String> mutantOperators)
			throws IOException, InterruptedException {
		List<File> dirs = Utils.listDirectories(mujavaSession + "/saferefactor/");

		logger.info("Executing SafeRefactor!!!");
		for (File classId : dirs) {

			// if(classId.getName().contains("8221")){

			String pathOriginal = classId.getAbsolutePath() + "/original/";
			String pathMutants = classId.getAbsolutePath() + "/mutants/";

			List<File> dirMutans = Utils.listDirectories(pathMutants);

			for (File mutantOp : dirMutans) {

				String safeRefactorCommand = makeComand(saferefactorDir);
				safeRefactorCommand += " " + pathOriginal;
				safeRefactorCommand += " " + mutantOp.getAbsolutePath() + "/";

				runCommand(safeRefactorCommand, mutantOp.getName(), classId.getName());

			}

			// }

		}
	}

	/**
	 * Organização dos arquivos por operador de mutação. Ex. Mutant:
	 * ./saferefactor/mutants/SDL_1/ClassId_1.java Original:
	 * ./saferefactor/original/ClassId_1.java
	 * 
	 * @param mujavaSession
	 * @param saferefactorDir
	 * @param mutantOperators
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void executeByOperator(String mujavaSession, String saferefactorDir, List<String> mutantOperators)
			throws IOException, InterruptedException {
		String safeRefactorCommand = makeComand(saferefactorDir);

		for (String operator : mutantOperators) {
			System.out.println("Executing SafeRefactor for Operator: " + operator);
			safeRefactorCommand += " " + mujavaSession + SAFEREFACTOR_ORIGINAL_DIR;
			safeRefactorCommand += " " + mujavaSession + SAFEREFACTOR_MUTANTS_DIR + operator + "/";

			runCommand(safeRefactorCommand, operator, "");
		}

	}

	private void runCommand(String safeRefactorCommand, String mutantOp, String fileName)
			throws IOException, InterruptedException {
		Process pro = Runtime.getRuntime().exec(safeRefactorCommand);
		InputStream err = pro.getErrorStream(); // Logar os erros
		InputStream in = pro.getInputStream();
		pro.waitFor();
		// ERROR InputStream
		processInputStream(err, true, mutantOp, fileName);
		// InputStream
		processInputStream(in, false, mutantOp, fileName);

	}

	private String makeComand(String saferefactorDir) {
		String safeRefactorLibDir = saferefactorDir + "lib/*";
		String safeRefactorBinDir = saferefactorDir + "bin/";
		String safeRefactorCommand = "java -cp " + safeRefactorLibDir + ":" + safeRefactorBinDir + SAFEREFACTOR_MAIN;
		return safeRefactorCommand;
	}

	private void processInputStream(InputStream in, boolean isError, String mutantOp, String fileName)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			out.append(line + "\n");
			if (line.contains("behavioral")) {
				// logger.info(mutantOp + ":" + line);
				System.out.println(mutantOp + ":" + line + " in " + fileName);
				// Checagem do Log gerado pelo SafeRefator.
				// Necessario para validar a análise.
				int numTests = checkSRTempFolder();
				if (numTests > 10) {
					addMutants(mutantOp, line + " " + fileName);
				} else {
					System.out.println("Descartado: " + mutantOp + " para o arquivo " + fileName);
					Utils.logSRFailedAnalysis(
							"failed:" + fileName + ":operator:" + mutantOp + ":num_tests:" + numTests);
				}

			}
			// System.out.println(line);
		}
		if (isError) {
			// System.err.println(out.toString());
		} else {
			// System.out.println(out.toString());
		}
		reader.close();
	}

	private Map<String, List<String>> getMutants() {
		return mutants;
	}

	private void addMutants(String mutantOp, String result) throws IOException {
		if (this.mutants.containsKey(mutantOp)) {
			List<String> list = this.mutants.get(mutantOp);
			list.add(result);
			// if (list.size() % 2 == 0) {
			printListResult();
			// }
		} else {
			List<String> list = new ArrayList<String>();
			list.add(result);
			mutants.put(mutantOp, list);
		}
	}

	private void printListResult() throws IOException {
		for (String mutantOp : getMutants().keySet()) {
			List<String> lines = new ArrayList<String>();
			for (String line : getMutants().get(mutantOp)) {
				lines.add(line);
			}
			Utils.logAppend(null, mutantOp, lines);
		}
		// Clear the mutant Set
		mutants = new HashMap<String, List<String>>();
	}

	private int checkSRTempFolder() throws IOException {
		int result = 0;
		List<File> sfDirs = Utils.listDirectories("/tmp");
		for (File folder : sfDirs) {
			if (folder.isDirectory() && folder.getName().contains("SafeRefactor")) {
				List<File> files = Utils.listFilesAndFilesSubDirectories(folder.getAbsolutePath(), ".txt");
				for (File logFile : files) {
					if (logFile.getName().contains("log_saferefactor_testrunner")) {
						BufferedReader br = new BufferedReader(new FileReader(logFile));
						String sCurrentLine = "";
						while ((sCurrentLine = br.readLine()) != null) {
							sCurrentLine = sCurrentLine.toLowerCase();
							if (sCurrentLine.contains("tests run:")) {
								int start = sCurrentLine.indexOf(":") + 1;
								int end = sCurrentLine.indexOf(",");
								result += Integer.parseInt(sCurrentLine.substring(start, end).trim());
							}
						}
						br.close();
					}
				}
			}
		}
		deleteSRTempFolders();
		return result;
	}

	private void execute(String mujavaSession, String saferefactorDir, List<String> mutantOperators)
			throws IOException, InterruptedException {

		deleteSRTempFolders();
		executeByFile(mujavaSession, saferefactorDir, mutantOperators);
		// executeByOperator(mujavaSession, saferefactorDir, mutantOperators);
	}
}
