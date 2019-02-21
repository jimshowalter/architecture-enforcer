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
	
	public static File targetDir() {
		return Paths.get(System.getProperty("user.dir"), "target").toFile();
	}
	
	public static File targetFile(String file) {
		return Paths.get(targetDir().toString(), file).toFile();
	}
	
	public static String readTargetFile(String file) throws Exception {
		return new String(Files.readAllBytes(targetFile(file).toPath()), StandardCharsets.UTF_8.name());
	}
	
	public static void compareTargetFile(String file, String canned) throws Exception {
		String s1 = readTargetFile(file).trim().replaceAll("\r\n\r\n", "\r\n");
		String s2 = readTestClassesFile(canned).trim().replaceAll("\r\n\r\n", "\r\n");
		assertEquals(s1, s2);
	}
	
	public static Path testClassesPath(String file) {
		return Paths.get(targetDir().toString(), "test-classes", file);
	}
	
	public static File testClassesFile(String file) {
		return testClassesPath(file).toFile();
	}
	
	public static File sampleWar() {
		return testClassesFile("architecture-enforcer-sample-1.0-SNAPSHOT.war");
	}
	
	public static String readTestClassesFile(String file) throws Exception {
		return new String(Files.readAllBytes(testClassesPath(file)), StandardCharsets.UTF_8.name());
	}
	
	public static void compareTestClassesFile(ByteArrayOutputStream baos, String canned) throws Exception {
		String out = new String(baos.toByteArray(), StandardCharsets.UTF_8).trim().replaceAll("\r\n\r\n", "\r\n");
		String cannedOut = readTestClassesFile(canned).trim().replaceAll("\r\n\r\n", "\r\n");
		assertEquals(out, cannedOut);
	}
	
	public static Inputs inputs(boolean includeIgnores, boolean includeReflections, boolean includeFixUnresolveds) {
		Inputs inputs = new Inputs(testClassesFile("SampleTarget2.yaml"), sampleWar());
		if (includeIgnores) {
			inputs.setIgnores(testClassesFile("SampleIgnores.txt"));
		}
		if (includeReflections) {
			inputs.setReflections(testClassesFile("SampleReflections.txt"));
		}
		if (includeFixUnresolveds) {
			inputs.setFixUnresolveds(testClassesFile("SampleFixUnresolveds.txt"));
		}
		return inputs;
	}
	
	public static Outputs outputs() {
		Outputs outputs = new Outputs(targetDir());
		outputs.setUnresolvedTypes(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
		outputs.setIllegalReferences(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
		return outputs;
	}
}
