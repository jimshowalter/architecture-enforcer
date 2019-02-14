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

import java.io.File;

public class Enforcer {
	
	private static void usage(String error) {
		System.out.println(error);
		System.out.println(Enforcer.class.getSimpleName() + ": usage: /full/path/to/pf-CDA/.odem, /full/path/to/packages/to/ignore, /full/path/to/reflection/references");
	}
	
	private static File validate(String[] inputs, int i) {
		if (i > inputs.length - 1) {
			return null;
		}
		String name = inputs[i];
		try {
			File file = new File(name);
			if (!file.exists()) {
				throw new RuntimeException(name + " does not exist");
			}
			if (!file.canRead()) {
				throw new RuntimeException("cannot read " + name);
			}
			return file;
		} catch (Throwable t) {
			throw new RuntimeException("error validating file " + name);
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			usage("not enough args");
			return;
		}
		if (args.length > 3) {
			usage("too many args");
			return;
		}
		try {
			EnforcerUtils.enforce(new Inputs(validate(args, 0), validate(args, 1), validate(args, 2)));
		} catch (Throwable t) {
			System.out.println(t.getMessage());
		}
	}
}