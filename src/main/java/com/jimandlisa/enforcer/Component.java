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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Component {

	private final String name;
	private final String quotedName;
	private final Layer layer;
	private final Domain domain;
	private final String description;
	private final Set<String> packages = new LinkedHashSet<>();
	private final Set<String> classes = new LinkedHashSet<>();
	private final Map<String, Type> types = new HashMap<>();
	
	public Component(final String name, final Layer layer, final Domain domain, final String description) {
		super();
		this.name = ArgUtils.checkName(name, "name");
		this.quotedName = "'" + this.name + "'";
		this.layer = ArgUtils.check(layer, "layer for component " + quotedName + " (check if layer is defined)");
		this.domain = domain == null ? null : ArgUtils.check(domain, "domain for component " + quotedName + " (check if domain is defined)");
		this.description = description == null ? null : description.trim();
	}
	
	public String name() {
		return name;
	}
	
	public String quotedName() {
		return quotedName;
	}
	
	public Layer layer() {
		return layer;
	}
	
	public Domain domain() {
		return domain;
	}
	
	public String description() {
		return description;
	}
	
	public Set<String> packages() {
		return packages;
	}
	
	public Set<String> classes() {
		return classes;
	}
	
	public Map<String, Type> types() {
		return types;
	}
	
	public void add(Type type) {
		types().put(type.name(), type);
	}

	@Override
	public String toString() {
		return "name=" + quotedName() + ", layer=" + layer().quotedName() + ", depth=" + layer().depth() + ", domain=" + (domain() == null ? null : domain.quotedName());
	}
}
