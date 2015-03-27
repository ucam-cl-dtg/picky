package uk.ac.cam.cl.dtg.picky.util;

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

public class Loop {

	public static void doFor(UnsafeInt n, UnsafeRunnable r) {
		try {
			doFor(n.getInt(), r);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void doFor(int n, UnsafeRunnable r) {
		for (int i = 0; i < n; i++) {
			try {
				r.run();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public interface UnsafeRunnable {
		public void run() throws Exception;
	}

	public interface UnsafeInt {
		public int getInt() throws Exception;
	}
}
