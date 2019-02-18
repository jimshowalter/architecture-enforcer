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

	private final boolean preserveNestedTypes;
	private final boolean strict;
	private final boolean debug;
	
	public Flags(final boolean preserveNestedTypes, final boolean strict, final boolean debug) {
		super();
		this.preserveNestedTypes = preserveNestedTypes;
		this.strict = strict;
		this.debug = debug;
	}
	
	public boolean preserveNestedTypes() {
		return preserveNestedTypes;
	}
	
	public boolean strict() {
		return strict;
	}
	
	public boolean debug() {
		return debug;
	}
	
	@Override
	public String toString() {
		return "preserveNestedTypes=" + preserveNestedTypes() + ", strict=" + strict() + ", debug=" + debug();
	}
}
