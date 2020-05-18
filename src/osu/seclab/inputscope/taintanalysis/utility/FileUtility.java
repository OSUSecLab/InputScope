package osu.seclab.inputscope.taintanalysis.utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	
	public static String read(String path){

		try {
			return new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
}
