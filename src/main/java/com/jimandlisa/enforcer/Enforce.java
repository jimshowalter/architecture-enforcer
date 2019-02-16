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

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Enforce {
	
	static enum Optionals {
		IGNORES("-i"),
		REFLECTIONS("-r"),
		FIX_UNRESOLVEDS("-f");
		
		private final String indicator;
		private Optionals(final String indicator) {
			this.indicator = indicator;
		}
		
		public String indicator() {
			return indicator;
		}
		
		@Override
		public String toString() {
			return indicator();
		}
	}
	
	static void usage(String error) {
		throw new EnforcerException(error + ": usage: /full/path/to/target/architecture/.yaml /full/path/to/pf-CDA/.odem " + Optionals.IGNORES + "/full/path/to/packages/to/ignore " + Optionals.REFLECTIONS + "/full/path/to/reflection/references " + Optionals.FIX_UNRESOLVEDS + "/full/path/to/fixed/unresolveds [last three args optional and unordered]");
	}
	
	static void parse(String arg, Inputs inputs) {
		if (arg.startsWith(Optionals.IGNORES.indicator())) {
			if (inputs.ignores() != null) {
				usage("already specified " + Optionals.IGNORES.indicator() + " option");
			}
			inputs.setIgnores(new File(arg.replaceFirst(Optionals.IGNORES.indicator(), "")));
			return;
		}
		if (arg.startsWith(Optionals.REFLECTIONS.indicator())) {
			if (inputs.reflections() != null) {
				usage("already specified " + Optionals.REFLECTIONS.indicator() + " option");
			}
			inputs.setReflections(new File(arg.replaceFirst(Optionals.REFLECTIONS.indicator(), "")));
			return;
		}
		if (arg.startsWith(Optionals.FIX_UNRESOLVEDS.indicator())) {
			if (inputs.fixUnresolveds() != null) {
				usage("already specified " + Optionals.FIX_UNRESOLVEDS.indicator() + " option");
			}
			inputs.setFixUnresolveds(new File(arg.replaceFirst(Optionals.FIX_UNRESOLVEDS.indicator(), "")));
			return;
		}
		usage("unrecognized option " + arg);
	}
	
	static void mainImpl(Inputs inputs, PrintStream ps, boolean debug) throws Exception {
		Target target = TargetUtils.parse(inputs.target());
		Set<String> problems = new HashSet<>();
		Map<String, Type> types = EnforcerUtils.resolve(inputs, problems);
		EnforcerUtils.correlate(types, target, problems);
		if (debug) {
			TargetUtils.dump(target, ps);
			RollUp.dump(ps);
			ps.println("Total outermost types: " + types.size());
			for (String fullName : CollectionUtils.sort(new ArrayList<>(types.keySet()))) {
				ps.println("\t" + fullName);
				for (String referenceName : CollectionUtils.sort(new ArrayList<>(types.get(fullName).referenceNames()))) {
					ps.println("\t\t" + referenceName);
				}
			}
			if (!problems.isEmpty()) {
				ps.println("PROBLEMS:");
				for (String problem : CollectionUtils.sort(new ArrayList<>(problems))) {
					ps.println("\t" + problem);
				}
			}
		}
	}
	
	static final boolean DEBUG = true;

	public static void main(String[] args) {
		try {
			if (args.length < 2) {
				usage("not enough args");
			}
			if (args.length > 5) {
				usage("too many args");
			}
			Inputs inputs = new Inputs(new File(args[0]), new File(args[1]));
			for (int i = 2; i < args.length; i++) {
				parse(args[i], inputs);
			}
			System.out.println("Analyzing/enforcing architecture with " + inputs.toString());
			mainImpl(inputs, System.out, DEBUG);
		} catch (Throwable t) {
			System.out.println(t.getMessage());
		}
	}
}
