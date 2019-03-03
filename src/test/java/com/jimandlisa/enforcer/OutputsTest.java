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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

public class OutputsTest {

	@Test
	public void doTest() {
		Outputs outputs = new Outputs(TestUtils.targetDir());
		assertEquals(TestUtils.targetDir(), outputs.outputDirectory());
		outputs.toString();
		outputs.setIllegalReferences(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME);
		assertEquals(Outputs.ILLEGAL_REFERENCES_DEFAULT_FILE_NAME, outputs.illegalReferences().getName());
		outputs.toString();
		outputs.setUnresolvedTypes(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME);
		assertEquals(Outputs.UNRESOLVED_TYPES_DEFAULT_FILE_NAME, outputs.unresolvedTypes().getName());
		assertNull(outputs.allReferences());
		assertNull(outputs.allReferencesGephiNodes());
		assertNull(outputs.allReferencesGephiEdges());
		assertNull(outputs.allReferencesYeD());
		assertNull(outputs.allComponentReferences());
		assertNull(outputs.allComponentReferencesGephiNodes());
		assertNull(outputs.allComponentReferencesGephiEdges());
		assertNull(outputs.allComponentReferencesYeD());
		outputs.toString();
		outputs.enableAllReferences();
		assertNotNull(outputs.allReferences());
		assertNotNull(outputs.allReferencesGephiNodes());
		assertNotNull(outputs.allReferencesGephiEdges());
		assertNotNull(outputs.allReferencesYeD());
		assertNotNull(outputs.allComponentReferences());
		assertNotNull(outputs.allComponentReferencesGephiNodes());
		assertNotNull(outputs.allComponentReferencesGephiEdges());
		assertNotNull(outputs.allComponentReferencesYeD());
		outputs.toString();
		try {
			outputs.setIllegalReferences("foo");
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("illegal references output file already set"));
			assertEquals(Errors.ILLEGAL_REFERENCES_OUTPUT_FILE_ALREADY_SPECIFIED, e.error());
		}
		try {
			outputs.setUnresolvedTypes("foo");
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("unresolved types output file already set"));
			assertEquals(Errors.UNRESOLVED_TYPES_OUTPUT_FILE_ALREADY_SPECIFIED, e.error());
		}
		try {
			outputs.enableAllReferences();
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("all references already enabled"));
			assertEquals(Errors.ALL_REFERENCES_ALREADY_ENABLED, e.error());
		}
		assertEquals("foo", Outputs.check("foo"));
		try {
			Outputs.check("foo" + Outputs.ALL_REFERENCES_BASE_NAME + "bar");
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("conflicts with reserved all-references base name"));
			assertEquals(Errors.NAME_CONFLICTS_WITH_ALL_REFERENCES_BASE_NAME, e.error());
		}
		try {
			Outputs.check("foo" + Outputs.ALL_COMPONENT_REFERENCES_BASE_NAME + "bar");
			Assert.fail();
		} catch (EnforcerException e) {
			assertTrue(e.getMessage().contains("conflicts with reserved all-component-references base name"));
			assertEquals(Errors.NAME_CONFLICTS_WITH_ALL_COMPONENT_REFERENCES_BASE_NAME, e.error());
		}
	}
}
