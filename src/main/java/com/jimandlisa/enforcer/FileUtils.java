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
import java.nio.file.Files;

public class FileUtils {

	public static File checkReadFile(File file) {
		try {
			if (!file.exists()) {
				throw new EnforcerException(file + " does not exist", Errors.FILE_DOES_NOT_EXIST);
			}
			if (!file.canRead()) {
				throw new EnforcerException("cannot read " + file, Errors.CANNOT_READ_FILE);
			}
			return file;
		} catch (EnforcerException e) {
			throw e;
		} catch (Throwable t) {
			throw new EnforcerException("error validating file " + file + ": " + t.getMessage(), Errors.ERROR_VALIDATING_FILE, t);
		}
	}
	
	public static File checkWriteDir(File dir) {
		try {
			Files.createDirectories(dir.toPath());
			if (!dir.canWrite()) {
				throw new EnforcerException("cannot write to " + dir, Errors.CANNOT_WRITE_TO_DIRECTORY);
			}
			return dir;
		} catch (EnforcerException e) {
			throw e;
		} catch (Throwable t) {
			throw new EnforcerException("error validating directory " + dir + ": " + t.getMessage(), Errors.ERROR_VALIDATING_DIRECTORY, t);
		}
	}

	static File check(File newFile, File... otherFiles) {
		for (File otherFile : otherFiles) {
			if (otherFile == null) {
				continue;
			}
			if (newFile.getName().equals(otherFile.getName())) {
				throw new EnforcerException("file name '" + newFile.getName() + "' conflicts with other file '" + otherFile.getName() + "'", Errors.NAME_CONFLICTS_WITH_OTHER_FILE);
			}
		}
		return newFile;
	}
}
