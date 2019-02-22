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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TargetUtilsTest {
	
	@SuppressWarnings("unchecked")
	private static Set<String> allowed(String name) throws Exception {
		Field allowedField = TargetUtils.class.getDeclaredField(name);
		allowedField.setAccessible(true);
		return (Set<String>)allowedField.get(null);
	}

	@Test
	public void doTest() throws Exception {
		new TargetUtils();
		Target target = TargetUtils.parse(TestUtils.testClassesFile("TestTarget.yaml"));
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			TargetUtils.dump(target, ps);
			TestUtils.compareTestClassesFile(baos, "TestTargetCanned.txt");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		try {
			TargetUtils.validate(map, allowed("ALLOWED_LAYER_KEYS"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("unrecognized layer key:"));
			assertEquals(Errors.UNRECOGNIZED_LAYER_KEY, e.error());
		}
		try {
			TargetUtils.validate(map, allowed("ALLOWED_DOMAIN_KEYS"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("unrecognized domain key:"));
			assertEquals(Errors.UNRECOGNIZED_DOMAIN_KEY, e.error());
		}
		try {
			TargetUtils.validate(map, allowed("ALLOWED_COMPONENT_KEYS"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("unrecognized component key:"));
			assertEquals(Errors.UNRECOGNIZED_COMPONENT_KEY, e.error());
		}
		map.put("baz", "baz2");
		try {
			TargetUtils.validate(map, allowed("ALLOWED_LAYER_KEYS"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("unrecognized layer keys:"));
			assertEquals(Errors.UNRECOGNIZED_LAYER_KEY, e.error());
		}
		try {
			TargetUtils.validate(map, allowed("ALLOWED_DOMAIN_KEYS"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("unrecognized domain keys:"));
			assertEquals(Errors.UNRECOGNIZED_DOMAIN_KEY, e.error());
		}
		try {
			TargetUtils.validate(map, allowed("ALLOWED_COMPONENT_KEYS"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("unrecognized component keys:"));
			assertEquals(Errors.UNRECOGNIZED_COMPONENT_KEY, e.error());
		}
		try {
			TargetUtils.parse(TestUtils.testClassesFile("BadTarget1.yaml"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("duplicate layer depth"));
			assertEquals(Errors.DUPLICATE_LAYER_DEPTH, e.error());
		}
		try {
			TargetUtils.parse(TestUtils.testClassesFile("BadTarget2.yaml"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("duplicate layer name"));
			assertEquals(Errors.DUPLICATE_LAYER_NAME, e.error());
		}
		try {
			TargetUtils.parse(TestUtils.testClassesFile("BadTarget3.yaml"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("duplicate domain name"));
			assertEquals(Errors.DUPLICATE_DOMAIN_NAME, e.error());
		}
		try {
			TargetUtils.parse(TestUtils.testClassesFile("BadTarget4.yaml"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("duplicate component name"));
			assertEquals(Errors.DUPLICATE_COMPONENT_NAME, e.error());
		}
		try {
			TargetUtils.parse(TestUtils.testClassesFile("BadTarget5.yaml"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("null domain name"));
			assertEquals(Errors.NULL_STRING_ARG, e.error());
		}
		try {
			TargetUtils.parse(TestUtils.testClassesFile("BadTarget6.yaml"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("duplicate package name"));
			assertEquals(Errors.DUPLICATE_PACKAGE_NAME, e.error());
		}
		try {
			TargetUtils.parse(TestUtils.testClassesFile("BadTarget7.yaml"));
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("duplicate class name"));
			assertEquals(Errors.DUPLICATE_CLASS_NAME, e.error());
		}
	}
}
