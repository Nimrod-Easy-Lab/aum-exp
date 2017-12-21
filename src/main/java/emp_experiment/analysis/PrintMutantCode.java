package emp_experiment.analysis;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PrintMutantCode {

	private static final String MUTANTS_FILE_NAME = "mutants_no_equivalents_";
	private static final String FOUND_MUTANTS = "found behavioral";
	
	private static final String[] OPERATORS = {"AOIS_1", "AOIS_2", "AOIU_1", "JID_1", "JID_2", 
			"JSI_1", "JSI_2", "LOI_1", "SDL_1"};
	private static final String DIR = "/home/leofernandesmo/workspace/mutants/emp_experiment/example/exemplo02/";

	int teste = 0;
	
	public static void main(String[] args) {
		 
		
		System.out.println("INICIO!!");
		
		for (String operator : OPERATORS) {
			List<String> validFiles = listValidAnalysis(operator);
			List<String> contents = readFiles(validFiles, operator);
			writeFile(contents, operator);
			
		}
		System.out.println("FIM!!");

	}

	private static void writeFile(List<String> contents, String operator) {
		try {
			File f = new File(DIR + MUTANTS_FILE_NAME + operator + ".result");
			Files.write(f.toPath(), contents, UTF_8, APPEND, CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<String> listValidAnalysis(String operator) {
		List<String> result = new ArrayList<String>();

		try {
			Path p = new File(DIR + "emp_experiment_" + operator + ".log").toPath();
			List<String> lines = Files.readAllLines(p);
			for (String line : lines) {
				if (line.contains(FOUND_MUTANTS)) {
					String[] words = line.split(" ");
					result.add(words[words.length - 1].trim());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static List<String> readFiles(List<String> validFiles, String operator) {
		List<String> result = new ArrayList<String>();
		String saferefacorDir = DIR + "saferefactor/";
		for (String fileName : validFiles) {
			String path = saferefacorDir + fileName + "/mutants/" + operator + "/" + fileName + ".java";
			try {
				Path p = new File(path).toPath();
				String s = new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
				s = s + "----";
				result.add(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}
