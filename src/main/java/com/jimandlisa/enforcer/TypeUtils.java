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

public class TypeUtils {
	
	public static boolean isSelfReference(Type referringType, Type referredToType) {
		return referringType.belongsTo() == referredToType.belongsTo();
	}

	public static boolean isLayerViolation(Type referringType, Type referredToType) {
		if (isSelfReference(referringType, referredToType)) {
			return false;
		}
		return referringType.belongsTo().layer().depth() <= referredToType.belongsTo().layer().depth();
	}

	public static String parseableDescription(Type referringType, Type referredToType) {
		return referringType.name() + "!" + referringType.belongsTo().name() + "!" + referringType.belongsTo().layer().name() + "!" + referringType.belongsTo().layer().depth() + "|"
				+ referredToType.name() + "!" + referredToType.belongsTo().name() + "!" + referredToType.belongsTo().layer().name() + "!" + referredToType.belongsTo().layer().depth();
	}

	public static String humanReadableDescription(Type referringType, Type referredToType) {
		return "type " + referringType.name() + " in component " + referringType.belongsTo().quotedName() + " in layer " + referringType.belongsTo().layer().quotedName() + " depth "
				+ referringType.belongsTo().layer().depth() + " refers to type " + referredToType.name() + " in component " + referredToType.belongsTo().quotedName() + " in layer "
				+ referredToType.belongsTo().layer().quotedName() + " depth " + referredToType.belongsTo().layer().depth();
	}
}
