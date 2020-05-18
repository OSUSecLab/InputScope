package osu.seclab.inputscope.stringvsa.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import osu.seclab.inputscope.stringvsa.backwardslicing.BackwardContext;
import osu.seclab.inputscope.stringvsa.backwardslicing.BackwardController;
import osu.seclab.inputscope.stringvsa.backwardslicing.DataSourceType;
import osu.seclab.inputscope.stringvsa.base.StmtPoint;
import osu.seclab.inputscope.stringvsa.base.TargetType;
import osu.seclab.inputscope.stringvsa.forwardexec.SimulateEngine;
import osu.seclab.inputscope.stringvsa.utility.Logger;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.toolkits.graph.Block;

public class ValuePoint implements IDGNode {

	DGraph dg;

	SootMethod method_location;
	Block block_location;
	Unit instruction_location;
	HashSet<Integer> target_regs = new HashSet<Integer>();
	List<BackwardContext> bcs = null;
	HashMap<HashMap<Integer, HashSet<String>>, BackwardContext> simulatedRess = new HashMap<HashMap<Integer, HashSet<String>>, BackwardContext>();

	HashSet<BackwardContext> solvedBCs = new HashSet<BackwardContext>();

	Object appendix = "";

	ArrayList<HashMap<Integer, HashSet<String>>> result = new ArrayList<HashMap<Integer, HashSet<String>>>();

	boolean inited = false;
	boolean solved = false;

	public ValuePoint(DGraph dg, SootMethod method_location, Block block_location, Unit instruction_location,
			List<Integer> regIndex) {
		this.dg = dg;
		this.method_location = method_location;
		this.block_location = block_location;
		this.instruction_location = instruction_location;
		for (int i : regIndex) {
			target_regs.add(i);
		}
		dg.addNode(this);
	}

	public DGraph getDg() {
		return dg;
	}

	public List<BackwardContext> getBcs() {
		return bcs;
	}

	public SootMethod getMethod_location() {
		return method_location;
	}

	public Block getBlock_location() {
		return block_location;
	}

	public Unit getInstruction_location() {
		return instruction_location;
	}

	public Set<Integer> getTargetRgsIndexes() {
		return target_regs;
	}

	public void setAppendix(Object str) {
		appendix = str;
	}

	@Override
	public Set<IDGNode> getDependents() {
		// TODO Auto-generated method stub

		HashSet<IDGNode> dps = new HashSet<IDGNode>();
		for (BackwardContext bc : bcs) {
			for (IDGNode node : bc.getDependentHeapObjects()) {
				dps.add(node);
			}
		}
		return dps;
	}

	@Override
	public int getUnsovledDependentsCount() {
		// TODO Auto-generated method stub
		int count = 0;
		for (IDGNode node : getDependents()) {
			if (!node.hasSolved()) {
				count++;
			}
		}
		Logger.print(this.hashCode() + "[]" + count + " " + bcs.size());
		return count;
	}

	@Override
	public boolean hasSolved() {

		return solved;
	}

	@Override
	public boolean canBePartiallySolve() {
		boolean can = false;
		boolean dsolved;
		SimulateEngine tmp;
		for (BackwardContext bc : bcs) {
			if (!solvedBCs.contains(bc)) {
				dsolved = true;
				for (HeapObject ho : bc.getDependentHeapObjects()) {
					if (!ho.hasSolved()) {
						dsolved = false;
						break;
					}
				}
				if (dsolved) {
					solvedBCs.add(bc);
					can = true;
					tmp = new SimulateEngine(dg, bc);
					tmp.simulate();
					mergeResult(bc, tmp);
				}
			}
		}
		if (can) {
			solved = true;
		}

		return can;
	}

	@Override
	public void solve() {
		solved = true;
		Logger.print("[SOLVING ME]" + this.hashCode());
		SimulateEngine tmp;
		HashMap<Integer, HashSet<String>> resl;
		for (BackwardContext var : this.getBcs()) {
			tmp = new SimulateEngine(dg, var);
			tmp.simulate();
			resl = mergeResult(var, tmp);

			simulatedRess.put(resl, var);
			result.add(resl);
		}

	}

	public HashMap<Integer, HashSet<String>> mergeResult(BackwardContext var, SimulateEngine tmp) {
		HashMap<Value, HashSet<String>> sval = tmp.getCurrentValues();
		HashMap<Integer, HashSet<String>> resl = new HashMap<Integer, HashSet<String>>();
		Value reg;
		for (int i : target_regs) {
			if (i == TargetType.RIGHTPART.getType()) {
				reg = ((AssignStmt) var.getStmtPathTail()).getRightOp();
			} else if (i == TargetType.BASEOBJECT.getType()) {
				reg = ((soot.jimple.InstanceInvokeExpr) ((Stmt) var.getStmtPathTail()).getInvokeExpr()).getBase();
			} else {
				reg = ((Stmt) var.getStmtPathTail()).getInvokeExpr().getArg(i);
			}

			if (sval.containsKey(reg)) {
				resl.put(i, sval.get(reg));
			} else if (reg instanceof StringConstant) {
				resl.put(i, new HashSet<String>());
				resl.get(i).add(((StringConstant) reg).value);
			} else if (reg instanceof IntConstant) {
				resl.put(i, new HashSet<String>());
				resl.get(i).add(((IntConstant) reg).value + "");
			} else if (reg instanceof LongConstant) {
				resl.put(i, new HashSet<String>());
				resl.get(i).add(((LongConstant) reg).value + "");
			} else if (reg instanceof FloatConstant) {
				resl.put(i, new HashSet<String>());
				resl.get(i).add(((FloatConstant) reg).value + "");
			} else if (reg instanceof DoubleConstant) {
				resl.put(i, new HashSet<String>());
				resl.get(i).add(((DoubleConstant) reg).value + "");
			}
		}
		return resl;
	}

	@Override
	public boolean inited() {
		return inited;
	}

	@Override
	public void initIfHavenot() {
		inited = true;

		bcs = BackwardController.getInstance().doBackWard(this, dg);

	}

	@Override
	public ArrayList<HashMap<Integer, HashSet<String>>> getResult() {
		return result;
	}


	// Bruce
	public static List<ValuePoint> find(DGraph dg, String mtd, Hashtable<String, String> instrs, int maxsize) {
		List<ValuePoint> vps = new ArrayList<ValuePoint>();
		List<StmtPoint> sps;
		List<Integer> regIndex;
		String signature;

		for (String tins : instrs.keySet()) {
			sps = null;
			regIndex = new ArrayList<>();
			signature = getSignature(tins);
			sps = StmtPoint.findCaller(signature);

			if (sps.size() == 0) {
				System.out.println("NO Caller");
				continue;
			}

			if (tins.contains(
					"<android.text.TextUtils: boolean equals(java.lang.CharSequence,java.lang.CharSequence)>")) {
				if (tins.contains(">(taintedVariable")) {
					regIndex.add(1);
				} else {
					regIndex.add(0);
				}
			} else {
				if (tins.contains("taintedVariable.<")) {
					regIndex.add(0);
				} else {
					regIndex.add(-2);
				}
			}

			ValuePoint tmp;

			for (StmtPoint sp : sps) {
				if (sp.getMethod_location().toString().equals(mtd)
						&& instrs.get(tins).equals(sp.getInstruction_location().toString().trim())) {
					tmp = new ValuePoint(dg, sp.getMethod_location(), sp.getBlock_location(),
							sp.getInstruction_location(), regIndex);
					tmp.setAppendix(signature);
					vps.add(tmp);
					if (maxsize != -1 && vps.size() >= maxsize)
						break;
				}
			}

		}

		return vps;
	}

	public static String getSignature(String instruction) {
		String signature = "";
		if (instruction.contains("<java.lang.Object: boolean equals(java.lang.Object)>")) {
			signature = "<java.lang.Object: boolean equals(java.lang.Object)>";
		} else if (instruction.contains("<java.lang.String: boolean equals(java.lang.Object)>")) {
			signature = "<java.lang.String: boolean equals(java.lang.Object)>";
		} else if (instruction.contains("<java.lang.String: boolean contains(java.lang.CharSequence)>")) {
			signature = "<java.lang.String: boolean contains(java.lang.CharSequence)>";
		} else if (instruction.contains("<java.lang.String: boolean contentEquals(java.lang.StringBuffer)>")) {
			signature = "<java.lang.String: boolean contentEquals(java.lang.StringBuffer)>";
		} else if (instruction.contains("<java.lang.String: boolean contentEquals(java.lang.CharSequence)>")) {
			signature = "<java.lang.String: boolean contentEquals(java.lang.CharSequence)>";
		} else if (instruction.contains("<java.lang.String: boolean equalsIgnoreCase(java.lang.String)>")) {
			signature = "<java.lang.String: boolean equalsIgnoreCase(java.lang.String)>";
		} else if (instruction.contains("<java.lang.String: boolean equalsIgnoreCase(java.lang.String)>")) {
			signature = "<java.lang.String: boolean equalsIgnoreCase(java.lang.String)>";
		} else if (instruction.contains("<java.lang.String: int indexOf(java.lang.String)>")) {
			signature = "<java.lang.String: int indexOf(java.lang.String)>";
		} else if (instruction.contains("<java.lang.String: int lastIndexOf(java.lang.String)>")) {
			signature = "<java.lang.String: int lastIndexOf(java.lang.String)>";
		} else if (instruction.contains("<java.lang.StringBuffer: int indexOf(java.lang.String)>")) {
			signature = "<java.lang.StringBuffer: int indexOf(java.lang.String)>";
		} else if (instruction.contains("<java.lang.StringBuffer: int lastIndexOf(java.lang.String)>")) {
			signature = "<java.lang.StringBuffer: int lastIndexOf(java.lang.String)>";
		} else if (instruction
				.contains("<android.text.TextUtils: boolean equals(java.lang.CharSequence,java.lang.CharSequence)>")) {
			signature = "<android.text.TextUtils: boolean equals(java.lang.CharSequence,java.lang.CharSequence)>";
		} else if (instruction.contains("<java.util.HashMap: boolean containsKey(java.lang.Object)>")) {
			signature = "<java.util.HashMap: boolean containsKey(java.lang.Object)>";
		}
		return signature;
	}


	

	public static boolean hasVP(List<ValuePoint> vps, ValuePoint vp) {
		for (ValuePoint lvp : vps) {
			if (lvp.getMethod_location().toString().equals(vp.getMethod_location().toString())
					&& (lvp.getInstruction_location().toString().equals(vp.getInstruction_location().toString()))) {
				return true;
			}
		}
		return false;
	}

	public void print() {

		System.out.println("===============================================================");
		System.out.println("Class: " + method_location.getDeclaringClass().toString());
		System.out.println("Method: " + method_location.toString());
		System.out.println("Bolck: ");
		block_location.forEach(u -> {
			System.out.println("       " + u);
		});
		target_regs.forEach(u -> {
			System.out.println("              " + u);
		});

	}

	public String toString() {
		if (!inited)
			return super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append("===========================");
		sb.append(this.hashCode());
		sb.append("===========================\n");
		sb.append("Class: " + method_location.getDeclaringClass().toString() + "\n");
		sb.append("Method: " + method_location.toString() + "\n");
		sb.append("Target: " + instruction_location.toString() + "\n");
		sb.append("Solved: " + hasSolved() + "\n");
		sb.append("Depend: ");
		for (IDGNode var : this.getDependents()) {
			sb.append(var.hashCode());
			sb.append(", ");
		}
		sb.append("\n");
		sb.append("BackwardContexts: \n");
		BackwardContext tmp;
		for (int i = 0; i < this.bcs.size(); i++) {
			tmp = this.bcs.get(i);
			sb.append("  " + i + " " + tmp.hashCode() + "\n");
			for (StmtPoint stmt : tmp.getExecTrace()) {
				sb.append("    " + stmt.getInstruction_location() + "\n");
			}
			
		}
		sb.append("ValueSet: \n");
		for (HashMap<Integer, HashSet<String>> resl : result) {
			sb.append("  ");
			for (int i : resl.keySet()) {
				sb.append(" |" + i + ":");
				for (String str : resl.get(i)) {
					sb.append(str + ",");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public JSONObject toJson() {
		JSONObject js = new JSONObject();
		JSONObject tmp;
		for (HashMap<Integer, HashSet<String>> var : this.getResult()) {
			tmp = new JSONObject();
//			tmp.put("CorrespondingBackwardContext", simulatedRess.get(var).hashCode());
			for (int i : var.keySet()) {
				for (String str : var.get(i)) {
					tmp.append(i + "", str);
				}
			}
			js.append("values", tmp);
		}
		if (bcs != null)
			for (BackwardContext bc : bcs) {
//				js.append("BackwardContexts", bc.toJson());
			}

		for (DataSourceType i : this.getDataSrcs()) {
			js.append("src", i.name());
		}

//		js.put("hashCode", this.hashCode() + "");
		js.put("method", this.getMethod_location().toString());
//		js.put("Block", this.getBlock_location().hashCode());
		js.put("unit", this.getInstruction_location());
//		js.put("UnitHash", this.getInstruction_location().hashCode());
//		js.put("appendix", appendix);

		return js;
	}

	@Override
	public Set<IDGNode> getDirectAndIndirectDependents(Set<IDGNode> ret) {
		// TODO Auto-generated method stub
		for (IDGNode i : this.getDependents()) {
			if (!ret.contains(i)) {
				ret.add(i);
				i.getDirectAndIndirectDependents(ret);
			}
		}
		return ret;
	}

	@Override
	public HashSet<DataSourceType> getDataSrcs() {
		Set<IDGNode> dps = new HashSet<IDGNode>();
		getDirectAndIndirectDependents(dps);

		HashSet<DataSourceType> toRet = new HashSet<DataSourceType>();
		for (IDGNode i : dps) {
			if (i instanceof ValuePoint) {
				for (BackwardContext bc : ((ValuePoint) i).getBcs()) {
					toRet.addAll(bc.getDataSrcs());
				}
			}
		}

		for (BackwardContext bc : this.getBcs()) {
			toRet.addAll(bc.getDataSrcs());
		}
		return toRet;
	}

	@Override
	public HashSet<String> getDataHcStrings() {
		// TODO Auto-generated method stub
		Set<IDGNode> dps = new HashSet<IDGNode>();
		getDirectAndIndirectDependents(dps);

		HashSet<String> toRet = new HashSet<String>();
		for (IDGNode i : dps) {
			if (i instanceof ValuePoint) {
				for (BackwardContext bc : ((ValuePoint) i).getBcs()) {
					toRet.addAll(bc.getHcStrings());
				}
			}
		}

		for (BackwardContext bc : this.getBcs()) {
			toRet.addAll(bc.getHcStrings());
		}
		return toRet;
	}

}
