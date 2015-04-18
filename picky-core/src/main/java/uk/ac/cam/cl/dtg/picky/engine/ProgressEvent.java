package uk.ac.cam.cl.dtg.picky.engine;

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

public class ProgressEvent {

	private final Action action;
	private final String msg;
	private final int tasksDone;
	private long bytesDone;

	public enum Action {
		DELETE_DIR, DELETE_FILE, MAKE_DIR, DOWNLOAD_CHUNK, INSTALL_FILE, UPDATE_FILE
	}

	public ProgressEvent(Action action, String msg, int tasksDone, long bytesDone) {
		this.action = action;
		this.msg = msg;
		this.tasksDone = tasksDone;
		this.bytesDone = bytesDone;
	}

	public Action getAction() {
		return action;
	}

	public String getMsg() {
		return msg;
	}

	public int getTasksDone() {
		return tasksDone;
	}

	public long getBytesDone() {
		return bytesDone;
	}

}
