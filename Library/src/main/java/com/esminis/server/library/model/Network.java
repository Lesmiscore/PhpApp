/**
 * Copyright 2016 Tautvydas Andrikys
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
package com.esminis.server.library.model;

import android.support.annotation.NonNull;

import java.net.NetworkInterface;

public class Network {

	static public final Network ALL = new Network();

	public final String name;
	public final String address;
	public final String title;

	public Network(@NonNull NetworkInterface item, String address) {
		name = item.getName();
		this.address = address;
		this.title = address + " (" + item.getDisplayName() + ")";
	}

	private Network() {
		name = "all";
		address = "0.0.0.0";
		title = address + " (" + name + ")";
	}

	@Override
	public String toString() {
		return title;
	}

}
