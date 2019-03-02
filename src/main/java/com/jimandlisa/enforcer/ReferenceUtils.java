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

public class ReferenceUtils {

	public static boolean isSelfReference(Reference reference) {
		return reference.referringType().component().equals(reference.referredToType().component());
	}

	public static boolean isLayerViolation(Reference reference) {
		if (isSelfReference(reference)) { // Ignore intra-component references.
			return false;
		}
		return reference.referringType().component().layer().depth() <= reference.referredToType().component().layer().depth();
	}

	public static String parseableDescription(Reference reference) {
		return reference.referringType().name() + "!" + reference.referringType().component().name() + "!" + reference.referringType().component().layer().name() + "!"
				+ reference.referringType().component().layer().depth() + "!" + reference.referredToType().name() + "!" + reference.referredToType().component().name() + "!"
				+ reference.referredToType().component().layer().name() + "!" + reference.referredToType().component().layer().depth();
	}

	public static String humanReadableDescription(Reference reference) {
		return "type " + reference.referringType().name() + " in component " + reference.referringType().component().quotedName() + " in layer "
				+ reference.referringType().component().layer().quotedName() + " depth " + reference.referringType().component().layer().depth() + " refers to type "
				+ reference.referredToType().name() + " in component " + reference.referredToType().component().quotedName() + " in layer "
				+ reference.referredToType().component().layer().quotedName() + " depth " + reference.referredToType().component().layer().depth();
	}
}
