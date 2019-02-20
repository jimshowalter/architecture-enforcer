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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;

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
		String out = new String(baos.toByteArray(), StandardCharsets.UTF_8).trim().replaceAll("\r\n\r\n", "\r\n").replace("\\", "/").replaceAll("=[^=]+/architecture-enforcer/target/test-classes/", "=/architecture-enforcer/target/test-classes/");
		String cannedOut = TestUtils.read(canned).trim().replaceAll("\r\n\r\n", "\r\n");
		assertEquals(out, cannedOut);
	}

	@Test
	public void doTest() throws Exception {
		new Enforce();
		Inputs inputs = null;
		Flags flags = null;
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parse(Optionals.IGNORES.indicator() + Thread.currentThread().getContextClassLoader().getResource("SamplePackageIgnores.txt").getPath(), inputs, flags);
		assertEquals(Thread.currentThread().getContextClassLoader().getResource("SamplePackageIgnores.txt").getPath(), normalize(inputs.ignores().toPath()));
		try {
			Enforce.parse(Optionals.IGNORES.indicator() + "foo", TestUtils.inputs(true, false, false), flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.IGNORES_FILE_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parse(Optionals.REFLECTIONS.indicator() + Thread.currentThread().getContextClassLoader().getResource("SampleReflections.txt").getPath(), inputs, flags);
		assertEquals(Thread.currentThread().getContextClassLoader().getResource("SampleReflections.txt").getPath(),  normalize(inputs.reflections().toPath()));
		try {
			Enforce.parse(Optionals.REFLECTIONS.indicator() + "foo", TestUtils.inputs(false, true, false), flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.REFLECTIONS_FILE_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parse(Optionals.FIX_UNRESOLVEDS.indicator() + Thread.currentThread().getContextClassLoader().getResource("SampleFixUnresolveds.txt").getPath(), inputs, flags);
		assertEquals(Thread.currentThread().getContextClassLoader().getResource("SampleFixUnresolveds.txt").getPath(),  normalize(inputs.fixUnresolveds().toPath()));
		try {
			Enforce.parse(Optionals.FIX_UNRESOLVEDS.indicator() + "foo", TestUtils.inputs(false, false, true), flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.FIX_UNRESOLVEDS_FILE_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parse(Optionals.PRESERVE_NESTED_TYPES.indicator(), inputs, flags);
		assertTrue(flags.preserveNestedTypes());
		try {
			Enforce.parse(Optionals.PRESERVE_NESTED_TYPES.indicator(), inputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.PRESERVE_NESTED_TYPES_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parse(Optionals.STRICT.indicator(), inputs, flags);
		assertTrue(flags.strict());
		try {
			Enforce.parse(Optionals.STRICT.indicator(), inputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.STRICT_ALREADY_SPECIFIED, e.error());
		}
		inputs = TestUtils.inputs(false, false, false);
		flags = new Flags();
		Enforce.parse(Optionals.DEBUG.indicator(), inputs, flags);
		assertTrue(flags.debug());
		try {
			Enforce.parse(Optionals.DEBUG.indicator(), inputs, flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.DEBUG_ALREADY_SPECIFIED, e.error());
		}
		try {
			Enforce.parse("foo", TestUtils.inputs(false, false, false), flags);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals(Errors.UNRECOGNIZED_COMMAND_LINE_OPTION, e.error());
		}
		Enforce.debug(null, null, null, null, new Flags());
		Enforce.problems(new HashSet<Problem>(), null);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[0], ps);
			TestUtils.compare(baos, "TestEnforceCanned2.txt");
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i" }, ps);
			TestUtils.compare(baos, "TestEnforceCanned3.txt");
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] {Thread.currentThread().getContextClassLoader().getResource("SampleTarget2.yaml").getPath(), Thread.currentThread().getContextClassLoader().getResource("Sample.odem").getPath(), Optionals.IGNORES.indicator() + Thread.currentThread().getContextClassLoader().getResource("SamplePackageIgnores.txt").getPath()}, ps);
			compare(baos, "TestEnforceCanned4.txt");
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			Enforce.mainImpl(new String[] {Thread.currentThread().getContextClassLoader().getResource("SampleTarget2.yaml").getPath(), Thread.currentThread().getContextClassLoader().getResource("Sample.odem").getPath(), Optionals.IGNORES.indicator() + Thread.currentThread().getContextClassLoader().getResource("SamplePackageIgnores.txt").getPath(), "-s"}, ps);
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("FATAL ERRORS:"));
			assertTrue(e.getMessage().contains("UNRESOLVED_REFERENCE: com.jimandlisa.utils.Unresolved"));
			assertTrue(e.getMessage().contains("UNRESOLVED_REFERENCE: com.jimandlisa.utils.AnotherUnresolved"));
		}
	}
}
