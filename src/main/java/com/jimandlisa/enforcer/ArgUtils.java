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

import java.util.regex.Pattern;

public class ArgUtils {

	public static String check(String str, String name) {
		if (str == null) {
			throw new EnforcerException("null " + name, Errors.NULL_STRING_ARG);
		}
		String trimmed = str.trim();
		if (trimmed.isEmpty()) {
			throw new EnforcerException("empty " + name, Errors.EMPTY_STRING_ARG);
		}
		return trimmed;
	}
	
	private static Pattern NAME_PATTERN = Pattern.compile("^[0-9A-Za-z][0-9A-Za-z_ -]*$");
	
	public static String checkName(String str, String name) {
		String trimmed = check(str, name);
		if (!NAME_PATTERN.matcher(trimmed).matches()) {
			throw new EnforcerException("invalid name '" + name + "'", Errors.INVALID_NAME_ARG);
		}
		return trimmed;
	}
	
	public static Integer check(Integer num, String name) {
		if (num == null) {
			throw new EnforcerException("null " + name, Errors.NULL_INTEGER_ARG);
		}
		return num;
	}
	
	public static Layer check(Layer layer, String name) {
		if (layer == null) {
			throw new EnforcerException("null " + name, Errors.NULL_LAYER_ARG);
		}
		return layer;
	}
	
	public static Domain check(Domain domain, String name) {
		if (domain == null) {
			throw new EnforcerException("null " + name, Errors.NULL_DOMAIN_ARG);
		}
		return domain;
	}
	
	public static Component check(Component component, String name) {
		if (component == null) {
			throw new EnforcerException("null " + name, Errors.NULL_COMPONENT_ARG);
		}
		return component;
	}
	
	public static Type check(Type type, String name) {
		if (type == null) {
			throw new EnforcerException("null " + name, Errors.NULL_TYPE_ARG);
		}
		return type;
	}
	
	public static Problem check(Problem problem, String name) {
		if (problem == null) {
			throw new EnforcerException("null " + name, Errors.NULL_PROBLEM_ARG);
		}
		return problem;
	}
	
	public static Errors check(Errors error, String name) {
		if (error == null) {
			throw new EnforcerException("null " + name, Errors.NULL_ERRORS_ARG);
		}
		return error;
	}
}
