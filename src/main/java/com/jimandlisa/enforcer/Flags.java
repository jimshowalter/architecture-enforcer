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

public class Flags {

	private boolean preserveNestedTypes = false;
	private boolean strict = false;
	private boolean debug = false;
	
	public Flags() {
		super();
	}
	
	public void enablePreserveNestedTypes() {
		if (preserveNestedTypes()) {
			throw new EnforcerException("preserve nested types already set", Errors.PRESERVE_NESTED_TYPES_ALREADY_SPECIFIED);
		}
		this.preserveNestedTypes = true;
	}
	
	public boolean preserveNestedTypes() {
		return preserveNestedTypes;
	}
	
	public void enableStrict() {
		if (strict) {
			throw new EnforcerException("strict already set", Errors.STRICT_ALREADY_SPECIFIED);
		}
		this.strict = true;
	}
	
	public boolean strict() {
		return strict;
	}
	
	public void enableDebug() {
		if (debug) {
			throw new EnforcerException("debug already set", Errors.DEBUG_ALREADY_SPECIFIED);
		}
		this.debug = true;
	}
	
	public boolean debug() {
		return debug;
	}
	
	@Override
	public String toString() {
		return "preserveNestedTypes=" + preserveNestedTypes + ", strict=" + strict + ", debug=" + debug;
	}
}
