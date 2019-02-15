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
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EnforcerUtils {
	
	static Set<String> ignores(Inputs inputs) throws Exception {
		Set<String> ignores = new HashSet<>();
		if (inputs.ignores() == null) {
			return ignores;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(inputs.ignores()))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String ignore = line.trim();
				if (ignore.startsWith("#")) {
					continue;
				}
				if (ignore.endsWith("!")) {
					ignores.add(ignore.replaceAll("[!]$", ""));
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
	
	static boolean skip(String name, Set<String> ignores) {
		if (name.contains(":")) { // Handles errors in pf-CDA, for example "evelField:Ljava.lang.Object".
			return true;
		}
		for (String ignore : ignores) {
			if (name.startsWith(ignore)) {
				return true;
			}
		}
		return false;
	}
	
	static String denest(String typeName) {
		return typeName.replaceAll("[$].*$", ""); // To preserve class nesting, remove calls to this method, or make it just return the unmodified type name.
	}
	
	public static Map<String, Type> resolve(Inputs inputs, Set<String> unresolveds) throws Exception {
		Set<String> ignores = ignores(inputs);
		Map<String, Type> types = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(inputs.odem()))) {
			String line = null;
			Type type = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().startsWith("<type name=\"")) {
					String fullName = denest(line.trim().replace("<type name=\"", "").replaceAll("\".*$", ""));
					if (skip(fullName, ignores)) {
						type = null;
						continue;
					}
					type = types.get(fullName);
					if (type == null) {
						type = new Type(fullName);
						types.put(type.fullName(), type);
					}
					continue;
				}
				if (line.trim().startsWith("<depends-on name=\"")) {
					if (type == null) {
						continue;
					}
					String reference = denest(line.trim().replace("<depends-on name=\"", "").replaceAll("\".*$", ""));
					if (skip(reference, ignores) || reference.equals(type.fullName())) {
						continue;
					}
					type.referenceNames().add(reference);
				}
			}
		}
		for (Type type : types.values()) {
			for (String referenceName : type.referenceNames()) {
				Type reference = types.get(referenceName);
				if (reference == null) {
					unresolveds.add(referenceName);
					continue;
				}
				type.references().add(reference);
			}
		}
		return types;
	}
	
	public static void correlate(Map<String, Type> types, Target target) {
		
	}
}
