package osu.seclab.inputscope.taintanalysis.base;

import soot.Scene;
import soot.SootMethod;

public class SinkMethod {
	SootMethod methodLocation;

	public SinkMethod(SootMethod methodLocation) {
		this.methodLocation = methodLocation;
	}

	public SinkMethod(String methodSig) {
		this.methodLocation = Scene.v().getMethod(methodSig);
	}

	public SootMethod getMethodLocation() {
		return methodLocation;
	}

	public void setMethodLocation(SootMethod methodLocation) {
		this.methodLocation = methodLocation;
	}

}