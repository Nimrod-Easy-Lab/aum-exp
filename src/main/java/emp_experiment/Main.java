package emp_experiment;

import java.util.logging.Logger;

import emp_experiment.model.EquivalentMutantDetection;
import emp_experiment.model.Major;
import emp_experiment.model.MuJava;
import emp_experiment.model.MutationSystem;
import emp_experiment.model.Pitest;
import emp_experiment.model.SafeRefactor;
import emp_experiment.model.Session;
import emp_experiment.utils.Utils;

public class Main {
	private static Logger logger = Logger.getLogger(Utils.LOGGER_NAME);	
	
	private static Session session;
	private static MutationSystem mujava;
	private static MutationSystem pitest;
	private static MutationSystem major;
	private static EquivalentMutantDetection safe;

	public static void main(String[] args) {

		if (args.length > 0) {
			try {
				parseArguments(args);
				mujava = new MuJava(session);
				pitest = new Pitest(session);
				major = new Major(session);
				safe = new SafeRefactor(session);
				
				Controller controller = Controller.getInstance();
				controller.setSession(session);
				controller.addMutationSystem(mujava);
				controller.addMutationSystem(pitest);
				controller.addMutationSystem(major);
				controller.setEquivalentDetection(safe);
				
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
	
	private static void parseArguments(String[] args) {
		String arg;		
		session = Session.setup(args[0]);		
		int i = 1;
		while (i < args.length && args[i].startsWith("-")) {
			arg = args[i++];
			if (arg.equals("-timeout")) {
				if (i < args.length)
					session.setTimeoutTestGeneration(args[i++]);
				else
					System.err.println("-timeout requires a time");
			}
		}		
	}
}
