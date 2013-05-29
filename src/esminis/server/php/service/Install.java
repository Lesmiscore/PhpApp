package esminis.server.php.service;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Install {
	
	public void fromAsset(File target, String path, Context context) 
		throws IOException 
	{
		if (target.isFile()) {
			return;
		}
		InputStream input = context.getAssets().open(path);
		OutputStream output = new FileOutputStream(target);
		int read = 0;
		byte[] bytes = new byte[1024];
		while ((read = input.read(bytes)) != -1) {
			output.write(bytes, 0, read);
		}
		input.close();
		output.close();
	}
	
}
