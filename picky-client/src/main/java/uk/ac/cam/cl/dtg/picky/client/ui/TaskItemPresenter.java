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
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import de.ecclesia.kipeto.common.util.FileSizeFormatter;

public class TaskItemPresenter implements Initializable {

	@FXML Label task;
	@FXML ProgressBar progress;
	@FXML Label message;
	@FXML ProgressIndicator indicator;

	private String title;
	private int total;
	private long totalSize;

	private NumberFormat format = NumberFormat.getInstance();

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
		update(null, 0, 0L);
	}

	public void update(String msg, int tasksDone, long bytesDone) {
		Platform.runLater(() -> {
			double currentProgress = total == 0 ? 0 : tasksDone / (total * 1.0);

			String tasksProgress = format.format(tasksDone) + " of " + format.format(total);
			String bytesProgress = "";

			if (totalSize > 0 && bytesDone > 0) {
				bytesProgress = " (" + FileSizeFormatter.formateBytes(bytesDone, 2) + " of " + FileSizeFormatter.formateBytes(totalSize, 2) + ")";
			} else if (totalSize > 0) {
				bytesProgress = " (" + FileSizeFormatter.formateBytes(totalSize, 2) + ")";
			}

			String taskText = title + ": " + tasksProgress + bytesProgress;

			task.setText(taskText);
			progress.setProgress(currentProgress);
			indicator.setProgress(currentProgress);

			if (msg != null || tasksDone == total) this.message.setText(msg);
		});
	}
}
