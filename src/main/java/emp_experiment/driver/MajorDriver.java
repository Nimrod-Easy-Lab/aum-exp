package emp_experiment.driver;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MajorDriver {

	private static final String MAJOR_COMPILER = "lib/major/bin/javac "; 
	
	/**
	 * Comando:
	 * //../../bin/javac -J-Dmajor.export.mutants=true -J-Dmajor.export.directory=./mutants -XMutator:ALL triangle/Triangle.java
	 * @param javaFiles
	 * @param outputDir
	 * @param operators
	 */
	public void generateMutants(List<File> javaFiles, String outputDir, String operators){
		//Lista arquivos java
		String files = " ";
		for (File javaFile : javaFiles) {
			files += javaFile.getAbsolutePath() + " ";
		}
		
		String parametersCommand = "-J-Dmajor.export.mutants=true -J-Dmajor.export.directory="+outputDir;
		parametersCommand += " -XMutator:" + operators;
		String command = MAJOR_COMPILER + parametersCommand + files;
		Process pro;
		try {
			pro = Runtime.getRuntime().exec(command);
			pro.getErrorStream(); // Logar os erros
			pro.getInputStream();
			pro.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
}
