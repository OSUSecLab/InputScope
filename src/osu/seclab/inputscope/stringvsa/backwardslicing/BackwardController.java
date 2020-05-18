package osu.seclab.inputscope.stringvsa.backwardslicing;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import osu.seclab.inputscope.stringvsa.graph.DGraph;
import osu.seclab.inputscope.stringvsa.graph.ValuePoint;
import osu.seclab.inputscope.stringvsa.main.Config;

public class BackwardController {
	static BackwardController sc = new BackwardController();

	public static BackwardController getInstance() {
		return sc;
	}

	private BackwardController() {

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public List<BackwardContext> doBackWard(ValuePoint vp, DGraph dg) {
		List<BackwardContext> bcs = new ArrayList<BackwardContext>();
		bcs.add(new BackwardContext(vp, dg));

		long stime = System.currentTimeMillis();
		BackwardContext bc;
		while (true) {

			bc = null;
			for (BackwardContext tmp : bcs) {
				if (!tmp.backWardHasFinished()) {
					bc = tmp;
					break;
				}
			}
			if (bc == null) {
				break;
			}
			bcs.addAll(bc.oneStepBackWard());


			if (Config.BackwardContextTimeOut != -1 && System.currentTimeMillis() - stime > Config.BackwardContextTimeOut) {
				for (BackwardContext tmp : bcs) {
					tmp.finished();
				}
			}
		}

		bcs.forEach(var -> {
			var.printExceTrace();
		});

		return bcs;

	}

}
