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

import org.junit.Assert;
import org.junit.Test;

public class FlagsTest {

	@Test
	public void doTest() {
		Flags flags = new Flags();
		assertEquals("preserveNestedTypes=false, strict=false, debug=false", flags.toString());
		flags.enablePreserveNestedTypes();
		flags.enableStrict();
		flags.enableDebug();
		assertEquals("preserveNestedTypes=true, strict=true, debug=true", flags.toString());
		try {
			flags.enablePreserveNestedTypes();
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("preserve nested types already set"));
			assertEquals(Errors.PRESERVE_NESTED_TYPES_ALREADY_SPECIFIED, e.error());
		}
		try {
			flags.enableStrict();
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("strict already set"));
			assertEquals(Errors.STRICT_ALREADY_SPECIFIED, e.error());
		}
		try {
			flags.enableDebug();
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("debug already set"));
			assertEquals(Errors.DEBUG_ALREADY_SPECIFIED, e.error());
		}
	}
}
