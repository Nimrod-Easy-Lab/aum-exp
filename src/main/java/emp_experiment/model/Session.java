package emp_experiment.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import emp_experiment.driver.SafeRefactorDriver;
import emp_experiment.utils.CompilerException;
import emp_experiment.utils.Utils;

public class Session {

	private long startTime;
	private String path;
	private int totalProgramsGenerated;
	private int totalProgramsCompiled;
	private String timeoutTestGeneration = "3";
	private File sourcesDir;
	private File classesDir;
	
	private static Logger logger = Logger.getLogger(Utils.LOGGER_NAME);

	private Session(String path) {
		super();
		this.path = path;
		this.setupDirectories(getPath());
	}

	public static Session setup(String output){
		long currentTime = System.currentTimeMillis();
		Session s = new Session(output);
		s.setStartTime(currentTime);
		return s;
	}

	
	public void setupDirectories(String output) {
		this.sourcesDir = new File(output + "/src/");
		this.classesDir = new File(output + "/classes/");
		
		if(!this.sourcesDir.exists()){
			this.sourcesDir.mkdirs();
		}
		if(!this.classesDir.exists()){
			this.classesDir.mkdirs();
		}
	}

	public String getPath() {
		return path;
	}

	public long getStartTime() {
		return startTime;
	}



	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public double elapsedTime() {
		long tDelta = System.currentTimeMillis() - getStartTime();
		return tDelta / 1000.0;
	}
	
	@Deprecated
	public List<String> organizeFilesByProgram() throws IOException{
		String mujavaResultDir = getPath() + "/result/";
		String saferefactorDir = getPath() + "/saferefactor/";
		Set<String> mutantOperators = new HashSet<String>();

		new File(saferefactorDir).mkdirs();

		List<File> javaFiles = Utils.listFilesAndFilesSubDirectories(mujavaResultDir, ".java");
		for (File file : javaFiles) {
			String newDirName = saferefactorDir + file.getName().replace(".java", "");
			if (file.getAbsolutePath().contains("original")) {
				newDirName += "/original/";
				File newPath = new File(newDirName + file.getName());
				newPath.mkdirs();
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else if (file.getAbsolutePath().contains("traditional_mutants")) {
				newDirName += "/mutants/";
				String aux = file.getAbsolutePath();
				int index = aux.lastIndexOf("traditional_mutants");
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);

				File newPath = new File(newDirName + aux);
				newPath.mkdirs();
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);

				index = aux.indexOf("/");
				String mutantOp = aux.substring(0, index);
				mutantOperators.add(mutantOp);

			} else if (file.getAbsolutePath().contains("class_mutants")) {
				newDirName += "/mutants/";
				String aux = file.getAbsolutePath();
				int index = aux.lastIndexOf("class_mutants");
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);

				File newPath = new File(newDirName + aux);
				newPath.mkdirs();
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);

				index = aux.indexOf("/");
				String mutantOp = aux.substring(0, index);
				mutantOperators.add(mutantOp);
			}

			logger.info("File copied to: " + file.getAbsolutePath());
		}

		List<String> mutants = new ArrayList<String>();
		for (String mop : mutantOperators) {
			mutants.add(mop);
		}
		return mutants;
	}
	
	@Deprecated
	public List<String> organizeFilesByOperator() throws IOException{
		String mujavaResultDir = getPath() + "/result/";
		String originalDir = getPath() + SafeRefactorDriver.SAFEREFACTOR_ORIGINAL_DIR;
		String mutantsDir = getPath() + SafeRefactorDriver.SAFEREFACTOR_MUTANTS_DIR;

		new File(originalDir).mkdirs();
		new File(mutantsDir).mkdirs();

		List<File> javaFiles = Utils.listFilesAndFilesSubDirectories(mujavaResultDir, ".java");
		for (File file : javaFiles) {
			if (file.getAbsolutePath().contains("original")) {
				File newPath = new File(originalDir + file.getName());
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else if (file.getAbsolutePath().contains("traditional_mutants")) {
				String aux = file.getAbsolutePath();
				int index = aux.lastIndexOf("traditional_mutants");
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);

				File newPath = new File(mutantsDir + aux);
				newPath.mkdirs();
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else if (file.getAbsolutePath().contains("class_mutants")) {
				String aux = file.getAbsolutePath();
				int index = aux.lastIndexOf("class_mutants");
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);

				File newPath = new File(mutantsDir + aux);
				newPath.mkdirs();
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			logger.info("File copied to: " + file.getAbsolutePath());
		}

		List<File> mutantsDirs = Utils.listDirectories(mutantsDir);
		List<String> mutantOperators = new ArrayList<String>();
		for (File dir : mutantsDirs) {
			mutantOperators.add(dir.getName());
		}
		return mutantOperators;
	}
	

	public String getClassesDir(){
		return classesDir.getAbsolutePath();
	}
	
	public String getSourcesDir(){
		return sourcesDir.getAbsolutePath();
	}
	
	public boolean compileProgram(File testFolder) throws CompilerException{
		totalProgramsGenerated++;
		boolean result = false;
		List<File> javaFiles = Utils.listFilesAndFilesSubDirectories(testFolder.getAbsolutePath(), ".java");		
		if (javaFiles != null && javaFiles.size() > 0) {
			try {
				replacePackageNameOfProblematicPrograms(javaFiles);
				result = compileFiles(javaFiles, testFolder.getName());
				if(result){
					totalProgramsCompiled++;					
				}				
			} catch (IOException e) {
				throw new CompilerException("    " + testFolder.getName() + 
						" didn't compile.");
			} catch (InterruptedException e) {
				throw new CompilerException("    " + testFolder.getName() + 
						" didn't compile."); 
			}
		}
		return result;
	}

	private void replacePackageNameOfProblematicPrograms(List<File> javaFiles) throws IOException {
		for (File file : javaFiles) {
			Utils.replacePackageName(file);
		}
	}
	
	
	private boolean compileFiles(List<File> javaFiles, String testName) throws IOException, InterruptedException {
		// Change class name and file name to avoid compiler confusion
		String filesToCompile = " ";
		for (File javaFile : javaFiles) {
			filesToCompile += javaFile.getAbsolutePath() + " ";
		}
		
		File dest = new File(this.classesDir.getAbsolutePath());
		if(!dest.exists()) {
			dest.mkdirs();
		}

		String javacCmd = "javac " + filesToCompile + " -d " + dest.getAbsolutePath();
		Process pro = Runtime.getRuntime().exec(javacCmd);
//		pro.getErrorStream(); // Logar os erros
		
		InputStream error = pro.getErrorStream();
		String err = "";
		for (int i = 0; i < error.available(); i++) {
        	err += (char) error.read();
        }
        System.out.println(err);
        error = pro.getInputStream();
		for (int i = 0; i < error.available(); i++) {
        	err += (char) error.read();
        }
		pro.waitFor();
		if(pro.exitValue() != 0){
			return false;
		} else {
			return true;
		}

	}
	
	public void createDirectory(File testFolder) {
		File file = new File(getPath() + "/" + testFolder.getName());
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public int getTotalProgramsGenerated() {
		return totalProgramsGenerated;
	}

	public int getTotalProgramsCompiled() {
		return totalProgramsCompiled;
	}
	
	public List<String> getLogInfo(){
		List<String> lines = new ArrayList<String>();
		lines.add("Time: " + elapsedTime() + " seconds");
		lines.add("Total Programs: " + getTotalProgramsGenerated());
		lines.add("Total Compiled: " + getTotalProgramsCompiled());
		
		return lines;
	}

	public String getTimeoutTestGeneration() {
		return timeoutTestGeneration;
	}

	public void setTimeoutTestGeneration(String timeoutTestGeneration) {
		this.timeoutTestGeneration = timeoutTestGeneration;
	}

	public void deleteClassesDir() throws IOException {
		File classesDir = new File(getClassesDir());
		FileUtils.deleteDirectory(classesDir);
	}

}
