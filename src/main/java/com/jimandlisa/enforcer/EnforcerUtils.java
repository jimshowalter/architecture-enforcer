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
	
	static String denest(String typeName, boolean preserveNestedTypes) {
		if (typeName.startsWith("$")) {
			throw new EnforcerException("malformed class name '" + typeName + "'", Errors.MALFORMED_CLASS_NAME);
		}
		if (preserveNestedTypes) {
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
				String referringClass = denest(segments[0], flags.preserveNestedTypes());
				if (skip(referringClass, ignores)) {
					problems.add(new Problem(entryName.toUpperCase() + " class is listed as referring but also listed in ignores: " + referringClass, Errors.CLASS_BOTH_REFERRING_AND_IGNORED));
					continue;
				}
				type = get(referringClass, types);
				String[] segments2 = segments[1].split(",");
				for (String segment : segments2) {
					String referredToClass = denest(segment, flags.preserveNestedTypes());
					if (skip(referredToClass, ignores)) {
						problems.add(new Problem(entryName.toUpperCase() + " class is listed as referred-to but also listed in ignores: " + referringClass, Errors.CLASS_BOTH_REFERRED_TO_AND_IGNORED));
						continue;
					}
					type.referenceNames().add(referredToClass);
				}
			}
		}
	}
	
	static Errors error(Errors error, boolean strict) {
		if (strict) {
			return error;
		}
		return null;
	}
	
	static void resolve(Map<String, Type> types, Set<Problem> problems, Flags flags) {
		for (Type type : types.values()) {
			for (String referenceName : type.referenceNames()) {
				Type reference = types.get(referenceName);
				if (reference == null) {
					problems.add(new Problem("unresolved: " + referenceName, error(Errors.UNRESOLVED_REFERENCE, flags.strict())));
					continue;
				}
				type.references().add(reference);
			}
		}
	}
	
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	static void report(Set<Problem> problems) {
		StringBuilder builder = null;
		Errors firstError = null;
		boolean multipleErrors = false;
		for (Problem problem : problems) {
			if (problem.isFatal()) {
				if (builder == null) {
					builder = new StringBuilder();
					firstError = problem.error();
				} else {
					multipleErrors = true;
				}
				builder.append(LINE_SEPARATOR);
				builder.append(problem.description());
			}
		}
		if (builder != null) {
			throw new EnforcerException("error" + (multipleErrors ? "s" : "") + ":" + builder.toString(), firstError);
		}
	}
	
	public static Map<String, Type> resolve(Inputs inputs, Set<Problem> problems, Flags flags) throws Exception {
		Set<String> ignores = ignores(inputs.ignores());
		Map<String, Type> types = new HashMap<>();
		// Get referring and referred-to classes from pf-CDA output.
		try (BufferedReader reader = new BufferedReader(new FileReader(inputs.odem()))) {
			String line = null;
			Type type = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().startsWith("<type name=\"")) {
					String referringClass = denest(line.trim().replace("<type name=\"", "").replaceAll("\".*$", ""), flags.preserveNestedTypes());
					if (skip(referringClass, ignores)) {
						type = null;
						continue;
					}
					type = get(referringClass, types);
					continue;
				}
				if (line.trim().startsWith("<depends-on name=\"")) {
					if (type == null) {
						continue;
					}
					String referredToClass = denest(line.trim().replace("<depends-on name=\"", "").replaceAll("\".*$", ""), flags.preserveNestedTypes());
					if (skip(referredToClass, ignores)) {
						continue;
					}
//					if (referredToClass.equals(type.name())) { // Skip self-references. Commented out because pf-CDA seems to filter them for us.
//						continue;
//					}
					type.referenceNames().add(referredToClass);
				}
			}
		}
		parse(inputs.reflections(), types, ignores, problems, "reflection", true, flags); // Get reflection-based referring and referred-to classes from reflections file.
		parse(inputs.fixUnresolveds(), types, ignores, problems, "fix-unresolved", false, flags); // Get referring and referred-to classes from fix-unresolveds file.
		report(problems);
		resolve(types, problems, flags);
		report(problems);
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
		report(problems);
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
		report(problems);
		for (Type type : types.values()) {
			for (Type referredTo : type.references()) {
				if (type.belongsTo() == referredTo.belongsTo()) {
					continue; // Skip intra-component references.
				}
				if (type.belongsTo().layer().depth() <= referredTo.belongsTo().layer().depth()) {
					problems.add(new Problem("illegal reference: " + type + " in component '" + type.belongsTo().name() + "' in layer " + type.belongsTo().layer().depth() + " refers to component '" + referredTo.belongsTo().name() + "' in layer " + referredTo.belongsTo().layer().depth(), error(Errors.ILLEGAL_REFERENCE, flags.strict())));
				}
			}
		}
		report(problems);
	}
}
