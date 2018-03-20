package aumexp.easy.ufal.br;

import static org.junit.Assert.*;

import org.junit.Test;

import emp_experiment.model.Session;

public class CompileTest {

	@Test
	public void test() {
		Session session = Session.setup(System.getProperty("user.dir"));
		assertNotNull(session);
	}

}
