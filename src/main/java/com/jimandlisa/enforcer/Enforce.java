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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Handles interactions with the user, and outputs results.
public class Enforce {

	private static final String USAGE = ": usage: /full/path/to/target/architecture/.yaml /full/path/to/.war /full/path/to/writable/output/directory " + Optionals.IGNORES
			+ "/full/path/to/file/of/packages/and/classes/to/ignore " + Optionals.REFLECTIONS + "/full/path/to/file/of/reflection/references " + Optionals.FIX_UNRESOLVEDS
			+ "/full/path/to/file/of/fixed/unresolveds " + Optionals.PRESERVE_NESTED_TYPES + " (preserves nested types) " + Optionals.STRICT
			+ " (strict, requires that all types resolve and no illegal references) " + Optionals.DEBUG + " (debug) [last six args optional and unordered]";

	static void parseArg(String arg, Inputs inputs, Flags flags) {
		try {
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

	static int problemsCount(boolean foundUnresolvedTypes, boolean foundIllegalReferences) {
		int count = foundUnresolvedTypes ? 1 : 0;
		count = foundIllegalReferences ? count + 2 : count;
		return count;
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

	// To have gotten here, there can't be any fatal errors. In strict mode, that means we can't get here at all if there are any problems.
	// In non-strict mode, we can get here, but only if all problems are fatal only when strict is specified. We need to report those problems.
	static void reportProblems(Set<Problem> problems, PrintStream ps, Outputs outputs, Flags flags) throws Exception {
		boolean foundUnresolvedTypes = false;
		boolean foundIllegalReferences = false;
		List<String> warnings = new ArrayList<>();
		for (Problem problem : problems) {
			if (problem.error() == Errors.UNRESOLVED_REFERENCE) {
				foundUnresolvedTypes = true;
			} else if (problem.error() == Errors.ILLEGAL_REFERENCE || problem.error() == Errors.ILLEGAL_COMPONENT_REFERENCE) {
				foundIllegalReferences = true;
			} else {
				warnings.add(problem.description());
			}
		}
		if (warnings.size() == 1) {
			ps.println("WARNING:");
			ps.println(warnings.iterator().next());
		} else if (warnings.size() > 1) {
			ps.println("WARNINGS:");
			for (String warning : warnings) {
				ps.println(warning);
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
			reportProblems(problems, Errors.ILLEGAL_COMPONENT_REFERENCE, outputs.illegalComponentReferences());
			ps.println(outputs.illegalComponentReferences().getName());
			reportProblems(problems, Errors.ILLEGAL_REFERENCE, outputs.illegalReferences());
			ps.println(outputs.illegalReferences().getName());
		}
	}

	static void outputAllClassToClassReferences(Set<Reference> references, Outputs outputs) throws Exception {
		Set<String> classReferences = new HashSet<>();
		for (Reference reference : references) {
			classReferences.add(reference.parseableDescription() + "!" + reference.kind());
		}
		List<String> allRefs = CollectionUtils.sort(new ArrayList<>(classReferences));
		try (PrintStream ps = new PrintStream(new FileOutputStream(outputs.allReferences()))) {
			for (String ref : allRefs) {
				ps.println(ref);
			}
		}
		Map<String, Integer> refs = new LinkedHashMap<>();
		int id = 0;
		for (String reference : allRefs) {
			String[] segments = reference.split("!");
			String referringType = segments[0];
			String referredToType = segments[4];
			if (!refs.containsKey(referringType)) {
				id++;
				refs.put(referringType, id);
				continue;
			}
			if (!refs.containsKey(referredToType)) {
				id++;
				refs.put(referredToType, id);
			}
		}
		try (PrintStream gephiNodes = new PrintStream(new FileOutputStream(outputs.allReferencesGephiNodes()));
				PrintStream gephiEdges = new PrintStream(new FileOutputStream(outputs.allReferencesGephiEdges()));
				PrintStream yed = new PrintStream(new FileOutputStream(outputs.allReferencesYeD()))) {
			gephiNodes.println("ID;Label");
			for (Map.Entry<String, Integer> entry : refs.entrySet()) {
				gephiNodes.println(entry.getValue() + ";" + entry.getKey());
				yed.println(entry.getValue() + " " + entry.getKey());
			}
			yed.println("#");
			gephiEdges.println("Source;Target");
			for (String reference : allRefs) {
				String[] segments = reference.split("!");
				String referringType = segments[0];
				String referredToType = segments[4];
				gephiEdges.println(refs.get(referringType) + ";" + refs.get(referredToType));
				yed.println(refs.get(referringType) + " " + refs.get(referredToType));
			}
		}
	}

	static void outputAllComponentToComponentReferences(Collection<Component> components, Outputs outputs) throws Exception {
		Set<String> componentRefs = new HashSet<>();
		for (Component component : components) {
			for (Reference reference : component.references()) {
				componentRefs.add(reference.parseableComponentDescription() + "!" + reference.kind());
			}
		}
		List<String> allRefs = CollectionUtils.sort(new ArrayList<>(componentRefs));
		try (PrintStream ps = new PrintStream(new FileOutputStream(outputs.allComponentReferences()))) {
			for (String ref : allRefs) {
				ps.println(ref);
			}
		}
		Map<String, Integer> refs = new LinkedHashMap<>();
		int id = 0;
		for (String reference : allRefs) {
			String[] segments = reference.split("!");
			String referringComponent = segments[0];
			String referredToComponent = segments[3];
			if (!refs.containsKey(referringComponent)) {
				id++;
				refs.put(referringComponent, id);
				continue;
			}
			if (!refs.containsKey(referredToComponent)) {
				id++;
				refs.put(referredToComponent, id);
			}
		}
		try (PrintStream gephiNodes = new PrintStream(new FileOutputStream(outputs.allComponentReferencesGephiNodes()));
				PrintStream gephiEdges = new PrintStream(new FileOutputStream(outputs.allComponentReferencesGephiEdges()));
				PrintStream yed = new PrintStream(new FileOutputStream(outputs.allComponentReferencesYeD()))) {
			gephiNodes.println("ID;Label");
			for (Map.Entry<String, Integer> entry : refs.entrySet()) {
				gephiNodes.println(entry.getValue() + ";" + entry.getKey());
				yed.println(entry.getValue() + " " + entry.getKey());
			}
			yed.println("#");
			gephiEdges.println("Source;Target");
			for (String reference : allRefs) {
				String[] segments = reference.split("!");
				String referringComponent = segments[0];
				String referredToComponent = segments[3];
				gephiEdges.println(refs.get(referringComponent) + ";" + refs.get(referredToComponent));
				yed.println(refs.get(referringComponent) + " " + refs.get(referredToComponent));
			}
		}
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
		outputAllClassToClassReferences(references, outputs);
		outputAllComponentToComponentReferences(target.components().values(), outputs);
	}

	public static void mainImpl(String[] args, PrintStream ps) throws Exception {
		Inputs inputs = null;
		Outputs outputs = null;
		Flags flags = null;
		try {
			if (args.length < 3) {
				throw new EnforcerException("not enough args" + USAGE, Errors.NOT_ENOUGH_ARGS);
			}
			if (args.length > 9) {
				throw new EnforcerException("too many args" + USAGE, Errors.TOO_MANY_ARGS);
			}
			inputs = new Inputs(new File(args[0]), new File(args[1]));
			outputs = new Outputs(new File(args[2]));
			flags = new Flags();
			for (int i = 3; i < args.length; i++) {
				parseArg(args[i], inputs, flags);
			}
		} catch (Throwable t) {
			ps.println(t.getMessage());
			return;
		}
		ps.println("Analyzing/enforcing architecture with " + inputs.toString() + ", " + outputs.toString() + ", " + flags.toString() + " (may take 60+ seconds...)");
		mainImpl(inputs, outputs, ps, flags);
	}
}
