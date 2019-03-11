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

// Handles interactions with the user, and outputs results. Delegates analysis and enforcement to EnforcerUtils.
public class Enforce {

	private static final String ANALYZE_WAR_USAGE = ": usage: /full/path/to/target/architecture/.yaml /full/path/to/.war /full/path/to/writable/output/directory " + Optionals.IGNORES + "/full/path/to/file/of/packages/and/classes/to/ignore "
			+ Optionals.REFLECTIONS + "/full/path/to/file/of/reflection/references " + Optionals.FIX_UNRESOLVEDS + "/full/path/to/file/of/fixed/unresolveds " + Optionals.PRESERVE_NESTED_TYPES + " (preserves nested types) " + Optionals.STRICT
			+ " (strict, requires that all types resolve and no illegal references) " + Optionals.DEBUG + " (debug) [last six args optional and unordered]";

	private static final String RAPID_ITERATION_USAGE = ": usage: /full/path/to/target/architecture/.yaml /full/path/to/" + Outputs.ALL_REFERENCES_BASE_NAME + ".txt /full/path/to/writable/output/directory " + Optionals.STRICT
			+ " (strict, requires that all types resolve and no illegal references) " + Optionals.DEBUG + " (debug) [last two args optional and unordered]";

	static void parseArg(String arg, Inputs inputs, Flags flags) {
		boolean isWar = inputs instanceof AnalyzeWarInputs;
		String usage = isWar ? ANALYZE_WAR_USAGE : RAPID_ITERATION_USAGE;
		try {
			if (isWar) {
				if (arg.startsWith(Optionals.IGNORES.indicator())) {
					((AnalyzeWarInputs)inputs).setIgnores(new File(arg.replaceFirst(Optionals.IGNORES.indicator(), "")));
					return;
				}
				if (arg.startsWith(Optionals.REFLECTIONS.indicator())) {
					((AnalyzeWarInputs)inputs).setReflections(new File(arg.replaceFirst(Optionals.REFLECTIONS.indicator(), "")));
					return;
				}
				if (arg.startsWith(Optionals.FIX_UNRESOLVEDS.indicator())) {
					((AnalyzeWarInputs)inputs).setFixUnresolveds(new File(arg.replaceFirst(Optionals.FIX_UNRESOLVEDS.indicator(), "")));
					return;
				}
				if (arg.startsWith(Optionals.PRESERVE_NESTED_TYPES.indicator())) {
					((AnalyzeWarFlags)flags).enablePreserveNestedTypes();
					return;
				}
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
			throw new EnforcerException(e.getMessage() + usage, e.error());
		}
		throw new EnforcerException("unrecognized option " + arg + usage, Errors.UNRECOGNIZED_COMMAND_LINE_OPTION);
	}

	static void debug(Target target, Map<String, Type> types, RollUp rollUp, PrintStream console, Flags flags, int max) throws Exception {
		if (!flags.debug()) {
			return;
		}
		TargetUtils.dump(target, console);
		rollUp.dump(console);
		console.println("Total outermost types: " + types.size());
		if (types.size() > max) {
			return;
		}
		for (String typeName : CollectionUtils.sort(new ArrayList<>(types.keySet()))) {
			console.println("\t" + typeName);
			for (String referenceName : CollectionUtils.sort(new ArrayList<>(types.get(typeName).referenceNames()))) {
				console.println("\t\t" + referenceName);
			}
		}
	}

	static boolean output(Errors error, Problem problem) {
		if (error == null) {
			return problem.isWarning();
		}
		return problem.error() == error;
	}

	static String suffix(int count, File file) {
		if (count == 1) {
			return ", SEE " + file.getAbsolutePath();
		}
		return "S (" + count + "), SEE " + file.getAbsolutePath();
	}

	static String heading(Errors error, int count, File problemsFile) {
		if (error == null) {
			return "WARNING" + suffix(count, problemsFile);
		}
		return error.name().replace("_", " ") + suffix(count, problemsFile);
	}

	static void reportProblems(Set<Problem> problems, Errors error, int count, PrintStream console, File problemsFile) throws Exception {
		if (count == 0) {
			return;
		}
		try (PrintStream out = new PrintStream(new FileOutputStream(problemsFile))) {
			console.println(heading(error, count, problemsFile));
			for (Problem problem : CollectionUtils.sort(new ArrayList<>(problems))) {
				if (output(error, problem)) {
					if (problem.isWarning()) {
						out.println(problem.humanReadableToString());
					} else {
						out.println(problem.description());
					}
				}
			}
		}
	}

	static void reportProblems(Set<Problem> problems, PrintStream console, Outputs outputs, Flags flags) throws Exception {
		int warningsCount = 0;
		int unresolvedTypesCount = 0;
		int illegalReferencesCount = 0;
		int illegalComponentReferencesCount = 0;
		for (Problem problem : problems) {
			if (problem.error() == Errors.UNRESOLVED_REFERENCE) {
				unresolvedTypesCount++;
			} else if (problem.error() == Errors.ILLEGAL_REFERENCE) {
				illegalReferencesCount++;
			} else if (problem.error() == Errors.ILLEGAL_COMPONENT_REFERENCE) {
				illegalComponentReferencesCount++;
			} else {
				warningsCount++; // There are only three errors that aren't immediately fatal, and they're handled above, so this has to be a warning.
			}
		}
		reportProblems(problems, null, warningsCount, console, outputs.warnings());
		reportProblems(problems, Errors.UNRESOLVED_REFERENCE, unresolvedTypesCount, console, outputs.unresolvedTypes());
		reportProblems(problems, Errors.ILLEGAL_REFERENCE, illegalReferencesCount, console, outputs.illegalReferences());
		reportProblems(problems, Errors.ILLEGAL_COMPONENT_REFERENCE, illegalComponentReferencesCount, console, outputs.illegalComponentReferences());
	}

	static void output(Set<String> content, PrintStream output) {
		for (String line : CollectionUtils.sort(new ArrayList<>(content))) {
			output.println(line);
		}
		content.clear();
	}

	static Set<String> descriptions(Set<Reference> references, boolean includeClasses) {
		Set<String> descriptions = new HashSet<>();
		for (Reference reference : references) {
			descriptions.add(reference.parseableDescription(includeClasses, true));
		}
		return descriptions;
	}

	static Map<String, Integer> nodes(List<Reference> references, boolean includeClasses) {
		Map<String, Integer> nodes = new HashMap<>();
		int id = 0;
		for (Reference reference : references) {
			String referrer = name(reference.referringType(), includeClasses);
			if (!nodes.containsKey(referrer)) {
				id++;
				nodes.put(referrer, id);
			}
			String referredTo = name(reference.referredToType(), includeClasses);
			if (!nodes.containsKey(referredTo)) {
				id++;
				nodes.put(referredTo, id);
			}
		}
		return nodes;
	}

	static String name(Type type, boolean includeClasses) {
		if (includeClasses) {
			return type.name();
		}
		return type.component().name();
	}

	static void outputReferences(Set<Reference> references, boolean includeClasses, File allFile, File gephiNodesFile, File gephiEdgesFile, File yEdFile) throws Exception {
		// Main output.
		try (PrintStream out = new PrintStream(new FileOutputStream(allFile))) {
			output(descriptions(references, includeClasses), out);
		}
		// Output for graphics tools. Sorting makes output deterministic.
		List<Reference> allRefs = CollectionUtils.sort(new ArrayList<>(references));
		Map<String, Integer> nodes = nodes(allRefs, includeClasses);
		try (PrintStream gephiNodes = new PrintStream(new FileOutputStream(gephiNodesFile)); PrintStream gephiEdges = new PrintStream(new FileOutputStream(gephiEdgesFile)); PrintStream yed = new PrintStream(new FileOutputStream(yEdFile))) {
			gephiNodes.println("ID;Label");
			Set<String> gephiText = new HashSet<>();
			Set<String> yedText = new HashSet<>();
			for (String nodeName : CollectionUtils.sort(new ArrayList<>(nodes.keySet()))) {
				int nodeId = nodes.get(nodeName);
				gephiText.add(nodeId + ";" + nodeName);
				yedText.add(nodeId + " " + nodeName);
			}
			output(gephiText, gephiNodes);
			output(yedText, yed);
			yed.println("#");
			gephiEdges.println("Source;Target");
			for (Reference reference : allRefs) {
				String referrer = name(reference.referringType(), includeClasses);
				String referredTo = name(reference.referredToType(), includeClasses);
				if (referrer.equals(referredTo)) {
					continue;
				}
				int referrerNodeId = nodes.get(referrer);
				int referredToNodeId = nodes.get(referredTo);
				gephiText.add(referrerNodeId + ";" + referredToNodeId);
				yedText.add(referrerNodeId + " " + referredToNodeId);
			}
			output(gephiText, gephiEdges);
			output(yedText, yed);
		}
	}

	static void mainImpl(Inputs inputs, Outputs outputs, PrintStream console, Flags flags) throws Exception {
		Target target = TargetUtils.parse(inputs.target());
		Set<Problem> problems = new LinkedHashSet<>();
		Map<String, Type> types = EnforcerUtils.resolve(inputs, problems, flags);
		RollUp rollUp = new RollUp();
		Set<Reference> references = new HashSet<>();
		EnforcerUtils.correlate(types, target.components(), rollUp, references, problems, flags);
		debug(target, types, rollUp, console, flags, 100);
		reportProblems(problems, console, outputs, flags);
		outputReferences(references, true, outputs.allReferences(), outputs.allReferencesGephiNodes(), outputs.allReferencesGephiEdges(), outputs.allReferencesYeD());
		outputReferences(references, false, outputs.allComponentReferences(), outputs.allComponentReferencesGephiNodes(), outputs.allComponentReferencesGephiEdges(), outputs.allComponentReferencesYeD());
	}

	public static void mainImpl(String[] args, PrintStream console) throws Exception {
		Inputs inputs = null;
		Outputs outputs = null;
		Flags flags = null;
		try {
			if (args.length < 3) {
				throw new EnforcerException("not enough args" + ANALYZE_WAR_USAGE, Errors.NOT_ENOUGH_ARGS);
			}
			File target = new File(args[0]);
			File data = new File(args[1]);
			File outputDirectory = new File(args[2]);
			outputs = new Outputs(outputDirectory);
			boolean isWar = data.getName().endsWith(".war");
			if (isWar) {
				if (args.length > 9) {
					throw new EnforcerException("too many args" + ANALYZE_WAR_USAGE, Errors.TOO_MANY_ARGS);
				}
				inputs = new AnalyzeWarInputs(target, data);
				flags = new AnalyzeWarFlags();
			} else {
				if (args.length > 5) {
					throw new EnforcerException("too many args" + RAPID_ITERATION_USAGE, Errors.TOO_MANY_ARGS);
				}
				inputs = new RapidIterationInputs(target, data);
				if (((RapidIterationInputs)inputs).allReferences().getAbsolutePath().equals(outputs.allReferences().getAbsolutePath())) {
					throw new EnforcerException("Rapid-iteration input file " + data.getAbsolutePath() + " would be overwritten", Errors.RAPID_ITERATION_INPUT_FILE_WOULD_BE_OVERWRITTEN);
				}
				flags = new Flags();
			}
			for (int i = 3; i < args.length; i++) {
				parseArg(args[i], inputs, flags);
			}
		} catch (Throwable t) {
			console.println(t.getMessage());
			return;
		}
		console.println("Analyzing/enforcing architecture with " + inputs.toString() + ", " + outputs.toString() + ", " + flags.toString() + " (may take 60+ seconds...)");
		mainImpl(inputs, outputs, console, flags);
	}
}
