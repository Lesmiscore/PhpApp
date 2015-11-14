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
package com.esminis.server.mariadb.server;

import android.content.Context;

import com.esminis.server.library.service.server.ServerLauncher;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class MariaDbServerLauncher extends ServerLauncher {

	private final Object lock = new Object();

	MariaDbServerLauncher(com.esminis.server.library.model.manager.Process managerProcess) {
		super(managerProcess);
	}

	private void install(Context context, File fileSocket, File binary, File documentRoot, File directoryTemp) throws IOException {
		List<String> environment = getEnvironment();
		// setup_database.sql
		// mysql_system_tables.sql
		// mysql_performance_tables.sql
		// mysql_system_tables_data.sql
		// fill_help_tables.sql
		directoryTemp.setWritable(true);
		File directoryData = new File(documentRoot, "data");
		File directoryDataMysql = new File(directoryData, "mysql");
		File directoryDataTest = new File(directoryData, "test");
		Process process = Runtime.getRuntime().exec(
			new String[] {
				binary.getAbsolutePath(),
				"--bootstrap", //"--skip-innodb", "--default-storage-engine=myisam",
				"--lc-messages-dir=" + binary.getParentFile().getAbsolutePath(),
				"--socket=" + fileSocket,
				"--tmpdir=" + directoryTemp.getAbsolutePath(),
				"--basedir=" + documentRoot.getAbsolutePath(),
				"--datadir=" + directoryData.getAbsolutePath(),
				"--log-warnings=0", "--loose-skip-ndbcluster", "--max_allowed_packet=8M",
				"--net_buffer_length=16K"
			}, environment.toArray(new String[environment.size()]), documentRoot
		);

		if (!directoryDataMysql.isDirectory()) {
			directoryDataMysql.mkdirs();
		}
		if (!directoryDataTest.isDirectory()) {
			directoryDataTest.mkdirs();
		}
		process.getOutputStream().write("use mysql;\n".getBytes());
		process.getOutputStream().write(((IOUtils.toString(context.getAssets().open("sql/mysql_system_tables.sql")))).getBytes());
		process.getOutputStream().write(((IOUtils.toString(context.getAssets().open("sql/mysql_performance_tables.sql")))).getBytes());
		process.getOutputStream().write(((IOUtils.toString(context.getAssets().open("sql/mysql_system_tables_data.sql")))).getBytes());
		process.getOutputStream().write(((IOUtils.toString(context.getAssets().open("sql/fill_help_tables.sql")))).getBytes());
		process.getOutputStream().write("\nexit;\n".getBytes());
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private List<String> createCommand(Context context, File binary, String address, String root) throws IOException {
		final List<String> options = new ArrayList<>();
		final String[] addressParts = address.split(":");
		final File directoryTemp = new File(binary.getParentFile(), "temp");
		final File fileSocket = new File(binary.getParentFile(), "mysql.sock");
		directoryTemp.mkdirs();

		synchronized (lock) {
			install(context, fileSocket, binary, new File(root), directoryTemp);
		}

		options.add(binary.getAbsolutePath());
		options.add("--lc-messages-dir=" + binary.getParentFile().getAbsolutePath());
		options.add("--tmpdir=" + directoryTemp.getAbsolutePath());
		options.add("--socket=" + fileSocket);
		options.add("--bind-address=" + addressParts[0]);
		options.add("--port=" + addressParts[1]);
		options.add("--basedir=" + root);
		options.add("--datadir=" + new File(new File(root), "data").getAbsolutePath());
		return options;
	}

	Process start(
		File binary, String address, String root, File documentRoot, boolean keepRunning,
		Context context
	) throws IOException {
		return start(
			binary, createCommand(context, binary, address, root), context, getEnvironment(), documentRoot,
			keepRunning
		);
	}

}
