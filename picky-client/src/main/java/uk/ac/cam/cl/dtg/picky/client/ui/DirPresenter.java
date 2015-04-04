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

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;

import uk.ac.cam.cl.dtg.picky.client.validation.ValidatorFactory;

public class DirPresenter implements Initializable {

	@FXML
	Label label;
	@FXML
	TextField text;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ValidationSupport validationSupport = new ValidationSupport();
		validationSupport.registerValidator(text, false, ValidatorFactory.createIsDirectoryValidator(Severity.WARNING));
	}

	public void setLabel(String label) {
		this.label.setText(label);
	}

	@FXML
	public void chooseFile() {
		Stage stage = (Stage) label.getScene().getWindow();

		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select " + label.getText());

		File initialDirectory = new File(text.getText());
		if (initialDirectory.isDirectory()) {
			directoryChooser.setInitialDirectory(initialDirectory);
		}

		File selectedDirectory = directoryChooser.showDialog(stage);
		if (selectedDirectory != null) {
			text.setText(selectedDirectory.getAbsolutePath());
		}
	}

	public TextField getText() {
		return text;
	}
}
