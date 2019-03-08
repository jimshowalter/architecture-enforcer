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

public enum Errors {

	MULTIPLE_ERRORS, // When more than one fatal problem is accumulated, this general error code is used. See the error output for details on each error.
	NULL_STRING_ARG,
	EMPTY_STRING_ARG,
	NULL_INTEGER_ARG,
	NULL_LAYER_ARG,
	NULL_DOMAIN_ARG,
	NULL_COMPONENT_ARG,
	NULL_TYPE_ARG,
	NULL_PROBLEM_ARG,
	NULL_ERRORS_ARG,
	INVALID_NAME_ARG,
	IGNORES_FILE_ALREADY_SPECIFIED,
	REFLECTIONS_FILE_ALREADY_SPECIFIED,
	FIX_UNRESOLVEDS_FILE_ALREADY_SPECIFIED,
	NAME_CONFLICTS_WITH_OTHER_FILE,
	COMPONENT_ALREADY_SPECIFIED,
	PRESERVE_NESTED_TYPES_ALREADY_SPECIFIED,
	STRICT_ALREADY_SPECIFIED,
	DEBUG_ALREADY_SPECIFIED,
	UNRECOGNIZED_COMMAND_LINE_OPTION,
	NOT_ENOUGH_ARGS,
	TOO_MANY_ARGS,
	FILE_DOES_NOT_EXIST,
	CANNOT_READ_FILE,
	CANNOT_WRITE_TO_DIRECTORY,
	ERROR_VALIDATING_FILE,
	ERROR_VALIDATING_DIRECTORY,
	UNRECOGNIZED_LAYER_KEY,
	UNRECOGNIZED_DOMAIN_KEY,
	UNRECOGNIZED_COMPONENT_KEY,
	DUPLICATE_LAYER_DEPTH,
	DUPLICATE_LAYER_NAME,
	DUPLICATE_DOMAIN_NAME,
	DUPLICATE_COMPONENT_NAME,
	DUPLICATE_PACKAGE_NAME,
	DUPLICATE_CLASS_NAME,
	MALFORMED_CLASS_NAME,
	MALFORMED_CLASS_TO_CLASS_REFERENCE,
	MISSING_REFERRED_TO_CLASS,
	CLASS_BOTH_REFERRING_AND_IGNORED,
	CLASS_BOTH_REFERRED_TO_AND_IGNORED,
	CLASS_NOT_RESOLVED_TO_TYPE,
	TYPE_NOT_RESOLVED_TO_COMPONENT,
	UNUSED_PACKAGE,
	UNABLE_TO_RELEASE_WORKSET(Severities.WARNING),
	UNRESOLVED_REFERENCE(Severities.ERROR_IF_STRICT),
	ILLEGAL_REFERENCE(Severities.ERROR_IF_STRICT),
	ILLEGAL_COMPONENT_REFERENCE(Severities.ERROR_IF_STRICT);
	
	private final Severities severity;
	
	private Errors(final Severities severity) {
		this.severity = severity;
	}
	
	private Errors() {
		this(Severities.ALWAYS_ERROR);
	}
	
	public Severities severity() {
		return severity;
	}
	
	public boolean isFatal(boolean strict) {
		if (severity == Severities.WARNING) {
			return false;
		}
		if (severity == Severities.ALWAYS_ERROR) {
			return true;
		}
		if (strict) {
			return true;
		}
		return false;
	}
}