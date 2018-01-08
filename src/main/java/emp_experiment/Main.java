package emp_experiment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import emp_experiment.model.Dolly;
import emp_experiment.model.EquivalentMutantDetection;
import emp_experiment.model.Major;
import emp_experiment.model.MuJava;
import emp_experiment.model.MutationSystem;
import emp_experiment.model.Pitest;
import emp_experiment.model.SafeRefactor;
import emp_experiment.model.Session;
import emp_experiment.utils.Utils;

public class Main {

	private static final String JDOLLY_SKIP = "jdolly_skip";
	private static final String JDOLLY_ADDCONSTRAINT = "jdolly_addconstraint";
	private static final String JDOLLY_MAXPROGRAMS = "jdolly_maxprograms";
	private static final String PARAM_OUTPUT = "output";
	private static final String PARAM_JDOLLY_SCOPE = "jdolly_scope";
	private static Logger logger = Logger.getLogger(Utils.LOGGER_NAME);
	
	
	private static Session session;
//	private static Dolly dolly;
	private static MutationSystem mujava;
	private static MutationSystem pitest;
	private static MutationSystem major;
	private static EquivalentMutantDetection safe;

	public static void main(String[] args) {

		if (args.length > 0) {
//			Main m = new Main();

			try {
//				Properties prop = loadProperties(args, m);
//				// get property values
//				String jdollyScope = prop.getProperty(PARAM_JDOLLY_SCOPE);
//				String jdollyMaxPrograms = prop.getProperty(JDOLLY_MAXPROGRAMS);
//				String jdollyAddConstraint = prop.getProperty(JDOLLY_ADDCONSTRAINT);
//				String jdollySkip = prop.getProperty(JDOLLY_SKIP);
				
//				String output = prop.getProperty(PARAM_OUTPUT);

//				if (jdollyScope == null || output == null)
//					throw new Exception("Config file is invalid.");

				
				session = Session.setup(args[0]);
//				dolly = new Dolly(session); //Nao estamos mais usando Jdolly integrado. 
				//Os programas precisam ter sidos gerados anteriormente 
				mujava = new MuJava(session);
				pitest = new Pitest(session);
				major = new Major(session);
				safe = new SafeRefactor(session);
				
				Controller controller = Controller.getInstance();
				controller.setSession(session);
//				controller.setDolly(dolly);
				controller.addMutationSystem(mujava);
				controller.addMutationSystem(pitest);
				controller.addMutationSystem(major);
				controller.setEquivalentDetection(safe);
				
//				JDollyStrategy.start(controller);
				AUMStrategy.start(controller);
			
				logger.info("Finished in " + session.elapsedTime() + " seconds ");
				logger.info("Resulted in: " + session.getPath() + " ");

			} catch (Exception exc) {
				exc.printStackTrace();
				logger.severe(exc.getMessage());
			}

		} else {
			System.err.println("Config file not found. You must pass a config file as parameter.");
			System.err.println("Eg.: java emp_experiment.scripts.Main /path/to/config/configfile.config");
			System.err.println("Check a config file example in the root directory of the project.jar");
		}

	}

	private static Properties loadProperties(String[] args, Main m) throws IOException {
		// get config file
		String file = args[0];
		InputStream inputStream = m.getClass().getClassLoader().getResourceAsStream(file);
		Properties prop = new Properties();
		prop.load(inputStream);
		return prop;
	}
	
}
