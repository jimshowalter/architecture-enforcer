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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TypeTest {

	@Test
	public void doTest() {
		Type type = new Type("foo");
		assertEquals("foo", type.name());
		assertEquals("foo", type.toString());
		assertNull(type.belongsTo());
		assertTrue(type.referenceNames().isEmpty());
		assertTrue(type.references().isEmpty());
		Layer layer = new Layer("Layer1", 0, null);
		Component comp1 = new Component("Comp1", layer, null, null);
		type.setBelongsTo(comp1);
		assertEquals(comp1, type.belongsTo());
		type.referenceNames().add("bar");
		assertTrue(type.referenceNames().size() == 1);
		assertEquals("bar", type.referenceNames().iterator().next());
		Type type2 = new Type("bar");
		type.references().add(type2);
		assertTrue(type.references().size() == 1);
		assertEquals(type2, type.references().iterator().next());
	}
}
