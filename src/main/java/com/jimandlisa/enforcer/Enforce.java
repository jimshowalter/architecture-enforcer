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
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Enforce {

	private static final String USAGE = ": usage: /full/path/to/target/architecture/.yaml /full/path/to/.war /full/path/to/writable/output/directory " + Optionals.UNRESOLVED_TYPES_OUTPUT_FILE
			+ "unresolvedTypesOutputFileSimpleName " + Optionals.ILLEGAL_REFERENCES_OUTPUT_FILE + "illegalReferencesOutputFileSimpleName " + Optionals.ALL_REFERENCES_OUTPUT_FILE
			+ "allReferencesOutputFileSimpleName " + Optionals.IGNORES + "/full/path/to/packages/and/classes/to/ignore " + Optionals.REFLECTIONS + "/full/path/to/reflection/references "
			+ Optionals.FIX_UNRESOLVEDS + "/full/path/to/fixed/unresolveds " + Optionals.PRESERVE_NESTED_TYPES + " (preserves nested types) " + Optionals.STRICT
			+ " (strict, requires that all types resolve and no illegal references) " + Optionals.DEBUG + " (debug) [last nine args optional and unordered]";

	static void parseArg(String arg, Inputs inputs, Outputs outputs, Flags flags) {
		try {
			if (arg.startsWith(Optionals.UNRESOLVED_TYPES_OUTPUT_FILE.indicator())) {
				outputs.setUnresolvedTypes(arg.replaceFirst(Optionals.UNRESOLVED_TYPES_OUTPUT_FILE.indicator(), ""));
				return;
			}
			if (arg.startsWith(Optionals.ILLEGAL_REFERENCES_OUTPUT_FILE.indicator())) {
				outputs.setIllegalReferences(arg.replaceFirst(Optionals.ILLEGAL_REFERENCES_OUTPUT_FILE.indicator(), ""));
				return;
			}
			if (arg.startsWith(Optionals.ALL_REFERENCES_OUTPUT_FILE.indicator())) {
				outputs.setAllReferences(arg.replaceFirst(Optionals.ALL_REFERENCES_OUTPUT_FILE.indicator(), ""));
				return;
			}
			if (arg.startsWith(Optionals.IGNORES.indicator())) {
				inputs.setIgnores(new File(arg.replaceFirst(Optionals.IGNORES.indicator(), "")));
				return;
			}
			if (arg.startsWith(Optionals.REFLECTIONS.indicator())) {
				inputs.setReflections(new File(arg.replaceFirst(Optionals.REFLECTIONS.indicator(), "")));
				return;
			}
			if (arg.startsWith(Optionals.FIX_UNRESOLVEDS.indicator())) {
				inputs.setFixUnresolveds(new File(arg.replaceFirst(Optionals.FIX_UNRESOLVEDS.indicator(), "")));
				return;
			}
			if (arg.startsWith(Optionals.PRESERVE_NESTED_TYPES.indicator())) {
				flags.enablePreserveNestedTypes();
				return;
			}
			if (arg.startsWith(Optionals.STRICT.indicator())) {
				flags.enableStrict();
				return;
			}
			if (arg.startsWith(Optionals.DEBUG.indicator())) {
				flags.enableDebug();
				return;
			}
		} catch (EnforcerException e) {
			throw new EnforcerException(e.getMessage() + USAGE, e.error());
		}
		throw new EnforcerException("unrecognized option " + arg + USAGE, Errors.UNRECOGNIZED_COMMAND_LINE_OPTION);
	}

	static void debug(Target target, Map<String, Type> types, RollUp rollUp, PrintStream ps, Flags flags, int max) throws Exception {
		if (!flags.debug()) {
			return;
		}
		TargetUtils.dump(target, ps);
		rollUp.dump(ps);
		ps.println("Total outermost types: " + types.size());
		if (types.size() > max) {
			return;
		}
		for (String typeName : CollectionUtils.sort(new ArrayList<>(types.keySet()))) {
			ps.println("\t" + typeName);
			for (String referenceName : CollectionUtils.sort(new ArrayList<>(types.get(typeName).referenceNames()))) {
				ps.println("\t\t" + referenceName);
			}
		}
	}

	static void reportProblems(Set<Problem> problems, Errors error, File out) throws Exception {
		try (PrintStream ps = new PrintStream(new FileOutputStream(out))) {
			for (Problem problem : CollectionUtils.sort(new ArrayList<>(problems))) {
				if (problem.error() == error) {
					ps.println(problem.description());
				}
			}
		}
	}

	static int problemsCount(boolean foundUnresolvedTypes, boolean foundIllegalReferences) {
		int count = foundUnresolvedTypes ? 1 : 0;
		count = foundIllegalReferences ? count + 1 : count;
		return count;
	}

	// To have gotten here, there can't be any fatal errors. In strict mode, that
	// means we can't get here at all if there are any problems.
	// In non-strict mode, we can get here, but only if all problems are fatal only
	// when strict is specified. We need to report those problems.
	static void reportProblems(Set<Problem> problems, PrintStream ps, Outputs outputs, Flags flags) throws Exception {
		boolean foundUnresolvedTypes = false;
		boolean foundIllegalReferences = false;
		boolean foundWarnings = false;
		for (Problem problem : problems) {
			if (problem.error() == Errors.UNRESOLVED_REFERENCE) {
				foundUnresolvedTypes = true;
			} else if (problem.error() == Errors.ILLEGAL_REFERENCE) {
				foundIllegalReferences = true;
			} else {
				if (!foundWarnings) {
					ps.println("WARNINGS:");
					foundWarnings = true;
				}
				ps.println(problem.description());
			}
		}
		int count = problemsCount(foundUnresolvedTypes, foundIllegalReferences);
		if (count == 0) {
			return;
		}
		ps.println("PROBLEMS FOUND, SEE OUTPUT FILE" + (count == 1 ? "" : "S") + ":");
		if (foundUnresolvedTypes) {
			reportProblems(problems, Errors.UNRESOLVED_REFERENCE, outputs.unresolvedTypes());
			ps.println(outputs.unresolvedTypes().getName());
		}
		if (foundIllegalReferences) {
			reportProblems(problems, Errors.ILLEGAL_REFERENCE, outputs.illegalReferences());
			ps.println(outputs.illegalReferences().getName());
		}
	}

	static String legality(Reference reference) {
		return "!" + (reference.isLayerViolation() ? "ILLEGAL" : "LEGAL");
	}

	static void outputAllReferences(Set<Reference> references, Outputs outputs) throws Exception {
		if (outputs.allReferences() == null) {
			return;
		}
		if (references.isEmpty()) {
			return;
		}
		List<String> allReferences = new ArrayList<>();
		for (Reference reference : references) {
			allReferences.add(reference.parseableDescription() + legality(reference));
		}
		try (PrintStream ps = new PrintStream(new FileOutputStream(outputs.allReferences()))) {
			for (String reference : CollectionUtils.sort(allReferences)) {
				ps.println(reference);
			}
		}
		// Experimental output for https://gephi.org and https://www.yworks.com/products/yed.
		Map<String, String> refs = new HashMap<>();
		Integer id = 0;
		try (PrintStream ps = new PrintStream(new FileOutputStream(new File(outputs.allReferences().getAbsolutePath().replace(".txt", "_GephiNodes.csv"))))) {
			ps.println("ID;Label");
			for (String reference : CollectionUtils.sort(allReferences)) {
				id++;
				String referringType = reference.split("!")[0];
				refs.put(referringType, id.toString());
				ps.println(id + ";" + referringType);
			}
		}
		try (PrintStream ps = new PrintStream(new FileOutputStream(new File(outputs.allReferences().getAbsolutePath().replace(".txt", "_GephiEdges.csv"))))) {
			ps.println("Source;Target");
			for (String reference : CollectionUtils.sort(allReferences)) {
				String[] segments = reference.split("!");
				String referringType = segments[0];
				String referredToType = segments[4];
				ps.println(refs.get(referringType) + ";" + refs.get(referredToType));
			}
		}
		id = 0;
		try (PrintStream ps = new PrintStream(new FileOutputStream(new File(outputs.allReferences().getAbsolutePath().replace(".txt", ".tgf"))))) {
			for (String reference : CollectionUtils.sort(allReferences)) {
				id++;
				String referringType = reference.split("!")[0];
				refs.put(referringType, id.toString());
				ps.println(id + " " + referringType);
			}
			ps.println("#");
			for (String reference : CollectionUtils.sort(allReferences)) {
				String[] segments = reference.split("!");
				String referringType = segments[0];
				String referredToType = segments[4];
				ps.println(refs.get(referringType) + " " + refs.get(referredToType));
			}
		}
		// End of experimental output.
	}

	static void mainImpl(Inputs inputs, Outputs outputs, PrintStream ps, Flags flags) throws Exception {
		Target target = TargetUtils.parse(inputs.target());
		Set<Problem> problems = new LinkedHashSet<>();
		Map<String, Type> types = EnforcerUtils.resolve(inputs, problems, flags);
		RollUp rollUp = new RollUp();
		Set<Reference> references = new HashSet<>();
		EnforcerUtils.correlate(types, target.components(), rollUp, references, problems, flags);
		debug(target, types, rollUp, ps, flags, 100);
		reportProblems(problems, ps, outputs, flags);
		outputAllReferences(references, outputs);
	}

	public static void mainImpl(String[] args, PrintStream ps) throws Exception {
		Inputs inputs = null;
		Outputs outputs = null;
		Flags flags = null;
		try {
			if (args.length < 3) {
				throw new EnforcerException("not enough args" + USAGE, Errors.NOT_ENOUGH_ARGS);
			}
			if (args.length > 12) {
				throw new EnforcerException("too many args" + USAGE, Errors.TOO_MANY_ARGS);
			}
			inputs = new Inputs(new File(args[0]), new File(args[1]));
			outputs = new Outputs(new File(args[2]));
			flags = new Flags();
			for (int i = 3; i < args.length; i++) {
				parseArg(args[i], inputs, outputs, flags);
			}
			if (outputs.unresolvedTypes() == null) {
				outputs.setUnresolvedTypes(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
			}
			if (outputs.illegalReferences() == null) {
				outputs.setIllegalReferences(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
			}
		} catch (Throwable t) {
			ps.println(t.getMessage());
			return;
		}
		ps.println("Analyzing/enforcing architecture with " + inputs.toString() + ", " + outputs.toString() + ", " + flags.toString() + " (may take 30+ seconds...)");
		mainImpl(inputs, outputs, ps, flags);
	}
}
