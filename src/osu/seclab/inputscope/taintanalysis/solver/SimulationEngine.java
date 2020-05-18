package osu.seclab.inputscope.taintanalysis.solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import osu.seclab.inputscope.taintanalysis.base.TaintQuestion;
import osu.seclab.inputscope.taintanalysis.utility.BlockGenerator;
import osu.seclab.inputscope.taintanalysis.utility.Logger;
import osu.seclab.inputscope.taintanalysis.utility.MethodUtility;
import soot.Body;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.CompleteBlockGraph;

public class SimulationEngine extends AbstractStmtSwitch {

	static SimulationEngine se = new SimulationEngine();

	private SimulationEngine() {
	}

	public static SimulationEngine getInstance() {
		return se;
	}

	TaintQuestion tq;

	public List<SimulationContext> oneStepForward(TaintQuestion tq, SimulationContext sContext) {
		this.tq = tq;
		Unit nextInstrct = null;
		if (sContext.getInstructionLocation() == null) {
			nextInstrct = sContext.getBlockLocation().getHead();
		} else {
			nextInstrct = sContext.getBlockLocation().getSuccOf(sContext.getInstructionLocation());
		}
		// Logger.print(nextInstrct + "");
		if (nextInstrct != null) {
			return oneStepForward(tq, sContext, nextInstrct);
		} else {
			Logger.print(sContext.getBlockLocation().getIndexInMethod() + "=>>");
			List<Block> processableBlocks = getProcessableBlocks(sContext);
			for (Block b : sContext.getBlockTrace())
				Logger.print("t:" + b.getIndexInMethod() + "\t" + b.getBody().getMethod());
			for (Block b : processableBlocks)
				Logger.print(b.getIndexInMethod() + "");
			// if(processableBlocks.size() !=
			// sContext.getBlockLocation().getSuccs().size())
			// System.exit(0);
			if (!processableBlocks.isEmpty()) {
				return oneStepForward2EachBlock(tq, sContext, processableBlocks);
			} else {
				// backtofunc
				sContext.setTerminated(true);
			}
		}

		return new ArrayList<SimulationContext>();
	}

	private List<Block> getProcessableBlocks(SimulationContext sContext) {
		List<Block> allBlocks = sContext.getBlockLocation().getSuccs();
		List<Block> bs = new ArrayList<Block>();
		Block tmp;
		boolean canprocess;
		for (Block block : allBlocks) {
			canprocess = true;
			HashSet<Block> hist = new HashSet<Block>();
			ListIterator<Block> li = sContext.getBlockTrace().listIterator(sContext.getBlockTrace().size());
			li.previous();// skip current
			while (li.hasPrevious()) {
				tmp = li.previous();
				if (!tmp.getBody().equals(sContext.getBlockLocation().getBody()))
					break;

				if (tmp == block) {
					if (hist.contains(tmp)) {
						canprocess = false;
						break;
					} else
						hist.add(block);
				}
			}

			if (canprocess)
				bs.add(block);
		}
		return bs;
	}

	private List<SimulationContext> oneStepForward2EachBlock(TaintQuestion tq, SimulationContext sContext,
			List<Block> processableBlocks) {
		List<SimulationContext> newBc = new ArrayList<SimulationContext>();
		List<Block> sBlocks = processableBlocks;// sContext.getBlockLocation().getSuccs();

		for (int i = 1; i < sBlocks.size(); i++) {
			SimulationContext tsc = new SimulationContext(sContext);
			tsc.setBlockLocation(sBlocks.get(i));

			newBc.add(tsc);
			newBc.addAll(oneStepForward(tq, tsc, tsc.getBlockLocation().getHead()));
		}

		sContext.setBlockLocation(sBlocks.get(0));
		newBc.addAll(oneStepForward(tq, sContext, sContext.getBlockLocation().getHead()));

		return newBc;
	}

	SimulationContext sContext;

	private List<SimulationContext> oneStepForward(TaintQuestion tq, SimulationContext sContext,
			Unit currentInstruction) {
		this.sContext = sContext;
		List<SimulationContext> newBc = new ArrayList<SimulationContext>();

		Logger.print("inst:(" + sContext.hashCode() + ")(" + sContext.getBlockLocation().getIndexInMethod() + ")"
				+ currentInstruction);
		for (CallStackItem csi : sContext.getCallStack()) {
			Logger.print("    " + csi.getSmethd());
		}

		// for (Value value : sContext.getIntrestedVariable()) {
		// Logger.print("iv:" + value);
		// }
		boolean containsIntrestedThings = false;
		for (ValueBox box : currentInstruction.getUseAndDefBoxes()) {
			// Logger.print("vb:" + box.getValue());
			if (sContext.getIntrestedVariable().contains(box.getValue())) {
				containsIntrestedThings = true;
				sContext.setCurInterestedVariable(box.getValue());
//				sContext.setInstructionLocation(currentInstruction, containsIntrestedThings, box.getValue());
				break;
			} else if (box.getValue() instanceof ArrayRef
					&& sContext.getIntrestedVariable().contains(((ArrayRef) box.getValue()).getBase())) {
				containsIntrestedThings = true;
				sContext.setCurInterestedVariable(((ArrayRef) box.getValue()).getBase());
//				sContext.setInstructionLocation(currentInstruction, containsIntrestedThings, box.getValue());
				break;
			}
		}

		if (containsIntrestedThings) {
//			System.out.println("Found:\n" + currentInstruction);
//			String sbb = "";
//			for (Value sb : sContext.getIntrestedVariable()) {
//				sbb += sb.toString() + " ";
//			}
//			System.out.println("\t" + sbb);
		} else {
			// System.out.println("Not Found:\n" + currentInstruction);
		}

		sContext.setInstructionLocation(currentInstruction, containsIntrestedThings);

		if (!containsIntrestedThings && !(currentInstruction instanceof ReturnStmt)
				&& !(currentInstruction instanceof ReturnVoidStmt))
			return newBc;
		sContext.add2InstructionTrace(sContext.getMethodLocation(), sContext.getBlockLocation(), currentInstruction,
				containsIntrestedThings);

		// Logger.print("apply");
		// System.exit(0);
		currentInstruction.apply(this);

		return newBc;
	}

	// InstanceInvokeExpr
	public boolean handleInvokeStmt(Value assiTo, InvokeExpr invokExpr) {

		if (tq.isSink(invokExpr.getMethod())) {
//			this.sContext.setContainsSink(true);
			this.sContext.setContainsSink(true, this.sContext.getCurInterestedVariable());
		}

		if (!this.sContext.containsIntrested(invokExpr.getArgs())) {
			if (invokExpr instanceof InstanceInvokeExpr) {
				if (!this.sContext.isIntrested(((InstanceInvokeExpr) invokExpr).getBase())) {
					// if (assiTo != null)
					// this.sContext.getIntrestedVariable().remove(assiTo);
					return false;
				}
			} else if (invokExpr instanceof StaticInvokeExpr) {
				// if (assiTo != null)
				// this.sContext.getIntrestedVariable().remove(assiTo);
				return false;
			}
		}

		String mthSig = invokExpr.getMethod().toString();
		Value base = null;
		boolean interestinRet = false;
		if (mthSig.equals("<java.lang.StringBuilder: java.lang.String toString()>")
				|| mthSig.equals("<java.lang.String: java.lang.String substring(int,int)>")
				|| mthSig.equals("<java.lang.String: java.lang.String toString()>")
				|| mthSig.equals("<java.lang.Object: java.lang.String toString()>")
				|| mthSig.equals("<java.lang.String: java.lang.String trim()>")
				|| mthSig.equals("<java.lang.String: java.lang.String toLowerCase()>")
				|| mthSig.equals("<java.lang.String: java.lang.String toUpperCase()>")
				|| mthSig.equals("<java.lang.String: java.lang.String[] split(java.lang.String)>")) {
			base = ((InstanceInvokeExpr) invokExpr).getBase();
			if (this.sContext.isIntrested(base)) {
				interestinRet = true;
			}
		} else if (mthSig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")) {
			base = ((VirtualInvokeExpr) invokExpr).getBase();
			if (this.sContext.isIntrested(base) || this.sContext.containsIntrested(invokExpr.getArgs())) {
				interestinRet = true;
			}
		} else if (mthSig
				.equals("<android.text.TextUtils: boolean equals(java.lang.CharSequence,java.lang.CharSequence)>")) {

		} else {

			if (this.sContext.containsIntrested(invokExpr.getArgs())
					&& invokExpr.getMethod().getDeclaringClass().isApplicationClass()
					&& invokExpr.getMethod().isConcrete()) {

				this.jump2callee(assiTo, invokExpr);

			} else {
				defaultCase(invokExpr);
			}

		}
		/*
		 * if (assiTo != null) { if (interestinRet) {
		 * this.sContext.addIntrestedVariable(assiTo); } else {
		 * this.sContext.getIntrestedVariable().remove(assiTo); } }
		 */
		return interestinRet;
	}

	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
		handleInvokeStmt(null, stmt.getInvokeExpr());
	}

	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		// TODO Auto-generated method stub

		Value lop = stmt.getLeftOp();
		Value rop = stmt.getRightOp();
		boolean interestinRet = false;
		if (rop instanceof InvokeExpr) {
			interestinRet = handleInvokeStmt(lop, stmt.getInvokeExpr());
		} else {
			defaultCase(stmt);
		}

		if (interestinRet) {
			this.sContext.addIntrestedVariable(lop);
		} else {
			this.sContext.getIntrestedVariable().remove(lop);
		}
	}

	@Override
	public void caseReturnStmt(ReturnStmt stmt) {
		// TODO Auto-generated method stub
		boolean interestinRet = false;
		if (stmt != null && this.sContext.isIntrested(stmt.getOp())) {
			interestinRet = true;
		}

		for (ValueBox vb : this.sContext.getBlockLocation().getBody().getUseAndDefBoxes()) {
			this.sContext.intrestedVariable.remove(vb.getValue());
		}

		back2caller(interestinRet);
		// TODO jumpback
	}

	@Override
	public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
		// TODO Auto-generated method stub
		caseReturnStmt(null);
	}

	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		// TODO Auto-generated method stub
		if (stmt.getRightOp() instanceof ParameterRef) {
			if (this.sContext.isIntrested(stmt.getRightOp())) {
				this.sContext.addIntrestedVariable(stmt.getLeftOp());
			}
		} else
			defaultCase(stmt);
	}

	@Override
	public void defaultCase(Object obj) {
		// TODO Auto-generated method stub
		Logger.printW("unsupported stmt:" + obj);
	}

	// //////////////////////////////////////////////////
	// handle call

	private void jump2callee(Value assiTo, InvokeExpr invokExpr) {
		CallStackItem citem = new CallStackItem(this.sContext.getMethodLocation(), this.sContext.getBlockLocation(),
				this.sContext.getInstructionLocation(), assiTo);

		ParameterRef pref;
		for (int i = 0; i < invokExpr.getArgs().size(); i++) {
			if (this.sContext.isIntrested(invokExpr.getArgs().get(i))) {
				pref = MethodUtility.getParameterRef(invokExpr.getMethod(), i);
				this.sContext.addIntrestedVariable(pref);
			}
		}
		this.sContext.getCallStack().push(citem);

		this.sContext.setMethodLocation(invokExpr.getMethod());
		Body body = this.sContext.getMethodLocation().retrieveActiveBody();
		CompleteBlockGraph cbg = BlockGenerator.getInstance().generate(body);
		// for (Block b : cbg.getHeads()) {
		//
		// }

		if (cbg.getHeads().size() > 1) {
			Logger.printW("header size > 1");
		}

		this.sContext.setBlockLocation(cbg.getHeads().get(0));
		this.sContext.setInstructionLocation(null, false);

	}

	private void back2caller(boolean interestinRet) {
		if (this.sContext.getIntrestedVariable().isEmpty())
			this.sContext.setTerminated(true);
		else {
			if (this.sContext.callStack.isEmpty()) {
				Logger.printW("unimplemented return to outside");
				this.sContext.setTerminated(true);
			} else {
				CallStackItem citem = this.sContext.callStack.pop();

				this.sContext.setMethodLocation(citem.getSmethd());
				this.sContext.setBlockLocation(citem.getBlcok());
				this.sContext.setInstructionLocation(citem.getCurrentInstruction(), true);

				if (interestinRet && citem.getReturnTarget() != null) {
					this.sContext.addIntrestedVariable(citem.getReturnTarget());
				}
			}
		}
	}
}
