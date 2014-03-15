package com.esminis.server.php.model.manager;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.esminis.model.manager.Manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Log extends Manager<String> {

	private Preferences manager = Manager.get(Preferences.class);

	static private String KEY = "__log__";

	public void clear(Context context) {
		manager.set(context, KEY, "");
	}

	public void add(Context context, String line, boolean error) {
		String[] parts = manager.getString(context, KEY).split("\n");
		List<String> list = new ArrayList<String>();
		list.addAll(Arrays.asList(parts).subList(parts.length == 36 ? 1 : 0, parts.length));
		list.add((error ? "1" : "0") + line);
		manager.set(context, KEY, TextUtils.join("\n", list));
	}

	public CharSequence get(Context context) {
		String[] lines = manager.getString(context, KEY).split("\n");
		SpannableStringBuilder builder = new SpannableStringBuilder();
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.isEmpty()) {
				continue;
			}
			Spannable textLine = new Spannable.Factory().newSpannable(line.substring(1));
			textLine.setSpan(
				new ForegroundColorSpan(line.charAt(0) == '0' ? Color.rgb(0, 0x66, 0) : Color.RED), 0,
				line.length() - 1, 0
			);
			if (i > 0) {
				builder.append("\n");
			}
			builder.append(textLine);
		}
		return builder;
	}

}
