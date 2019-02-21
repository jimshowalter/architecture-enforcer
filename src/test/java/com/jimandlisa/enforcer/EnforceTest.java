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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class EnforceTest {

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
		Enforce.parseArgs(Optionals.UNRESOLVED_TYPES_OUTPUT_FILE.indicator() + "somefile1.txt", inputs, outputs, flags);
		assertEquals("somefile1.txt", outputs.unresolvedTypes().getName());
		try {
			Enforce.parseArgs(Optionals.UNRESOLVED_TYPES_OUTPUT_FILE.indicator() + "anotherfile.txt", inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.UNRESOLVED_TYPES_OUTPUT_FILE_ALREADY_SPECIFIED, e.error());
		}
		Enforce.parseArgs(Optionals.ILLEGAL_REFERENCES_OUTPUT_FILE.indicator() + "somefile2.txt", inputs, outputs, flags);
		assertEquals("somefile2.txt", outputs.illegalReferences().getName());
		try {
			Enforce.parseArgs(Optionals.ILLEGAL_REFERENCES_OUTPUT_FILE.indicator() + "anotherfile.txt", inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.ILLEGAL_REFERENCES_OUTPUT_FILE_ALREADY_SPECIFIED, e.error());
		}
		Enforce.parseArgs(Optionals.IGNORES.indicator() + TestUtils.testClassesFile("SamplePackageIgnores.txt").getAbsolutePath(), inputs, outputs, flags);
		assertEquals(normalize(TestUtils.testClassesFile("SamplePackageIgnores.txt").toPath()), normalize(inputs.ignores().toPath()));
		try {
			Enforce.parseArgs(Optionals.IGNORES.indicator() + "foo", TestUtils.inputs(true, false, false), outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.IGNORES_FILE_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parseArgs(Optionals.REFLECTIONS.indicator() + TestUtils.testClassesFile("SampleReflections.txt").getAbsolutePath(), inputs, outputs, flags);
		assertEquals(normalize(TestUtils.testClassesFile("SampleReflections.txt").toPath()), normalize(inputs.reflections().toPath()));
		try {
			Enforce.parseArgs(Optionals.REFLECTIONS.indicator() + "foo", TestUtils.inputs(false, true, false), outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.REFLECTIONS_FILE_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parseArgs(Optionals.FIX_UNRESOLVEDS.indicator() + TestUtils.testClassesFile("SampleFixUnresolveds.txt").getAbsolutePath(), inputs, outputs, flags);
		assertEquals(normalize(TestUtils.testClassesFile("SampleFixUnresolveds.txt").toPath()), normalize(inputs.fixUnresolveds().toPath()));
		try {
			Enforce.parseArgs(Optionals.FIX_UNRESOLVEDS.indicator() + "foo", TestUtils.inputs(false, false, true), outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.FIX_UNRESOLVEDS_FILE_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parseArgs(Optionals.PRESERVE_NESTED_TYPES.indicator(), inputs, outputs, flags);
		assertTrue(flags.preserveNestedTypes());
		try {
			Enforce.parseArgs(Optionals.PRESERVE_NESTED_TYPES.indicator(), inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.PRESERVE_NESTED_TYPES_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parseArgs(Optionals.STRICT.indicator(), inputs, outputs, flags);
		assertTrue(flags.strict());
		try {
			Enforce.parseArgs(Optionals.STRICT.indicator(), inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.STRICT_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parseArgs(Optionals.DEBUG.indicator(), inputs, outputs, flags);
		assertTrue(flags.debug());
		try {
			Enforce.parseArgs(Optionals.DEBUG.indicator(), inputs, outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.DEBUG_ALREADY_SPECIFIED, e.error());
		}
		try {
			Enforce.parseArgs("foo", TestUtils.inputs(false, false, false), outputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.UNRECOGNIZED_COMMAND_LINE_OPTION, e.error());
		}
	}

	@Test
	public void testDebug() throws Exception {
		Enforce.debug(null, null, null, null, new Flags());
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Target target = new Target();
			Flags flags = new Flags();
			flags.enableDebug();
			Enforce.debug(target, new HashMap<String, Type>(), new RollUp(), ps, flags);
			TestUtils.compareTestClassesFile(baos, "TestDebugCanned1.txt");
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Target target = new Target();
			Flags flags = new Flags();
			flags.enableDebug();
			Map<String, Type> types = new HashMap<>();
			types.put("foo", new Type("foo"));
			Enforce.debug(target, types, new RollUp(), ps, flags);
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
			Enforce.debug(target, types, new RollUp(), ps, flags);
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
			Enforce.reportProblems(problems, ps, outputs);
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
			Enforce.reportProblems(problems, ps, outputs);
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
			Enforce.reportProblems(problems, ps, outputs);
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
			Enforce.reportProblems(problems, ps, outputs);
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
			Enforce.reportProblems(problems, ps, outputs);
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
			Enforce.reportProblems(problems, ps, outputs);
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
			Enforce.reportProblems(problems, ps, outputs);
			assertFalse(outputs.unresolvedTypes().exists());
			assertFalse(outputs.illegalReferences().exists());
			TestUtils.compareTestClassesFile(baos, "CannedWarnings2.txt");
		}
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
			Enforce.mainImpl(new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l" }, ps);
			TestUtils.compareTestClassesFile(baos, "TestEnforceCanned3.txt");
		}
		assertTrue(!unresolvedTypesOutputFile.exists());
		assertTrue(!illegalReferencesOutputFile.exists());
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] { TestUtils.testClassesFile("SampleTarget2.yaml").getAbsolutePath(), TestUtils.sampleWar().getAbsolutePath(), TestUtils.targetDir().getAbsolutePath(),
					Optionals.IGNORES.indicator() + TestUtils.testClassesFile("SamplePackageIgnores.txt").getAbsolutePath() }, ps);
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
					Optionals.IGNORES.indicator() + TestUtils.testClassesFile("SamplePackageIgnores.txt").getAbsolutePath() }, ps);
			compare(baos, "TestEnforceCanned5.txt");
		}
		assertTrue(!file1.exists());
		assertTrue(file2.exists());
		TestUtils.compareTargetFile("file2.txt", "TestIllegalReferencesOutputCanned.txt");
		file2.delete();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] { TestUtils.testClassesFile("SampleTarget2.yaml").getAbsolutePath(), TestUtils.sampleWar().getAbsolutePath(),
					Optionals.IGNORES.indicator() + TestUtils.testClassesFile("SamplePackageIgnores.txt").getAbsolutePath(), "-s" }, ps);
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("FATAL ERRORS:"));
			assertTrue(e.getMessage().contains("UNRESOLVED_REFERENCE: com.jimandlisa.utils.Unresolved"));
			assertTrue(e.getMessage().contains("UNRESOLVED_REFERENCE: com.jimandlisa.utils.AnotherUnresolved"));
		}
	}
}
