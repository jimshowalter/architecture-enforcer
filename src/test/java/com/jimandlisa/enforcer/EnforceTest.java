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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class EnforceTest {
	
	private static final String ALL_REFERENCES_NAME = Outputs.ALL_REFERENCES_BASE_NAME + ".txt";
	private static final String ALL_COMPONENT_REFERENCES_NAME = Outputs.ALL_COMPONENT_REFERENCES_BASE_NAME + ".txt";

	private static String normalize(Path path) {
		String normalized = path.toString().replace("\\", "/");
		if (!normalized.startsWith("/")) {
			return "/" + normalized;
		}
		return normalized;
	}

	private static void compare(ByteArrayOutputStream baos, String canned) throws Exception {
		String out = new String(baos.toByteArray(), StandardCharsets.UTF_8).trim().replaceAll("\r\n\r\n", "\r\n").replace("\\", "/").replaceAll("=[^=]+/architecture-enforcer/target/",
				"=/architecture-enforcer/target/");
		String cannedOut = TestUtils.readTestClassesFile(canned).trim().replaceAll("\r\n\r\n", "\r\n");
		assertEquals(out, cannedOut);
	}

	@Test
	public void testMisc() {
		new Enforce();
	}

	@Test
	public void testArgs() {
		Inputs inputs = TestUtils.inputs(false, false, false);
		Outputs outputs = new Outputs(TestUtils.targetDir());
		Flags flags = new Flags();
		Enforce.parseArg(Optionals.UNRESOLVED_TYPES_OUTPUT_FILE.indicator() + "somefile1.txt", inputs, outputs, flags);
		assertEquals("somefile1.txt", outputs.unresolvedTypes().getName());
		try {
			Enforce.parseArg(Optionals.UNRESOLVED_TYPES_OUTPUT_FILE.indicator() + "anotherfile.txt", inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.UNRESOLVED_TYPES_OUTPUT_FILE_ALREADY_SPECIFIED, e.error());
		}
		Enforce.parseArg(Optionals.ILLEGAL_REFERENCES_OUTPUT_FILE.indicator() + "somefile2.txt", inputs, outputs, flags);
		assertEquals("somefile2.txt", outputs.illegalReferences().getName());
		try {
			Enforce.parseArg(Optionals.ILLEGAL_REFERENCES_OUTPUT_FILE.indicator() + "anotherfile.txt", inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.ILLEGAL_REFERENCES_OUTPUT_FILE_ALREADY_SPECIFIED, e.error());
		}
		Enforce.parseArg(Optionals.ALL_REFERENCES.indicator(), inputs, outputs, flags);
		assertEquals(ALL_REFERENCES_NAME, outputs.allReferences().getName());
		try {
			Enforce.parseArg(Optionals.ALL_REFERENCES.indicator() + "anotherfile.txt", inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.ALL_REFERENCES_ALREADY_ENABLED, e.error());
		}
		Enforce.parseArg(Optionals.IGNORES.indicator() + TestUtils.testClassesFile("SampleIgnores.txt").getAbsolutePath(), inputs, outputs, flags);
		assertEquals(normalize(TestUtils.testClassesFile("SampleIgnores.txt").toPath()), normalize(inputs.ignores().toPath()));
		try {
			Enforce.parseArg(Optionals.IGNORES.indicator() + "foo", TestUtils.inputs(true, false, false), outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.IGNORES_FILE_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parseArg(Optionals.REFLECTIONS.indicator() + TestUtils.testClassesFile("SampleReflections.txt").getAbsolutePath(), inputs, outputs, flags);
		assertEquals(normalize(TestUtils.testClassesFile("SampleReflections.txt").toPath()), normalize(inputs.reflections().toPath()));
		try {
			Enforce.parseArg(Optionals.REFLECTIONS.indicator() + "foo", TestUtils.inputs(false, true, false), outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.REFLECTIONS_FILE_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parseArg(Optionals.FIX_UNRESOLVEDS.indicator() + TestUtils.testClassesFile("SampleFixUnresolveds.txt").getAbsolutePath(), inputs, outputs, flags);
		assertEquals(normalize(TestUtils.testClassesFile("SampleFixUnresolveds.txt").toPath()), normalize(inputs.fixUnresolveds().toPath()));
		try {
			Enforce.parseArg(Optionals.FIX_UNRESOLVEDS.indicator() + "foo", TestUtils.inputs(false, false, true), outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.FIX_UNRESOLVEDS_FILE_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parseArg(Optionals.PRESERVE_NESTED_TYPES.indicator(), inputs, outputs, flags);
		assertTrue(flags.preserveNestedTypes());
		try {
			Enforce.parseArg(Optionals.PRESERVE_NESTED_TYPES.indicator(), inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.PRESERVE_NESTED_TYPES_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parseArg(Optionals.STRICT.indicator(), inputs, outputs, flags);
		assertTrue(flags.strict());
		try {
			Enforce.parseArg(Optionals.STRICT.indicator(), inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.STRICT_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parseArg(Optionals.DEBUG.indicator(), inputs, outputs, flags);
		assertTrue(flags.debug());
		try {
			Enforce.parseArg(Optionals.DEBUG.indicator(), inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.DEBUG_ALREADY_SPECIFIED, e.error());
		}
		try {
			Enforce.parseArg("foo", TestUtils.inputs(false, false, false), outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.UNRECOGNIZED_COMMAND_LINE_OPTION, e.error());
		}
	}

	@Test
	public void testDebug() throws Exception {
		Enforce.debug(null, null, null, null, new Flags(), 100);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Target target = new Target();
			Flags flags = new Flags();
			flags.enableDebug();
			Enforce.debug(target, new HashMap<String, Type>(), new RollUp(), ps, flags, 100);
			TestUtils.compareTestClassesFile(baos, "TestDebugCanned1.txt");
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Target target = new Target();
			Flags flags = new Flags();
			flags.enableDebug();
			Map<String, Type> types = new HashMap<>();
			types.put("foo", new Type("foo"));
			Enforce.debug(target, types, new RollUp(), ps, flags, 100);
			TestUtils.compareTestClassesFile(baos, "TestDebugCanned2.txt");
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Target target = new Target();
			Flags flags = new Flags();
			flags.enableDebug();
			Map<String, Type> types = new HashMap<>();
			Type type = new Type("foo");
			type.referenceNames().add("bar");
			types.put(type.name(), type);
			Enforce.debug(target, types, new RollUp(), ps, flags, 100);
			Enforce.debug(target, types, new RollUp(), ps, flags, 0);
			TestUtils.compareTestClassesFile(baos, "TestDebugCanned3.txt");
		}
	}

	@Test
	public void testReportProblems() throws Exception {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Set<Problem> problems = new LinkedHashSet<Problem>();
			problems.add(new Problem("foo", Errors.ILLEGAL_REFERENCE));
			Outputs outputs = new Outputs(Paths.get(TestUtils.targetDir().toString(), "reports").toFile());
			outputs.setUnresolvedTypes(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
			outputs.setIllegalReferences(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
			outputs.unresolvedTypes().delete();
			outputs.illegalReferences().delete();
			Enforce.reportProblems(problems, ps, outputs, new Flags());
			assertFalse(outputs.unresolvedTypes().exists());
			assertTrue(outputs.illegalReferences().exists());
			outputs.illegalReferences().delete();
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Set<Problem> problems = new LinkedHashSet<Problem>();
			problems.add(new Problem("bar", Errors.UNRESOLVED_REFERENCE));
			Outputs outputs = new Outputs(Paths.get(TestUtils.targetDir().toString(), "reports").toFile());
			outputs.setUnresolvedTypes(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
			outputs.setIllegalReferences(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
			outputs.unresolvedTypes().delete();
			outputs.illegalReferences().delete();
			Enforce.reportProblems(problems, ps, outputs, new Flags());
			assertTrue(outputs.unresolvedTypes().exists());
			assertFalse(outputs.illegalReferences().exists());
			outputs.unresolvedTypes().delete();
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Set<Problem> problems = new LinkedHashSet<Problem>();
			problems.add(new Problem("foo", Errors.ILLEGAL_REFERENCE));
			problems.add(new Problem("bar", Errors.UNRESOLVED_REFERENCE));
			Outputs outputs = new Outputs(Paths.get(TestUtils.targetDir().toString(), "reports").toFile());
			outputs.setUnresolvedTypes(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
			outputs.setIllegalReferences(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
			outputs.unresolvedTypes().delete();
			outputs.illegalReferences().delete();
			Enforce.reportProblems(problems, ps, outputs, new Flags());
			assertTrue(outputs.unresolvedTypes().exists());
			assertTrue(outputs.illegalReferences().exists());
			outputs.unresolvedTypes().delete();
			outputs.illegalReferences().delete();
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Set<Problem> problems = new LinkedHashSet<Problem>();
			problems.add(new Problem("bar", Errors.UNRESOLVED_REFERENCE));
			problems.add(new Problem("foo", Errors.ILLEGAL_REFERENCE));
			Outputs outputs = new Outputs(Paths.get(TestUtils.targetDir().toString(), "reports").toFile());
			outputs.setUnresolvedTypes(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
			outputs.setIllegalReferences(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
			outputs.unresolvedTypes().delete();
			outputs.illegalReferences().delete();
			Enforce.reportProblems(problems, ps, outputs, new Flags());
			assertTrue(outputs.unresolvedTypes().exists());
			assertTrue(outputs.illegalReferences().exists());
			outputs.unresolvedTypes().delete();
			outputs.illegalReferences().delete();
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Set<Problem> problems = new LinkedHashSet<Problem>();
			problems.add(new Problem("foo", Errors.CANNOT_READ_FILE)); // Won't happen, but need to do this to force a codepath.
			Outputs outputs = new Outputs(Paths.get(TestUtils.targetDir().toString(), "reports").toFile());
			outputs.setUnresolvedTypes(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
			outputs.setIllegalReferences(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
			outputs.unresolvedTypes().delete();
			outputs.illegalReferences().delete();
			Enforce.reportProblems(problems, ps, outputs, new Flags());
			assertFalse(outputs.unresolvedTypes().exists());
			assertFalse(outputs.illegalReferences().exists());
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Set<Problem> problems = new LinkedHashSet<Problem>();
			problems.add(new Problem("foo", Errors.UNABLE_TO_RELEASE_WORKSET));
			Outputs outputs = new Outputs(Paths.get(TestUtils.targetDir().toString(), "reports").toFile());
			outputs.setUnresolvedTypes(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
			outputs.setIllegalReferences(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
			outputs.unresolvedTypes().delete();
			outputs.illegalReferences().delete();
			Enforce.reportProblems(problems, ps, outputs, new Flags());
			assertFalse(outputs.unresolvedTypes().exists());
			assertFalse(outputs.illegalReferences().exists());
			TestUtils.compareTestClassesFile(baos, "CannedWarnings1.txt");
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Set<Problem> problems = new LinkedHashSet<Problem>();
			problems.add(new Problem("foo", Errors.UNABLE_TO_RELEASE_WORKSET));
			problems.add(new Problem("bar", Errors.UNABLE_TO_RELEASE_WORKSET));
			Outputs outputs = new Outputs(Paths.get(TestUtils.targetDir().toString(), "reports").toFile());
			outputs.setUnresolvedTypes(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
			outputs.setIllegalReferences(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
			outputs.unresolvedTypes().delete();
			outputs.illegalReferences().delete();
			Enforce.reportProblems(problems, ps, outputs, new Flags());
			assertFalse(outputs.unresolvedTypes().exists());
			assertFalse(outputs.illegalReferences().exists());
			TestUtils.compareTestClassesFile(baos, "CannedWarnings2.txt");
		}
	}
	
	@Test
	public void testProblemsCount() {
		assertEquals(0, Enforce.problemsCount(false, false));
		assertEquals(1, Enforce.problemsCount(false, true));
		assertEquals(1, Enforce.problemsCount(true, false));
		assertEquals(2, Enforce.problemsCount(true, true));
	}

	@Test
	public void testOutputAllClassToClassReferences() throws Exception {
		Set<Reference> references = new HashSet<>();
		Outputs outputs = TestUtils.outputs();
		Enforce.outputAllClassToClassReferences(references, outputs);
		outputs.enableAllReferences();
		Enforce.outputAllClassToClassReferences(references, outputs);
		Layer layer1 = new Layer("One", 1, null);
		Component comp1 = new Component("Comp1", layer1, null, null);
		Component comp2 = new Component("Comp2", layer1, null, null);
		Layer layer2 = new Layer("Two", 2, null);
		Component comp3 = new Component("Comp3", layer2, null, null);
		Type type1 = new Type("foo");
		type1.setComponent(comp1);
		Type type2 = new Type("bar");
		type2.setComponent(comp1);
		Type type3 = new Type("baz");
		type3.setComponent(comp2);
		Type type4 = new Type("bum");
		type4.setComponent(comp3);
		outputs.allReferences().delete();
		Enforce.outputAllClassToClassReferences(references, outputs);
		assertFalse(outputs.allReferences().exists());
		Reference legalIntraComponent = new Reference(type1, type2);
		references.add(legalIntraComponent);
		Enforce.outputAllClassToClassReferences(references, outputs);
		TestUtils.compareTargetFile(ALL_REFERENCES_NAME, "CannedAllReferences1.txt");
		outputs.allReferences().delete();
		Reference illegalInterComponentSameLayer1 = new Reference(type1, type3);
		references.add(illegalInterComponentSameLayer1);
		Enforce.outputAllClassToClassReferences(references, outputs);
		TestUtils.compareTargetFile(ALL_REFERENCES_NAME, "CannedAllReferences2.txt");
		outputs.allReferences().delete();
		Reference illegalInterComponentSameLayer2 = new Reference(type2, type3);
		references.add(illegalInterComponentSameLayer2);
		Reference legalDifferentLayersDownwards = new Reference(type4, type1);
		references.add(legalDifferentLayersDownwards);
		Reference illegalDifferentLayersUpwards = new Reference(type1, type4);
		references.add(illegalDifferentLayersUpwards);
		Enforce.outputAllClassToClassReferences(references, outputs);
		TestUtils.compareTargetFile(ALL_REFERENCES_NAME, "CannedAllReferences3.txt");
		TestUtils.compareTargetFile(Outputs.ALL_REFERENCES_BASE_NAME + Outputs.GEPHI_NODES_SUFFIX, "CannedAllReferences3" + Outputs.GEPHI_NODES_SUFFIX);
		TestUtils.compareTargetFile(Outputs.ALL_REFERENCES_BASE_NAME +Outputs.GEPHI_EDGES_SUFFIX, "CannedAllReferences3" + Outputs.GEPHI_EDGES_SUFFIX);
		TestUtils.compareTargetFile(Outputs.ALL_REFERENCES_BASE_NAME + Outputs.YED_SUFFIX, "CannedAllReferences3" + Outputs.YED_SUFFIX);
		outputs.allReferences().delete();
	}
	
	@Test
	public void testOutputAllComponentToComponentReferences() throws Exception {
		Set<Component> components = new HashSet<>();
		Outputs outputs = TestUtils.outputs();
		Enforce.outputAllComponentToComponentReferences(components, outputs);
		outputs.enableAllReferences();
		Enforce.outputAllComponentToComponentReferences(components, outputs);
		Layer layer1 = new Layer("One", 1, null);
		Component comp1 = new Component("Comp1", layer1, null, null);
		Component comp2 = new Component("Comp2", layer1, null, null);
		Layer layer2 = new Layer("Two", 2, null);
		Component comp3 = new Component("Comp3", layer2, null, null);
		Type type1 = new Type("foo");
		type1.setComponent(comp1);
		Type type2 = new Type("bar");
		type2.setComponent(comp1);
		Type type3 = new Type("baz");
		type3.setComponent(comp2);
		Type type4 = new Type("bum");
		type4.setComponent(comp3);
		outputs.allComponentReferences().delete();
		Enforce.outputAllComponentToComponentReferences(components, outputs);
		assertFalse(outputs.allComponentReferences().exists());
		components.add(comp1);
		components.add(comp2);
		components.add(comp3);
		Reference legalIntraComponent = new Reference(type1, type2);
		comp1.references().add(legalIntraComponent);
		Enforce.outputAllComponentToComponentReferences(components, outputs);
		TestUtils.compareTargetFile(ALL_COMPONENT_REFERENCES_NAME, "CannedAllComponentReferences1.txt");
		outputs.allComponentReferences().delete();
		Reference illegalInterComponentSameLayer1 = new Reference(type1, type3);
		comp1.references().add(illegalInterComponentSameLayer1);
		Enforce.outputAllComponentToComponentReferences(components, outputs);
		TestUtils.compareTargetFile(ALL_COMPONENT_REFERENCES_NAME, "CannedAllComponentReferences2.txt");
		outputs.allComponentReferences().delete();
		Reference illegalInterComponentSameLayer2 = new Reference(type2, type3);
		comp1.references().add(illegalInterComponentSameLayer2);
		Reference legalDifferentLayersDownwards = new Reference(type4, type1);
		comp3.references().add(legalDifferentLayersDownwards);
		Reference illegalDifferentLayersUpwards = new Reference(type1, type4);
		comp1.references().add(illegalDifferentLayersUpwards);
		Enforce.outputAllComponentToComponentReferences(components, outputs);
		TestUtils.compareTargetFile(ALL_COMPONENT_REFERENCES_NAME, "CannedAllComponentReferences3.txt");
		TestUtils.compareTargetFile(Outputs.ALL_COMPONENT_REFERENCES_BASE_NAME + Outputs.GEPHI_NODES_SUFFIX, "CannedAllComponentReferences3" + Outputs.GEPHI_NODES_SUFFIX);
		TestUtils.compareTargetFile(Outputs.ALL_COMPONENT_REFERENCES_BASE_NAME + Outputs.GEPHI_EDGES_SUFFIX, "CannedAllComponentReferences3" + Outputs.GEPHI_EDGES_SUFFIX);
		TestUtils.compareTargetFile(Outputs.ALL_COMPONENT_REFERENCES_BASE_NAME + Outputs.YED_SUFFIX, "CannedAllComponentReferences3" + Outputs.YED_SUFFIX);
		outputs.allComponentReferences().delete();
	}

	@Test
	public void testMainImpl() throws Exception {
		File unresolvedTypesOutputFile = TestUtils.targetFile(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
		File illegalReferencesOutputFile = TestUtils.targetFile(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
		unresolvedTypesOutputFile.delete();
		illegalReferencesOutputFile.delete();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[0], ps);
			TestUtils.compareTestClassesFile(baos, "TestEnforceCanned2.txt");
		}
		assertTrue(!unresolvedTypesOutputFile.exists());
		assertTrue(!illegalReferencesOutputFile.exists());
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m" }, ps);
			TestUtils.compareTestClassesFile(baos, "TestEnforceCanned3.txt");
		}
		assertTrue(!unresolvedTypesOutputFile.exists());
		assertTrue(!illegalReferencesOutputFile.exists());
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] { TestUtils.testClassesFile("SampleTarget2.yaml").getAbsolutePath(), TestUtils.sampleWar().getAbsolutePath(), TestUtils.targetDir().getAbsolutePath(),
					Optionals.IGNORES.indicator() + TestUtils.testClassesFile("SampleIgnores.txt").getAbsolutePath() }, ps);
			compare(baos, "TestEnforceCanned4.txt");
		}
		assertTrue(!unresolvedTypesOutputFile.exists());
		assertTrue(illegalReferencesOutputFile.exists());
		TestUtils.compareTargetFile(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME, "TestIllegalReferencesOutputCanned.txt");
		illegalReferencesOutputFile.delete();
		File file1 = TestUtils.targetFile("file1.txt");
		file1.delete();
		File file2 = TestUtils.targetFile("file2.txt");
		file2.delete();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] { TestUtils.testClassesFile("SampleTarget2.yaml").getAbsolutePath(), TestUtils.sampleWar().getAbsolutePath(), TestUtils.targetDir().getAbsolutePath(),
					Optionals.UNRESOLVED_TYPES_OUTPUT_FILE + "file1.txt", Optionals.ILLEGAL_REFERENCES_OUTPUT_FILE + "file2.txt",
					Optionals.IGNORES.indicator() + TestUtils.testClassesFile("SampleIgnores.txt").getAbsolutePath() }, ps);
			compare(baos, "TestEnforceCanned5.txt");
		}
		assertTrue(!file1.exists());
		assertTrue(file2.exists());
		TestUtils.compareTargetFile("file2.txt", "TestIllegalReferencesOutputCanned.txt");
		file2.delete();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] { TestUtils.testClassesFile("SampleTarget2.yaml").getAbsolutePath(), TestUtils.sampleWar().getAbsolutePath(), TestUtils.targetDir().getAbsolutePath(),
					Optionals.ALL_REFERENCES.indicator(),
					Optionals.IGNORES.indicator() + TestUtils.testClassesFile("SampleIgnores.txt").getAbsolutePath() }, ps);
		}
		File allReferences = TestUtils.targetFile(ALL_REFERENCES_NAME);
		assertTrue(allReferences.exists());
		TestUtils.compareTargetFile(ALL_REFERENCES_NAME, "TestAllReferencesOutputCanned1.txt");
		allReferences.delete();
		illegalReferencesOutputFile.delete();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] { TestUtils.testClassesFile("SampleTarget2.yaml").getAbsolutePath(), TestUtils.sampleWar().getAbsolutePath(), TestUtils.targetDir().getAbsolutePath(),
					Optionals.ALL_REFERENCES.indicator(),
					Optionals.IGNORES.indicator() + TestUtils.testClassesFile("SampleIgnores.txt").getAbsolutePath(), "-p" }, ps);
		}
		assertTrue(allReferences.exists());
		TestUtils.compareTargetFile(ALL_REFERENCES_NAME, "TestAllReferencesOutputCanned2.txt");
		allReferences.delete();
		illegalReferencesOutputFile.delete();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] { TestUtils.testClassesFile("SampleTarget2.yaml").getAbsolutePath(), TestUtils.sampleWar().getAbsolutePath(), TestUtils.targetDir().getAbsolutePath(),
					Optionals.IGNORES.indicator() + TestUtils.testClassesFile("SampleIgnores.txt").getAbsolutePath(), "-s" }, ps);
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("FATAL ERROR:"));
			assertTrue(e.getMessage().contains(
					"ILLEGAL_REFERENCE: type com.jimandlisa.app.one.App1 in component 'App One' in layer 'App' depth 1 refers to type com.jimandlisa.app.two.App2 in component 'App Two' in layer 'App' depth 1"));
		}
	}
}
