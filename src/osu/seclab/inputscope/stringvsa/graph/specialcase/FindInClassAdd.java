package osu.seclab.inputscope.stringvsa.graph.specialcase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.CompleteBlockGraph;
import osu.seclab.inputscope.stringvsa.base.StmtPoint;
import osu.seclab.inputscope.stringvsa.graph.DGraph;
import osu.seclab.inputscope.stringvsa.utility.BlockGenerator;

public class FindInClassAdd {

	public static List<StmtPoint> getAll(DGraph dg, SootField sf) {
		// sf.getDeclaringClass().
		List<StmtPoint> sps = new ArrayList<StmtPoint>();
		Body body;
		boolean contains;
		System.out.println(sf.getDeclaringClass());

		for (SootMethod sm : sf.getDeclaringClass().getMethods()) {
			body = sm.retrieveActiveBody();
			contains = false;
			for (ValueBox vb : body.getUseAndDefBoxes()) {

				if (vb.getValue() instanceof FieldRef)
					if (((FieldRef) vb.getValue()).getField().equals(sf)) {
						contains = true;
						break;
					}
			}
			if (contains)
				sps.addAll(onSootMethod(dg, sf, sm));
		}
		return sps;
	}

	private static List<StmtPoint> onSootMethod(DGraph dg, SootField sf, SootMethod sm) {
		List<StmtPoint> sps = new ArrayList<StmtPoint>();

		CompleteBlockGraph cbg = BlockGenerator.getInstance().generate(sm.retrieveActiveBody());
		Local reg = null;
		Unit tu;
		Value tv;
		for (Block block : cbg.getBlocks()) {
			Iterator<Unit> us = block.iterator();
			while (us.hasNext()) {
				tu = us.next();
				if (reg == null) {
					if (tu instanceof AssignStmt) {
						Value vvv = ((AssignStmt) tu).getRightOp();
						if (vvv instanceof FieldRef && ((FieldRef) vvv).getField().equals(sf)) {
							reg = (Local) ((AssignStmt) tu).getLeftOp();
						}
					}
				} else {
					for (ValueBox vb : tu.getUseAndDefBoxes()) {
						tv = vb.getValue();
						if (tv instanceof InstanceInvokeExpr) {

							if (((InstanceInvokeExpr) tv).getBase().equivTo(reg) && tmths.contains(((InstanceInvokeExpr) tv).getMethod().getName())) {
								sps.add(new StmtPoint(sm, block, tu));
							}
						}
					}
				}

			}
		}
		return sps;
	}

	static HashSet<String> tmths = new HashSet<String>();
	static HashSet<String> addableClasses = new HashSet<String>();
	static {
		tmths.add("put");
		tmths.add("add");

		addableClasses.add("java.util.Map");
		addableClasses.add("java.util.HashMap");
		addableClasses.add("java.util.Set");
		addableClasses.add("java.util.HashSet");
		addableClasses.add("java.util.ArrayList");
		addableClasses.add("java.util.List");
	}

	public static boolean isAddable(SootField sf) {
		return addableClasses.contains(sf.getType().toString());
	}

}
