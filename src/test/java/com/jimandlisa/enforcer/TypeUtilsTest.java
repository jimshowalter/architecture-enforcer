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

import org.junit.Test;

public class TypeUtilsTest {

	@Test
	public void doTest() {
		new TypeUtils();
		Layer layer1 = new Layer("One", 1, null);
		Component comp1 = new Component("Comp1", layer1, null, null);
		Component comp2 = new Component("Comp2", layer1, null, null);
		Layer layer2 = new Layer("Two", 2, null);
		Component comp3 = new Component("Comp3", layer2, null, null);
		Type type1 = new Type("foo");
		type1.setBelongsTo(comp1);
		Type type2 = new Type("bar");
		type2.setBelongsTo(comp1);
		assertTrue(TypeUtils.isSelfReference(type1, type1));
		assertTrue(TypeUtils.isSelfReference(type1, type2));
		assertTrue(TypeUtils.isSelfReference(type2, type1));
		assertFalse(TypeUtils.isLayerViolation(type1, type2));
		assertFalse(TypeUtils.isLayerViolation(type2, type1));
		type2 = new Type("bar");
		type2.setBelongsTo(comp2);
		assertTrue(TypeUtils.isSelfReference(type1, type1));
		assertFalse(TypeUtils.isSelfReference(type1, type2));
		assertFalse(TypeUtils.isSelfReference(type2, type1));
		assertTrue(TypeUtils.isLayerViolation(type1, type2));
		assertTrue(TypeUtils.isLayerViolation(type2, type1));
		type2 = new Type("bar");
		type2.setBelongsTo(comp3);
		assertTrue(TypeUtils.isSelfReference(type1, type1));
		assertFalse(TypeUtils.isSelfReference(type1, type2));
		assertFalse(TypeUtils.isSelfReference(type2, type1));
		assertTrue(TypeUtils.isLayerViolation(type1, type2));
		assertFalse(TypeUtils.isLayerViolation(type2, type1));
		assertEquals("foo!Comp1!One!1|bar!Comp3!Two!2", TypeUtils.parseableDescription(type1, type2));
		assertEquals("type foo in component 'Comp1' in layer 'One' depth 1 refers to type bar in component 'Comp3' in layer 'Two' depth 2", TypeUtils.humanReadableDescription(type1, type2));
	}
}
