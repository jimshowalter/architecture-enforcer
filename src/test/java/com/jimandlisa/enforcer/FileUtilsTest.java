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
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class FileUtilsTest {

	@Test
	public void doTest() throws Exception {
		new FileUtils();
		File file = FileUtils.checkReadFile(TestUtils.testClassesFile("SampleIgnores.txt"));
		assertNotNull(file);
		assertTrue(file.isFile());
		try {
			FileUtils.checkReadFile(new File(TestUtils.testClassesPath("SampleIgnores.txt") + "bogus"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("does not exist"));
			assertEquals(Errors.FILE_DOES_NOT_EXIST, e.error());
		}
		try {
			File mockFile = Mockito.mock(File.class);
			when(mockFile.exists()).thenReturn(true);
			when(mockFile.canRead()).thenReturn(false);
			FileUtils.checkReadFile(mockFile);
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("cannot read"));
			assertEquals(Errors.CANNOT_READ_FILE, e.error());
		}
		try {
			File mockFile = Mockito.mock(File.class);
			when(mockFile.exists()).thenThrow(new RuntimeException("COVERAGE"));
			FileUtils.checkReadFile(mockFile);
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("error validating file"));
			assertEquals(Errors.ERROR_VALIDATING_FILE, e.error());
			assertEquals("COVERAGE", e.getCause().getMessage());
		}
		File dir = FileUtils.checkWriteDir(TestUtils.testClassesFile("SampleTarget2.yaml").getParentFile());
		assertNotNull(dir);
		assertTrue(dir.isDirectory());
		try {
			File mockFile = Mockito.mock(File.class);
			when(mockFile.toPath()).thenReturn(dir.toPath());
			when(mockFile.canWrite()).thenReturn(false);
			FileUtils.checkWriteDir(mockFile);
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("cannot write"));
			assertEquals(Errors.CANNOT_WRITE_TO_DIRECTORY, e.error());
		}
		try {
			FileUtils.checkWriteDir(new File("< > ##$#%SFS&*@#$#@ SDF []{}||"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("error validating directory"));
			assertEquals(Errors.ERROR_VALIDATING_DIRECTORY, e.error());
		}
		assertEquals(new File("foo"), FileUtils.check(new File("foo"), new File("bar"), null));
		try {
			FileUtils.check(new File("foo"), new File("bar"), new File("foo"), null);
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("conflicts with other file"));
			assertEquals(Errors.NAME_CONFLICTS_WITH_OTHER_FILE, e.error());
		}
	}
}
