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

	public static final String WARNINGS_FILE_NAME = "warnings.txt";
	public static final String UNRESOLVED_TYPES_FILE_NAME = "unresolved_types.txt";
	public static final String ILLEGAL_REFERENCES_BASE_NAME = "illegal_references";
	public static final String ILLEGAL_COMPONENT_REFERENCES_BASE_NAME = "illegal_component_references";
	public static final String ALL_REFERENCES_BASE_NAME = "all_references";
	public static final String ALL_COMPONENT_REFERENCES_BASE_NAME = "all_component_references";
	public static final String GEPHI_NODES_SUFFIX = "_GephiNodes.csv";
	public static final String GEPHI_EDGES_SUFFIX = "_GephiEdges.csv";
	public static final String YED_SUFFIX = "_yed.tgf";

	private final File outputDirectory;
	private final File warnings;
	private final File unresolvedTypes;
	private final File illegalReferences;
	private final File illegalComponentReferences;
	private final File allReferences;
	private final File allReferencesGephiNodes;
	private final File allReferencesGephiEdges;
	private final File allReferencesYeD;
	private final File allComponentReferences;
	private final File allComponentReferencesGephiNodes;
	private final File allComponentReferencesGephiEdges;
	private final File allComponentReferencesYeD;

	public Outputs(final File outputDirectory) {
		super();
		this.outputDirectory = FileUtils.checkWriteDir(outputDirectory);
		warnings = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), WARNINGS_FILE_NAME).toFile());
		unresolvedTypes = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), UNRESOLVED_TYPES_FILE_NAME).toFile());
		illegalReferences = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), ILLEGAL_REFERENCES_BASE_NAME + ".txt").toFile());
		illegalComponentReferences = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), ILLEGAL_COMPONENT_REFERENCES_BASE_NAME + ".txt").toFile());
		allReferences = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), ALL_REFERENCES_BASE_NAME + ".txt").toFile());
		allReferencesGephiNodes = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), ALL_REFERENCES_BASE_NAME + GEPHI_NODES_SUFFIX).toFile());
		allReferencesGephiEdges = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), ALL_REFERENCES_BASE_NAME + GEPHI_EDGES_SUFFIX).toFile());
		allReferencesYeD = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), ALL_REFERENCES_BASE_NAME + YED_SUFFIX).toFile());
		allComponentReferences = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), ALL_COMPONENT_REFERENCES_BASE_NAME + ".txt").toFile());
		allComponentReferencesGephiNodes = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), ALL_COMPONENT_REFERENCES_BASE_NAME + GEPHI_NODES_SUFFIX).toFile());
		allComponentReferencesGephiEdges = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), ALL_COMPONENT_REFERENCES_BASE_NAME + GEPHI_EDGES_SUFFIX).toFile());
		allComponentReferencesYeD = FileUtils.clear(Paths.get(outputDirectory.getAbsolutePath(), ALL_COMPONENT_REFERENCES_BASE_NAME + YED_SUFFIX).toFile());
	}

	public File outputDirectory() {
		return outputDirectory;
	}

	public File warnings() {
		return warnings;
	}

	public File unresolvedTypes() {
		return unresolvedTypes;
	}

	public File illegalReferences() {
		return illegalReferences;
	}

	public File illegalComponentReferences() {
		return illegalComponentReferences;
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
		return "warnings=" + warnings + ", unresolvedTypes=" + unresolvedTypes + ", illegalReferences=" + illegalReferences + ", illegalComponentReferences=" + illegalComponentReferences + ", allReferences=" + allReferences
				+ ", allReferencesGephiNodes=" + allReferencesGephiNodes + ", allReferencesGephiEdges=" + allReferencesGephiEdges + ", allReferencesYeD=" + allReferencesYeD + ", allComponentReferences=" + allComponentReferences
				+ ", allComponentReferencesGephiNodes=" + allComponentReferencesGephiNodes + ", allComponentReferencesGephiEdges=" + allComponentReferencesGephiEdges + ", allComponentReferencesYeD=" + allComponentReferencesYeD;
	}
}
