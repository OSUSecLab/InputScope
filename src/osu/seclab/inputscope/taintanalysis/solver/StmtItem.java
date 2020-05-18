package osu.seclab.inputscope.taintanalysis.solver;

import org.json.JSONObject;

import osu.seclab.inputscope.taintanalysis.utility.MethodUtility;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.Block;

public class StmtItem {
	SootMethod sm;
	Block block;
	Unit u;
	int unitIndex;
	Value curIntst = null;

	boolean containsInteresting = false;
	boolean containsSink = false;

	public JSONObject toJson(boolean containsMethodDetailes) {
		JSONObject jsobj = new JSONObject();

		jsobj.put("class", sm.getDeclaringClass().getName());
		jsobj.put("method", containsMethodDetailes?MethodUtility.toJson(sm):sm.toString());
		jsobj.put("unit", u);
		jsobj.put("unitIndex", unitIndex);

		return jsobj;
	}

	public StmtItem(SootMethod sm, Block block, Unit u) {
		super();
		this.sm = sm;
		this.block = block;
		this.u = u;
		this.unitIndex = MethodUtility.getUnitIndex(sm, u);
	}

	public SootMethod getSm() {
		return sm;
	}

	public void setSm(SootMethod sm) {
		this.sm = sm;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public Unit getU() {
		return u;
	}

	public void setU(Unit u) {
		this.u = u;
	}

	public int getUnitIndex() {
		return unitIndex;
	}

	public void setUnitIndex(int unitIndex) {
		this.unitIndex = unitIndex;
	}

	public boolean isContainsInteresting() {
		return containsInteresting;
	}

	public void setContainsInteresting(boolean containsInteresting) {
		this.containsInteresting = containsInteresting;
	}

	public boolean isContainsSink() {
		return containsSink;
	}

//	public void setContainsSink(boolean containsSink) {
//		this.containsSink = containsSink;
//	}
	
	public void setContainsSink(boolean containsSink, Value curIntst) {
		this.containsSink = containsSink;
		this.curIntst = curIntst;
	}

	public Value getCurIntst() {
		return curIntst;
	}


	public String toString() {
		return String.format("  %s\t%s\n\t%s", u, containsSink, sm);
	}
}
