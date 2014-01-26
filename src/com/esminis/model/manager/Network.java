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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.conn.util.InetAddressUtils;

public class Network extends Manager<com.esminis.model.Network> {

	private List<com.esminis.model.Network> list = new LinkedList<com.esminis.model.Network>();
	
	protected Network() {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			add(interfaces, false);
			add(interfaces, true);
		} catch (SocketException ignored) {}
		add("0.0.0.0", "all", "0.0.0.0 (all)");
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
		if (InetAddressUtils.isIPv4Address(host)) {
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
	
	public List<com.esminis.model.Network> get() {
		return list;
	}

	public com.esminis.model.Network get(int position) {
		return list.get(position);
	}

	public com.esminis.model.Network create(String address, String name, String title) {
		com.esminis.model.Network model = create();
		model.address = address;
		model.name = name;
		model.title = title;
		return model;
	}
	
}
