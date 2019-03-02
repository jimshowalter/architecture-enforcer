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

public class ComponentTest {

	@Test
	public void doTest() {
		Layer layer = new Layer("name", 0, "description");
		Domain domain = new Domain("name", "description");
		Component component = new Component("name", layer, domain, "description");
		assertEquals("name", component.name());
		assertEquals("'name'", component.quotedName());
		assertEquals(layer, component.layer());
		assertEquals(domain, component.domain());
		assertEquals("name='name', layer='name', depth=0, domain='name'", component.toString());
		assertEquals("description", component.description());
		component = new Component("name", layer, null, "description");
		assertEquals("name='name', layer='name', depth=0, domain=null", component.toString());
		assertEquals("description", component.description());
		component = new Component("name", layer, domain, null);
		assertEquals("name='name', layer='name', depth=0, domain='name'", component.toString());
		assertNull(component.description());
		component = new Component("name", layer, null, null);
		assertEquals("name='name', layer='name', depth=0, domain=null", component.toString());
		assertNull(component.description());
		assertTrue(component.packages().isEmpty());
		assertTrue(component.classes().isEmpty());
		assertTrue(component.types().isEmpty());
		component.add(new Type("abcd"));
		assertEquals("abcd", component.types().values().iterator().next().name());
	}
}
