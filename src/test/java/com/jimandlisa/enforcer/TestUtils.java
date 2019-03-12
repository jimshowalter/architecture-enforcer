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
import java.util.concurrent.ThreadLocalRandom;

public class TestUtils {

	// To avoid file collisions when running tests threaded.
	public static String uniqueSubdir() {
		return "subdir_" + ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
	}

	public static File targetDir(String subdir) {
		if (subdir == null) {
			throw new RuntimeException("null subdir");
		}
		return Paths.get(System.getProperty("user.dir"), "target", subdir).toFile();
	}

	public static File targetFile(String subdir, String file) {
		return Paths.get(targetDir(subdir).toString(), file).toFile();
	}

	public static Path testClassesPath(String file) {
		return Paths.get(System.getProperty("user.dir"), "target", "test-classes", file);
	}

	public static File testClassesFile(String file) {
		return testClassesPath(file).toFile();
	}

	public static String readTargetFile(String subdir, String file) throws Exception {
		return new String(Files.readAllBytes(targetFile(subdir, file).toPath()), StandardCharsets.UTF_8.name());
	}

	public static void compareTargetFile(String subdir, String file, String canned) throws Exception {
		String s1 = readTargetFile(subdir, file).trim().replaceAll("\r\n\r\n", "\r\n");
		String s2 = readTestClassesFile(canned).trim().replaceAll("\r\n\r\n", "\r\n");
		assertEquals(s1, s2);
	}

	public static File sampleWar() {
		return testClassesFile("architecture-enforcer-sample-1.0-SNAPSHOT.war");
	}

	public static File sampleAllReferences() {
		return testClassesFile(Outputs.ALL_REFERENCES_BASE_NAME + ".txt");
	}

	public static String readTestClassesFile(String file) throws Exception {
		return new String(Files.readAllBytes(testClassesPath(file)), StandardCharsets.UTF_8.name());
	}

	public static void compareTestClassesFile(ByteArrayOutputStream baos, String canned) throws Exception {
		String out = new String(baos.toByteArray(), StandardCharsets.UTF_8).trim().replaceAll("\r\n\r\n", "\r\n").replace("\\", "/").replaceAll("SEE .*/subdir_.*/", "SEE ");
		String cannedOut = readTestClassesFile(canned).trim().replaceAll("\r\n\r\n", "\r\n");
		assertEquals(out, cannedOut);
	}

	public static AnalyzeBinaryInputs analyzeWarInputs(File data, boolean includeIgnores, boolean includeReflections, boolean includeFixUnresolveds) {
		AnalyzeBinaryInputs inputs = new AnalyzeBinaryInputs(testClassesFile("SampleTarget2.yaml"), data);
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

	public static AnalyzeBinaryInputs analyzeWarInputs(boolean includeIgnores, boolean includeReflections, boolean includeFixUnresolveds) {
		return analyzeWarInputs(sampleWar(), includeIgnores, includeReflections, includeFixUnresolveds);
	}

	public static RapidIterationInputs rapidIterationInputs() {
		return new RapidIterationInputs(testClassesFile("SampleTarget2.yaml"), sampleAllReferences());
	}

	public static Outputs outputs(String subdir) {
		return new Outputs(Paths.get(targetDir(subdir).getAbsolutePath()).toFile());
	}
}
