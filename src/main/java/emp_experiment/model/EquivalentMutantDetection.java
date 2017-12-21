package emp_experiment.model;

import java.io.File;
import java.util.List;

public interface EquivalentMutantDetection {
	public File setupStructure(String testName);
	public void execute(String testName, Mutant mutant);
	public void execute(String testName, List<Mutant> mutants);
	public List<String> getLogInfo();
}
