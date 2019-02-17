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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class InputsTest {
	
	private static class MockFile extends File { // TODO: Replace with mockito.

		private static final long serialVersionUID = -6543042914615276188L;

		public MockFile(final String pathname) {
			super(pathname);
		}
		
		private boolean blowUp = false;
		
		public MockFile(final String pathname, final boolean blowUp) {
			super(pathname);
			this.blowUp = blowUp;
		}
		
		@Override
		public boolean canRead() {
			if (blowUp) {
				throw new RuntimeException("COVERAGE");
			}
			return false;
		}
	}

	@Test
	public void doTest() {
		try {
			Inputs.check(new File("no/such/file"));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("does not exist"));
		}
		try {
			Inputs.check(new MockFile(Thread.currentThread().getContextClassLoader().getResource("SampleTarget.yaml").getPath()));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("cannot read"));
		}
		try {
			Inputs.check(new MockFile(Thread.currentThread().getContextClassLoader().getResource("SampleTarget.yaml").getPath(), true));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("error validating file"));
		}
		Inputs inputs = TestUtils.inputs(false, false, false);
		inputs.toString();
		inputs.setIgnores(new File(Thread.currentThread().getContextClassLoader().getResource("SamplePackageIgnores.txt").getPath()));
		inputs.toString();
		inputs.setReflections(new File(Thread.currentThread().getContextClassLoader().getResource("SampleReflections.txt").getPath()));
		inputs.toString();
		inputs.setFixUnresolveds(new File(Thread.currentThread().getContextClassLoader().getResource("SampleFixUnresolveds.txt").getPath()));
		inputs.toString();
		inputs = TestUtils.inputs(true, true, true);
		inputs.toString();
		try {
			inputs.setIgnores(new File("foo"));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("already set ignores file"));
		}
		try {
			inputs.setReflections(new File("foo"));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("already set reflections file"));
		}
		try {
			inputs.setFixUnresolveds(new File("foo"));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("already set fix-unresolveds file"));
		}
	}
}

/*
*/