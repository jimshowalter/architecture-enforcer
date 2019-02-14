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
import java.util.ArrayList;
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
				if (!ignore.endsWith(".")) {
					ignore = ignore + ".";
				}
				ignores.add(ignore);
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
	
	public static Map<String, Type> resolve(Inputs inputs, Set<String> unresolveds) throws Exception {
		Set<String> ignores = ignores(inputs);
		Map<String, Type> types = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(inputs.odem()))) {
			String line = null;
			Type type = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().startsWith("<type name=\"")) {
					String fullName = line.trim().replace("<type name=\"", "").replaceAll("\".*$", "").replaceAll("[$].*$", "");
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
					String reference = line.trim().replace("<depends-on name=\"", "").replaceAll("\".*$", "").replaceAll("[$].*$", "");
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
	
	private static final boolean DEBUG = true;

	public static void enforce(Inputs inputs) throws Exception {
		Set<String> unresolveds = new HashSet<>();
		Map<String, Type> types = resolve(inputs, unresolveds);
		if (DEBUG) {
			System.out.println("Total outermost types: " + types.size());
			for (String fullName : CollectionUtils.sort(new ArrayList<>(types.keySet()))) {
				System.out.println(fullName);
				for (String referenceName : CollectionUtils.sort(new ArrayList<>(types.get(fullName).referenceNames()))) {
					System.out.println("\t" + referenceName);
				}
			}
			if (!unresolveds.isEmpty()) {
				System.out.println("UNRESOLVED REFERENCES:");
				for (String unresolved : CollectionUtils.sort(new ArrayList<>(unresolveds))) {
					System.out.println("\t" + unresolved);
				}
			}
		}
	}
}
