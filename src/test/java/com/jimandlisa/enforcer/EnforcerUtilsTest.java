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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class EnforcerUtilsTest {

	@Test
	public void doTest() throws Exception {
		new EnforcerUtils();
		Set<String> ignores = EnforcerUtils.ignores(null);
		assertTrue(ignores.isEmpty());
		ignores = EnforcerUtils.ignores(new File(Thread.currentThread().getContextClassLoader().getResource("TestIgnores.txt").getPath()));
		assertEquals(3, ignores.size());
		ignores.remove("foo");
		ignores.remove("bar.");
		ignores.remove("baz.");
		assertTrue(ignores.isEmpty());
		assertTrue(EnforcerUtils.skip("foo:bar", ignores));
		ignores.add("foo.");
		assertTrue(EnforcerUtils.skip("foo.bar", ignores));
		assertFalse(EnforcerUtils.skip("baz.baz2", ignores));
		assertEquals("com.foo.Bar", EnforcerUtils.denest("com.foo.Bar$Baz"));
		try {
			EnforcerUtils.denest("$Foo");
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("malformed class name"));
		}
		Map<String, Type> types = new HashMap<>();
		Type type0 = EnforcerUtils.get("foo", types);
		assertEquals("foo", type0.name());
		assertEquals(1, types.size());
		type0 = EnforcerUtils.get("foo", types);
		assertEquals("foo", type0.name());
		assertEquals(1, types.size());
		types.clear();
		ignores.clear();
		Set<String> problems = new HashSet<>();
		try {
			EnforcerUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("BadFixUnresolveds.txt").getPath()), types, ignores, problems, "fix-unresolved");
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("invalid fix-unresolved entry in"));
		}
		EnforcerUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("TestFixUnresolveds.txt").getPath()), types, ignores, problems, "fix-unresolved");
		assertTrue(problems.isEmpty());
		ignores.add("foo.");
		EnforcerUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("TestFixUnresolveds.txt").getPath()), types, ignores, problems, "fix-unresolved");
		assertEquals(1, problems.size());
		assertTrue(problems.iterator().next().contains("CLASS IS LISTED AS REFERRING BUT ALSO LISTED IN IGNORES:"));
		types.clear();
		ignores.clear();
		problems.clear();
		ignores.add("com.x.");
		EnforcerUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("TestFixUnresolveds.txt").getPath()), types, ignores, problems, "fix-unresolved");
		assertEquals(1, problems.size());
		assertTrue(problems.iterator().next().contains("CLASS IS LISTED AS REFERRED-TO BUT ALSO LISTED IN IGNORES:"));
		types.clear();
		ignores.clear();
		problems.clear();
		type0 = new Type("foo");
		type0.referenceNames().add("bar");
		types.put(type0.name(), type0);
		EnforcerUtils.resolve(types, problems);
		assertEquals(1, problems.size());
		assertTrue(problems.iterator().next().contains("UNRESOLVED:"));
		problems.clear();
		types.put("bar", new Type("bar"));
		EnforcerUtils.resolve(types, problems);
		assertTrue(problems.isEmpty());
		types.clear();
		ignores.clear();
		problems.clear();
		Type type1 = new Type("com.foo.bar.Baz");
		types.put(type1.name(), type1);
		Type type2 = new Type("com.foo.bar.Baz2");
		types.put(type2.name(), type2);
		type1.referenceNames().add(type2.name());
		type1.references().add(type2);
		Layer layer1 = new Layer("L1", 1, null);
		Map<String, Component> components = new HashMap<>();
		Component component1 = new Component("Comp1", layer1, null, null);
		component1.packages().add("com.foo");
		components.put(component1.name(), component1);
		EnforcerUtils.correlate(types, components, problems);
		assertTrue(problems.isEmpty());
		assertEquals(component1, type1.definedIn());
		assertEquals(type1, component1.types().get(type1.name()));
		Type type3 = new Type("com.bar.Baz");
		types.put(type3.name(), type3);
		EnforcerUtils.correlate(types, components, problems);
		assertTrue(problems.iterator().next().contains("UNABLE TO RESOLVE TYPE TO COMPONENT NAME:"));
		problems.clear();
		Layer layer0 = new Layer("L0", 0, null);
		Component component2 = new Component("Comp0", layer0, null, null);
		component2.packages().add("com.bar");
		components.put(component2.name(), component2);
		EnforcerUtils.correlate(types, components, problems);
		assertTrue(problems.isEmpty());
		type3.referenceNames().add("com.foo.bar.Baz");
		type3.references().add(type1);
		EnforcerUtils.correlate(types, components, problems);
		assertTrue(problems.iterator().next().contains("ILLEGAL REFERENCE:"));
	}
}