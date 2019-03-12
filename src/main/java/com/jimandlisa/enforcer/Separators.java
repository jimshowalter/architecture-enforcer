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

// Some code might use these in class names, in which case parsing/splitting will break. Making these constants allows those projects to use different
// strings (which can even have more than one character). We felt this would be so rare it didn't justify supporting these as command-line overrides,
// plus in some cases the regular expressions in split and replaceAll calls might also need to be changed when changing the separators.
public enum Separators {

	REFERENCE_SEPARATOR("!"),
	IGNORES_VERBATIM_INDICATOR("!"),
	SUPPLEMENTAL_TYPES_FROM_TO_SEPARATOR(":"),
	SUPPLEMENTAL_TYPES_TO_LIST_SEPARATOR(",");
	
	private final String value;
	
	private Separators(final String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
}
