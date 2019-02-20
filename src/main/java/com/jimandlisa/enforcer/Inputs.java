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

public class Inputs {

	private final File target;
	private final File odem;
	private File ignores = null;
	private File reflections = null;
	private File fixUnresolveds = null;

	public Inputs(final File target, final File odem) {
		super();
		this.target = FileUtils.checkReadFile(target);
		this.odem = FileUtils.checkReadFile(odem);
	}

	public final File target() {
		return target;
	}

	public final File odem() {
		return odem;
	}

	public final void setIgnores(File ignores) {
		if (ignores() != null) {
			throw new EnforcerException("already set ignores file " + ignores(), Errors.IGNORES_FILE_ALREADY_SPECIFIED);
		}
		this.ignores = FileUtils.checkReadFile(ignores);
	}

	public final File ignores() {
		return ignores;
	}

	public final void setReflections(File reflections) {
		if (reflections() != null) {
			throw new EnforcerException("already set reflections file " + reflections(), Errors.REFLECTIONS_FILE_ALREADY_SPECIFIED);
		}
		this.reflections = FileUtils.checkReadFile(reflections);
	}

	public final File reflections() {
		return reflections;
	}

	public final void setFixUnresolveds(File fixedUnresolveds) {
		if (fixUnresolveds() != null) {
			throw new EnforcerException("already set fix-unresolveds file " + fixUnresolveds(), Errors.FIX_UNRESOLVEDS_FILE_ALREADY_SPECIFIED);
		}
		this.fixUnresolveds = FileUtils.checkReadFile(fixedUnresolveds);
	}

	public final File fixUnresolveds() {
		return fixUnresolveds;
	}

	@Override
	public String toString() {
		return "target=" + target() + ", ODEM=" + odem() + ", ignores=" + ignores() + ", reflections=" + reflections() + ", fix-unresolveds=" + fixUnresolveds();
	}
}
