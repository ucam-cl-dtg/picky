package uk.ac.cam.cl.dtg.picky.client.ui;

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

import java.net.URL;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.ResourceBundle;

import org.reactfx.EventSource;
import org.reactfx.EventStream;

import de.ecclesia.kipeto.common.util.FileSizeFormatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

public class TaskItemPresenter implements Initializable {

	@FXML Label task;
	@FXML ProgressBar progress;
	@FXML Label message;
	@FXML ProgressIndicator indicator;

	private String title;
	private int total;
	private long totalSize;

	private NumberFormat format = NumberFormat.getInstance();

	private EventSource<Event> events = new EventSource<>();
	private EventStream<Event> eventStream = events.reduceSuccessions((a, b) -> b, Duration.ofMillis(500));

	private class Event {

		final String msg;
		final int tasksDone;
		final long bytesDone;

		public Event(String msg, int tasksDone, long bytesDone) {
			this.msg = msg;
			this.tasksDone = tasksDone;
			this.bytesDone = bytesDone;
		}
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		eventStream.subscribe(event -> {
			Platform.runLater(() -> {
				double currentProgress = total == 0 ? 0 : event.tasksDone / (total * 1.0);

				String tasksProgress = format.format(event.tasksDone) + " of " + format.format(total);
				String bytesProgress = "";

				if (totalSize > 0 && event.bytesDone > 0) {
					bytesProgress = " (" + FileSizeFormatter.formateBytes(event.bytesDone, 2) + " of " + FileSizeFormatter.formateBytes(totalSize, 2)
							+ ")";
				} else if (totalSize > 0) {
					bytesProgress = " (" + FileSizeFormatter.formateBytes(totalSize, 2) + ")";
				}

				String taskText = title + ": " + tasksProgress + bytesProgress;

				task.setText(taskText);
				progress.setProgress(currentProgress);
				indicator.setProgress(currentProgress);

				if (event.msg != null || event.tasksDone == total) this.message.setText(event.msg);
			});
		});

		update(null, 0, 0L);
	}

	public void update(String msg, int tasksDone, long bytesDone) {
		try {
			events.push(new Event(msg, tasksDone, bytesDone));
		} catch (Exception e) {
			// reactfx apparently does force an IllegalAccumulation for some
			// reasons sometimes?
		}
	}
}
