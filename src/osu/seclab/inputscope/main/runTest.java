package osu.seclab.inputscope.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.jf.smali.SmaliMethodParameter;
import org.json.JSONArray;
import org.json.JSONObject;

import osu.seclab.inputscope.stringvsa.backwardslicing.TaintRules;
import osu.seclab.inputscope.stringvsa.graph.CallGraph;
import osu.seclab.inputscope.stringvsa.graph.DGraph;
import osu.seclab.inputscope.stringvsa.graph.IDGNode;
import osu.seclab.inputscope.stringvsa.graph.ValuePoint;
import osu.seclab.inputscope.stringvsa.utility.Logger;
import osu.seclab.inputscope.taintanalysis.base.TaintQuestion;
import osu.seclab.inputscope.taintanalysis.main.QuestionGenerator;
import osu.seclab.inputscope.taintanalysis.utility.FileUtility;
import osu.seclab.inputscope.taintanalysis.utility.TimeUtility;
import soot.Scene;
import soot.options.Options;

public class runTest {
	public static String path;
	public static String pn;
	static Hashtable<String, Hashtable<String, String>> m2m;
	
	// args[0] app package name
	// args[1] android.jar path
	public static void main(String[] args) {

		String fp = "";
		fp = args[0];

//		String ajar = "android.jar";
		String ajar = args[1];
		path = fp;
		

		String[] tpn = fp.split("/");
		pn = tpn[tpn.length - 1].substring(0, tpn[tpn.length - 1].length() - 4);
		System.out.println(pn);

		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_process_dir(Collections.singletonList(fp));
		Options.v().set_force_android_jar(ajar);
		Options.v().set_process_multiple_dex(true);
		Options.v().set_android_api_version(24);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_force_overwrite(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_whole_program(true);
		Options.v().ignore_resolution_errors();

		Scene.v().loadNecessaryClasses();
		TimeUtility.startWatcherBruce(7 * 60);

		// Taint Analysis
		QuestionGenerator qg = new QuestionGenerator();
		
		HashSet<String> init_taint_res = qg.generateInputQuestions().solveInputQuestions(false);
		
		HashSet<String> taint_res = combineTaintRes(init_taint_res);

		System.out.println("\n===============Taint Analysis Result===============\n");

		for (String result : taint_res)
			System.out.println(result.toString());
		

		// String VSA
		CallGraph.init();
		System.out.println("\n===============String Value Analysis Result===============\n");
		HashSet<String> strVSA_res = runStrVSA(taint_res);

		System.out.println("\n===============Final Result===============\n");

		for (String result : strVSA_res) {
			System.out.println(result.toString());
			FileUtility.wf("./" + pn, result.toString(), true);
		}

	}

	public static HashSet<String> combineTaintRes(HashSet<String> taint_res) {
		Hashtable<String, String> cres = new Hashtable<String, String>();
		for (String result : taint_res) {
			JSONObject cur_json = new JSONObject(result);
			String tmp_key = cur_json.getJSONObject("source").get("unit").toString();
			tmp_key += cur_json.getJSONObject("source").get("method").toString();
			tmp_key += cur_json.getJSONObject("source").get("unitIndex").toString();
			if (!cres.containsKey(tmp_key)) {
				JSONObject tmp_json = new JSONObject();
				tmp_json.put("sinks", cur_json.getJSONArray("sinks"));
				tmp_json.put("source", cur_json.getJSONObject("source"));
				cres.put(tmp_key, tmp_json.toString());
				
			} else {
				JSONObject tmp_json = new JSONObject(cres.get(tmp_key));
				for (Object nsink : cur_json.getJSONArray("sinks")) {
					boolean isHave = false;
					for (Object osink : tmp_json.getJSONArray("sinks")) {
						if (checkSinkJsonStrEquality(nsink.toString(), osink.toString())) {
							isHave = true;
							break;
						}
					}
					if (!isHave) {
						tmp_json.getJSONArray("sinks").put((JSONObject) nsink);
					}
				}
				cres.put(tmp_key, tmp_json.toString());
			}
		}
		HashSet<String> ret_res = new HashSet<String>();
		for (String tkey : cres.keySet()) {
			ret_res.add(cres.get(tkey).toString());
		}
		return ret_res;
		

	}

	public static boolean checkSinkJsonStrEquality(String jstr1, String jstr2) {
		JSONObject json1 = new JSONObject(jstr1);
		JSONObject json2 = new JSONObject(jstr2);
		if (!json1.getString("unit").equals(json2.getString("unit"))) {
			return false;
		} else if (!json1.getString("method").equals(json2.getString("method"))) {
			return false;
		} else if (!json1.getString("taint_var").equals(json2.getString("taint_var"))) {
			return false;
		} else if (!json1.get("unitIndex").toString().equals(json2.get("unitIndex").toString())) {
			return false;
		}
		return true;
	}

	public static HashSet<String> runStrVSA(HashSet<String> tres) {
		HashSet<String> results = null;
		HashSet<String> fresults = new HashSet<String>();
		String tmp_smtd;
		String tmp_sinstr;
		String tmp_tvar;
		String tmp_rinstr;

		for (String result : tres) {
			m2m = new Hashtable<String, Hashtable<String, String>>();
			JSONObject cur_json = new JSONObject(result);
			JSONArray sinks_arr = cur_json.getJSONArray("sinks");
			for (Object str : sinks_arr) {
				tmp_smtd = ((JSONObject) str).getString("method").trim();
				tmp_sinstr = ((JSONObject) str).getString("unit").trim();
				tmp_tvar = ((JSONObject) str).getString("taint_var").trim();
				tmp_rinstr = tmp_sinstr.replace(tmp_tvar, "taintedVariable");
				if (!m2m.containsKey(tmp_smtd)) {
					m2m.put(tmp_smtd, new Hashtable<String, String>());
				}
				if (!m2m.get(tmp_smtd).contains(tmp_rinstr)) {
					m2m.get(tmp_smtd).put(tmp_rinstr, tmp_sinstr);
				}
			}
			results = vsa(m2m);

			for (String tmp_res : results) {
				JSONObject tmp_json = new JSONObject(tmp_res);
				tmp_json.put("source", cur_json.get("source"));
				fresults.add(tmp_json.toString());
			}

			
		}
		
		return fresults;
	}

	public static HashSet<String> vsa(Hashtable<String, Hashtable<String, String>> m2m2) {

		DGraph dg = new DGraph();

		List<ValuePoint> allvps = new ArrayList<ValuePoint>();
		List<ValuePoint> vps = null;
		JSONObject tmp;

		for (String tmtd : m2m2.keySet()) {
			
			vps = ValuePoint.find(dg, tmtd, m2m2.get(tmtd), 10000);
			for (ValuePoint vp : vps) {
				// vp.print();
				allvps.add(vp);
			}
		}

		dg.solve(allvps);

		HashSet<String> result = new HashSet<String>();

		JSONObject result_json = new JSONObject();

		for (IDGNode tn : dg.getNodes()) {
			Logger.print(tn.toString());
		}

		for (ValuePoint vp : allvps) {
			tmp = vp.toJson();
			if (tmp.has("values"))
				Logger.print(tmp.getJSONArray("values").toString());
			result_json.append("sinks", vp.toJson());
		}
		// result.put("package", pn);

		System.out.println(result_json.toString());

		if (!result.contains(result_json.toString())) {
			result.add(result_json.toString());
		}


		return result;
	}

	public static void saveFinalResult(HashSet<String> taint_res, HashSet<String> vsa_res) {
		JSONObject result = new JSONObject();
//		JSONObject tmp_result = new JSONObject();
		for (String tres : taint_res) {
			JSONObject cur_tres = new JSONObject(tres);
			result.put("package", pn);

			result.put("source", cur_tres.get("source"));

		}
	}

}
