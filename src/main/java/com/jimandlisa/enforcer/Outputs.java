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
	public static final String ALL_REFERENCES_BASE_NAME = "all_references";
	public static final String ALL_COMPONENT_REFERENCES_BASE_NAME = "all_component_references";
	public static final String GEPHI_NODES_SUFFIX = "_GephiNodes.csv";
	public static final String GEPHI_EDGES_SUFFIX = "_GephiEdges.csv";
	public static final String YED_SUFFIX = "_yed.tgf";

	private final File outputDirectory;
	private File unresolvedTypes = null;
	private File illegalReferences = null;
	private File allReferences = null;
	private File allReferencesGephiNodes = null;
	private File allReferencesGephiEdges = null;
	private File allReferencesYeD = null;
	private File allComponentReferences = null;
	private File allComponentReferencesGephiNodes = null;
	private File allComponentReferencesGephiEdges = null;
	private File allComponentReferencesYeD = null;

	public Outputs(final File outputDirectory) {
		super();
		this.outputDirectory = FileUtils.checkWriteDir(outputDirectory);
	}

	public File outputDirectory() {
		return outputDirectory;
	}
	
	static String check(String name) {
		if (name.contains(ALL_REFERENCES_BASE_NAME)) {
			throw new EnforcerException("file name '" + name + "' conflicts with reserved all-references base name '" + ALL_REFERENCES_BASE_NAME + "'", Errors.NAME_CONFLICTS_WITH_ALL_REFERENCES_BASE_NAME);
		}
		if (name.contains(ALL_COMPONENT_REFERENCES_BASE_NAME)) {
			throw new EnforcerException("file name '" + name + "' conflicts with reserved all-component-references base name '" + ALL_COMPONENT_REFERENCES_BASE_NAME + "'", Errors.NAME_CONFLICTS_WITH_ALL_COMPONENT_REFERENCES_BASE_NAME);
		}
		return name;
	}
	
	public void setUnresolvedTypes(String name) {
		if (unresolvedTypes() != null) {
			throw new EnforcerException("unresolved types output file already set", Errors.UNRESOLVED_TYPES_OUTPUT_FILE_ALREADY_SPECIFIED);
		}
		unresolvedTypes = FileUtils.check(Paths.get(outputDirectory.getAbsolutePath(), check(ArgUtils.check(name, "name"))).toFile(), illegalReferences);
	}

	public File unresolvedTypes() {
		return unresolvedTypes;
	}

	public void setIllegalReferences(String name) {
		if (illegalReferences() != null) {
			throw new EnforcerException("illegal references output file already set", Errors.ILLEGAL_REFERENCES_OUTPUT_FILE_ALREADY_SPECIFIED);
		}
		illegalReferences = FileUtils.check(Paths.get(outputDirectory.getAbsolutePath(), check(ArgUtils.check(name, "name"))).toFile(), unresolvedTypes);
	}

	public File illegalReferences() {
		return illegalReferences;
	}

	public void enableAllReferences() {
		if (allReferences() != null) {
			throw new EnforcerException("all references already enabled", Errors.ALL_REFERENCES_ALREADY_ENABLED);
		}
		allReferences = Paths.get(outputDirectory.getAbsolutePath(), ALL_REFERENCES_BASE_NAME + ".txt").toFile();
		allReferencesGephiNodes = Paths.get(outputDirectory.getAbsolutePath(), ALL_REFERENCES_BASE_NAME + GEPHI_NODES_SUFFIX).toFile();
		allReferencesGephiEdges = Paths.get(outputDirectory.getAbsolutePath(), ALL_REFERENCES_BASE_NAME + GEPHI_EDGES_SUFFIX).toFile();
		allReferencesYeD = Paths.get(outputDirectory.getAbsolutePath(), ALL_REFERENCES_BASE_NAME + YED_SUFFIX).toFile();
		allComponentReferences = Paths.get(outputDirectory.getAbsolutePath(), ALL_COMPONENT_REFERENCES_BASE_NAME + ".txt").toFile();
		allComponentReferencesGephiNodes = Paths.get(outputDirectory.getAbsolutePath(), ALL_COMPONENT_REFERENCES_BASE_NAME + GEPHI_NODES_SUFFIX).toFile();
		allComponentReferencesGephiEdges = Paths.get(outputDirectory.getAbsolutePath(), ALL_COMPONENT_REFERENCES_BASE_NAME + GEPHI_EDGES_SUFFIX).toFile();
		allComponentReferencesYeD = Paths.get(outputDirectory.getAbsolutePath(), ALL_COMPONENT_REFERENCES_BASE_NAME + YED_SUFFIX).toFile();
	}

	public File allReferences() {
		return allReferences;
	}

	public File allReferencesGephiNodes() {
		return allReferencesGephiNodes;
	}

	public File allReferencesGephiEdges() {
		return allReferencesGephiEdges;
	}

	public File allReferencesYeD() {
		return allReferencesYeD;
	}

	public File allComponentReferences() {
		return allComponentReferences;
	}

	public File allComponentReferencesGephiNodes() {
		return allComponentReferencesGephiNodes;
	}

	public File allComponentReferencesGephiEdges() {
		return allComponentReferencesGephiEdges;
	}

	public File allComponentReferencesYeD() {
		return allComponentReferencesYeD;
	}

	@Override
	public String toString() {
		return "unresolvedTypes=" + unresolvedTypes + ", illegalReferences=" + illegalReferences + ", allReferences=" + allReferences + ", allReferencesGephiNodes=" + allReferencesGephiNodes
				+ ", allReferencesGephiEdges=" + allReferencesGephiEdges + ", allReferencesYeD=" + allReferencesYeD + ", allComponentReferences=" + allComponentReferences
				+ ", allComponentReferencesGephiNodes=" + allComponentReferencesGephiNodes + ", allComponentReferencesGephiEdges=" + allComponentReferencesGephiEdges + ", allComponentReferencesYeD="
				+ allComponentReferencesYeD;
	}
}
