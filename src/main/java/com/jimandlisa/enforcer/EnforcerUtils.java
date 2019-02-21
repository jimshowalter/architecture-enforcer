// Copyright 2019 jimandlisa.com.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package com.jimandlisa.enforcer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pfsw.odem.IType;
import org.pfsw.tools.cda.base.model.ClassInformation;
import org.pfsw.tools.cda.base.model.Workset;
import org.pfsw.tools.cda.base.model.workset.ClasspathPartDefinition;
import org.pfsw.tools.cda.core.init.WorksetInitializer;

public class EnforcerUtils {
	
	static Set<String> ignores(File ignoresFile) throws Exception {
		Set<String> ignores = new HashSet<>();
		if (ignoresFile == null) {
			return ignores;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(ignoresFile))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String ignore = line.trim();
				if (ignore.startsWith("#")) {
					continue;
				}
				if (ignore.endsWith("!")) {
					ignores.add(ignore.replaceAll("[!]+$", ""));
					continue;
				}
				if (ignore.endsWith(".")) {
					ignores.add(ignore.replaceAll("[.]+$", "."));
					continue;
				}
				ignores.add(ignore + ".");
			}
		}
		return ignores;
	}
	
	static boolean skip(String typeName, Set<String> ignores) {
		if (typeName.contains(":")) { // Handles glitches in pf-CDA, for example "evelField:Ljava.lang.Object".
			return true;
		}
		for (String ignore : ignores) {
			if (typeName.startsWith(ignore)) {
				return true;
			}
		}
		return false;
	}
	
	static boolean skip(String referringTypeName, String referredToTypeName, Set<String> ignores) {
		if (referringTypeName.equals(referredToTypeName)) {
			return true; // Skip self-references.
		}
		return skip(referredToTypeName, ignores);
	}
	
	static String denest(String typeName, Flags flags) {
		if (typeName.startsWith("$")) {
			// TODO: Classes can start with dollar signs, so this is a problem. See if pf-CDA provides a way to determine if a class is nested, and, if so, to get its outermost class.
			throw new EnforcerException("malformed class name '" + typeName + "'", Errors.MALFORMED_CLASS_NAME);
		}
		if (flags.preserveNestedTypes()) {
			return typeName;
		}
		return typeName.replaceAll("[$].*$", "");
	}
	
	static Type get(String typeName, Map<String, Type> types) {
		Type type = types.get(typeName);
		if (type == null) {
			type = new Type(typeName);
			types.put(type.name(), type);
		}
		return type;
	}
	
	static void parse(File file, Map<String, Type> types, Set<String> ignores, Set<Problem> problems, String entryName, boolean requireReferredTo, Flags flags) throws Exception {
		if (file == null) {
			return;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			Type type = null;
			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();
				if (trimmed.isEmpty()) {
					continue;
				}
				if (trimmed.startsWith("#")) {
					continue;
				}
				String[] segments = trimmed.replaceAll("[ \t]+", "").split(":");
				if (segments.length > 2) {
					throw new EnforcerException("invalid " + entryName + " entry in " + file + ": " + line, Errors.MALFORMED_CLASS_TO_CLASS_REFERENCE);
				}
				if (requireReferredTo) {
					if (segments.length < 2) {
						throw new EnforcerException("invalid " + entryName + " entry in " + file + ": " + line, Errors.MISSING_REFERRED_TO_CLASS);
					}
				}
				String referringClass = denest(segments[0], flags);
				if (skip(referringClass, ignores)) {
					problems.add(new Problem(entryName.toUpperCase() + " class is listed as referring but also listed in ignores: " + referringClass, Errors.CLASS_BOTH_REFERRING_AND_IGNORED));
					continue;
				}
				type = get(referringClass, types);
				String[] segments2 = segments[1].split(",");
				for (String segment : segments2) {
					String referredToClass = denest(segment, flags);
					if (skip(referredToClass, ignores)) {
						problems.add(new Problem(entryName.toUpperCase() + " class is listed as referred-to but also listed in ignores: " + referringClass, Errors.CLASS_BOTH_REFERRED_TO_AND_IGNORED));
						continue;
					}
					type.referenceNames().add(referredToClass);
				}
			}
		}
	}
	
	static void resolve(Map<String, Type> types, Set<Problem> problems) {
		for (Type type : types.values()) {
			for (String referenceName : type.referenceNames()) {
				Type reference = types.get(referenceName);
				if (reference == null) {
					problems.add(new Problem(referenceName, Errors.UNRESOLVED_REFERENCE));
					continue;
				}
				type.references().add(reference);
			}
		}
	}
	
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	static String plural(Errors error) {
		if (error == Errors.MULTIPLE_ERRORS) {
			return "S";
		}
		return "";
	}

	static void report(Set<Problem> problems, Flags flags) {
		StringBuilder builder = null;
		Errors error = null;
		for (Problem problem : problems) {
			if (problem.isFatal(flags.strict())) {
				if (builder == null) {
					builder = new StringBuilder();
					error = problem.error();
				} else {
					error = Errors.MULTIPLE_ERRORS;
				}
				builder.append(LINE_SEPARATOR);
				builder.append(problem);
			}
		}
		if (builder != null) {
			throw new EnforcerException("FATAL ERROR" + plural(error) + ":" + builder.toString(), error);
		}
	}
	
	static void release(Workset workset, Set<Problem> problems) {
		if (workset == null) {
			return;
		}
		try {
			workset.release();
		} catch (Throwable t) {
			problems.add(new Problem("unable to release workset " + workset.getName() + ": " + t.getMessage(), Errors.UNABLE_TO_RELEASE_WORKSET));
		}
	}
	
	public static Map<String, Type> resolve(Inputs inputs, Set<Problem> problems, Flags flags) throws Exception {
		Set<String> ignores = ignores(inputs.ignores());
		Map<String, Type> types = new HashMap<>();
		Workset workset = null;
		try {
			workset = new Workset("ArchitectureEnforcer");
			ClasspathPartDefinition partDefinition = new ClasspathPartDefinition(inputs.war().getAbsolutePath());
			workset.addClasspathPartDefinition(partDefinition);
			WorksetInitializer wsInitializer = new WorksetInitializer(workset);
			wsInitializer.initializeWorksetAndWait(null);
			for (ClassInformation classInfo : workset.getAllContainedClasses()) {
				String referringClass = denest(classInfo.getName().trim(), flags);
				// TODO: Instead of creating the entire graph and then ignoring, see if there's a way to pass in a filter when initializing the workspace.
				if (skip(referringClass, ignores)) {
					continue;
				}
				Type type = get(referringClass, types);
				for (IType dependency : classInfo.getDirectReferredTypes()) {
					String referredToClass = denest(dependency.getName().trim(), flags);
					if (skip(type.name(), referredToClass, ignores)) {
						continue;
					}
					type.referenceNames().add(referredToClass);
				}
			}
		} finally {
			release(workset, problems);
		}
		parse(inputs.reflections(), types, ignores, problems, "reflection", true, flags); // Get reflection-based referring and referred-to classes from reflections file.
		parse(inputs.fixUnresolveds(), types, ignores, problems, "fix-unresolved", false, flags); // Get referring and referred-to classes from fix-unresolveds file.
		report(problems, flags);
		resolve(types, problems);
		report(problems, flags);
		return types;
	}
	
	public static void correlate(Map<String, Type> types, Map<String, Component> components, RollUp rollUp, Set<Problem> problems, Flags flags) {
		for (Component component : components.values()) {
			for (String className : component.classes()) {
				Type type = types.get(className);
				if (type == null) {
					problems.add(new Problem("unable to resolve class to type: " + className, Errors.CLASS_NOT_RESOLVED_TO_TYPE));
					continue;
				}
				component.add(type);
				type.setBelongsTo(component);
			}
		}
		report(problems, flags);
		rollUp.add(components.values());
		for (Type type : types.values()) {
			if (type.belongsTo() != null) {
				continue;
			}
			String componentName = rollUp.get(type.name());
			if (componentName == null) {
				problems.add(new Problem("unable to resolve type to component name: " + type.name(), Errors.TYPE_NOT_RESOLVED_TO_COMPONENT));
				continue;
			}
			Component component = components.get(componentName);
			component.add(type);
			type.setBelongsTo(component);
		}
		report(problems, flags);
		for (Type type : types.values()) {
			for (Type referredTo : type.references()) {
				if (type.belongsTo() == referredTo.belongsTo()) {
					continue; // Skip intra-component references.
				}
				if (type.belongsTo().layer().depth() <= referredTo.belongsTo().layer().depth()) {
					String parseableDescription = type.name() + "!" + type.belongsTo().name() + "!" + type.belongsTo().layer().name() + "!" + type.belongsTo().layer().depth() + "|" + referredTo.name()
							+ "!" + referredTo.belongsTo().name() + "!" + referredTo.belongsTo().layer().name() + "!" + referredTo.belongsTo().layer().depth();
					String humanReadableDescription = "type " + type.name() + " in component " + type.belongsTo().quotedName() + " in layer " + type.belongsTo().layer().quotedName() + " depth "
							+ type.belongsTo().layer().depth() + " refers to type " + referredTo.name() + " in component " + referredTo.belongsTo().quotedName() + " in layer "
							+ referredTo.belongsTo().layer().quotedName() + " depth " + referredTo.belongsTo().layer().depth();
					problems.add(new Problem(parseableDescription, Errors.ILLEGAL_REFERENCE, humanReadableDescription));
				}
			}
		}
		report(problems, flags);
	}
}
