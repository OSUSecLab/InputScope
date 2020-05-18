package osu.seclab.inputscope.stringvsa.utility;

import java.lang.Thread.UncaughtExceptionHandler;

public class ErrorHandler implements UncaughtExceptionHandler {
	String tag;

	public ErrorHandler(String tag) {
		this.tag = tag;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append("File: " + tag);
		sb.append('\n');
		sb.append("Msge: " + e.getMessage());
		sb.append('\n');
		for (StackTraceElement st : e.getStackTrace()) {
			sb.append("     " + st.toString());
			sb.append('\n');
		}
		Logger.printE(sb.toString());
	}

}
