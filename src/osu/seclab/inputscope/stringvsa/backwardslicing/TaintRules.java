package osu.seclab.inputscope.stringvsa.backwardslicing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

public class TaintRules {

	static String RULE_PATH = "taintrules.json";
	static String RULE_TAINT_KEY = "taint";
	static String RULE_TAINT_BASENAME = "base";
	static String RULE_TAINT_ALLARGS = "args";
	static String RULE_TAINT_ARGS_PRE = "arg";
	static String RULE_TAINT_IS_SYS_API_SRC = "isSystemAPISrc";

	static JSONObject rules;
	static {
		String rjs = null;
		try {
			rjs = new String(Files.readAllBytes(Paths.get(RULE_PATH)));
		} catch (IOException e) {
			System.err.println("TaintRules load error!");
			e.printStackTrace();
			System.exit(0);
		}

		rules = new JSONObject(rjs);
	}

	private TaintRules() {
	}

	static TaintRules tr = new TaintRules();

	public static TaintRules getInstance() {
		return tr;
	}

	HashMap<String, Boolean> CACHEisBaseIntrested = new HashMap<String, Boolean>();
	HashMap<String, List<Integer>> CACHEgetInterestedArgIndexes = new HashMap<String, List<Integer>>();

	public boolean hasRuleFor(String msig) {
		return rules.has(msig);
	}

	public boolean isBaseIntrested(String msig) {
		if (CACHEisBaseIntrested.containsKey(msig)) {
			return CACHEisBaseIntrested.get(msig);
		}
		boolean res = rules.getJSONObject(msig).getJSONArray(RULE_TAINT_KEY).toList().contains(RULE_TAINT_BASENAME);
		CACHEisBaseIntrested.put(msig, res);
		return res;
	}

	public List<Integer> getInterestedArgIndexes(String msig, int argsLen) {
		if (CACHEgetInterestedArgIndexes.containsKey(msig)) {
			return CACHEgetInterestedArgIndexes.get(msig);
		}

		List<Integer> indexes = new ArrayList<Integer>();
		List<Object> ts = rules.getJSONObject(msig).getJSONArray(RULE_TAINT_KEY).toList();

		for (int i = 0; i < argsLen; i++) {
			if (ts.contains(RULE_TAINT_ALLARGS) || ts.contains(RULE_TAINT_ARGS_PRE + i)) {
				indexes.add(i);
			}
		}
		CACHEgetInterestedArgIndexes.put(msig, indexes);
		return indexes;

	}

	public List<Object> getDataSrc(String msig) {
		if (rules.getJSONObject(msig).has(RULE_TAINT_IS_SYS_API_SRC))
			return rules.getJSONObject(msig).getJSONArray(RULE_TAINT_IS_SYS_API_SRC).toList();
		return null;
	}


}
