package emp_experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import emp_experiment.model.Mutant;
import emp_experiment.utils.Utils;

/**
 * Esta estrategia difere da outra (JDollyStrategy), pq nesta não é necessário
 * gerar os programas. Você pode gera-los anteriormente e coloca-los na pasta
 * src/
 * 
 * @author leofernandesmo
 *
 */
public class AUMStrategy {

	private static Controller controller;

	public static void start(Controller c) throws Exception {

		controller = c;
		getAlreadyGeneratedPrograms();

	}

	private static void getAlreadyGeneratedPrograms() throws Exception {
		List<Mutant> allMutants = new ArrayList<Mutant>();
		List<File> dirList = Utils.listDirectories(controller.getSession().getSourcesDir());
		for (File dir : dirList) {
			List<File> programs = Utils.listDirectories(dir.getAbsolutePath());
			sortByTestNumber(programs);
			for (File testDir : programs) {
				//Check if the program was analyzed before
				List<File> tag = Utils.listFilesAndFilesSubDirectories(testDir.getAbsolutePath(), "alredy_analyzed.log");
				if(!tag.isEmpty()){
					System.out.println("Test: " + testDir.getName() + " was analyzed before.");
				} else {
					controller.startAnalysis(testDir, allMutants);
				}
			}

		}

	}

	private static void sortByTestNumber(List<File> files) {
		Collections.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				int n1 = extractNumber(o1.getName());
				int n2 = extractNumber(o2.getName());
				return n1 - n2;
			}

			private int extractNumber(String name) {
				int i = 0;
				try {
					if(name.startsWith("test")){
						String number = name.substring(4, name.length());
						i = Integer.parseInt(number);	
					} else {
						i = 0;
					}
				} catch (Exception e) {
					i = 0; // if filename does not match the format
							// then default to 0
				}
				return i;
			}
		});
	}

}
