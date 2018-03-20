package emp_experiment.model;

import java.io.File;
import java.util.List;

import emp_experiment.utils.CopyFilesException;
import emp_experiment.utils.MutationException;

public interface MutationSystem {
	public File setupStructure(File testDir) throws CopyFilesException;
	public void mutate(File testName) throws MutationException;
	public List<Mutant> getMutants();
	public List<String> getLogInfo();
	public File getSourcesDir();
	public File getClassesDir();
}
