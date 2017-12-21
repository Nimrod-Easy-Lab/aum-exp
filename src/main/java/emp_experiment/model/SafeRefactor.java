package emp_experiment.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import emp_experiment.driver.SafeRefactorDriver;

public class SafeRefactor implements EquivalentMutantDetection {
	private static final String SAFEREFACTOR_DIR = "/saferefactor/";
	private static final String NON_EQUIVALENT_DIR = "/non-equivalents/";
	private static final String EQUIVALENT_DIR = "/equivalents/";
	private Session session;
	private int totalEquivalents;
	private int totalNonEquivalents;
	private int totalFailedAnalysis;

	public SafeRefactor(Session s) {
		super();
		this.session = s;
	}

	@Override
	public File setupStructure(String folderName) {
		// Cria o diretorio do MuJava
		File safeRefactorDir = new File(getSession().getPath() + "/" + folderName + SAFEREFACTOR_DIR);
		if (!safeRefactorDir.exists()) {
			safeRefactorDir.mkdirs();
		}
		File equivalent = new File(safeRefactorDir.getAbsolutePath() + EQUIVALENT_DIR);
		File nonEquivalent = new File(safeRefactorDir.getAbsolutePath() + NON_EQUIVALENT_DIR);
		equivalent.mkdirs();
		nonEquivalent.mkdirs();

		return safeRefactorDir;
	}
	
	@Override
	public void execute(String testName, List<Mutant> mutants) {
		String[] args = new String[mutants.size()];
		args[0] = mutants.get(0).getOriginalDir().getAbsolutePath();
		for (int i = 1; i < mutants.size(); i++) {
			Mutant mutant = mutants.get(i);
			args[i] = mutant.getMutantDir().getAbsolutePath();
		}
		
//		String[] args = { mutant.getOriginalDir().getAbsolutePath(), 
//				mutant.getMutantDir().getAbsolutePath() };

		new SafeRefactorDriver().execute(args, testName, mutants, this);
	}
	

	@Override
	public void execute(String testName, Mutant mutant) {

		String[] args = { mutant.getOriginalDir().getAbsolutePath(), 
				mutant.getMutantDir().getAbsolutePath() };


//		new SafeRefactorDriver().execute(args, testName, mutant, this);

	}

	public Session getSession() {
		return session;
	}

	@Override
	public List<String> getLogInfo() {
		List<String> lines = new ArrayList<String>();
		lines.add("Total Equivalents According to SafeRefactor: " + getTotalEquivalents());
		lines.add("Total NON-Equivalents According to SafeRefactor: " + getTotalNonEquivalents());
		lines.add("Total Failed Analysis: " + getTotalFailedAnalysis());
		return lines;
	}
	
	public void copyToEquivalentDir(File sourceDir, String folderName){
		try {
			File destiny = new File(getSession().getPath() + "/" + folderName + SAFEREFACTOR_DIR+ EQUIVALENT_DIR);
			FileUtils.copyDirectoryToDirectory(sourceDir, destiny);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void copyToNonEquivalentDir(File sourceDir, String folderName){
		try {
			File destiny = new File(getSession().getPath() + "/" + folderName + SAFEREFACTOR_DIR + NON_EQUIVALENT_DIR);
			FileUtils.copyDirectoryToDirectory(sourceDir, destiny);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getTotalEquivalents() {
		return totalEquivalents;
	}

	public int getTotalNonEquivalents() {
		return totalNonEquivalents;
	}

	public void addOneToNonEquivalents() {
		totalNonEquivalents++;
	}

	public void addOneToEquivalents() {
		totalEquivalents++;
	}
	
	public int getTotalFailedAnalysis() {
		return totalFailedAnalysis;
	}
	
	
	public void addOneToTotalFailedAnalysis() {
		totalFailedAnalysis++;
	}

	

}
