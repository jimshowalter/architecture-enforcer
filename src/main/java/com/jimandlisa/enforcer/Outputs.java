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

import java.io.File;
import java.nio.file.Paths;

public class Outputs {

	public static final String UNRESOLVED_TYPES_DEFAULT_FILE_NAME = "unresolved_types.txt";
	public static final String ILLEGAL_REFERENCES_DEFAULT_FILE_NAME = "illegal_references.txt";

	private final File outputDirectory;
	private File unresolvedTypes;
	private File illegalReferences;

	public Outputs(final File outputDirectory) {
		super();
		this.outputDirectory = FileUtils.checkWriteDir(outputDirectory);
	}

	public File outputDirectory() {
		return outputDirectory;
	}
	
	public void setUnresolvedTypes(String name) {
		if (unresolvedTypes() != null) {
			throw new EnforcerException("unresolved types output file already set", Errors.UNRESOLVED_TYPES_OUTPUT_FILE_ALREADY_SPECIFIED);
		}
		unresolvedTypes = Paths.get(outputDirectory.getAbsolutePath(), ArgUtils.check(name, "name")).toFile();
	}

	public File unresolvedTypes() {
		return unresolvedTypes;
	}
	
	public void setIllegalReferences(String name) {
		if (illegalReferences() != null) {
			throw new EnforcerException("illegal references output file already set", Errors.ILLEGAL_REFERENCES_OUTPUT_FILE_ALREADY_SPECIFIED);
		}
		illegalReferences = Paths.get(outputDirectory.getAbsolutePath(), ArgUtils.check(name, "name")).toFile();
	}

	public File illegalReferences() {
		return illegalReferences;
	}

	@Override
	public String toString() {
		return "unresolvedTypes=" + unresolvedTypes() + ", illegalReferences=" + illegalReferences();
	}
}
