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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Monitor;

// Taken from http://stackoverflow.com/questions/9748710/how-to-pause-resume-all-threads-in-an-executorservice-in-java
public class PausableExecutor extends ScheduledThreadPoolExecutor {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private boolean isPaused;

	private List<WeakReference<Future<?>>> pendingTasks = Collections.synchronizedList(new ArrayList<WeakReference<Future<?>>>());

	private final Monitor monitor = new Monitor();
	private final Monitor.Guard paused = new Monitor.Guard(monitor) {
		@Override
		public boolean isSatisfied() {
			return isPaused;
		}
	};

	private final Monitor.Guard notPaused = new Monitor.Guard(monitor) {
		@Override
		public boolean isSatisfied() {
			return !isPaused;
		}
	};

	public Monitor.Guard getNotPaused() {
		return notPaused;
	}

	public PausableExecutor(int corePoolSize, ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		monitor.enterWhenUninterruptibly(notPaused);
		try {
			monitor.waitForUninterruptibly(notPaused);
		} finally {
			monitor.leave();
		}
	}

	@Override
	public <T> Future<T> submit(final Callable<T> task) {
		Callable<T> wrappedTask = new Callable<T>() {
			@Override
			public T call() throws Exception {
				try {
					T result = task.call();
					return result;
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e.getMessage(), e);
					throw e;
				}
			}
		};

		Future<T> future = super.submit(wrappedTask);

		pendingTasks.add(new WeakReference<Future<?>>(future));

		return future;
	}

	public void cancelRemainingTasks() {
		shutdown();
		pendingTasks.stream().filter(w -> w.get() != null).map(WeakReference::get).forEach((f) -> {
			if (f != null) {
				f.cancel(false);
			}
		});
	}

	public void pause() {
		monitor.enterIf(notPaused);
		try {
			isPaused = true;
		} finally {
			monitor.leave();
		}
	}

	public void resume() {
		monitor.enterIf(paused);
		try {
			isPaused = false;
		} finally {
			monitor.leave();
		}
	}
}
