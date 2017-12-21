package emp_experiment.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dolly {

	private static final String JDOLLY_OUTPUT = "/src/";
	
	private String scopePackages;
	private String scopeClasses;
	private String scopeMethods;
	private String scopeFileds;
	private String scopeClassesId;
	private String scopeBody;
	private String scopeMethodsId;
	private String scopeId;
	private String scopeFiledId;
	
	
	private String skip;
	private String maxPrograms;
	private File constraint; 
	
	private String output;
	
	private Map<String, List<File>> subjectPrograms;
	private Map<String, List<File>> classFiles;

	private Session session;
	
	public Dolly(Session s) {
		super();
		this.session = s;
		this.output = session.getPath() + JDOLLY_OUTPUT;
		this.subjectPrograms = new HashMap<String, List<File>>();
		this.classFiles = new HashMap<String, List<File>>();
	}

	public Dolly(Session s, String jdollyScope, String jdollyAddConstraint, String jdollyMaxPrograms, String jdollySkip) {
		super();
		this.session = s;
		this.skip = jdollySkip;
		this.maxPrograms = jdollyMaxPrograms;
		this.output = session.getPath() + JDOLLY_OUTPUT;
		String[] scopes = jdollyScope.split(" ");
		this.scopePackages = scopes[0];
		this.scopeClasses = scopes[1];
		this.scopeClassesId = scopes[2];
		this.scopeMethods = scopes[3];
		this.scopeMethodsId = scopes[4];
		this.scopeBody = scopes[5];
		this.scopeId = scopes[6];
		this.scopeFileds = scopes[7];
		this.scopeFiledId = scopes[8];
		
		
		this.constraint = new File(jdollyAddConstraint);
		this.subjectPrograms = new HashMap<String, List<File>>();
		this.classFiles = new HashMap<String, List<File>>();
	}

//	// JDolly program generator
//	public void generatePrograms() throws IOException, InterruptedException {
//		JDollyDriver jdd = new JDollyDriver();
//		jdd.execute(this);
//
//		// After generate all programs, load them into this model.
//		loadSubjectPrograms();
////		compile();
//		loadClassFiles();
//	}



//	private void loadSubjectPrograms() throws IOException {
//		List<File> javaFiles = Utils.listFilesAndFilesSubDirectories(getOutput(), ".java");
//		for (File javaFile : javaFiles) {
//			boolean stop = false;
//			File testDir = javaFile.getParentFile();
//			while (!stop) {
//				String test = testDir.getName();
//				if (test.contains("test")) { // until get diretory testXXX
//					stop = true;
//				} else {
//					testDir = testDir.getParentFile();
//				}
//			}
//			// change package name
//			getSession().replacePackageName(javaFile);
//			addSubjectPrograms(testDir.getName(), javaFile);
//		}
//	}

//	private void loadClassFiles() throws IOException {
//		List<File> classFiles = Utils.listFilesAndFilesSubDirectories(getSession().getClassesDir(), ".class");
//		for (File classFile : classFiles) {
//			boolean stop = false;
//			File testDir = classFile.getParentFile();
//			while (!stop) {
//				String test = testDir.getName();
//				if (test.contains("test")) { // until get diretory testXXX
//					stop = true;
//				} else {
//					testDir = testDir.getParentFile();
//				}
//			}
//			addClassFiles(testDir.getName(), classFile);
//		}
//	}

	public String getScopeClassesId() {
		return scopeClassesId;
	}

	public String getScopeBody() {
		return scopeBody;
	}

	public String getScopeMethodsId() {
		return scopeMethodsId;
	}

	public String getScopeId() {
		return scopeId;
	}

	public String getScopeFiledId() {
		return scopeFiledId;
	}

	public String getScopePackages() {
		return scopePackages;
	}

	public void setScopePackages(String scopePackages) {
		this.scopePackages = scopePackages;
	}

	public String getScopeClasses() {
		return scopeClasses;
	}

	public void setScopeClasses(String scopeClasses) {
		this.scopeClasses = scopeClasses;
	}

	public String getScopeMethods() {
		return scopeMethods;
	}

	public void setScopeMethods(String scopeMethods) {
		this.scopeMethods = scopeMethods;
	}

	public String getScopeFileds() {
		return scopeFileds;
	}

	public void setScopeFileds(String scopeFileds) {
		this.scopeFileds = scopeFileds;
	}

	public String getSkip() {
		return skip;
	}

	public void setSkip(String skip) {
		this.skip = skip;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public Map<String, List<File>> getSubjectPrograms() {
		return subjectPrograms;
	}

	public void addSubjectPrograms(String key, File subject) {
		if (getSubjectPrograms().containsKey(key)) {
			getSubjectPrograms().get(key).add(subject);
		} else {
			List<File> files = new ArrayList<>();
			files.add(subject);
			getSubjectPrograms().put(key, files);
		}
	}

	public Map<String, List<File>> getClassFiles() {
		return this.classFiles;
	}

	public void addClassFiles(String key, File subject) {
		if (getClassFiles().containsKey(key)) {
			getClassFiles().get(key).add(subject);
		} else {
			List<File> files = new ArrayList<>();
			files.add(subject);
			getClassFiles().put(key, files);
		}
	}

	public String getMaxProrgams() {
		return this.maxPrograms;
	}
	
	
	public String[] args(){
//		String[] args = { "-scope", getScopePackages(), getScopeClasses(), getScopeMethods(), getScopeFileds(), 
//				"-output", getOutput(), 
//				"-maxprograms", getMaxProrgams(),
//				"-addconstraints", getConstraint().getPath(),
//				"-skip", getSkip() };
		
		List<String> parameters = new ArrayList<String>();
		//Mandatory
		parameters.add("-scope");
		parameters.add(getScopePackages());
		parameters.add(getScopeClasses());
		parameters.add(getScopeClassesId());
		parameters.add(getScopeMethods());
		parameters.add(getScopeMethodsId());
		parameters.add(getScopeBody());
		parameters.add(getScopeId());
		parameters.add(getScopeFileds());
		parameters.add(getScopeFiledId());
		parameters.add("-output");
		parameters.add(getOutput());
		//Optionals
		if(getMaxProrgams() != null){
			parameters.add("-maxprograms");
			parameters.add(getMaxProrgams());
		}
		if(getConstraint() != null && getConstraint().exists()){
			parameters.add("-addconstraints");
			parameters.add(getConstraint().getAbsolutePath());
		}
		if(getSkip() != null && !getSkip().equals("0")){
			parameters.add("-skip");
			parameters.add(getSkip());
		}
		
		String[] args = parameters.toArray(new String[parameters.size()]);
		
		return args;
	}

	public File getConstraint() {
		return constraint;
	}

	

}
