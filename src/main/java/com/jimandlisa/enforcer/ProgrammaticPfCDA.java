package com.jimandlisa.enforcer;

import org.pfsw.odem.IType;
import org.pfsw.tools.cda.base.model.ClassInformation;
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
			System.out.println("Analyzing " + args[0] + "...");
			WorksetInitializer wsInitializer = new WorksetInitializer(workset);
			wsInitializer.initializeWorksetAndWait(null);
			for (ClassInformation classInfo : workset.getAllContainedClasses()) {
				System.out.println(classInfo);
				for (IType type : classInfo.getDirectReferredTypes()) {
					System.out.println("\t" + type.getName());
				}
			}
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
