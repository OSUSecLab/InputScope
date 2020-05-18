package osu.seclab.inputscope.stringvsa.graph;

import java.util.HashSet;

import soot.SootMethod;

public class CallGraphNode {
	SootMethod smthd;

	HashSet<CallGraphNode> callBy = new HashSet<CallGraphNode>();
	HashSet<CallGraphNode> callTo = new HashSet<CallGraphNode>();

	public CallGraphNode(SootMethod smthd) {
		this.smthd = smthd;
	}

	public void addCallBy(CallGraphNode smtd) {
		callBy.add(smtd);
	}

	public void addCallTo(CallGraphNode smtd) {
		callTo.add(smtd);
	}

	public HashSet<CallGraphNode> getCallBy() {
		return callBy;
	}

	public HashSet<CallGraphNode> getCallTo() {
		return callTo;
	}

	public SootMethod getSmthd() {
		return smthd;
	}

}
