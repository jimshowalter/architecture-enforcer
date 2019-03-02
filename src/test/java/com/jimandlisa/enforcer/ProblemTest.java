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

import org.junit.Test;

public class ProblemTest {

	@Test
	public void doTest() {
		Problem problem1 = new Problem("foo", Errors.UNRESOLVED_REFERENCE);
		assertEquals("foo", problem1.description());
		assertFalse(problem1.isFatal(false));
		assertTrue(problem1.isFatal(true));
		assertEquals(Errors.UNRESOLVED_REFERENCE, problem1.error());
		assertEquals(Errors.UNRESOLVED_REFERENCE.toString() + ": foo", problem1.toString());
		assertNull(problem1.detail());
		problem1 = new Problem("bar", Errors.CANNOT_READ_FILE);
		assertEquals("bar", problem1.description());
		assertTrue(problem1.isFatal(false));
		assertTrue(problem1.isFatal(true));
		assertEquals(Errors.CANNOT_READ_FILE, problem1.error());
		assertEquals(problem1.error().hashCode() + problem1.description().hashCode(), problem1.hashCode());
		assertFalse(problem1.equals(null));
		assertTrue(problem1.equals(problem1));
		assertFalse(problem1.equals(new Object()));
		assertFalse(problem1.equals(new Problem("foo", Errors.CANNOT_READ_FILE)));
		assertTrue(problem1.equals(new Problem("bar", Errors.CANNOT_READ_FILE)));
		assertTrue(problem1.equals(new Problem("bar", Errors.CLASS_BOTH_REFERRED_TO_AND_IGNORED)));
		assertEquals(0, problem1.compareTo(new Problem("bar", Errors.CANNOT_READ_FILE)));
		assertEquals(Errors.CANNOT_READ_FILE.toString() + ": bar", problem1.toString());
		problem1 = new Problem("foo", Errors.UNABLE_TO_RELEASE_WORKSET);
		assertEquals("foo", problem1.description());
		assertFalse(problem1.isFatal(false));
		assertFalse(problem1.isFatal(true));
		problem1 = new Problem("foo", Errors.ILLEGAL_REFERENCE, "big long explanation");
		assertEquals("big long explanation", problem1.detail());
		assertEquals(Errors.ILLEGAL_REFERENCE.toString() + ": big long explanation", problem1.humanReadableToString());
		Problem problem2 = new Problem("foo", Errors.ILLEGAL_REFERENCE, "big long explanation");
		Set<Problem> problems = new HashSet<>();
		problems.add(problem1);
		problems.add(problem2);
		assertEquals(1, problems.size());
		problems.clear();
		problems.add(problem1);
		problem2 = new Problem("foo2", Errors.ILLEGAL_REFERENCE, "big long explanation");
		problems.add(problem2);
		assertEquals(2, problems.size());
	}
}
