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
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class TargetTest {
	
	private static Path path(String file) {
		return Paths.get(System.getProperty("user.dir"), "target", "test-classes", file);
	}
	
	private static String read(String file) throws UnsupportedEncodingException, IOException {
		return new String(Files.readAllBytes(path(file)), StandardCharsets.UTF_8.name());
	}
	
	private static void compare(ByteArrayOutputStream baos, String canned) throws IOException {
		String out = new String(baos.toByteArray(), StandardCharsets.UTF_8).trim().replaceAll("\r\n\r\n", "\r\n");
		String cannedOut = read(canned).trim().replaceAll("\r\n\r\n", "\r\n");
		assertEquals(out, cannedOut);
	}

	@Test
	public void doTest() throws Exception {
		Target target = TargetUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("TestTarget.yaml").getPath()));
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			TargetUtils.dump(target, ps);
			compare(baos, "TestTargetCanned.txt");
		}
	}
}