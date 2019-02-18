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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Enforce {

	private static final String USAGE = ": usage: /full/path/to/target/architecture/.yaml /full/path/to/pf-CDA/.odem " + Optionals.IGNORES + "/full/path/to/packages/to/ignore " + Optionals.REFLECTIONS + "/full/path/to/reflection/references " + Optionals.FIX_UNRESOLVEDS + "/full/path/to/fixed/unresolveds [last three args optional and unordered]";

	static void parse(String arg, Inputs inputs) {
		if (arg.startsWith(Optionals.IGNORES.indicator())) {
			if (inputs.ignores() != null) {
				throw new EnforcerException("already specified " + Optionals.IGNORES.indicator() + " option" + USAGE, Errors.IGNORES_FILE_ALREADY_SPECIFIED);
			}
			inputs.setIgnores(new File(arg.replaceFirst(Optionals.IGNORES.indicator(), "")));
			return;
		}
		if (arg.startsWith(Optionals.REFLECTIONS.indicator())) {
			if (inputs.reflections() != null) {
				throw new EnforcerException("already specified " + Optionals.REFLECTIONS.indicator() + " option" + USAGE, Errors.REFLECTIONS_FILE_ALREADY_SPECIFIED);
			}
			inputs.setReflections(new File(arg.replaceFirst(Optionals.REFLECTIONS.indicator(), "")));
			return;
		}
		if (arg.startsWith(Optionals.FIX_UNRESOLVEDS.indicator())) {
			if (inputs.fixUnresolveds() != null) {
				throw new EnforcerException("already specified " + Optionals.FIX_UNRESOLVEDS.indicator() + " option" + USAGE, Errors.FIX_UNRESOLVEDS_FILE_ALREADY_SPECIFIED);
			}
			inputs.setFixUnresolveds(new File(arg.replaceFirst(Optionals.FIX_UNRESOLVEDS.indicator(), "")));
			return;
		}
		throw new EnforcerException("unrecognized option " + arg + USAGE, Errors.UNRECOGNIZED_COMMAND_LINE_OPTION);
	}
	
	static void debug(boolean debug, Target target, Map<String, Type> types, PrintStream ps) throws Exception {
		if (!debug) {
			return;
		}
		TargetUtils.dump(target, ps);
		RollUp.dump(ps);
		ps.println("Total outermost types: " + types.size());
		for (String typeName : CollectionUtils.sort(new ArrayList<>(types.keySet()))) {
			ps.println("\t" + typeName);
			for (String referenceName : CollectionUtils.sort(new ArrayList<>(types.get(typeName).referenceNames()))) {
				ps.println("\t\t" + referenceName);
			}
		}
	}
	
	static void problems(Set<Problem> problems, PrintStream ps) {
		if (problems.isEmpty()) {
			return;
		}
		ps.println("PROBLEMS:");
		for (Problem problem : CollectionUtils.sort(new ArrayList<>(problems))) {
			ps.println("\t" + problem);
		}
	}
	
	static void mainImpl(Inputs inputs, PrintStream ps, boolean debug) throws Exception {
		Target target = TargetUtils.parse(inputs.target());
		Set<Problem> problems = new LinkedHashSet<>();
		Map<String, Type> types = EnforcerUtils.resolve(inputs, problems);
		EnforcerUtils.correlate(types, target.components(), problems);
		debug(debug, target, types, ps);
		problems(problems, ps);
	}
	
	static final boolean DEBUG = false;

	public static void mainImpl(String[] args) {
		try {
			if (args.length < 2) {
				throw new EnforcerException("not enough args" + USAGE, Errors.NOT_ENOUGH_ARGS);
			}
			if (args.length > 5) {
				throw new EnforcerException("too many args" + USAGE, Errors.TOO_MANY_ARGS);
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
