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
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class FileUtilsTest {

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
		
		@Override
		public boolean canWrite() {
			return false;
		}
	}

	@Test
	public void doTest() {
		new FileUtils();
		File file = FileUtils.checkReadFile(new File(Thread.currentThread().getContextClassLoader().getResource("SamplePackageIgnores.txt").getPath()));
		assertNotNull(file);
		assertTrue(file.isFile());
		try {
			FileUtils.checkReadFile(new File(Thread.currentThread().getContextClassLoader().getResource("SamplePackageIgnores.txt").getPath() + "bogus"));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("does not exist"));
			assertEquals(Errors.FILE_DOES_NOT_EXIST, e.error());
		}
		try {
			FileUtils.checkReadFile(new MockFile(Thread.currentThread().getContextClassLoader().getResource("SampleTarget2.yaml").getPath()));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("cannot read"));
			assertEquals(Errors.CANNOT_READ_FILE, e.error());
		}
		try {
			FileUtils.checkReadFile(new MockFile(Thread.currentThread().getContextClassLoader().getResource("SampleTarget2.yaml").getPath(), true));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("error validating file"));
			assertEquals(Errors.ERROR_VALIDATING_FILE, e.error());
			assertEquals("COVERAGE", e.getCause().getMessage());
		}
		File dir = FileUtils.checkWriteDir(new File(Thread.currentThread().getContextClassLoader().getResource("SampleTarget2.yaml").getPath()).getParentFile());
		assertNotNull(dir);
		assertTrue(dir.isDirectory());
		try {
			FileUtils.checkWriteDir(new MockFile(new File(Thread.currentThread().getContextClassLoader().getResource("SampleTarget2.yaml").getPath()).getParentFile().getPath()));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("cannot write"));
			assertEquals(Errors.CANNOT_WRITE_TO_DIRECTORY, e.error());
		}
		try {
			FileUtils.checkWriteDir(new File("< > ##$#%SFS&*@#$#@ SDF []{}||"));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("error validating directory"));
			assertEquals(Errors.ERROR_VALIDATING_DIRECTORY, e.error());
		}
	}
}
