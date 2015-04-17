package uk.ac.cam.cl.dtg.picky.engine;

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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.dataset.Block;
import uk.ac.cam.cl.dtg.picky.dataset.Chunk;
import uk.ac.cam.cl.dtg.picky.engine.ProgressEvent.Action;
import uk.ac.cam.cl.dtg.picky.planner.Plan;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import de.ecclesia.kipeto.repository.Blob;
import de.ecclesia.kipeto.repository.CachedReadingStrategy;
import de.ecclesia.kipeto.repository.ReadingRepository;

public class Engine {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private List<ProgressListener> listeners = new ArrayList<ProgressListener>();

	private Plan plan;

	private PausableExecutor sequentialExecutor;
	private PausableExecutor parallelExecutor;

	private CachedReadingStrategy readingStrategy;
	private ReadingRepository repository;

	private long bytesWritten;

	private Map<Action, AtomicInteger> progressCounter = new ImmutableMap.Builder<Action, AtomicInteger>()
			.put(Action.DELETE_DIR, new AtomicInteger())
			.put(Action.DELETE_FILE, new AtomicInteger())
			.put(Action.DOWNLOAD_CHUNK, new AtomicInteger())
			.put(Action.INSTALL_FILE, new AtomicInteger())
			.put(Action.MAKE_DIR, new AtomicInteger())
			.put(Action.UPDATE_FILE, new AtomicInteger())
			.build();

	private Map<Action, Integer> totalActionNumber;
	private Set<String> chunksToDownload;

	public Engine(CachedReadingStrategy readingStrategy, Plan plan) {
		this.readingStrategy = readingStrategy;
		this.repository = new ReadingRepository(readingStrategy);
		this.plan = plan;
		this.chunksToDownload = Sets.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		this.chunksToDownload.addAll(plan.getChunksToDownload().stream().map(Chunk::getBlobId).collect(Collectors.toList()));

		int availableProcessors = Runtime.getRuntime().availableProcessors();

		this.sequentialExecutor = new PausableExecutor(1, new WorkerThreadFactory("engine_sequential_worker"));
		this.parallelExecutor = new PausableExecutor(availableProcessors, new WorkerThreadFactory("engine_parallel_worker"));

		this.totalActionNumber = new ImmutableMap.Builder<Action, Integer>()
				.put(Action.DELETE_DIR, plan.getDeleteDirActions().size())
				.put(Action.DELETE_FILE, plan.getDeleteFileActions().size())
				.put(Action.DOWNLOAD_CHUNK, plan.getChunksToDownload().size())
				.put(Action.INSTALL_FILE, plan.getInstallFileActions().size())
				.put(Action.MAKE_DIR, plan.getMakeDirActions().size())
				.put(Action.UPDATE_FILE, plan.getUpdateFileActions().size())
				.build();
	}

	public void execute() {
		parallelExecutor.pause();

		plan.getDeleteFileActions().stream().forEach(
				a -> sequentialExecutor.submit(wrapAction(Action.DELETE_FILE, "Deleting file " + a.getFile(), () -> {
					Preconditions.checkArgument(a.getFile().delete(), "Could not delete " + a.getFile());
				})));

		plan.getDeleteDirActions().stream().forEach(
				a -> sequentialExecutor.submit(wrapAction(Action.DELETE_DIR, "Deleting dir " + a.getDir(), () -> {
					Preconditions.checkArgument(a.getDir().delete(), "Could not delete " + a.getDir());
				})));

		plan.getMakeDirActions().stream().forEach(
				a -> sequentialExecutor.submit(wrapAction(Action.MAKE_DIR, "Creating dir " + a.getDir(), () -> {
					Preconditions.checkArgument(a.getDir().mkdir(), "Could not create " + a.getDir());
				})));

		// start parallel execution
		sequentialExecutor.submit(() -> {
			parallelExecutor.resume();
		});

		plan.getInstallFileActions().stream().forEach(
				a -> parallelExecutor.submit(wrapAction(Action.INSTALL_FILE, "Installing file " + a.getFile(), () -> {
					installFile(a.getFile(), a.getLastModified(), a.getBlocks(), a.getFileHeader());
				})));

		plan.getUpdateFileActions().stream().forEach(
				a -> parallelExecutor.submit(wrapAction(Action.UPDATE_FILE, "Updating file " + a.getFile(), () -> {
					Preconditions.checkArgument(a.getFile().delete(), "Could not delete " + a.getFile());
					installFile(a.getFile(), a.getLastModified(), a.getBlocks(), a.getFileHeader());
				})));

		sequentialExecutor.shutdown();
		parallelExecutor.shutdown();
	}

	public void resume() {
		parallelExecutor.resume();
		parallelExecutor.resume();
	}

	public void pause() {
		parallelExecutor.pause();
		parallelExecutor.pause();
	}

	public void stop() {
		parallelExecutor.cancelRemainingTasks();
		sequentialExecutor.cancelRemainingTasks();
	}

	public void addListener(ProgressListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ProgressListener listener) {
		listeners.remove(listener);
	}

	public long getBytesWrittenToCache() {
		return readingStrategy.getCache().bytesWritten();
	}

	public long getBytesReadFromCache() {
		return readingStrategy.getCache().bytesRead();
	}

	public long getBytesDownloaded() {
		return readingStrategy.getRepository().bytesRead();
	}

	public long getBytesWrittenToTarget() {
		return bytesWritten;
	}

	private void installFile(File target, long lastModified, List<Block> blocks, byte[] fileHeader) {
		if (blocks.isEmpty()) return;

		try {
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target));

			List<BlockInputSource> sources = blocks.stream()
					.map(block -> new BlockInputSource(new RepositoryCallback(), block))
					.sorted().collect(Collectors.toList());

			// Write FileHeader
			outputStream.write(fileHeader);
			bytesWritten += fileHeader.length;

			// the next source to read from will always be first in the list
			while (sources.get(0).hasNext()) {
				byte[] data = sources.get(0).read();
				bytesWritten += data.length;
				outputStream.write(data);

				// Make sure we read from right block next time
				Collections.sort(sources);
			}

			outputStream.close();
			target.setLastModified(lastModified);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public interface IRepositoryCallback {
		public Blob getOrDownload(String id) throws IOException;
	}

	private class RepositoryCallback implements IRepositoryCallback {

		public Blob getOrDownload(String id) throws IOException {
			Blob blob;
			if (chunksToDownload.contains(id)) {
				Integer total = totalActionNumber.get(Action.DOWNLOAD_CHUNK);

				fireOnActionStart(new ProgressEvent(Action.DOWNLOAD_CHUNK, "Downloading " + id, total - chunksToDownload.size(), total));
				blob = repository.retrieve(id, Blob.class);
				chunksToDownload.remove(id);
				fireOnActionFinished(new ProgressEvent(Action.DOWNLOAD_CHUNK, null, total - chunksToDownload.size(), total));
			} else {
				blob = repository.retrieve(id, Blob.class);
			}

			return blob;
		}
	}

	private Callable<Void> wrapAction(Action action, String msg, Runnable task) {
		return new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				try {
					log.info(msg);

					Integer total = totalActionNumber.get(action);
					AtomicInteger current = progressCounter.get(action);

					fireOnActionStart(new ProgressEvent(action, msg, current.get(), total));
					task.run();
					fireOnActionFinished(new ProgressEvent(action, null, current.incrementAndGet(), total));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	private void fireOnActionStart(ProgressEvent event) {
		listeners.stream().forEach(s -> s.onActionStart(event));
	}

	private void fireOnActionFinished(ProgressEvent event) {
		listeners.stream().forEach(s -> s.onActionFinished(event));
	}

}
