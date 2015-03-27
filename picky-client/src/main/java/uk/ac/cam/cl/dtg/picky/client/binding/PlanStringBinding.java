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
import javafx.beans.binding.StringBinding;
import de.ecclesia.kipeto.engine.Plan;

public class PlanStringBinding extends StringBinding {

	private ObjectBinding<Plan> plan;
	private PlanToString planToString;

	public static interface PlanToString {
		public String compute(Plan plan);
	}

	public PlanStringBinding(ObjectBinding<Plan> plan, PlanToString planToString) {
		this.plan = plan;
		this.planToString = planToString;
		bind(plan);
	}

	@Override
	protected String computeValue() {
		return planToString.compute(plan.get());
	}

}
