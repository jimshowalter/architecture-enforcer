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

import org.junit.Test;

public class TargetTest {

	@Test
	public void testTarget() {
		Target target = new Target();
		assertTrue(target.layers().isEmpty());
		assertTrue(target.domains().isEmpty());
		assertTrue(target.components().isEmpty());
		Layer layer = new Layer("foo", 0, null);
		Domain domain = new Domain("bar", null);
		Component component = new Component("baz", layer, domain, null);
		target.add(layer);
		target.add(domain);
		target.add(component);
		assertEquals(1, target.layers().size());
		assertEquals(layer, target.layers().values().iterator().next());
		assertEquals(1, target.domains().size());
		assertEquals(domain, target.domains().values().iterator().next());
		assertEquals(1, target.components().size());
		assertEquals(component, target.components().values().iterator().next());
	}
}
