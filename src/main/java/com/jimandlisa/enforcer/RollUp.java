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
import java.util.List;

public class RollUp {

	private final List<String> packagesAndComponents = new ArrayList<>();

	public void add(Collection<Component> components) {
		for (Component component : components) {
			for (String pkg : component.packages()) {
				packagesAndComponents.add(pkg + "!" + component.name());
			}
		}
		Collections.sort(packagesAndComponents, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s2.compareTo(s1); // Reverse sort so longest matches are found first.
			}
		});
	}
	
	public String get(String packageName) {
		for (String packageAndComponent : packagesAndComponents) {
			String[] segments = packageAndComponent.split("!");
			if (packageName.startsWith(segments[0])) {
				return segments[1];
			}
		}
		return null;
	}
	
	void dump(PrintStream ps) {
		ps.println("Packages and components:");
		for (String packageAndComponent : packagesAndComponents) {
			ps.println("\t" + packageAndComponent);
		}
	}
}
