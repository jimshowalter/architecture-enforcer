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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TypeTest {

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void doTest() {
		Type type1 = new Type("foo");
		Type type2 = new Type("foo");
		assertFalse(type1.isSynthesized());
		assertEquals("foo", type1.name());
		assertEquals("foo", type1.toString());
		assertNull(type1.component());
		assertTrue(type1.referenceNames().isEmpty());
		assertTrue(type1.references().isEmpty());
		assertEquals(type1.name().hashCode(), type1.hashCode());
		assertFalse(type1.equals(null));
		assertTrue(type1.equals(type1));
		assertFalse(type1.equals("foo"));
		assertTrue(type1.equals(type1));
		assertTrue(type1.equals(type2));
		assertEquals(0, type1.compareTo(type2));
		type2 = new Type("bar");
		assertFalse(type1.equals(type2));
		assertEquals(4, type1.compareTo(type2));
		Layer layer = new Layer("Layer1", 0, null);
		Component comp1 = new Component("Comp1", layer, null, null);
		type1.setComponent(comp1);
		assertEquals(comp1, type1.component());
		type1.addReferenceName("bar");
		assertEquals(1, type1.referenceNames().size());
		assertEquals("bar", type1.referenceNames().iterator().next());
		type1.addReference(type2);
		assertEquals(1, type1.references().size());
		assertEquals(type2, type1.references().iterator().next());
		Set<Type> types = new HashSet<>();
		types.add(type1);
		types.add(type2);
		assertEquals(2, types.size());
		types.clear();
		type2 = new Type("foo");
		types.add(type1);
		types.add(type2);
		assertEquals(1, types.size());
		assertTrue(new Type("baz", true).isSynthesized());
		try {
			new Type(null);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals("null name", e.getMessage());
			assertEquals(Errors.NULL_STRING_ARG, e.error());
		}
		try {
			type1 = new Type("foo");
			type1.setComponent(null);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals("null component", e.getMessage());
			assertEquals(Errors.NULL_COMPONENT_ARG, e.error());
		}
		try {
			type1 = new Type("foo");
			type1.setComponent(comp1);
			type1.setComponent(comp1);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals("component already set", e.getMessage());
			assertEquals(Errors.COMPONENT_ALREADY_SPECIFIED, e.error());
		}
		try {
			type1 = new Type("foo");
			type1.addReferenceName("bar");
			type1.addReferenceName("bar");
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals("duplicate reference name bar", e.getMessage());
			assertEquals(Errors.DUPLICATE_REFERENCE_NAME, e.error());
		}
		try {
			type1 = new Type("foo");
			type1.addReferenceName("bar");
			type2 = new Type("bar");
			type1.addReference(type2);
			type1.addReference(type2);
			Assert.fail();
		} catch (EnforcerException e) {
			assertEquals("duplicate reference bar", e.getMessage());
			assertEquals(Errors.DUPLICATE_REFERENCE, e.error());
		}
	}
}
