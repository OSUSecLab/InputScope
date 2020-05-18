package osu.seclab.inputscope.stringvsa.base;

import org.json.JSONObject;

public class GlobalStatistics {
	static GlobalStatistics gs = new GlobalStatistics();

	private GlobalStatistics() {
	}

	public static GlobalStatistics getInstance() {
		return gs;
	}

	public void countGetString() {
		getString++;
	}

	public void countAppendString() {
		appendString++;
	}

	public void countFormatString() {
		formatString++;
	}

	public void countDiveIntoMethodCall() {
		diveIntoMethodCall++;
	}

	public void countBackWard2Caller() {
		backWard2Caller++;
	}

	public void updateMaxCallStack(int i) {
		if (i > maxCallStack)
			maxCallStack = i;
	}

	int getString = 0;
	int appendString = 0;
	int formatString = 0;
	int diveIntoMethodCall = 0;
	int backWard2Caller = 0;
	int maxCallStack = 0;

	public static int replace = 0;
	public static int addExp = 0;
	public static int subExp = 0;
	public static int mulExp = 0;
	public static int divExp = 0;
	public static int andExp = 0;
	public static int orExp = 0;
	public static int shlExp = 0;
	public static int shrExp = 0;
	public static int xorExp = 0;
	public static int ushrExp = 0;

	public JSONObject toJson() {
		JSONObject result = new JSONObject();
		result.put("getString", getString);
		result.put("appendString", appendString);
		result.put("formatString", formatString);
		result.put("diveIntoMethodCall", diveIntoMethodCall);
		result.put("backWard2Caller", backWard2Caller);
		result.put("maxCallStack", maxCallStack);
		result.put("addExp", addExp);
		result.put("subExp", subExp);
		result.put("mulExp", mulExp);
		result.put("divExp", divExp);
		result.put("andExp", andExp);
		result.put("orExp", orExp);
		result.put("shlExp", shlExp);
		result.put("shrExp", shrExp);
		result.put("xorExp", xorExp);
		result.put("ushrExp", ushrExp);
		return result;
	}
}
