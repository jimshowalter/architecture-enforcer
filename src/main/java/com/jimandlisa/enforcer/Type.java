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
import java.util.Set;

public class Type implements Comparable<Type> {

	private final String name;
	private final boolean isSynthesized;
	private final Set<String> referenceNames = new HashSet<>();
	private final Set<Type> references = new HashSet<>();
	private Component component = null;
	
	public Type(final String name, final boolean isSynthesized) {
		super();
		this.name = ArgUtils.check(name, "name");
		this.isSynthesized = isSynthesized;
	}

	public Type(final String name) {
		this(name, false);
	}

	public String name() {
		return name;
	}
	
	public boolean isSynthesized() {
		return isSynthesized;
	}

	public Set<String> referenceNames() {
		return referenceNames;
	}

	public Set<Type> references() {
		return references;
	}

	public void setComponent(final Component component) {
		if (component() != null) {
			throw new EnforcerException("component already set", Errors.COMPONENT_ALREADY_SPECIFIED);
		}
		this.component = ArgUtils.check(component, "component");
	}

	public Component component() {
		return component;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Type)) {
			return false;
		}
		return name.equals(((Type)obj).name);
	}

	@Override
	public int compareTo(Type other) {
		return name.compareTo(other.name);
	}

	@Override
	public String toString() {
		return name;
	}
}
