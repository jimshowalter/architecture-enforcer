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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {
	
	public static Path path(String file) {
		return Paths.get(System.getProperty("user.dir"), "target", "test-classes", file);
	}
	
	public static String read(String file) throws Exception {
		return new String(Files.readAllBytes(path(file)), StandardCharsets.UTF_8.name());
	}
	
	public static void compare(ByteArrayOutputStream baos, String canned) throws Exception {
		String out = new String(baos.toByteArray(), StandardCharsets.UTF_8).trim().replaceAll("\r\n\r\n", "\r\n");
		String cannedOut = read(canned).trim().replaceAll("\r\n\r\n", "\r\n");
		assertEquals(out, cannedOut);
	}
	
	public static Inputs inputs(boolean includeIgnores, boolean includeReflections, boolean includeFixUnresolveds) {
		Inputs inputs = new Inputs(new File(Thread.currentThread().getContextClassLoader().getResource("SampleTarget2.yaml").getPath()), new File(Thread.currentThread().getContextClassLoader().getResource("Sample.odem").getPath()));
		if (includeIgnores) {
			inputs.setIgnores(new File(Thread.currentThread().getContextClassLoader().getResource("SamplePackageIgnores.txt").getPath()));
		}
		if (includeReflections) {
			inputs.setReflections(new File(Thread.currentThread().getContextClassLoader().getResource("SampleReflections.txt").getPath()));
		}
		if (includeFixUnresolveds) {
			inputs.setFixUnresolveds(new File(Thread.currentThread().getContextClassLoader().getResource("SampleFixUnresolveds.txt").getPath()));
		}
		return inputs;
	}
}
