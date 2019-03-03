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

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ReferenceTest {

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testReference() {
		Layer layer1 = new Layer("One", 1, null);
		Component comp1 = new Component("Comp1", layer1, null, null);
		Component comp2 = new Component("Comp2", layer1, null, null);
		Layer layer2 = new Layer("Two", 2, null);
		Component comp3 = new Component("Comp3", layer2, null, null);
		Type type1 = new Type("foo");
		type1.setComponent(comp1);
		Type type2 = new Type("bar");
		type2.setComponent(comp1);
		Type type3 = new Type("baz");
		type3.setComponent(comp3);
		Reference reference1 = new Reference(type1, type2);
		Reference reference2 = new Reference(type1, type2);
		Reference reference3 = new Reference(type3, type1);
		Reference reference4 = new Reference(type1, type3);
		assertEquals(type1, reference1.referringType());
		assertEquals(type2, reference1.referredToType());
		assertFalse(reference1.isIllegal());
		assertEquals(ReferenceKinds.INTRA, reference1.kind());
		assertEquals(type1.name() + " -> " + type2.name() + " [" + ReferenceKinds.INTRA + "]", reference1.toString());
		assertEquals(reference1.referringType().hashCode() + reference1.referredToType().hashCode(), reference1.hashCode());
		assertFalse(reference1.equals(null));
		assertTrue(reference1.equals(reference1));
		assertFalse(reference1.equals("foo"));
		assertTrue(reference1.equals(reference1));
		assertTrue(reference1.equals(reference2));
		assertEquals(0, reference1.compareTo(reference2));
		assertEquals(type3.name() + " -> " + type1.name() + " [" + ReferenceKinds.LEGAL + "]", reference3.toString());
		assertEquals(type1.name() + " -> " + type3.name() + " [" + ReferenceKinds.ILLEGAL + "]", reference4.toString());
		Set<Reference> references = new HashSet<>();
		references.add(reference1);
		references.add(reference2);
		assertEquals(1, references.size());
		references.clear();
		references.add(reference1);
		references.add(reference3);
		assertEquals(2, references.size());
		assertEquals(reference1.referringType().hashCode() + reference1.referredToType().hashCode(), reference1.hashCode());
		assertTrue(new Reference(type1, type1).isIntraComponentReference());
		assertTrue(new Reference(type1, type2).isIntraComponentReference());
		assertTrue(new Reference(type2, type1).isIntraComponentReference());
		assertFalse(new Reference(type1, type2).isLayerViolation());
		assertFalse(new Reference(type2, type1).isLayerViolation());
		type2 = new Type("bar");
		type2.setComponent(comp2);
		assertTrue(new Reference(type1, type1).isIntraComponentReference());
		assertFalse(new Reference(type1, type2).isIntraComponentReference());
		assertFalse(new Reference(type2, type1).isIntraComponentReference());
		assertTrue(new Reference(type1, type2).isLayerViolation());
		assertTrue(new Reference(type2, type1).isLayerViolation());
		type2 = new Type("bar");
		type2.setComponent(comp3);
		assertTrue(new Reference(type1, type1).isIntraComponentReference());
		assertFalse(new Reference(type1, type2).isIntraComponentReference());
		assertFalse(new Reference(type2, type1).isIntraComponentReference());
		assertTrue(new Reference(type1, type2).isLayerViolation());
		assertFalse(new Reference(type2, type1).isLayerViolation());
		assertEquals("foo!Comp1!One!1!bar!Comp3!Two!2", new Reference(type1, type2).parseableDescription());
		assertEquals("type foo in component 'Comp1' in layer 'One' depth 1 refers to type bar in component 'Comp3' in layer 'Two' depth 2", new Reference(type1, type2).humanReadableDescription());
		try {
			new Reference(null, null);
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("null referringType"));
			assertEquals(Errors.NULL_TYPE_ARG, e.error());
		}
		try {
			new Reference(type1, null);
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("null referredToType"));
			assertEquals(Errors.NULL_TYPE_ARG, e.error());
		}
	}
}
