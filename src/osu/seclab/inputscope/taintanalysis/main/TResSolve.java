package osu.seclab.inputscope.taintanalysis.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONObject;

import osu.seclab.inputscope.main.runTest;
import osu.seclab.inputscope.taintanalysis.base.TaintQuestion;
import osu.seclab.inputscope.taintanalysis.solver.SimulationContext;
import osu.seclab.inputscope.taintanalysis.solver.StmtItem;
import osu.seclab.inputscope.taintanalysis.utility.FileUtility;
import osu.seclab.inputscope.taintanalysis.utility.MethodUtility;

public class TResSolve {

	@SuppressWarnings("unchecked")
	public static HashSet<String> saveSolved(List<TaintQuestion> solved) {

		HashSet<String> svd = new HashSet<String>();
		ArrayList<StmtItem> stmts;
		String tmp;
		for (TaintQuestion tq : solved) {

			JSONObject source = new JSONObject();
			source.put("method", tq.getSourcep().getMethodLocation().toString());
			source.put("unit", tq.getSourcep().getInstructionLocation().toString());
			source.put("unitIndex", MethodUtility.getUnitIndex(tq.getSourcep().getMethodLocation(), tq.getSourcep().getInstructionLocation()));

			for (SimulationContext sc : tq.getsContexts()) {
				if (sc.isContainsSink()) {

					JSONObject ret = new JSONObject();
					ret.put("package", runTest.pn);
					ret.put("source", source);

					stmts = (ArrayList<StmtItem>) sc.getInstructionTrace().clone();
					for (StmtItem stmt : stmts)
						if (stmt.isContainsSink()) {

							JSONObject sink = new JSONObject();
							sink.put("method", stmt.getSm().toString());
							sink.put("unit", stmt.getU());
							sink.put("unitIndex", MethodUtility.getUnitIndex(stmt.getSm(), stmt.getU()));
							
							if (stmt.getCurIntst() != null)
								sink.put("taint_var", stmt.getCurIntst());
							ret.append("sinks", sink);

						}
					tmp = ret.toString();
					if (!svd.contains(tmp)) {
						svd.add(tmp);
//						FileUtility.wf("taintResTmp.txt", tmp, true);
					}
				}
			}
		}
		return svd;
		
	}

}
