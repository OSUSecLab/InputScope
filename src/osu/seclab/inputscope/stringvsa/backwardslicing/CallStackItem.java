package osu.seclab.inputscope.stringvsa.backwardslicing;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.Block;

public class CallStackItem {
	SootMethod smethd;
	Block blcok;
	Unit currentInstruction;
	Value returnTarget;

	public CallStackItem(SootMethod smethd, Block blcok, Unit currentInstruction, Value returnTarget) {
		super();
		this.smethd = smethd;
		this.blcok = blcok;
		this.currentInstruction = currentInstruction;
		this.returnTarget = returnTarget;
	}

	public SootMethod getSmethd() {
		return smethd;
	}

	public void setSmethd(SootMethod smethd) {
		this.smethd = smethd;
	}

	public Block getBlcok() {
		return blcok;
	}

	public void setBlcok(Block blcok) {
		this.blcok = blcok;
	}

	public Unit getCurrentInstruction() {
		return currentInstruction;
	}

	public void setCurrentInstruction(Unit currentInstruction) {
		this.currentInstruction = currentInstruction;
	}

	public Value getReturnTarget(int fff) {
		return returnTarget;
	}

	public void setReturnTarget(Value returnTarget) {
		this.returnTarget = returnTarget;
	}
}
