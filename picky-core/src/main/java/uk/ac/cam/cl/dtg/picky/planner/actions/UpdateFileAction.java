package uk.ac.cam.cl.dtg.picky.planner.actions;

/*
 * #%L
 * Picky
 * %%
 * Copyright (C) 2015 Daniel Hintze <dh526@cl.cam.ac.uk>
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.util.List;

import uk.ac.cam.cl.dtg.picky.dataset.Block;

public class UpdateFileAction implements IAction {

	private File file;
	private long fileSize;
	private long lastModified;
	private List<Block> blocks;
	private byte[] fileHeader;

	public UpdateFileAction(List<Block> blocks, long lastModified, long fileSize, File file, byte[] fileHeader) {
		this.blocks = blocks;
		this.lastModified = lastModified;
		this.fileSize = fileSize;
		this.file = file;
		this.fileHeader = fileHeader;
	}

	public File getFile() {
		return file;
	}

	public long getLastModified() {
		return lastModified;
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	public long getFileSize() {
		return fileSize;
	}

	public byte[] getFileHeader() {
		return fileHeader;
	}

}
