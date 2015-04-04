package uk.ac.cam.cl.dtg.picky.client.binding;

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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AsyncBinding<T> extends ObjectBinding<T> {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private FutureTask<T> future;

	private BooleanProperty busy = new SimpleBooleanProperty();

	private StringProperty errorMessage = new SimpleStringProperty();

	public AsyncBinding(Observable... dependencies) {
		bind(dependencies);
	}

	@Override
	protected T computeValue() {
		if (future != null) {
			if (future.isDone()) {
				try {
					busy.set(false);
					return future.get(1, TimeUnit.SECONDS);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					return null;
				} finally {
					future = null;
				}
			}
		} else {
			errorMessage.set(null);
			future = new FutureTask<T>(this::doCompute) {
				@Override
				protected void done() {
					try {
						if (!isCancelled()) get();
					} catch (Exception e) {
						Platform.runLater(() -> {
							errorMessage.set(e.getMessage());
							LOG.error(e.getMessage(), e);
						});
					}
				}
			};
			busy.set(true);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(future);
			executor.execute(() -> Platform.runLater(() -> invalidate()));
			executor.shutdown();
		}

		return null;
	}

	public StringProperty errorMessageProperty() {
		return errorMessage;
	}

	protected abstract T doCompute() throws Exception;

	public BooleanProperty busy() {
		return busy;
	}

}
