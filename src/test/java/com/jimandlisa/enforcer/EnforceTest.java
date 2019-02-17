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

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

public class EnforceTest {

	@Test
	public void doTest() throws Exception {
		new Enforce();
		for (Optionals optional : Optionals.values()) {
			optional.toString();
		}
		Enforce.parse(Optionals.IGNORES.indicator() + Thread.currentThread().getContextClassLoader().getResource("SamplePackageIgnores.txt").getPath(), TestUtils.inputs(false, false, false));
		try {
			Enforce.parse(Optionals.IGNORES.indicator() + "foo", TestUtils.inputs(true, false, false));
			Assert.fail();
		} catch (EnforcerException e) {
			// Do nothing, expected.
		}
		Enforce.parse(Optionals.REFLECTIONS.indicator() + Thread.currentThread().getContextClassLoader().getResource("SampleReflections.txt").getPath(), TestUtils.inputs(false, false, false));
		try {
			Enforce.parse(Optionals.REFLECTIONS.indicator() + "foo", TestUtils.inputs(false, true, false));
			Assert.fail();
		} catch (EnforcerException e) {
			// Do nothing, expected.
		}
		Enforce.parse(Optionals.FIX_UNRESOLVEDS.indicator() + Thread.currentThread().getContextClassLoader().getResource("SampleFixUnresolveds.txt").getPath(), TestUtils.inputs(false, false, false));
		try {
			Enforce.parse(Optionals.FIX_UNRESOLVEDS.indicator() + "foo", TestUtils.inputs(false, false, true));
			Assert.fail();
		} catch (EnforcerException e) {
			// Do nothing, expected.
		}
		try {
			Enforce.parse("foo", TestUtils.inputs(false, false, false));
			Assert.fail();
		} catch (EnforcerException e) {
			// Do nothing, expected.
		}
		Enforce.debug(false, null, null, null);
		Enforce.problems(new HashSet<String>(), null);
		Enforce.mainImpl(new String[0]);
		Enforce.mainImpl(new String[] { "a", "b", "c", "d", "e", "f" });
		Enforce.mainImpl(new String[] {Thread.currentThread().getContextClassLoader().getResource("SampleTarget.yaml").getPath(), Thread.currentThread().getContextClassLoader().getResource("Sample.odem").getPath(), Optionals.IGNORES.indicator() + Thread.currentThread().getContextClassLoader().getResource("SamplePackageIgnores.txt").getPath()});
	}
}
