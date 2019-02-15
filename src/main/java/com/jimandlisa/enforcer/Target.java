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

public class Target {

	private final Map<String, Layer> layers = new HashMap<>();
	private final Map<String, Domain> domains = new HashMap<>();
	private final Map<String, Component> components = new HashMap<>();

	public Target() {
		super();
	}

	public Map<String, Layer> layers() {
		return layers;
	}

	public void add(Layer layer) {
		layers().put(layer.name(), layer);
	}

	public Map<String, Domain> domains() {
		return domains;
	}

	public void add(Domain domain) {
		domains().put(domain.name(), domain);
	}

	public Map<String, Component> components() {
		return components;
	}

	public void add(Component component) {
		components().put(component.name(), component);
	}
}
