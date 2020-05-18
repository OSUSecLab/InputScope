package osu.seclab.inputscope.stringvsa.utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtility {

	public static void wf(String path, String content, boolean append) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, append)));
			out.println(content);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
