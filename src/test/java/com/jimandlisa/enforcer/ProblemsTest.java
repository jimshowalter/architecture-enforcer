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

public class ProblemsTest {

	@Test
	public void doTest() {
		Problem problem = new Problem("foo", Errors.UNRESOLVED_REFERENCE);
		assertEquals("foo", problem.description());
		assertFalse(problem.isFatal(false));
		assertTrue(problem.isFatal(true));
		assertEquals(Errors.UNRESOLVED_REFERENCE, problem.error());
		assertEquals("foo: error=" + Errors.UNRESOLVED_REFERENCE.toString(), problem.toString());
		problem = new Problem("bar", Errors.CANNOT_READ_FILE);
		assertEquals("bar", problem.description());
		assertTrue(problem.isFatal(false));
		assertTrue(problem.isFatal(true));
		assertEquals(Errors.CANNOT_READ_FILE, problem.error());
		assertEquals(problem.description().hashCode(), problem.hashCode());
		assertFalse(problem.equals(null));
		assertTrue(problem.equals(problem));
		assertFalse(problem.equals(new Object()));
		assertFalse(problem.equals(new Problem("foo", Errors.CANNOT_READ_FILE)));
		assertTrue(problem.equals(new Problem("bar", Errors.CANNOT_READ_FILE)));
		assertTrue(problem.equals(new Problem("bar", Errors.CLASS_BOTH_REFERRED_TO_AND_IGNORED)));
		assertEquals(0, problem.compareTo(new Problem("bar", Errors.CANNOT_READ_FILE)));
		assertEquals("bar: error=CANNOT_READ_FILE", problem.toString());
	}
}
