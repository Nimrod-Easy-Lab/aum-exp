package emp_experiment.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface MutationSystem {
	public File setupStructure(File testDir) throws IOException;
	public void mutate(File testName) throws Exception;
	public List<Mutant> getMutants();
	public List<String> getLogInfo();
}
