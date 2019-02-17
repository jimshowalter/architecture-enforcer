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

public class ArgUtils {

	public static String check(String val, String name) {
		if (val == null) {
			throw new EnforcerException("null " + name, Errors.NULL_STRING_ARG);
		}
		String trimmed = val.trim();
		if (trimmed.isEmpty()) {
			throw new EnforcerException("empty " + name, Errors.EMPTY_STRING_ARG);
		}
		return trimmed;
	}
	
	public static Integer check(Integer val, String name) {
		if (val == null) {
			throw new EnforcerException("null " + name, Errors.NULL_INTEGER_ARG);
		}
		return val;
	}
	
	public static Layer check(Layer val, String name) {
		if (val == null) {
			throw new EnforcerException("null " + name, Errors.NULL_LAYER_ARG);
		}
		return val;
	}
	
	public static Domain check(Domain val, String name) {
		if (val == null) {
			throw new EnforcerException("null " + name, Errors.NULL_DOMAIN_ARG);
		}
		return val;
	}
}
