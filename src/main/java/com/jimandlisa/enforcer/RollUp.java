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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RollUp {

	private final List<String> packages = new ArrayList<>();
	private final Map<String, Component> packagesToComponents = new HashMap<>();
	private final Set<String> matchedPackages = new HashSet<>();

	public void add(Collection<Component> components) {
		for (Component component : components) {
			for (String pkg : component.packages()) {
				packages.add(pkg);
				packagesToComponents.put(pkg, component);
			}
		}
		Collections.sort(packages, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s2.compareTo(s1); // Reverse sort so longest matches are found first.
			}
		});
	}

	public Component get(String packageName) {
		for (String pkg : packages) {
			if (packageName.startsWith(pkg)) {
				matchedPackages.add(pkg);
				return packagesToComponents.get(pkg);
			}
		}
		return null;
	}

	public void validate(Set<Problem> problems) {
		Set<String> check = new HashSet<>(packages);
		check.removeAll(matchedPackages);
		for (String unused : check) {
			problems.add(new Problem("unused package " + unused + " in component " + packagesToComponents.get(unused).quotedName(), Errors.UNUSED_PACKAGE));
		}
	}

	void dump(PrintStream ps) {
		ps.println("Packages and components:");
		for (String pkg : packages) {
			ps.println("\t" + pkg + ": " + packagesToComponents.get(pkg).name());
		}
	}
}
