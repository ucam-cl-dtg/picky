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

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import org.reactfx.EventStreams;

import uk.ac.cam.cl.dtg.picky.dataset.FileEntry;
import uk.ac.cam.cl.dtg.picky.planner.Plan;
import uk.ac.cam.cl.dtg.picky.planner.Planner;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.ecclesia.kipeto.repository.ReadingRepository;

public class PlanBinding extends AsyncBinding<Plan> {

	private FileSelectionBinding fileSelectionBinding;
	private ObservableList<TreeItem<String>> entrySelectionProperty;
	private ObjectBinding<ReadingRepository> cacheBinding;

	private List<FileEntry> fileSelection;
	private List<TreeItem<String>> entrySelection;
	private ReadingRepository cache;
	private StringProperty targetDir;
	private String target;

	public PlanBinding(ObjectBinding<ReadingRepository> cacheBinding,
			StringProperty targetDir,
			FileSelectionBinding fileSelectionBinding,
			ObservableList<TreeItem<String>> entrySelection) {

		super(cacheBinding, targetDir);

		this.cacheBinding = cacheBinding;
		this.targetDir = targetDir;
		this.fileSelectionBinding = fileSelectionBinding;
		this.entrySelectionProperty = entrySelection;

		EventStreams.changesOf(fileSelectionBinding)
				.successionEnds(Duration.ofMillis(100))
				.subscribe(s -> invalidate());

		EventStreams.changesOf(entrySelection)
				.successionEnds(Duration.ofMillis(200))
				.subscribe(s -> invalidate());
	}

	@Override
	protected Plan computeValue() {
		cache = cacheBinding.get();
		target = targetDir.get();
		fileSelection = fileSelectionBinding.get();
		entrySelection = new ArrayList<>(entrySelectionProperty);

		return super.computeValue();
	}

	@Override
	protected Plan doCompute() throws Exception {
		if (cache == null || Strings.isNullOrEmpty(target) || fileSelection == null) return null;

		System.out.println("PlanBinding.doCompute()");

		File targetFile = new File(target);
		if (!targetFile.isDirectory()) return null;

		ListMultimap<String, String> entrySelectionMap = ArrayListMultimap.create();

		entrySelection
				.stream().filter(i -> i.getParent() != null)
				.forEach(i -> entrySelectionMap.put(i.getParent().getValue(), i.getValue()));

		Planner planner = new Planner(fileSelection, entrySelectionMap, cache, targetFile);
		return planner.plan();
	}

}
