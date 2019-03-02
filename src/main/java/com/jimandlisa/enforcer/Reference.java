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

	private final Type referringType;
	private final Type referredToType;
	private final int hashCode;
	private final String baseToString;
	private Problem problem = null;
	private boolean isIllegal = false;

	public Reference(final Type referringType, final Type referredToType) {
		super();
		this.referringType = ArgUtils.check(referringType, "referringType");
		this.referredToType = ArgUtils.check(referredToType, "referredToType");
		this.hashCode = referringType.hashCode() + referredToType.hashCode();
		this.baseToString = referringType.name() + " -> " + referredToType.name();
	}

	public Type referringType() {
		return referringType;
	}

	public Type referredToType() {
		return referredToType;
	}

	public void setProblem(final Problem problem) {
		if (problem() != null) {
			throw new EnforcerException("already set problem " + problem, Errors.PROBLEM_ALREADY_SPECIFIED);
		}
		this.problem = ArgUtils.check(problem, "problem");
		this.isIllegal = problem.error() == Errors.ILLEGAL_REFERENCE;
	}

	public Problem problem() {
		return problem;
	}

	public boolean isIllegal() {
		return isIllegal;
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
		return baseToString + (isIllegal ? " [ILLEGAL]" : "");
	}
}
