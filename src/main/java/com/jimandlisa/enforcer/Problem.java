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

// We accumulate problems (both fatal and warnings) so we can report more than one at various points during execution, instead of just throwing on first error.
// This avoids antagonizing the developer early in a decomposition project, when there can be hundreds or thousands of errors.
public class Problem implements Comparable<Problem> {

	private final String description;
	private final Errors error;
	
	public Problem(final String description, final Errors error) { // If fatal.
		super();
		this.description = description;
		this.error = error;
	}
	
	public Problem(final String description) { // If not fatal.
		this(description, null);
	}
	
	public String description() {
		return description;
	}
	
	public Errors error() {
		return error;
	}
	
	public boolean isFatal() {
		return error() != null;
	}
	
	@Override
	public int hashCode() {
		return description().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Problem)) {
			return false;
		}
		return description().equals(((Problem)obj).description());
	}

	@Override
	public int compareTo(Problem other) {
		return description().compareTo(other.description());
	}
	
	@Override
	public String toString() {
		return description() + ": error=" + error();
	}
}
