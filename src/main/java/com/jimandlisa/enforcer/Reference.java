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

public class Reference implements Comparable<Reference> {

	public static final String SEPARATOR = Separators.REFERENCE_SEPARATOR.value();

	private final Type referringType;
	private final Type referredToType;
	private final ReferenceKinds kind;
	private final int hashCode;
	private final String baseToString;

	static ReferenceKinds kind(final Type referringType, final Type referredToType) {
		if (referringType.component().equals(referredToType.component())) {
			return ReferenceKinds.INTRA_COMPONENT;
		}
		int referringDepth = referringType.component().layer().depth();
		int referredToDepth = referredToType.component().layer().depth();
		if (referringDepth == referredToDepth) {
			return ReferenceKinds.INTER_COMPONENT_SAME_LAYER;
		}
		if (referringDepth < referredToDepth) {
			return ReferenceKinds.INTER_COMPONENT_LOWER_TO_HIGHER;
		}
		return ReferenceKinds.INTER_COMPONENT_HIGHER_TO_LOWER;
	}

	public Reference(final Type referringType, final Type referredToType) {
		super();
		this.referringType = ArgUtils.check(referringType, "referringType");
		this.referredToType = ArgUtils.check(referredToType, "referredToType");
		this.hashCode = referringType.hashCode() + referredToType.hashCode();
		this.baseToString = referringType.name() + " -> " + referredToType.name();
		this.kind = kind(referringType, referredToType);
	}

	public Type referringType() {
		return referringType;
	}

	public Type referredToType() {
		return referredToType;
	}

	public ReferenceKinds kind() {
		return kind;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Reference)) {
			return false;
		}
		return baseToString.equals(((Reference)obj).baseToString);
	}

	@Override
	public int compareTo(Reference other) {
		return baseToString.compareTo(other.baseToString);
	}

	@Override
	public String toString() {
		return baseToString + " [" + kind + "]";
	}

	public String parseableDescription(boolean includeClasses, boolean includeKind) {
		return (includeClasses ? referringType.name() + SEPARATOR : "") + referringType.component().name() + SEPARATOR + referringType.component().layer().name() + SEPARATOR + referringType.component().layer().depth() + SEPARATOR
				+ (includeClasses ? referredToType.name() + SEPARATOR : "") + referredToType.component().name() + SEPARATOR + referredToType.component().layer().name() + SEPARATOR + referredToType.component().layer().depth()
				+ (includeKind ? SEPARATOR + kind() + SEPARATOR + (kind.isLegal() ? "LEGAL" : "ILLEGAL") : "");
	}

	public String humanReadableDescription(boolean includeClasses, boolean includeKind) {
		return (includeClasses ? "type " + referringType.name() + " in " : "") + "component " + referringType.component().quotedName() + " in layer " + referringType.component().layer().quotedName() + " depth "
				+ referringType.component().layer().depth() + " refers to " + (includeClasses ? "type " + referredToType.name() + " in " : "") + "component " + referredToType.component().quotedName() + " in layer "
				+ referredToType.component().layer().quotedName() + " depth " + referredToType.component().layer().depth() + (includeKind ? " [" + kind + " (" + (kind.isLegal() ? "LEGAL" : "ILLEGAL") + ")]" : "");
	}
}
