package osu.seclab.inputscope.taintanalysis.solver;

import java.util.List;

import osu.seclab.inputscope.taintanalysis.base.TaintQuestion;
import osu.seclab.inputscope.taintanalysis.utility.Logger;

public class TaintQuestionSolver {

	public void solve(TaintQuestion tq) {

		SimulationContext target = null;
		while (true) {

			// printState(tq);

			while (tq.getsContexts().size() > 10000)
				tq.getsContexts().remove(tq.getsContexts().size() - 1);

			target = null;
			for (SimulationContext sContext : tq.getsContexts()) {
				if (!sContext.isTerminated()) {
					target = sContext;
					break;
				}
			}

			if (target == null)
				break;

			processOneInstruction(tq, target);
		}
		print(tq);

	}

	public void processOneInstruction(TaintQuestion tq, SimulationContext sContext) {

		List<SimulationContext> diversed = SimulationEngine.getInstance().oneStepForward(tq, sContext);

		for (SimulationContext sc : diversed) {
			tq.addSContexts(sc);
		}
	}

	public void print(TaintQuestion tq) {
		for (SimulationContext sContext : tq.getsContexts()) {
			Logger.print(sContext.isContainsSink() + " " + sContext.hashCode());
			for (StmtItem u : sContext.getInstructionTrace()) {
				Logger.print("    " + u.isContainsSink() + " " + u.getU());
				Logger.print("        " + u.getUnitIndex() + " " + u.getSm());
			}
		}
	}

	private void printState(TaintQuestion tq) {

		int ended = 0;
		int all = 0;
		for (SimulationContext sContext : tq.getsContexts()) {

			if (sContext.isTerminated())
				ended++;
			all++;

		}

		System.out.println(all + " / " + ended);
	}
}
