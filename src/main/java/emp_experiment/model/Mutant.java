package emp_experiment.model;

import java.io.File;

public class Mutant {
	
	private File originalDir;
	private File mutantDir;
	private File mutatedFile;
	public File getMutatedFile() {
		return mutatedFile;
	}
	public void setMutatedFile(File mutatedFile) {
		this.mutatedFile = mutatedFile;
	}
	private String mutationOperator;
	private String mutationTestingTool;
	private boolean needCompile;
	private boolean isEquivalent;
	
	public File getOriginalDir() {
		return originalDir;
	}
	public void setOriginalDir(File originalDir) {
		this.originalDir = originalDir;
	}
	public File getMutantDir() {
		return mutantDir;
	}
	public void setMutantDir(File mutantDir) {
		this.mutantDir = mutantDir;
	}
	public String getMutationOperator() {
		return mutationOperator;
	}
	public void setMutationOperator(String mutationDescription) {
		this.mutationOperator = mutationDescription;
	}
	public String getMutationTestingTool() {
		return mutationTestingTool;
	}
	public void setMutationTestingTool(String mutationTestingTool) {
		this.mutationTestingTool = mutationTestingTool;
	}
	public boolean isNeedCompile() {
		return needCompile;
	}
	public void setNeedCompile(boolean needCompile) {
		this.needCompile = needCompile;
	}
	public boolean isEquivalent() {
		return isEquivalent;
	}
	public void setEquivalent(boolean isEquivalent) {
		this.isEquivalent = isEquivalent;
	}
	

}
