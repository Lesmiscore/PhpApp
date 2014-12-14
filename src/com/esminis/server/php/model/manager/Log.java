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

public class Log extends Manager {

	private Preferences manager = Manager.get(Preferences.class);

	private CharSequence text = null;

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
		text = getText(context);
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

	public CharSequence get() {
		return text;
	}

}
