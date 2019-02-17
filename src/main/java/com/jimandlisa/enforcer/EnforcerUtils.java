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
	
	static String denest(String typeName) {
		if (typeName.startsWith("$")) {
			throw new EnforcerException("malformed class name '" + typeName + "'");
		}
		return typeName.replaceAll("[$].*$", ""); // To stop denesting, change this to just return the unmodified type name.
	}
	
	static Type get(String typeName, Map<String, Type> types) {
		Type type = types.get(typeName);
		if (type == null) {
			type = new Type(typeName);
			types.put(type.name(), type);
		}
		return type;
	}
	
	static void parse(File file, Map<String, Type> types, Set<String> ignores, Set<String> problems, String entryName) throws Exception {
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
				if (segments.length != 2) {
					throw new EnforcerException("invalid " + entryName + " entry in " + file + ": " + line);
				}
				String referringClass = denest(segments[0]);
				if (skip(referringClass, ignores)) {
					problems.add(entryName.toUpperCase() + " CLASS IS LISTED AS REFERRING BUT ALSO LISTED IN IGNORES: " + referringClass);
					continue;
				}
				type = get(referringClass, types);
				String[] segments2 = segments[1].split(",");
				for (String segment : segments2) {
					String referredToClass = denest(segment);
					if (skip(referredToClass, ignores)) {
						problems.add(entryName.toUpperCase() + " CLASS IS LISTED AS REFERRED-TO BUT ALSO LISTED IN IGNORES: " + referringClass);
						continue;
					}
					type.referenceNames().add(referredToClass);
				}
			}
		}
	}
	
	static void resolve(Map<String, Type> types, Set<String> problems) {
		for (Type type : types.values()) {
			for (String referenceName : type.referenceNames()) {
				Type reference = types.get(referenceName);
				if (reference == null) {
					problems.add("UNRESOLVED: " + referenceName);
					continue;
				}
				type.references().add(reference);
			}
		}
	}
	
	public static Map<String, Type> resolve(Inputs inputs, Set<String> problems) throws Exception {
		Set<String> ignores = ignores(inputs.ignores());
		Map<String, Type> types = new HashMap<>();
		// Get referring and referred-to classes from pf-CDA output.
		try (BufferedReader reader = new BufferedReader(new FileReader(inputs.odem()))) {
			String line = null;
			Type type = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().startsWith("<type name=\"")) {
					String referringClass = denest(line.trim().replace("<type name=\"", "").replaceAll("\".*$", ""));
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
					String referredToClass = denest(line.trim().replace("<depends-on name=\"", "").replaceAll("\".*$", ""));
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
		parse(inputs.reflections(), types, ignores, problems, "reflection"); // Get reflection-based referring and referred-to classes from reflections file.
		parse(inputs.fixUnresolveds(), types, ignores, problems, "fix-unresolved"); // Get referring and referred-to classes from fix-unresolveds file.
		resolve(types, problems);
		return types;
	}
	
	public static void correlate(Map<String, Type> types, Map<String, Component> components, Set<String> problems) {
		RollUp.add(components.values());
		for (Type type : types.values()) {
			String componentName = RollUp.get(type.name());
			if (componentName == null) {
				problems.add("UNABLE TO RESOLVE TYPE TO COMPONENT NAME: " + type.name());
				continue;
			}
			Component component = components.get(componentName);
			component.add(type);
			type.setBelongsTo(component);
		}
		for (Type type : types.values()) {
			for (Type referredTo : type.references()) {
				if (type.belongsTo() == referredTo.belongsTo()) {
					continue; // Skip intra-component references.
				}
				if (type.belongsTo().layer().depth() <= referredTo.belongsTo().layer().depth()) {
					problems.add("ILLEGAL REFERENCE: " + type + " in component '" + type.belongsTo().name() + "' in layer " + type.belongsTo().layer().depth() + " refers to component '" + referredTo.belongsTo().name() + "' in layer " + referredTo.belongsTo().layer().depth());
				}
			}
		}
	}
}
