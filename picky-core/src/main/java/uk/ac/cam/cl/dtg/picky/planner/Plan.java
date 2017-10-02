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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import uk.ac.cam.cl.dtg.picky.dataset.Chunk;
import uk.ac.cam.cl.dtg.picky.planner.actions.DeleteDirAction;
import uk.ac.cam.cl.dtg.picky.planner.actions.DeleteFileAction;
import uk.ac.cam.cl.dtg.picky.planner.actions.IAction;
import uk.ac.cam.cl.dtg.picky.planner.actions.InstallFileAction;
import uk.ac.cam.cl.dtg.picky.planner.actions.MakeDirAction;
import uk.ac.cam.cl.dtg.picky.planner.actions.UpdateFileAction;

public class Plan {

	private final Set<Chunk> chunksToDownload;

	private final List<MakeDirAction> makeDirActions;
	private final List<DeleteDirAction> deleteDirActions;
	private final List<UpdateFileAction> updateFileActions;
	private final List<DeleteFileAction> deleteFileActions;
	private final List<InstallFileAction> installFileActions;

	private long subsetSize;

	public Plan(List<IAction> actions, Set<Chunk> chunksToDownload, long subsetSize) {
		this.chunksToDownload = chunksToDownload;
		this.subsetSize = subsetSize;

		this.makeDirActions = filterActions(actions, MakeDirAction.class);
		this.deleteDirActions = filterActions(actions, DeleteDirAction.class);
		this.updateFileActions = filterActions(actions, UpdateFileAction.class);
		this.deleteFileActions = filterActions(actions, DeleteFileAction.class);
		this.installFileActions = filterActions(actions, InstallFileAction.class);
	}

	public List<MakeDirAction> getMakeDirActions() {
		return makeDirActions;
	}

	public List<DeleteDirAction> getDeleteDirActions() {
		return deleteDirActions;
	}

	public List<UpdateFileAction> getUpdateFileActions() {
		return updateFileActions;
	}

	public List<DeleteFileAction> getDeleteFileActions() {
		return deleteFileActions;
	}

	public List<InstallFileAction> getInstallFileActions() {
		return installFileActions;
	}

	public Set<Chunk> getChunksToDownload() {
		return chunksToDownload;
	}

	public long getSubsetSize() {
		return subsetSize;
	}

	@SuppressWarnings("unchecked")
	private static <T> List<T> filterActions(List<IAction> actions, final Class<T> type) {
		return actions.stream().filter(type::isInstance).map(a -> (T) a).collect(Collectors.toList());
	}
}
