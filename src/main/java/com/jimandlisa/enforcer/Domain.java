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

import java.util.HashMap;
import java.util.Map;

public class Domain {

	private final String name;
	private final String quotedName;
	private final String description;
	private final Map<String, Component> components = new HashMap<>();
	
	public Domain(final String name, final String description) {
		super();
		this.name = ArgUtils.checkName(name, "name");
		this.quotedName = "'" + this.name + "'";
		this.description = description == null ? null : description.trim();
	}
	
	public String name() {
		return name;
	}
	
	public String quotedName() {
		return quotedName;
	}
	
	public String description() {
		return description;
	}
	
	public Map<String, Component> components() {
		return components;
	}

	@Override
	public String toString() {
		return "name=" + quotedName();
	}
}
