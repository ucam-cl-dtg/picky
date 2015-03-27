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

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressIndicator;

public class BusyGraphicsBinding extends ObjectBinding<Node> {

	public static void install(Labeled target, AsyncBinding<?> asyncBinding) {
		target.graphicProperty().bind(new BusyGraphicsBinding(asyncBinding, target.getGraphic()));
	}

	private BooleanProperty busy;
	private Node nonBusy;
	private ProgressIndicator busyIndicator;

	public BusyGraphicsBinding(AsyncBinding<?> asyncBinding, Node nonBusy) {
		this.nonBusy = nonBusy;
		this.busy = asyncBinding.busy();

		this.busyIndicator = new ProgressIndicator(-1);
		this.busyIndicator.setPrefSize(16, 16);

		bind(busy);
	}

	@Override
	protected Node computeValue() {
		if (busy.get()) {
			return busyIndicator;
		} else {
			return nonBusy;
		}
	}

}
