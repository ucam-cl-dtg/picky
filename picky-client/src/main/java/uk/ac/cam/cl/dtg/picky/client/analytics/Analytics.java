package uk.ac.cam.cl.dtg.picky.client.analytics;

/*
 * #%L
 * Picky
 * %%
 * Copyright (C) 2015 - 2016 Daniel Hintze <dh526@cl.cam.ac.uk>
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

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.picky.client.ui.ClientModel;
import uk.ac.cam.cl.dtg.picky.dataset.Dataset;
import uk.ac.cam.cl.dtg.picky.dataset.FileEntry;
import uk.ac.cam.cl.dtg.picky.planner.Plan;

import com.google.common.base.Strings;

import de.ecclesia.kipeto.repository.CachedReadingStrategy;

public class Analytics {

	private static final String URL = "analytics";

	public static final String KEY_ANALYTICS_OCCASION = "analytics.occasion";

	public static final String KEY_DATASET_ID = "dataset.id";
	public static final String KEY_DATASET_DESCRIPTION = "dataset.description";
	public static final String KEY_DATASET_NUMBER_OF_CHUNKS = "dataset.numberOfChunks";
	public static final String KEY_DATASET_NUMBER_OF_FILES = "dataset.numberOfFiles";
	public static final String KEY_DATASET_AVG_NUMBER_OF_BLOCKS = "dataset.avgNumberOfBlocks";

	public static final String KEY_FILE_FILTER = "file.filter";
	public static final String KEY_FILE_MATCHES = "file.matches";

	public static final String KEY_ENTRY_SELECTION = "entry.selection.";

	private static final String KEY_PLAN_DELETE_DIR_ACTIONS = "plan.deleteDirActions";
	private static final String KEY_PLAN_DELETE_FILE_ACTIONS = "plan.deleteFileActions";
	private static final String KEY_PLAN_INSTALL_FILE_ACTIONS = "plan.installFileActions";
	private static final String KEY_PLAN_MAKE_DIR_ACTIONS = "plan.makeDirActions";
	private static final String KEY_PLAN_UPDATE_FILE_ACTIONS = "plan.updateFileActions";
	private static final String KEY_PLAN_CHUNKS_TO_DOWNLOAD = "plan.chunksToDownload";
	private static final String KEY_PLAN_BYTES_TO_DOWNLOAD = "plan.bytesToDownload";

	private static final String KEY_REPOSITORY_BYTES_DOWNLOADED = "repository.bytesDownloaded";

	private static Logger LOG = LoggerFactory.getLogger(Analytics.class);

	public static void sendAnalytics(ClientModel clientModel, String occasion) {

		Properties properties = new Properties();

		properties.put(KEY_ANALYTICS_OCCASION, occasion);

		fillProperties(properties, clientModel.getDatasetBinding().get());
		fillProperties(properties, clientModel.getFilterBinding().get());
		fillProperties(properties, clientModel.getFileSelectionBinding().get());
		fillProperties(properties, clientModel.getPlanBinding().get());
		fillProperties(properties, clientModel.getRepositoryBinding().get());
		fillProperties(properties, clientModel.getEntrySelection());

		String url = clientModel.getServerURL().get();

		new Thread(() -> upload(properties, url)).start();
	}

	private static void upload(Properties properties, String datasetURL) {
		if (datasetURL == null) return;
		URL url;

		try {
			url = new URL(new URL(datasetURL), URL);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return;
		}

		try {
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

			HttpPost post = new HttpPost(url.toURI());
			post.setHeader("Content-Type", "text/plain; charset=utf-8");

			StringWriter stringWriter = new StringWriter();
			properties.store(stringWriter, "");

			HttpEntity entity = new StringEntity(stringWriter.toString());
			post.setEntity(entity);

			HttpResponse response = client.execute(post);

			LOG.info("Uploading analytics: " + response.getStatusLine());
		} catch (IOException | URISyntaxException e) {
			LOG.error(e.getMessage());
		}
	}

	private static void fillProperties(Properties properties, ObservableList<TreeItem<String>> entrySelection) {
		if (entrySelection == null) return;

		entrySelection.forEach(e -> {
			properties.put(KEY_ENTRY_SELECTION + e.getValue(), "selected");
		});
	}

	private static void fillProperties(Properties properties, CachedReadingStrategy cachedReadingStrategy) {
		if (cachedReadingStrategy == null) return;

		properties.put(KEY_REPOSITORY_BYTES_DOWNLOADED, "" + cachedReadingStrategy.getRepository().bytesRead());
	}

	private static void fillProperties(Properties properties, Plan plan) {
		if (plan == null) return;

		long bytesToDownload = plan.getChunksToDownload().stream().mapToLong(c -> c.getLengthCompressed()).sum();

		properties.put(KEY_PLAN_DELETE_DIR_ACTIONS, "" + plan.getDeleteDirActions().size());
		properties.put(KEY_PLAN_DELETE_FILE_ACTIONS, "" + plan.getDeleteFileActions().size());
		properties.put(KEY_PLAN_INSTALL_FILE_ACTIONS, "" + plan.getInstallFileActions().size());
		properties.put(KEY_PLAN_MAKE_DIR_ACTIONS, "" + plan.getMakeDirActions().size());
		properties.put(KEY_PLAN_UPDATE_FILE_ACTIONS, "" + plan.getUpdateFileActions().size());

		properties.put(KEY_PLAN_CHUNKS_TO_DOWNLOAD, "" + plan.getChunksToDownload().size());
		properties.put(KEY_PLAN_BYTES_TO_DOWNLOAD, "" + bytesToDownload);
	}

	private static void fillProperties(Properties properties, String filter) {
		properties.put(KEY_FILE_FILTER, Strings.nullToEmpty(filter));
	}

	private static void fillProperties(Properties properties, List<FileEntry> fileEntries) {
		if (fileEntries != null) properties.put(KEY_FILE_MATCHES, "" + fileEntries.size());
	}

	private static void fillProperties(Properties properties, Dataset dataset) {
		if (dataset == null) return;

		List<FileEntry> files = dataset.getFiles();

		long numberOfChunks = files.stream()
				.flatMap(f -> f.getBlocks().stream())
				.flatMap(b -> b.getChunks().stream())
				.count();

		double avgNumberofBlocks = files.stream()
				.mapToLong(f -> f.getBlocks().size())
				.average()
				.getAsDouble();

		properties.put(KEY_DATASET_DESCRIPTION, Strings.nullToEmpty(dataset.getDescription()));
		properties.put(KEY_DATASET_ID, dataset.getId());
		properties.put(KEY_DATASET_NUMBER_OF_FILES, "" + files.size());
		properties.put(KEY_DATASET_NUMBER_OF_CHUNKS, "" + numberOfChunks);
		properties.put(KEY_DATASET_AVG_NUMBER_OF_BLOCKS, "" + avgNumberofBlocks);
	}
}
