/**
 * Copyright 2014 Tautvydas Andrikys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esminis.model.manager;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;

abstract public class Manager<T> {

	private Class<T> modelClass = null;

	static private HashMap<Class<? extends Manager>,Manager> instances =
		new HashMap<Class<? extends Manager>, Manager>();

	private Class<T> getModelClass() {
		if (modelClass == null) {
			modelClass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		}
		return modelClass;
	}

	protected T create() {
		try {
			return getModelClass().newInstance();
		} catch (Exception ignored) {
			throw new RuntimeException("Could not create model instance for class " + getModelClass());
		}
	}

	static public <M extends Manager> M get(Class<M> managerClass) {
		if (!instances.containsKey(managerClass)) {
			try {
				instances.put(managerClass, managerClass.newInstance());
			} catch (Exception ignored) {
				throw new RuntimeException("Could not create manager instance for class");
			}
		}
		return (M)instances.get(managerClass);
	}

}
