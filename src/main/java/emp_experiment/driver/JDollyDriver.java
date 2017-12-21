package emp_experiment.driver;

import emp_experiment.model.Dolly;

public class JDollyDriver {

	public void execute(Dolly dolly) {
		String[] argsJDolly = { "-scope", dolly.getScopePackages(), dolly.getScopeClasses(), dolly.getScopeMethods(),
				dolly.getScopeFileds(), "-output", dolly.getOutput(), "-maxprograms", dolly.getMaxProrgams(), "-skip",
				dolly.getSkip() };
		jdolly.main.Main.main(argsJDolly);
	}

}
