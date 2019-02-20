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

import org.junit.Test;

public class ArgUtilsTest {

	@Test
	public void doTest() {
		new ArgUtils();
		assertEquals("abc", ArgUtils.check(" abc \t", "name"));
		try {
			ArgUtils.check((String) null, "name");
		} catch (EnforcerException e) {
			assertEquals("null name", e.getMessage());
			assertEquals(Errors.NULL_STRING_ARG, e.error());
		}
		try {
			ArgUtils.check(" \t ", "name");
		} catch (EnforcerException e) {
			assertEquals("empty name", e.getMessage());
			assertEquals(Errors.EMPTY_STRING_ARG, e.error());
		}
		ArgUtils.checkName("This is a Very_Fine0-name", "name");
		try {
			ArgUtils.checkName("$S()*@##$", "name");
		} catch (EnforcerException e) {
			assertEquals("invalid name 'name'", e.getMessage());
			assertEquals(Errors.INVALID_NAME_ARG, e.error());
		}
		assertEquals((Integer) 123, ArgUtils.check(123, "name"));
		try {
			ArgUtils.check((Integer) null, "name");
		} catch (EnforcerException e) {
			assertEquals("null name", e.getMessage());
			assertEquals(Errors.NULL_INTEGER_ARG, e.error());
		}
		Layer layer = new Layer("name", 4, null);
		assertEquals(layer, ArgUtils.check(layer, "name"));
		try {
			ArgUtils.check((Layer) null, "name");
		} catch (EnforcerException e) {
			assertEquals("null name", e.getMessage());
			assertEquals(Errors.NULL_LAYER_ARG, e.error());
		}
		Domain domain = new Domain("name", null);
		assertEquals(domain, ArgUtils.check(domain, "name"));
		try {
			ArgUtils.check((Domain) null, "name");
		} catch (EnforcerException e) {
			assertEquals("null name", e.getMessage());
			assertEquals(Errors.NULL_DOMAIN_ARG, e.error());
		}
		assertEquals(Errors.CANNOT_READ_FILE, ArgUtils.check(Errors.CANNOT_READ_FILE, "name"));
		try {
			ArgUtils.check((Errors) null, "name");
		} catch (EnforcerException e) {
			assertEquals("null name", e.getMessage());
			assertEquals(Errors.NULL_ERRORS_ARG, e.error());
		}
	}
}
