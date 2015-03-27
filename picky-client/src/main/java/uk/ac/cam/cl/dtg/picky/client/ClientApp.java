package uk.ac.cam.cl.dtg.picky.client;

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

import java.util.Properties;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import uk.ac.cam.cl.dtg.picky.client.ui.ClientView;
import uk.ac.cam.cl.dtg.picky.util.BuildProperties;

public class ClientApp extends Application {

	private static ClientApp INSTANCE;

	public static void main(String[] args) {
		launch(args);
	}

	public static ClientApp getInstance() {
		return INSTANCE;
	}

	public void start(Stage stage) {
		INSTANCE = this;

		System.setProperty("prism.lcdtext", "false");
		System.setProperty("prism.text", "t2k");

		Properties buildProperties = BuildProperties.readBuildProperties();
		String version = buildProperties.getProperty("version");

		ClientView clientView = new ClientView();

		stage.setTitle("Picky Client " + version);
		stage.setScene(new Scene(clientView.getView()));
		stage.getIcons().add(new Image("/icon/icon16x16.png"));
		stage.getIcons().add(new Image("/icon/icon32x32.png"));
		stage.getIcons().add(new Image("/icon/icon48x48.png"));
		stage.getIcons().add(new Image("/icon/icon64x64.png"));
		stage.getIcons().add(new Image("/icon/icon128x128.png"));
		stage.getIcons().add(new Image("/icon/icon256x256.png"));
		stage.getIcons().add(new Image("/icon/icon512x512.png"));

		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();

		stage.setX(bounds.getMinX());
		stage.setY(bounds.getMinY());
		stage.setWidth(bounds.getWidth());
		stage.setHeight(bounds.getHeight());
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				clientView.persistSettings();
			}
		});

		stage.show();
	}
}
