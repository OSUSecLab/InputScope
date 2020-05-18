package osu.seclab.inputscope.stringvsa.utility;

public class Logger {
	public static String TAG = "Logger";
	static boolean showMsg = false;

	public static void printI(String args) {
		if (showMsg)
			System.out.println(TAG + args);
	}

	public static void printW(String args) {
		String str = TAG + "[W]" + args;
		if (showMsg)
			System.out.println(str);
//		FileUtility.wf("./warnings/warnning.txt", str, true);
	}

	public static void print(String args) {
		if (showMsg)
			System.out.println(TAG + args);
	}

	public static void printE(String args) {
		args = TAG + args;
//		 FileUtility.wf("./warnings/error.txt", args, true);
		if (showMsg)
			System.out.println(args);
	}

}
