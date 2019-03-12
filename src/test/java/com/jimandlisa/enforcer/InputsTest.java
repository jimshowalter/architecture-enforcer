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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class InputsTest {

	@Test
	public void doTest() {
		AnalyzeBinaryInputs analyzeBinaryInputs = TestUtils.analyzeWarInputs(false, false, false);
		assertNotNull(analyzeBinaryInputs.target());
		assertNotNull(analyzeBinaryInputs.binary());
		analyzeBinaryInputs.toString();
		assertNull(analyzeBinaryInputs.ignores());
		analyzeBinaryInputs.setIgnores(TestUtils.testClassesFile("SampleIgnores.txt"));
		assertNotNull(analyzeBinaryInputs.ignores());
		analyzeBinaryInputs.toString();
		assertNull(analyzeBinaryInputs.reflections());
		analyzeBinaryInputs.setReflections(TestUtils.testClassesFile("SampleReflections.txt"));
		assertNotNull(analyzeBinaryInputs.reflections());
		analyzeBinaryInputs.toString();
		assertNull(analyzeBinaryInputs.fixUnresolveds());
		analyzeBinaryInputs.setFixUnresolveds(TestUtils.testClassesFile("SampleFixUnresolveds.txt"));
		assertNotNull(analyzeBinaryInputs.fixUnresolveds());
		analyzeBinaryInputs.toString();
		analyzeBinaryInputs = TestUtils.analyzeWarInputs(true, true, true);
		analyzeBinaryInputs.toString();
		RapidIterationInputs rapidIterationInputs = TestUtils.rapidIterationInputs();
		assertNotNull(rapidIterationInputs.target());
		assertNotNull(rapidIterationInputs.allReferences());
		rapidIterationInputs.toString();
		try {
			analyzeBinaryInputs.setIgnores(new File("foo"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("already set ignores file"));
			assertEquals(Errors.IGNORES_FILE_ALREADY_SPECIFIED, e.error());
		}
		try {
			analyzeBinaryInputs.setReflections(new File("foo"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("already set reflections file"));
			assertEquals(Errors.REFLECTIONS_FILE_ALREADY_SPECIFIED, e.error());
		}
		try {
			analyzeBinaryInputs.setFixUnresolveds(new File("foo"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("already set fix-unresolveds file"));
			assertEquals(Errors.FIX_UNRESOLVEDS_FILE_ALREADY_SPECIFIED, e.error());
		}
	}
}