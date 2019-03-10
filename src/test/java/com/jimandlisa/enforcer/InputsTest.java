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
		AnalyzeWarInputs analyzeWarInputs = TestUtils.analyzeWarInputs(false, false, false);
		assertNotNull(analyzeWarInputs.target());
		assertNotNull(analyzeWarInputs.war());
		analyzeWarInputs.toString();
		assertNull(analyzeWarInputs.ignores());
		analyzeWarInputs.setIgnores(TestUtils.testClassesFile("SampleIgnores.txt"));
		assertNotNull(analyzeWarInputs.ignores());
		analyzeWarInputs.toString();
		assertNull(analyzeWarInputs.reflections());
		analyzeWarInputs.setReflections(TestUtils.testClassesFile("SampleReflections.txt"));
		assertNotNull(analyzeWarInputs.reflections());
		analyzeWarInputs.toString();
		assertNull(analyzeWarInputs.fixUnresolveds());
		analyzeWarInputs.setFixUnresolveds(TestUtils.testClassesFile("SampleFixUnresolveds.txt"));
		assertNotNull(analyzeWarInputs.fixUnresolveds());
		analyzeWarInputs.toString();
		analyzeWarInputs = TestUtils.analyzeWarInputs(true, true, true);
		analyzeWarInputs.toString();
		RapidIterationInputs rapidIterationInputs = TestUtils.rapidIterationInputs();
		assertNotNull(rapidIterationInputs.target());
		assertNotNull(rapidIterationInputs.allReferences());
		rapidIterationInputs.toString();
		try {
			analyzeWarInputs.setIgnores(new File("foo"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("already set ignores file"));
			assertEquals(Errors.IGNORES_FILE_ALREADY_SPECIFIED, e.error());
		}
		try {
			analyzeWarInputs.setReflections(new File("foo"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("already set reflections file"));
			assertEquals(Errors.REFLECTIONS_FILE_ALREADY_SPECIFIED, e.error());
		}
		try {
			analyzeWarInputs.setFixUnresolveds(new File("foo"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("already set fix-unresolveds file"));
			assertEquals(Errors.FIX_UNRESOLVEDS_FILE_ALREADY_SPECIFIED, e.error());
		}
	}
}