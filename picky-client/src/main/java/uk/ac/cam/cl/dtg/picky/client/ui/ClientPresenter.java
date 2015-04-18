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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.control.CheckTreeView;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.decoration.GraphicValidationDecoration;
import org.reactfx.EventStreams;

import uk.ac.cam.cl.dtg.picky.client.ClientApp;
import uk.ac.cam.cl.dtg.picky.client.binding.BusyGraphicsBinding;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;
import uk.ac.cam.cl.dtg.picky.engine.Engine;
import uk.ac.cam.cl.dtg.picky.engine.ProgressEvent;
import uk.ac.cam.cl.dtg.picky.engine.ProgressEvent.Action;
import uk.ac.cam.cl.dtg.picky.engine.ProgressListener;
import uk.ac.cam.cl.dtg.picky.planner.Plan;
import de.ecclesia.kipeto.common.util.FileSizeFormatter;
import de.ecclesia.kipeto.repository.CachedReadingStrategy;
import de.ecclesia.kipeto.repository.ReadingRepositoryStrategy;

public class ClientPresenter implements Initializable {

	private static final File SETTINGS = new File("settings.properties");

	private static final String SETTINGS_SERVER = "server";
	private static final String SETTINGS_DATASET = "dataset";
	private static final String SETTINGS_TARGET = "targetDir";
	private static final String SETTINGS_CACHE = "cacheDir";
	private static final String SETTINGS_TEMP = "tempDir";
	private static final String SETTINGS_FILE_FILTER = "fileFilter";
	private static final String SETTINGS_ENTRY_SELECTION = "entrySelection";

	private static final long DOWNLOAD_CHART_TICKS = TimeUnit.HOURS.toSeconds(1);

	@FXML private VBox fileSelectionVBox;
	@FXML private VBox entrySelectionVBox;
	@FXML private VBox tasksVBox;
	@FXML private TitledPane fileSelectionTitledPane;
	@FXML private TitledPane entrySelectionTitledPane;
	@FXML private VBox settings;
	@FXML private VBox areaChartBox;
	@FXML private Label datasetLabel;
	@FXML private Label hashLabel;
	@FXML private Label contentLabel;
	@FXML private Hyperlink urlLink;
	@FXML private ImageView logoImage;
	@FXML private TextField fileFilter;
	@FXML private Label fileFilterError;
	@FXML private TilePane fileFilterContext;
	@FXML private ImageView fileFilterTick;
	@FXML private TitledPane datasetTitledPane;
	@FXML private TitledPane changesTitledPane;
	@FXML private TextField serverText;
	@FXML private TextField referenceText;

	private DirView targetDir;
	private DirView cacheDir;
	private DirView tempDir;

	private CheckTreeView<String> entrySelectionTreeView;
	private AreaChart<Number, Number> areaChart;

	private Engine engine;
	private Map<Action, TaskItemView> tasks = new HashMap<>();

	private ClientModel model;

	private XYChart.Series<Number, Number> byteDownloadSeries;
	// private XYChart.Series<Number, Number> byteReadSeries;
	// private XYChart.Series<Number, Number> byteWriteSeries;

	private NumberAxis xAxis;
	private NumberAxis yAxis;

	private DateFormat timeFormat;
	private Button applyButton;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		entrySelectionTreeView = new CheckTreeView<String>(new CheckBoxTreeItem<String>());
		entrySelectionTreeView.showRootProperty().set(false);
		entrySelectionVBox.getChildren().add(entrySelectionTreeView);

		targetDir = new DirView("Target");
		cacheDir = new DirView("Cache");
		tempDir = new DirView("Temp");

		settings.getChildren().addAll(targetDir.getView(), cacheDir.getView(), tempDir.getView());

		restoreSettings();

		model = new ClientModel(serverText.textProperty(), referenceText.textProperty(), targetDir.getText().textProperty(), cacheDir
				.getText().textProperty(), tempDir.getText().textProperty(), fileFilter.textProperty(), entrySelectionTreeView
				.getCheckModel().getCheckedItems());

		fileFilterError.textProperty().bind(model.getFilterErrorBinding());
		fileFilterTick.visibleProperty().bind(Bindings.isNotNull(model.getFilterBinding()));
		fileSelectionTitledPane.textProperty().bind(model.getFileSelectionLabel());
		entrySelectionTitledPane.textProperty().bind(model.getEntrySelectionLabel());

		BusyGraphicsBinding.install(fileSelectionTitledPane, model.getDatasetBinding());
		BusyGraphicsBinding.install(datasetTitledPane, model.getDatasetBinding());
		BusyGraphicsBinding.install(changesTitledPane, model.getPlanBinding());
		BusyGraphicsBinding.install(entrySelectionTitledPane, model.getFileSelectionBinding());

		urlLink.setOnAction((e) -> {
			ClientApp.getInstance().getHostServices().showDocument(urlLink.getText());
		});

		model.getPlanBinding().addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> {
			stopApplyingChanges();
		});

		model.getDatasetBinding().addListener(new ChangeListener<Dataset>() {

			@Override
			public void changed(ObservableValue<? extends Dataset> observable, Dataset oldValue, Dataset newValue) {
				if (newValue != null) {
					Image image = new Image(new ByteArrayInputStream(newValue.getIcon()));
					logoImage.setImage(image);
					datasetLabel.setText(newValue.getDescription());
					hashLabel.setText(newValue.getId());
					contentLabel.setText(newValue.getContentDescription());
					urlLink.setText(newValue.getUrl());
				} else {
					logoImage.setImage(null);
					datasetLabel.setText("");
					hashLabel.setText("");
					contentLabel.setText("");
					urlLink.setText("");
				}
			}
		});

		Bindings.bindContent(fileFilterContext.getChildren(), model.getFileContextLabels());

		model.getEntryTreeBinding().addListener(new ChangeListener<CheckBoxTreeItem<String>>() {

			private String entrySelection;

			@Override
			public void changed(ObservableValue<? extends CheckBoxTreeItem<String>> observable, CheckBoxTreeItem<String> oldValue,
					CheckBoxTreeItem<String> newValue) {
				if (newValue == null) {
					entrySelection = CheckTreeViewPersistenceUtil.persist(entrySelectionTreeView);
					entrySelectionTreeView.getRoot().getChildren().clear();
					entrySelectionTreeView.getCheckModel().getCheckedItems().clear();
				} else {
					entrySelectionTreeView.getRoot().getChildren().addAll(newValue.getChildren());
					CheckTreeViewPersistenceUtil.restore(entrySelectionTreeView, entrySelection);
				}
			}
		});

		model.getPlanBinding().addListener(new UpdateTasksListener());

		byteDownloadSeries = new AreaChart.Series<Number, Number>();
		byteDownloadSeries.setName("Download");
		byteDownloadSeries.getData().add(new Data<Number, Number>(getCurrentTS(), 0L));

		// byteReadSeries = new AreaChart.Series<Number, Number>();
		// byteReadSeries.setName("Read");
		// byteReadSeries.getData().add(
		// new Data<Number, Number>(getCurrentTS(), 0L));
		//
		// byteWriteSeries = new AreaChart.Series<Number, Number>();
		// byteWriteSeries.setName("Write");
		// byteWriteSeries.getData().add(
		// new Data<Number, Number>(getCurrentTS(), 0L));

		timeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT);

		xAxis = new NumberAxis(0, DOWNLOAD_CHART_TICKS, DOWNLOAD_CHART_TICKS / 10);
		xAxis.setForceZeroInRange(false);
		xAxis.setAutoRanging(false);
		xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {
			@Override
			public String toString(Number object) {
				return timeFormat.format(new Date(object.longValue() * 1000));
			}
		});

		yAxis = new NumberAxis();
		yAxis.setAutoRanging(true);
		yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
			@Override
			public String toString(Number object) {
				String label;
				label = FileSizeFormatter.formateBytes(object.longValue(), 0);
				return label;
			}
		});

		areaChart = new AreaChart<Number, Number>(xAxis, yAxis) {
			// Override to remove symbols on each data point
			@Override
			protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
			}
		};
		areaChart.setAnimated(false);
		areaChart.getData().addAll(Arrays.asList( // byteReadSeries,
													// byteWriteSeries,
				byteDownloadSeries));

		areaChartBox.getChildren().add(areaChart);

		EventStreams.ticks(Duration.ofSeconds(1)).subscribe(new AreaChartUpdater());

		GraphicValidationDecoration validationDecoration = new GraphicValidationDecoration();
		validationDecoration.applyRequiredDecoration(serverText);

		model.getDatasetBinding().errorMessageProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
			if (newValue != null) {
				validationDecoration.applyValidationDecoration(ValidationMessage.error(serverText, newValue));
			} else {
				validationDecoration.removeDecorations(serverText);
			}
		});

	}

	private void toggleApplyChanges(ActionEvent event) {
		if (engine == null) {
			startApplyingChanges();
		} else {
			stopApplyingChanges();
		}
	}

	private void startApplyingChanges() {
		if (engine != null) stopApplyingChanges();

		Plan plan = model.getPlanBinding().get();

		engine = new Engine(model.getRepositoryBinding().get(), plan);
		engine.addListener(new EngineProgressListener());
		engine.execute();

		if (applyButton != null) applyButton.setText("Stop");
	}

	private void stopApplyingChanges() {
		if (engine != null) {
			engine.stop();
			engine = null;
		}

		if (applyButton != null) applyButton.setText("Start");
	}

	private long getCurrentTS() {
		return System.currentTimeMillis() / 1000;
	}

	private final class UpdateTasksListener implements ChangeListener<Plan> {

		@Override
		public void changed(ObservableValue<? extends Plan> observable, Plan oldPlan, Plan newPlan) {
			tasks.clear();
			applyButton = null;

			if (newPlan != null) {
				if (!newPlan.getDeleteDirActions().isEmpty()) {
					tasks.put(Action.DELETE_DIR, new TaskItemView("Delete Directories", newPlan.getDeleteDirActions().size()));
				}

				if (!newPlan.getDeleteFileActions().isEmpty()) {
					tasks.put(Action.DELETE_FILE, new TaskItemView("Delete Files", newPlan.getDeleteFileActions().size()));
				}

				if (!newPlan.getMakeDirActions().isEmpty()) {
					tasks.put(Action.MAKE_DIR, new TaskItemView("Make Directories", newPlan.getMakeDirActions().size()));
				}

				if (!newPlan.getChunksToDownload().isEmpty()) {
					tasks.put(Action.DOWNLOAD_CHUNK, new TaskItemView("Download Chunks", newPlan.getChunksToDownload().size()));
				}

				if (!newPlan.getInstallFileActions().isEmpty()) {
					tasks.put(Action.INSTALL_FILE, new TaskItemView("Install Files", newPlan.getInstallFileActions().size()));
				}

				if (!newPlan.getUpdateFileActions().isEmpty()) {
					tasks.put(Action.UPDATE_FILE, new TaskItemView("Update Files", newPlan.getUpdateFileActions().size()));
				}
			}

			tasksVBox.getChildren().setAll(
					tasks.keySet().stream().sorted().map(tasks::get).map(t -> t.getView()).collect(Collectors.toList()));

			if (!tasks.isEmpty()) {
				applyButton = new Button("Start");
				applyButton.setOnAction(ClientPresenter.this::toggleApplyChanges);
				tasksVBox.getChildren().add(applyButton);
			}

		}
	}

	private class AreaChartUpdater implements Consumer<Object> {
		long bytesReadLastTime;

		// long read;
		// long written;

		@Override
		public void accept(Object t) {
			long bytesRead = 0;

			CachedReadingStrategy cachedReadingStrategy = model.getRepositoryBinding().get();
			if (cachedReadingStrategy != null) {
				ReadingRepositoryStrategy repository = cachedReadingStrategy.getRepository();
				bytesRead = repository.bytesRead();
			}

			bytesReadLastTime = updateDiff("Download", byteDownloadSeries, bytesReadLastTime, bytesRead);
			// read = updateDiff("Read", byteReadSeries, read, engine != null ?
			// engine.getBytesReadFromCache() : 0);
			// written = updateDiff("Write", byteWriteSeries, written, engine !=
			// null ? engine.getBytesWrittenToTarget() : 0);

			xAxis.setLowerBound(getCurrentTS() - DOWNLOAD_CHART_TICKS);
			xAxis.setUpperBound(getCurrentTS() - 1);
		}

		private long updateDiff(String label, Series<Number, Number> series, long bytesReadLastTime, long currentRead) {
			long diff = bytesReadLastTime <= currentRead ? currentRead - bytesReadLastTime : currentRead;

			series.setName(label + (diff > 0 ? " (" + FileSizeFormatter.formateBytes(diff, 1) + "/s)" : ""));
			series.getData().add(new Data<>(getCurrentTS(), diff));
			if (series.getData().size() > DOWNLOAD_CHART_TICKS) {
				series.getData().remove(0);
			}

			return currentRead;
		}

	}

	private class EngineProgressListener implements ProgressListener {

		@Override
		public void onActionStart(ProgressEvent event) {
			setProgress(event.getAction(), event.getMsg(), event.getCurrent(), event.getTotal());
		}

		@Override
		public void onActionFinished(ProgressEvent event) {
			setProgress(event.getAction(), event.getMsg(), event.getCurrent(), event.getTotal());
		}

	}

	private void setProgress(Action action, String msg, int current, int total) {
		TaskItemView taskItemView = tasks.get(action);

		if (taskItemView != null) ((TaskItemPresenter) tasks.get(action).getPresenter()).update(msg, current);
	}

	public void restoreSettings() {
		Properties properties = new Properties();

		if (SETTINGS.exists()) try {
			properties.load(new BufferedInputStream(new FileInputStream(SETTINGS)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		serverText.setText(properties.getProperty(SETTINGS_SERVER, ""));
		referenceText.setText(properties.getProperty(SETTINGS_DATASET, ""));
		cacheDir.getText().setText(properties.getProperty(SETTINGS_CACHE, ""));
		targetDir.getText().setText(properties.getProperty(SETTINGS_TARGET, ""));
		tempDir.getText().setText(properties.getProperty(SETTINGS_TEMP, ""));
		fileFilter.setText(properties.getProperty(SETTINGS_FILE_FILTER, ""));

		CheckTreeViewPersistenceUtil.restore(entrySelectionTreeView, properties.getProperty(SETTINGS_ENTRY_SELECTION, ""));
	}

	public void persistSettings() {
		Properties settings = new Properties();

		settings.put(SETTINGS_CACHE, cacheDir.getText().getText());
		settings.put(SETTINGS_DATASET, referenceText.getText());
		settings.put(SETTINGS_SERVER, serverText.getText());
		settings.put(SETTINGS_TARGET, targetDir.getText().getText());
		settings.put(SETTINGS_TEMP, tempDir.getText().getText());
		settings.put(SETTINGS_FILE_FILTER, fileFilter.getText());
		settings.put(SETTINGS_ENTRY_SELECTION, CheckTreeViewPersistenceUtil.persist(entrySelectionTreeView));

		try {
			settings.store(new BufferedOutputStream(new FileOutputStream(SETTINGS)), null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@FXML
	public void close() {
		persistSettings();

		Stage stage = (Stage) contentLabel.getScene().getWindow();
		stage.close();
	}

	@FXML
	public void about() {
		Dialog<Void> dialog = new Dialog<>();
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		dialog.getDialogPane().setContent(new AboutDialogView().getView());
		dialog.showAndWait();
	}

}
