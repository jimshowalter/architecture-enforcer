package com.jimandlisa.enforcer;

import org.pfsw.tools.cda.base.model.Workset;
import org.pfsw.tools.cda.base.model.workset.ClasspathPartDefinition;
import org.pfsw.tools.cda.core.init.WorksetInitializer;

public class ProgrammaticPfCDA {

	public static void main(String[] args) {
		Workset workset = null;
		try {
			workset = new Workset("ArchitectureEnforcer");
			ClasspathPartDefinition partDefinition = new ClasspathPartDefinition(args[0]);
			workset.addClasspathPartDefinition(partDefinition);
			WorksetInitializer wsInitializer = new WorksetInitializer(workset);
			wsInitializer.initializeWorksetAndWait(null);
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			t.printStackTrace(System.out);
		} finally {
			if (workset != null) {
				try {
					workset.release();
				} catch (Throwable t) {
					System.out.println(t.getMessage());
					t.printStackTrace(System.out);
				}
			}
		}
	}
}
