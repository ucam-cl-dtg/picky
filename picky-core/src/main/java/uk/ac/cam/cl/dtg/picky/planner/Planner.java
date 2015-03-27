package uk.ac.cam.cl.dtg.picky.planner;

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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.dataset.Block;
import uk.ac.cam.cl.dtg.picky.dataset.Chunk;
import uk.ac.cam.cl.dtg.picky.dataset.FileEntry;
import uk.ac.cam.cl.dtg.picky.planner.actions.DeleteDirAction;
import uk.ac.cam.cl.dtg.picky.planner.actions.DeleteFileAction;
import uk.ac.cam.cl.dtg.picky.planner.actions.IAction;
import uk.ac.cam.cl.dtg.picky.planner.actions.InstallFileAction;
import uk.ac.cam.cl.dtg.picky.planner.actions.MakeDirAction;
import uk.ac.cam.cl.dtg.picky.planner.actions.UpdateFileAction;
import uk.ac.cam.cl.dtg.picky.util.IO;

import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

import de.ecclesia.kipeto.repository.ReadingRepository;

public class Planner {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private ReadingRepository cache;
	private File rootTarget;

	private List<IAction> actions;
	private Set<Chunk> chunksToDownload;

	private List<File> preExistingFilesToRemove;

	private List<FileEntry> fileSelection;
	private ListMultimap<String, String> entrySelection;

	public Planner(
			List<FileEntry> fileSelection,
			ListMultimap<String, String> entrySelection,
			ReadingRepository cache,
			File rootTarget) {

		this.fileSelection = fileSelection;
		this.entrySelection = entrySelection;
		this.cache = cache;
		this.rootTarget = rootTarget;
		this.actions = new LinkedList<>();
		this.chunksToDownload = new HashSet<>();
	}

	public Plan plan() {
		if (!rootTarget.exists()) {
			actions.add(new MakeDirAction(rootTarget));
		}

		preExistingFilesToRemove = StreamSupport
				.stream(Files.fileTreeTraverser().preOrderTraversal(rootTarget).spliterator(), true)
				.filter(f -> !f.equals(rootTarget))
				.sorted((File o1, File o2) -> o2.getAbsolutePath().compareTo(o1.getAbsolutePath()))
				.collect(Collectors.toList());

		fileSelection.forEach(this::installFile);

		// Remove all alien dirs and files
		preExistingFilesToRemove.stream().filter(File::isFile).forEach(file -> actions.add(new DeleteFileAction(file)));
		preExistingFilesToRemove.stream().filter(File::isDirectory).forEach(dir -> actions.add(new DeleteDirAction(dir)));

		return new Plan(actions, chunksToDownload);
	}

	private void installFile(FileEntry fileEntry) {
		File target = new File(rootTarget, fileEntry.getAttributes().get(FileEntry.PATH_KEY).toString());

		List<Block> selectedBlocks = fileEntry.getBlocks()
				.stream()
				.filter(block -> block.matches(entrySelection))
				.collect(Collectors.toList());

		if (selectedBlocks.isEmpty()) return;

		long rLM = fileEntry.getLastModified();
		long rFL = selectedBlocks.stream().mapToLong(Block::getLengthUncompressed).sum();

		long tLM = target.lastModified();
		long tFL = target.length();

		// file does not exist, is directory or is changed
		// FIXME: Will fail with different but equally sized blocks
		if (!target.exists() || target.isDirectory() || tLM != rLM || tFL != rFL) {
			if (target.isDirectory()) {
				actions.add(new DeleteDirAction(target));
			}

			if (target.isFile()) {
				log.debug(String.format("%s differs from dataset. Length is %d, should be %d, lastModified is %d, should be %d",
						target, tFL, rFL, tLM, rLM));
				actions.add(new UpdateFileAction(selectedBlocks, rLM, rFL, target, fileEntry.getFileHeader()));
			} else {
				actions.add(new InstallFileAction(selectedBlocks, rLM, rFL, target, fileEntry.getFileHeader()));
			}

			chunksToDownload.addAll(selectedBlocks
					.stream()
					.flatMap(block -> block.getChunks().stream())
					.filter(chunk -> IO.wrapBoolean(() -> !cache.contains(chunk.getBlobId())))
					.collect(Collectors.toList()));
		}

		removeParentsFromList(preExistingFilesToRemove, target);
	}

	private void removeParentsFromList(List<File> list, File file) {
		list.remove(file);
		if (file.getParentFile() != null) {
			removeParentsFromList(list, file.getParentFile());
		}
	}

}
