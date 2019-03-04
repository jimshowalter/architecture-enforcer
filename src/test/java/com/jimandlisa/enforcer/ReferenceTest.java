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
		type3.setComponent(comp2);
		Type type4 = new Type("cat");
		type4.setComponent(comp3);
		Reference reference1 = new Reference(type1, type2);
		Reference reference2 = new Reference(type2, type1);
		assertEquals(type1, reference1.referringType());
		assertEquals(type2, reference1.referredToType());
		assertEquals(ReferenceKinds.INTRA_COMPONENT, reference1.kind());
		assertTrue(reference1.kind().isLegal());
		assertEquals(type1.name() + " -> " + type2.name() + " [" + ReferenceKinds.INTRA_COMPONENT + "]", reference1.toString());
		assertEquals(reference1.referringType().hashCode() + reference1.referredToType().hashCode(), reference1.hashCode());
		assertFalse(reference1.equals(null));
		assertFalse(reference1.equals("foo"));
		assertTrue(reference1.equals(reference1));
		assertEquals(0, reference1.compareTo(reference1));
		assertFalse(reference1.equals(reference2));
		assertEquals(4, reference1.compareTo(reference2));
		assertEquals(type2, reference2.referringType());
		assertEquals(type1, reference2.referredToType());
		assertEquals(ReferenceKinds.INTRA_COMPONENT, reference2.kind());
		assertTrue(reference2.kind().isLegal());
		assertEquals(type2.name() + " -> " + type1.name() + " [" + ReferenceKinds.INTRA_COMPONENT + "]", reference2.toString());
		assertEquals(reference2.referringType().hashCode() + reference2.referredToType().hashCode(), reference2.hashCode());
		assertFalse(reference2.equals(null));
		assertFalse(reference2.equals("foo"));
		assertTrue(reference2.equals(reference2));
		assertFalse(reference2.equals(reference1));
		assertEquals(-4, reference2.compareTo(reference1));
		Reference reference3 = new Reference(type1, type3);
		assertEquals(ReferenceKinds.INTER_COMPONENT_SAME_LAYER, reference3.kind());
		assertFalse(reference3.kind().isLegal());
		assertEquals(type1.name() + " -> " + type3.name() + " [" + ReferenceKinds.INTER_COMPONENT_SAME_LAYER + "]", reference3.toString());
		Reference reference4 = new Reference(type1, type4);
		assertEquals(ReferenceKinds.INTER_COMPONENT_LOWER_TO_HIGHER, reference4.kind());
		assertFalse(reference4.kind().isLegal());
		assertEquals(type1.name() + " -> " + type4.name() + " [" + ReferenceKinds.INTER_COMPONENT_LOWER_TO_HIGHER + "]", reference4.toString());
		Reference reference5 = new Reference(type4, type1);
		assertEquals(ReferenceKinds.INTER_COMPONENT_HIGHER_TO_LOWER, reference5.kind());
		assertTrue(reference5.kind().isLegal());
		assertEquals(type4.name() + " -> " + type1.name() + " [" + ReferenceKinds.INTER_COMPONENT_HIGHER_TO_LOWER + "]", reference5.toString());
		Set<Reference> references = new HashSet<>();
		references.add(reference1);
		references.add(reference2);
		assertEquals(2, references.size());
		references.clear();
		references.add(reference1);
		references.add(reference3);
		assertEquals(2, references.size());
		assertEquals("Comp1!One!1!Comp1!One!1", reference1.parseableDescription(false, false));
		assertEquals("Comp1!One!1!Comp1!One!1!INTRA_COMPONENT", reference1.parseableDescription(false, true));
		assertEquals("foo!Comp1!One!1!bar!Comp1!One!1", reference1.parseableDescription(true, false));
		assertEquals("foo!Comp1!One!1!bar!Comp1!One!1!INTRA_COMPONENT", reference1.parseableDescription(true, true));
		assertEquals("component 'Comp1' in layer 'One' depth 1 refers to component 'Comp1' in layer 'One' depth 1", reference1.humanReadableDescription(false, false));
		assertEquals("component 'Comp1' in layer 'One' depth 1 refers to component 'Comp1' in layer 'One' depth 1 [INTRA_COMPONENT]", reference1.humanReadableDescription(false, true));
		assertEquals("type foo in component 'Comp1' in layer 'One' depth 1 refers to type bar in component 'Comp1' in layer 'One' depth 1", reference1.humanReadableDescription(true, false));
		assertEquals("type foo in component 'Comp1' in layer 'One' depth 1 refers to type bar in component 'Comp1' in layer 'One' depth 1 [INTRA_COMPONENT]", reference1.humanReadableDescription(true, true));
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
