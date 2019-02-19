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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class RollUpTest {

	@Test
	public void doTest() throws Exception {
		RollUp rollUp = new RollUp();
		assertNull(rollUp.get("foo"));
		Layer layer1 = new Layer("L1", 1, null);
		Layer layer2 = new Layer("L2", 2, null);
		Set<Component> components = new HashSet<>();
		Component component1 = new Component("Comp1", layer1, null, null);
		component1.packages().add("com.foo");
		component1.classes().add("NoPackage");
		components.add(component1);
		Component component2 = new Component("Comp2", layer2, null, null);
		component2.packages().add("com.other");
		component2.classes().add("com.foo.bar.Baz");
		components.add(component2);
		rollUp.add(components);
		assertNull(rollUp.get("com.no.match"));
		assertNull(rollUp.get("NoPackage"));
		assertEquals("Comp1", rollUp.get("com.foo.bar"));
		assertEquals("Comp1", rollUp.get("com.foo.bar.Baz"));
		assertEquals("Comp2", rollUp.get("com.other.XYZ"));
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
			rollUp.dump(ps);
			TestUtils.compare(baos, "RollUpCanned.txt");
		}
	}
}
