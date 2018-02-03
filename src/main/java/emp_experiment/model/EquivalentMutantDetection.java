package emp_experiment.model;

import java.io.File;
import java.util.List;

public interface EquivalentMutantDetection {
	public File setupStructure(File testName);
	public void execute(String testName, Mutant mutant);
	public void execute(MutationSystem mutationSystem);
	public List<String> getLogInfo();
}
