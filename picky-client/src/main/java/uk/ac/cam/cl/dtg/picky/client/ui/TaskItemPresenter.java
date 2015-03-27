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

public class TaskItemPresenter implements Initializable {

	@FXML Label task;
	@FXML ProgressBar progress;
	@FXML Label message;
	@FXML ProgressIndicator indicator;

	private String title;
	private int total;

	private NumberFormat format = NumberFormat.getInstance();

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		update(null, 0);
	}

	public void update(String msg, int current) {
		Platform.runLater(() -> {
			double currentProgress = total == 0 ? 0 : current / (total * 1.0);

			task.setText(title + " (" + format.format(current) + " of " + format.format(total) + ")");
			progress.setProgress(currentProgress);
			indicator.setProgress(currentProgress);

			if (msg != null || current == total) this.message.setText(msg);
		});
	}
}
