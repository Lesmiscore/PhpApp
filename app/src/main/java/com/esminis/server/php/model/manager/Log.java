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
package com.esminis.server.php.model.manager;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Log {

	@Inject
	protected Preferences manager;

	static private String KEY = "__log__";

	public void clear(Context context) {
		manager.set(context, KEY, "");
	}

	public void add(Context context, String lines) {
		String[] linesArray = TextUtils.split(lines, "\n");
		List<String> list = new ArrayList<>();
		list.addAll(Arrays.asList(manager.getString(context, KEY).split("\n")));
		for (String line : linesArray) {
			list.add((line.matches("^.+: /[^ ]*$") ? "0" : "1") + line);
		}
		manager.set(
			context, KEY, TextUtils.join("\n", list.subList(Math.max(list.size() - 36, 0), list.size()))
		);
	}

	private CharSequence getText(Context context) {
		String[] lines = manager.getString(context, KEY).split("\n");
		SpannableStringBuilder builder = new SpannableStringBuilder();
		Spannable.Factory factory = new Spannable.Factory();
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.isEmpty()) {
				continue;
			}
			Spannable textLine = factory.newSpannable(line.substring(1));
			textLine.setSpan(
				new ForegroundColorSpan(line.charAt(0) == '0' ? Color.rgb(0, 0x66, 0) : Color.RED),
				0, line.length() - 1, 0
			);
			if (i > 0) {
				builder.append("\n");
			}
			builder.append(textLine);
		}
		return builder;
	}

	public CharSequence get(Context context) {
		return getText(context);
	}

}
