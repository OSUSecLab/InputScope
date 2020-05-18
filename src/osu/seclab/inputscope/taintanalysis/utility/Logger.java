package osu.seclab.inputscope.taintanalysis.utility;

public class Logger {
	public static String TAG = "Logger ";

	public static void printI(String args) {
		println(TAG + args);
	}

	public static void printW(String args) {
		String str = TAG + "[W]" + args;
		println(str);
		// FileUtility.wf("./logs/warnning.txt", str, true);
	}

	public static void print(Object args) {
		println(TAG + args);
	}

	public static void printE(String args) {
		args = TAG + args;
		// FileUtility.wf("./logs/error.txt", args, true);
		println(args);
	}

	private static void println(Object obj) {
		//System.out.println(obj);
	}

}
