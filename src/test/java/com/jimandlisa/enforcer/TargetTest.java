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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class TargetTest {

	@Test
	public void doTest() throws Exception {
		new TargetUtils();
		Target target = TargetUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("TestTarget.yaml").getPath()));
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			TargetUtils.dump(target, ps);
			TestUtils.compare(baos, "TestTargetCanned.txt");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		Set<String> allowed = new HashSet<>();
		try {
			TargetUtils.validate(map, allowed, "coverage");
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("unrecognized coverage key:"));
		}
		map.put("baz", "baz2");
		try {
			TargetUtils.validate(map, allowed, "coverage");
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("unrecognized coverage keys:"));
		}
		try {
			TargetUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("BadTarget1.yaml").getPath()));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("duplicate layer depth"));
		}
		try {
			TargetUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("BadTarget2.yaml").getPath()));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("duplicate layer name"));
		}
		try {
			TargetUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("BadTarget3.yaml").getPath()));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("duplicate domain name"));
		}
		try {
			TargetUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("BadTarget4.yaml").getPath()));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("duplicate component name"));
		}
		try {
			TargetUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("BadTarget5.yaml").getPath()));
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("null domain name"));
		}
	}
}
