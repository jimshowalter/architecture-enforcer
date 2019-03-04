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

public enum ReferenceKinds {

	INTRA_COMPONENT(true), // Intra-component reference.
	INTER_COMPONENT_HIGHER_TO_LOWER(true), // Legal inter-component reference from higher layer to lower layer.
	INTER_COMPONENT_SAME_LAYER(false), // Illegal inter-component reference in same layer.
	INTER_COMPONENT_LOWER_TO_HIGHER(false); // Illegal inter-component reference from lower layer to higher layer.
	
	private final boolean isLegal;
	
	private ReferenceKinds(final boolean isLegal) {
		this.isLegal = isLegal;
	}
	
	public boolean isLegal() {
		return isLegal;
	}
}
