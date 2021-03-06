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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class OutputsTest {

	private static void add(File file, boolean checkExists, Set<String> names) {
		assertNotNull(file);
		assertFalse(names.contains(file.getAbsolutePath()));
		if (checkExists) {
			assertFalse(file.exists());
		}
		names.add(file.getAbsolutePath());
	}
	
	private static void add(File file, Set<String> names) {
		add(file, true, names);
	}

	@Test
	public void doTest() {
		File targetDir = TestUtils.targetDir(TestUtils.uniqueSubdir());
		Outputs outputs = new Outputs(targetDir);
		assertEquals(targetDir, outputs.outputDirectory());
		Set<String> names = new HashSet<>();
		add(outputs.outputDirectory(), false, names);
		add(outputs.warnings(), names);
		add(outputs.unresolvedTypes(), names);
		add(outputs.illegalReferences(), names);
		add(outputs.illegalComponentReferences(), names);
		add(outputs.allReferences(), names);
		add(outputs.allReferencesGephiNodes(), names);
		add(outputs.allReferencesGephiEdges(), names);
		add(outputs.allReferencesYeD(), names);
		add(outputs.allComponentReferences(), names);
		add(outputs.allComponentReferencesGephiNodes(), names);
		add(outputs.allComponentReferencesGephiEdges(), names);
		add(outputs.allComponentReferencesYeD(), names);
		assertEquals(13, names.size());
		outputs.toString();
	}
}
