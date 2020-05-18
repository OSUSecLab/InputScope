package osu.seclab.inputscope.taintanalysis.utility;

import osu.seclab.inputscope.main.runTest;

public class TimeUtility {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	
	public static void startWatcherBruce(int sec) {
		Thread t = new Thread() {
			public void run() {
				try {
					Thread.sleep(sec * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Logger.printE("TimeOut");

				String line = String.format("%s | %s | %s | %s", 0, "timeout", runTest.pn, "");

				FileUtility.wf("TimeOuts.txt", line, true);

				System.exit(0);
			}
		};
		t.setDaemon(true);
		t.start();
	}
}
