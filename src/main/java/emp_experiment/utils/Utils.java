package emp_experiment.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import emp_experiment.model.Session;

public class Utils {
	public static final String LOGGER_NAME = "emp";
	public static String session = "";
	private static Logger logger = Logger.getLogger(Utils.LOGGER_NAME);

	public static void logSetup(String sessionPath) throws SecurityException, IOException {
		// construct a general file handler
		session = sessionPath;
		String generalLogFile = sessionPath + "/" + LOGGER_NAME + ".log";
		Handler fh1 = new FileHandler(generalLogFile);
		fh1.setFormatter(new SimpleFormatter());
		fh1.setLevel(Level.SEVERE);

		Logger logger = Logger.getLogger(LOGGER_NAME);
		logger.addHandler(fh1);
	}

	public static void logAppend(String path, String fileName, List<String> lines) throws IOException {
		File f = new File(path + "/" + LOGGER_NAME + "_" + fileName + ".log");
		Files.write(f.toPath(), lines, UTF_8, APPEND, CREATE);
	}
	
	public static void logWrite(String path, String fileName, List<String> lines) throws IOException {
		File f = new File(path + "/" + LOGGER_NAME + "_" + fileName + ".log");
		Files.write(f.toPath(), lines, UTF_8, WRITE, CREATE);
	}

	public static void logSRFailedAnalysis(String line) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add(line);
		File f = new File(session + "/" + LOGGER_NAME + "_sr_invalid_tests.log");
		Files.write(f.toPath(), lines, UTF_8, APPEND, CREATE);
	}

	/**
	 * List all files from a directory and its subdirectories (recursive)
	 * 
	 * @param directoryName
	 *            to be listed
	 */
	public static List<File> listFilesAndFilesSubDirectories(String directoryName, String... extensions) {
		List<File> result = new ArrayList<File>();
		File directory = new File(directoryName);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			for (String ext : extensions) {
				if (file.isFile() && file.getName().endsWith(ext)) {
					result.add(file);
				} else if (file.isDirectory()) {
					List<File> temp = listFilesAndFilesSubDirectories(file.getAbsolutePath(), extensions);
					if (temp != null && temp.size() > 0) {
						result.addAll(temp);
					}
				}
			}
		}
		return result;
	}

	/**
	 * List all files from a directory and its subdirectories (recursive)
	 * 
	 * @param directoryName
	 *            to be listed
	 */
	public static List<File> listFilesAndFilesSubDirectories(String directoryName) {
		List<File> result = new ArrayList<File>();
		File directory = new File(directoryName);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {

			if (file.isFile()) {
				result.add(file);
			} else if (file.isDirectory()) {
				List<File> temp = listFilesAndFilesSubDirectories(file.getAbsolutePath());
				if (temp != null && temp.size() > 0) {
					result.addAll(temp);
				}
			}

		}
		return result;
	}

	/**
	 * List all directories from a directory (not recursive)
	 * 
	 * @param directoryName
	 *            to be listed
	 */
	public static List<File> listDirectories(String directoryName) {
		List<File> result = new ArrayList<File>();
		File directory = new File(directoryName);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		if (fList == null)
			fList = new File[0];
		for (File file : fList) {
			if (file.isDirectory()) {
				result.add(file);
			}
		}
		return result;
	}

	public static void replacePackageName(File javaFile) throws IOException {
		// input the file content to the String "input"
		BufferedReader file = new BufferedReader(new FileReader(javaFile));
		String line;
		String input = "";
		String newPackageName = extractPackage(javaFile);

		while ((line = file.readLine()) != null) {
			// Change all line
			if (line.contains("package Package_")) {
				line = "package " + newPackageName + ";";
			} else if(line.contains("import Package_")){
				String[] lineParts = line.trim().split(" ");
				line = "import " + newPackageName.substring(0, newPackageName.lastIndexOf(".")) + 
						"." + lineParts[1];
			}
			input += line + '\n';
		}
		file.close();
		FileOutputStream fileOut = new FileOutputStream(javaFile);
		fileOut.write(input.getBytes());
		fileOut.close();

	}

	private static String extractPackage(File file) {
		boolean stop = false;
		String result = "";
		File testDir = file.getParentFile();
		while (!stop) {
			result = testDir.getName() + "." + result;
			if (result.contains("test")) { // until get diretory testXXX
				stop = true;
			} else {
				testDir = testDir.getParentFile();
			}
		}
		return result.substring(0, result.length() - 1);
	}

	
	public static String getPackageName(File javaFile) throws IOException {
		BufferedReader file = new BufferedReader(new FileReader(javaFile));
		String line;		
		while ((line = file.readLine()) != null) {
			if (line.contains("package test")) {
				return line.substring(line.indexOf("test"), line.length());
			}
			
		}
		file.close();
		return "";
	}

}
