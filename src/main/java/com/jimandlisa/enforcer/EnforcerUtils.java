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

	static Type get(String typeName, Map<String, Type> types) {
		Type type = types.get(typeName);
		if (type == null) {
			type = new Type(typeName);
			types.put(type.name(), type);
		}
		return type;
	}

	static Map<String, Type> typesFromAllReferences(File allReferences) throws Exception {
		Map<String, Type> types = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(allReferences))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] segments = line.split(Separators.REFERENCE_SEPARATOR.value());
				String referringClass = segments[0];
				Type referringType = get(referringClass, types);
				String referredToClass = segments[4];
				Type referredToType = get(referredToClass, types);
				referringType.addReferenceName(referredToType.name());
				referringType.addReference(referredToType);
			}
		}
		return types;
	}

	static void add(String ignore, Set<String> ignores) {
		if (!ignores.add(ignore)) {
			throw new EnforcerException("duplicate ignore " + ignore, Errors.DUPLICATE_IGNORE);
		}
	}

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
				if (ignore.endsWith(Separators.IGNORES_VERBATIM_INDICATOR.value())) {
					add(ignore.replaceAll("[" + Separators.IGNORES_VERBATIM_INDICATOR.value() + "]+$", ""), ignores);
					continue;
				}
				if (ignore.endsWith(".")) {
					add(ignore.replaceAll("[.]+$", "."), ignores);
					continue;
				}
				add(ignore + ".", ignores);
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

	// Denesting is a lot trickier than it should be, because any number of $ signs can be used in class names, even if they aren't nested. For example, $$$Foo$$$Bar.java is a perfectly legal source file.
	// When we have nothing structured to work with, as is the case here (where we just have a string), we can only do best-effort, and then throw up our hands if that doesn't work.
	static String denest(String typeName, AnalyzeWarFlags flags) {
		if (flags.preserveNestedTypes()) {
			return typeName;
		}
		if (typeName.startsWith("$")) {
			// If you get this exception, you need to add special-casing to this method for your types that have weird names. Or maybe rename them.
			throw new EnforcerException("malformed class name '" + typeName + "'", Errors.MALFORMED_CLASS_NAME);
		}
		return typeName.replaceAll("[$].*$", "");
	}
	
	static Type get(String referringClass, Map<String, Type> types, boolean errorIfExists) {
		Type type = types.get(referringClass);
		if (type == null) {
			type = new Type(referringClass);
			types.put(type.name(), type);
		} else {
			if (errorIfExists) {
				throw new EnforcerException("fix-unresolveds class " + referringClass + " is already resolved from binary", Errors.SUPPLEMENTAL_TYPE_NOT_NEEDED);
			}
		}
		return type;
	}

	static void addSupplementalTypes(File file, Map<String, Type> types, Set<String> ignores, Set<Problem> problems, boolean reflection, AnalyzeWarFlags flags) throws Exception {
		if (file == null) {
			return;
		}
		String entryName = reflection ? "reflection" : "fix-unresolved";
		boolean requireReferredTo = reflection;
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
				String[] segments = trimmed.replaceAll("[ \t]+", "").split(Separators.SUPPLEMENTAL_TYPES_FROM_TO_SEPARATOR.value());
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
					problems.add(new Problem(entryName + " class is listed as referring but also listed in ignores: " + referringClass, Errors.CLASS_BOTH_REFERRING_AND_IGNORED));
					continue;
				}
				type = get(referringClass, types, !reflection);
				String[] segments2 = segments[1].split(Separators.SUPPLEMENTAL_TYPES_TO_LIST_SEPARATOR.value());
				for (String segment : segments2) {
					String referredToClass = denest(segment, flags);
					if (skip(referredToClass, ignores)) {
						problems.add(new Problem(entryName + " class is listed as referred-to but also listed in ignores: " + referringClass, Errors.CLASS_BOTH_REFERRED_TO_AND_IGNORED));
						continue;
					}
					type.addReferenceName(referredToClass);
				}
			}
		}
	}
	
	static void checkSeparators(String className) {
		for (Separators separator : Separators.values()) {
			if (className.contains(separator.value())) {
				throw new EnforcerException("class " + className + " contains reserved separator '" + separator.value() + "', see comments in Separators class", Errors.RESERVED_SEPARATOR_IN_CLASS_NAME);
			}
		}
	}

	// See comments on other denest method explaining why this is problematic. At least here we have some structure to work with, although pf-CDA doesn't seem to provide a getEnclosingType method, which would have been useful.
	// Instead, we have to peel one $ sign off at a time, and see if the result comes back as a class or not. If not, we keep going. If so, we check if the class is no longer nested.
	static String denest(ClassInformation classInfo, Workset workset, AnalyzeWarFlags flags) {
		if (flags.preserveNestedTypes()) {
			return classInfo.getName();
		}
		if (!classInfo.isInnerClass()) {
			return classInfo.getName();
		}
		ClassInformation current = classInfo;
		while (current.isInnerClass()) {
			ClassInformation probe = null;
			while (probe == null) {
				probe = workset.getClassInfo(current.getName().replaceAll("[$][^$]*$", ""));
			}
			current = probe;
		}
		return current.getName();
	}

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	static String plural(Errors error) {
		if (error == Errors.MULTIPLE_ERRORS) {
			return "S";
		}
		return "";
	}

	static void reportFatalErrors(Set<Problem> problems, Flags flags) {
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
				if (problem.error() == Errors.ILLEGAL_REFERENCE) {
					builder.append(problem.humanReadableToString());
				} else {
					builder.append(problem);
				}
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

	static Map<String, Type> typesFromWar(File war, Set<String> ignores, Set<Problem> problems, AnalyzeWarFlags flags) throws Exception {
		Map<String, Type> types = new HashMap<>();
		Workset workset = null;
		try {
			workset = new Workset("ArchitectureEnforcer");
			ClasspathPartDefinition partDefinition = new ClasspathPartDefinition(war.getAbsolutePath());
			workset.addClasspathPartDefinition(partDefinition);
			WorksetInitializer wsInitializer = new WorksetInitializer(workset);
			wsInitializer.initializeWorksetAndWait(null);
			for (ClassInformation classInfo : workset.getAllContainedClasses()) {
				checkSeparators(classInfo.getName());
				String referringClass = denest(classInfo, workset, flags);
				if (skip(referringClass, ignores)) {
					continue;
				}
				Type type = get(referringClass, types);
				for (IType dependency : classInfo.getDirectReferredTypes()) {
					checkSeparators(dependency.getName());
					ClassInformation dependencyClassInfo = workset.getClassInfo(dependency.getName());
					String referredToClass = denest(dependencyClassInfo, workset, flags);
					if (skip(type.name(), referredToClass, ignores)) {
						continue;
					}
					type.referenceNames().add(referredToClass);
				}
			}
		} finally {
			release(workset, problems);
		}
		return types;
	}

	static void resolve(Map<String, Type> types, Set<Problem> problems) {
		Set<Type> synthetics = new HashSet<>();
		for (Type type : types.values()) {
			for (String referenceName : type.referenceNames()) {
				Type reference = types.get(referenceName);
				if (reference == null) {
					problems.add(new Problem(referenceName, Errors.UNRESOLVED_REFERENCE));
					synthetics.add(new Type(referenceName, true));
					continue;
				}
				type.addReference(reference);
			}
		}
		for (Type synthetic : synthetics) {
			types.put(synthetic.name(), synthetic);
		}
	}

	public static Map<String, Type> resolve(RapidIterationInputs inputs, Set<Problem> problems) throws Exception {
		return typesFromAllReferences(inputs.allReferences());
	}

	public static Map<String, Type> resolve(AnalyzeBinaryInputs inputs, Set<Problem> problems, AnalyzeWarFlags flags) throws Exception {
		Set<String> ignores = ignores(inputs.ignores());
		Map<String, Type> types = typesFromWar(inputs.binary(), ignores, problems, flags);
		addSupplementalTypes(inputs.reflections(), types, ignores, problems, true, flags); // Add reflection-based referring and referred-to classes from reflections file.
		addSupplementalTypes(inputs.fixUnresolveds(), types, ignores, problems, false, flags); // Add referring and referred-to classes from fix-unresolveds file.
		reportFatalErrors(problems, flags);
		resolve(types, problems);
		reportFatalErrors(problems, flags);
		return types;
	}

	public static Map<String, Type> resolve(Inputs inputs, Set<Problem> problems, Flags flags) throws Exception {
		if (inputs instanceof RapidIterationInputs) {
			return resolve((RapidIterationInputs)inputs, problems);
		}
		return resolve((AnalyzeBinaryInputs)inputs, problems, (AnalyzeWarFlags)flags);
	}

	static void correlateComponentClassesToTypes(Map<String, Type> types, Map<String, Component> components, Set<Problem> problems) {
		Set<String> matchedClasses = new HashSet<>();
		for (Component component : components.values()) {
			for (String className : component.classes()) {
				Type type = types.get(className);
				if (type == null) {
					problems.add(new Problem("unable to resolve class to type " + className, Errors.CLASS_NOT_RESOLVED_TO_TYPE));
					matchedClasses.add(className);
					continue;
				}
				matchedClasses.add(className);
				component.add(type);
				type.setComponent(component);
			}
		}
	}

	static void correlateTypesToComponents(Map<String, Type> types, Map<String, Component> components, RollUp rollUp, Set<Problem> problems) {
		rollUp.add(components.values());
		for (Type type : types.values()) {
			if (type.component() != null) {
				continue; // Was already resolved by class name.
			}
			Component component = rollUp.get(type.name());
			if (component == null) {
				problems.add(new Problem("unable to resolve type " + type.name() + " to component", Errors.TYPE_NOT_RESOLVED_TO_COMPONENT));
				continue;
			}
			component.add(type);
			type.setComponent(component);
		}
		rollUp.validate(problems);
	}

	static void correlateTypesToReferences(Map<String, Type> types, Set<Reference> references, Set<Problem> problems) {
		for (Type referringType : types.values()) {
			for (Type referredToType : referringType.references()) {
				Reference reference = new Reference(referringType, referredToType);
				references.add(reference);
				referringType.component().references().add(reference);
				if (!reference.kind().isLegal()) {
					problems.add(new Problem(reference.parseableDescription(true, false), Errors.ILLEGAL_REFERENCE, reference.humanReadableDescription(true, false)));
					problems.add(new Problem(reference.parseableDescription(false, false), Errors.ILLEGAL_COMPONENT_REFERENCE, reference.humanReadableDescription(false, false)));
				}
			}
		}
	}

	public static void correlate(Map<String, Type> types, Map<String, Component> components, RollUp rollUp, Set<Reference> references, Set<Problem> problems, Flags flags) {
		correlateComponentClassesToTypes(types, components, problems);
		reportFatalErrors(problems, flags);
		correlateTypesToComponents(types, components, rollUp, problems);
		reportFatalErrors(problems, flags);
		correlateTypesToReferences(types, references, problems);
		reportFatalErrors(problems, flags);
	}
}
