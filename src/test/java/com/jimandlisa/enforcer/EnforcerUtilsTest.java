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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class EnforcerUtilsTest {
	
	@Test
	public void testMisc() {
		new EnforcerUtils();
	}
	
	@Test
	public void testIgnores() throws Exception {
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
		ignores.clear();
		assertTrue(EnforcerUtils.skip("com.foo.Bar", "com.foo.Bar", ignores));
		assertFalse(EnforcerUtils.skip("com.foo.Bar", "com.foo.Baz", ignores));
	}
	
	@Test
	public void testDenest() {
		assertEquals("com.foo.Bar", EnforcerUtils.denest("com.foo.Bar$Baz", false));
		assertEquals("com.foo.Bar$Baz", EnforcerUtils.denest("com.foo.Bar$Baz", true));
		try {
			EnforcerUtils.denest("$Foo", false);
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("malformed class name"));
			assertEquals(Errors.MALFORMED_CLASS_NAME, e.error());
		}
	}
	
	@Test
	public void testGetType() {
		Map<String, Type> types = new HashMap<>();
		Type type0 = EnforcerUtils.get("foo", types);
		assertEquals("foo", type0.name());
		assertEquals(1, types.size());
		type0 = EnforcerUtils.get("foo", types);
		assertEquals("foo", type0.name());
		assertEquals(1, types.size());
	}
	
	@Test
	public void testParser()  throws Exception {
		Set<String> ignores = new HashSet<>();
		Map<String, Type> types = new HashMap<>();
		Set<Problem> problems = new LinkedHashSet<>();
		Flags flags = new Flags(false, false, false);
		EnforcerUtils.parse(null, null, null, null, null, false, null);
		EnforcerUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("TestReflections.txt").getPath()), types, ignores, problems, "reflection", true, flags);
		try {
			EnforcerUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("BadReflections.txt").getPath()), types, ignores, problems, "reflection", true, flags);
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("invalid reflection entry in"));
			assertEquals(Errors.MISSING_REFERRED_TO_CLASS, e.error());
		}
		try {
			EnforcerUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("BadFixUnresolveds.txt").getPath()), types, ignores, problems, "fix-unresolved", false, flags);
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("invalid fix-unresolved entry in"));
			assertEquals(Errors.MALFORMED_CLASS_TO_CLASS_REFERENCE, e.error());
		}
		EnforcerUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("TestFixUnresolveds.txt").getPath()), types, ignores, problems, "fix-unresolved", false, flags);
		assertTrue(problems.isEmpty());
		ignores.add("foo.");
		EnforcerUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("TestFixUnresolveds.txt").getPath()), types, ignores, problems, "fix-unresolved", false, flags);
		assertEquals(1, problems.size());
		assertTrue(problems.iterator().next().description().contains("class is listed as referring but also listed in ignores:"));
		types.clear();
		ignores.clear();
		problems.clear();
		ignores.add("com.x.");
		EnforcerUtils.parse(new File(Thread.currentThread().getContextClassLoader().getResource("TestFixUnresolveds.txt").getPath()), types, ignores, problems, "fix-unresolved", false, flags);
		assertEquals(1, problems.size());
		assertTrue(problems.iterator().next().description().contains("class is listed as referred-to but also listed in ignores:"));
	}
	
	@Test
	public void testReport() {
		Set<Problem> problems = new LinkedHashSet<>();
		EnforcerUtils.report(problems);
		problems.add(new Problem("COVERAGE0", null));
		EnforcerUtils.report(problems);
		problems.add(new Problem("COVERAGE1", Errors.CANNOT_READ_FILE));
		try {
			EnforcerUtils.report(problems);
		} catch (EnforcerException e) {
			assertEquals(Errors.CANNOT_READ_FILE, e.error());
			assertFalse(e.getMessage().contains("COVERAGE0"));
			assertTrue(e.getMessage().contains("COVERAGE1"));
		}
		problems.add(new Problem("COVERAGE2", Errors.CLASS_BOTH_REFERRED_TO_AND_IGNORED));
		try {
			EnforcerUtils.report(problems);
		} catch (EnforcerException e) {
			assertEquals(Errors.CANNOT_READ_FILE, e.error());
			assertFalse(e.getMessage().contains("COVERAGE0"));
			assertTrue(e.getMessage().contains("COVERAGE1"));
			assertTrue(e.getMessage().contains("COVERAGE2"));
		}
	}
	
	@Test
	public void testErrorMaker() {
		assertNull(EnforcerUtils.error(Errors.CANNOT_READ_FILE, false));
		assertEquals(Errors.CANNOT_READ_FILE, EnforcerUtils.error(Errors.CANNOT_READ_FILE, true));
	}
	
	@Test
	public void testResolve() {
		Map<String, Type> types = new HashMap<>();
		Set<Problem> problems = new LinkedHashSet<>();
		Flags flags = new Flags(false, false, false);
		Type type0 = new Type("foo");
		type0.referenceNames().add("bar");
		types.put(type0.name(), type0);
		EnforcerUtils.resolve(types, problems, flags);
		assertEquals(1, problems.size());
		assertTrue(problems.iterator().next().description().contains("unresolved:"));
		problems.clear();
		types.put("bar", new Type("bar"));
		EnforcerUtils.resolve(types, problems, flags);
		assertTrue(problems.isEmpty());
	}
	
	@Test
	public void testCorrelate() {
		Map<String, Type> types = new HashMap<>();
		Set<Problem> problems = new LinkedHashSet<>();
		Flags flags = new Flags(false, false, false);
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
		EnforcerUtils.correlate(types, components, new RollUp(), problems, flags);
		assertTrue(problems.isEmpty());
		assertEquals(component1, type1.belongsTo());
		assertEquals(type1, component1.types().get(type1.name()));
	}
	
	@Test
	public void testCorrelateWithClasses() {
		Map<String, Type> types = new HashMap<>();
		Set<Problem> problems = new LinkedHashSet<>();
		Flags flags = new Flags(false, false, false);
		Type type1 = new Type("com.foo.bar.Baz");
		types.put(type1.name(), type1);
		Type type2 = new Type("com.foo.bar.Baz2");
		types.put(type2.name(), type2);
		type1.referenceNames().add(type2.name());
		type1.references().add(type2);
		Type type3 = new Type("NoPackage");
		types.put(type3.name(), type3);
		Layer layer1 = new Layer("L1", 1, null);
		Layer layer2 = new Layer("L2", 2, null);
		Map<String, Component> components = new HashMap<>();
		Component component1 = new Component("Comp1", layer1, null, null);
		component1.packages().add("com.foo");
		component1.classes().add("NoPackage");
		components.put(component1.name(), component1);
		Component component2 = new Component("Comp2", layer2, null, null);
		component2.packages().add("com.other");
		component2.classes().add("com.foo.bar.Baz");
		components.put(component2.name(), component2);
		EnforcerUtils.correlate(types, components, new RollUp(), problems, flags);
		assertTrue(problems.isEmpty());
		assertEquals(component2, type1.belongsTo());
		assertEquals(type1, component2.types().get(type1.name()));
	}
	
	@Test
	public void testFailedCorrelateWithClasses() {
		Map<String, Type> types = new HashMap<>();
		Set<Problem> problems = new LinkedHashSet<>();
		Flags flags = new Flags(false, false, false);
		Type type1 = new Type("com.foo.bar.Baz");
		types.put(type1.name(), type1);
		Type type2 = new Type("com.foo.bar.Baz2");
		types.put(type2.name(), type2);
		type1.referenceNames().add(type2.name());
		type1.references().add(type2);
		Layer layer1 = new Layer("L1", 1, null);
		Layer layer2 = new Layer("L2", 2, null);
		Map<String, Component> components = new HashMap<>();
		Component component1 = new Component("Comp1", layer1, null, null);
		component1.packages().add("com.foo");
		components.put(component1.name(), component1);
		Component component2 = new Component("Comp2", layer2, null, null);
		component2.packages().add("com.other");
		component2.classes().add("com.foo.bar.Bat");
		components.put(component2.name(), component2);
		try {
			EnforcerUtils.correlate(types, components, new RollUp(), problems, flags);
		} catch (EnforcerException e) {
			assertTrue(problems.iterator().next().description().contains("unable to resolve class to type:"));
			assertEquals(Errors.CLASS_NOT_RESOLVED_TO_TYPE, e.error());
		}
	}
	
	@Test
	public void testFailedTypeToComponentCorrelate() {
		Map<String, Type> types = new HashMap<>();
		Set<Problem> problems = new LinkedHashSet<>();
		Flags flags = new Flags(false, false, false);
		Type type1 = new Type("com.foo.bar.Baz");
		types.put(type1.name(), type1);
		Type type2 = new Type("com.foo.bar.Baz2");
		types.put(type2.name(), type2);
		type1.referenceNames().add(type2.name());
		type1.references().add(type2);
		Type type3 = new Type("com.bar.Baz3");
		types.put(type3.name(), type3);
		Layer layer1 = new Layer("L1", 1, null);
		Map<String, Component> components = new HashMap<>();
		Component component1 = new Component("Comp1", layer1, null, null);
		component1.packages().add("com.foo");
		components.put(component1.name(), component1);
		try {
			EnforcerUtils.correlate(types, components, new RollUp(), problems, flags);
		} catch (EnforcerException e) {
			assertTrue(problems.iterator().next().description().contains("unable to resolve type to component name:"));
			assertEquals(Errors.TYPE_NOT_RESOLVED_TO_COMPONENT, e.error());
		}
	}

	@Test
	public void testLayerViolationCorrelate() {
		Map<String, Type> types = new HashMap<>();
		Set<Problem> problems = new LinkedHashSet<>();
		Flags flags = new Flags(false, false, false);
		Type type1 = new Type("com.foo.bar.Baz");
		types.put(type1.name(), type1);
		Type type2 = new Type("com.foo.bar.Baz2");
		types.put(type2.name(), type2);
		type1.referenceNames().add(type2.name());
		type1.references().add(type2);
		Type type3 = new Type("com.bar.Baz3");
		types.put(type3.name(), type3);
		Layer layer1 = new Layer("L1", 1, null);
		Map<String, Component> components = new HashMap<>();
		Component component1 = new Component("Comp1", layer1, null, null);
		component1.packages().add("com.foo");
		components.put(component1.name(), component1);
		Layer layer0 = new Layer("L0", 0, null);
		Component component2 = new Component("Comp0", layer0, null, null);
		component2.packages().add("com.bar");
		components.put(component2.name(), component2);
		type3.referenceNames().add("com.foo.bar.Baz");
		type3.references().add(type1);
		EnforcerUtils.correlate(types, components, new RollUp(), problems, flags);
		assertTrue(problems.iterator().next().description().contains("illegal reference:"));
	}
}
