/**
 * Copyright 2015 Tautvydas Andrikys
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
package com.esminis.server.library.model.manager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Network {

	private static final Pattern PATTERN_IPV4 = Pattern.compile(
			"^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

	private List<com.esminis.server.library.model.Network> list = new LinkedList<>();

	@Inject
	public Network() {
		refresh();
	}

	public boolean refresh() {
		List<com.esminis.server.library.model.Network> listOld = new LinkedList<>();
		listOld.addAll(list);
		list.clear();
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			add(interfaces, false);
			add(interfaces, true);
		} catch (SocketException ignored) {}
		add("0.0.0.0", "all", "0.0.0.0 (all)");
		if (listOld.size() != list.size()) {
			return true;
		}
		for (int i = 0; i < list.size(); i++) {
			if (!list.get(i).title.equals(listOld.get(i).title)) {
				return true;
			}
		}
		return false;
	}

	private void add(List<NetworkInterface> interfaces, boolean loopback) {
		for (NetworkInterface itemInterface : interfaces) {
			List<InetAddress> addresses = Collections.list(itemInterface.getInetAddresses());
			for (InetAddress item : addresses) {
				if ((!loopback && !item.isLoopbackAddress()) || (loopback && item.isLoopbackAddress())) {
					add(itemInterface, item);
				}
			}
		}
	}

	private void add(NetworkInterface item, InetAddress address) {
		String host = address.getHostAddress().toUpperCase();
		if (PATTERN_IPV4.matcher(host).matches()) {
			add(host, item.getName(), host + "(" + item.getDisplayName() + ")");
		}
	}

	private void add(String address, String name, String title) {
		list.add(create(address, name, title));
	}
	
	public int getPosition(String name) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).name.equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public List<com.esminis.server.library.model.Network> get() {
		return list;
	}

	public com.esminis.server.library.model.Network get(int position) {
		return list.get(position);
	}

	public com.esminis.server.library.model.Network create(String address, String name, String title) {
		com.esminis.server.library.model.Network model = new com.esminis.server.library.model.Network();
		model.address = address;
		model.name = name;
		model.title = title;
		return model;
	}
	
}
