package osu.seclab.inputscope.taintanalysis.base;

import java.util.ArrayList;
import java.util.List;

import osu.seclab.inputscope.taintanalysis.solver.SimulationContext;
import soot.SootMethod;

public class TaintQuestion {
	SourcePoint sourcep;
	List<SinkMethod> sinks;

	List<SimulationContext> sContexts = new ArrayList<>();

	public TaintQuestion(SourcePoint sourcep) {
		setSourcep(sourcep);
		sinks = new ArrayList<>();

		SimulationContext sc = new SimulationContext(sourcep.getMethodLocation(), sourcep.getBlockLocation(), sourcep.getInstructionLocation());
		sc.addIntrestedVariable(sourcep.getTartgetValue());
		sContexts.add(sc);

	}

	public SourcePoint getSourcep() {
		return sourcep;
	}

	public void setSourcep(SourcePoint sourcep) {
		this.sourcep = sourcep;
	}

	public List<SinkMethod> getSinks() {
		return sinks;
	}

	public void addSinks(SinkMethod sink) {
		this.sinks.add(sink);
	}

	public List<SimulationContext> getsContexts() {
		return sContexts;
	}

	public void addSContexts(SimulationContext sContext) {
		if (!sContexts.contains(sContext))
			this.sContexts.add(sContext);
	}

	public boolean isSink(SootMethod sm) {

		for (SinkMethod sinkm : sinks) {
			if (sinkm.getMethodLocation().equals(sm))
				return true;
		}
		return false;

	}

	public boolean isPositive() {
		for (SimulationContext sContext : sContexts) {
			if(sContext.isContainsSink())
				return true;
		}
		return false;
	}
}
