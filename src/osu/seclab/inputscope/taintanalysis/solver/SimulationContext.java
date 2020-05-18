package osu.seclab.inputscope.taintanalysis.solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.Block;

public class SimulationContext {
	SootMethod methodLocation;
	Block blockLocation;
	Unit instructionLocation;

	HashSet<Value> intrestedVariable;
//	Bruce
	Value curInterestedVariable=null;


	ArrayList<Block> blockTrace;
	ArrayList<StmtItem> instructionTrace;

	Stack<CallStackItem> callStack;

	boolean terminated = false;

	boolean containsSink = false;

	@SuppressWarnings("unchecked")
	public SimulationContext(SimulationContext src) {
		this.methodLocation = src.getMethodLocation();
		this.blockLocation = src.getBlockLocation();
		this.instructionLocation = src.getInstructionLocation();
		this.intrestedVariable = (HashSet<Value>) src.getIntrestedVariable().clone();
		this.blockTrace = (ArrayList<Block>) src.getBlockTrace().clone();
		this.instructionTrace = (ArrayList<StmtItem>) src.getInstructionTrace().clone();
		callStack = (Stack<CallStackItem>) src.getCallStack().clone();

		this.containsSink = src.isContainsSink();
	}

	public SimulationContext(SootMethod methodLocation, Block blockLocation, Unit instructionLocation) {
		this.intrestedVariable = new HashSet<Value>();
		this.instructionTrace = new ArrayList<StmtItem>();
		this.blockTrace = new ArrayList<Block>();
		this.callStack = new Stack<CallStackItem>();

		this.methodLocation = methodLocation;
		this.setBlockLocation(blockLocation);
		this.instructionLocation = instructionLocation;
		add2InstructionTrace(methodLocation, blockLocation, instructionLocation, true);
	}

	public SootMethod getMethodLocation() {
		return methodLocation;
	}

	public void setMethodLocation(SootMethod methodLocation) {
		this.methodLocation = methodLocation;
	}

	public Block getBlockLocation() {
		return blockLocation;
	}

	public void setBlockLocation(Block blockLocation) {
		this.blockLocation = blockLocation;
		blockTrace.add(blockLocation);
	}

	public Unit getInstructionLocation() {
		return instructionLocation;
	}

	public void setInstructionLocation(Unit instructionLocation, boolean containsInteresting) {
		this.instructionLocation = instructionLocation;
	}
//	public void setInstructionLocation(Unit instructionLocation, boolean containsInteresting, Value iv) {
//		this.instructionLocation = instructionLocation;
//		this.curInterestedVariable = iv;
//	}

	public boolean isTerminated() {
		return terminated || intrestedVariable.isEmpty();
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

	public HashSet<Value> getIntrestedVariable() {
		return intrestedVariable;
	}

	public void setIntrestedVariable(HashSet<Value> intrestedVariable) {
		this.intrestedVariable = intrestedVariable;
	}

	public void addIntrestedVariable(Value intrestedVariable) {
		this.intrestedVariable.add(intrestedVariable);
	}

	public boolean isIntrested(Value intr) {
		return this.intrestedVariable.contains(intr);
	}

	public boolean containsIntrested(List<Value> intrs) {
		for (Value val : intrs)
			if (isIntrested(val))
				return true;

		return false;
	}

	public ArrayList<Block> getBlockTrace() {
		return blockTrace;
	}

	public void setBlockTrace(ArrayList<Block> blockTrace) {
		this.blockTrace = blockTrace;
	}

	public ArrayList<StmtItem> getInstructionTrace() {
		return instructionTrace;
	}

	public void setInstructionTrace(ArrayList<StmtItem> instructionTrace) {
		this.instructionTrace = instructionTrace;
	}

	public void add2InstructionTrace(SootMethod methodLocation, Block blockLocation, Unit instructionLocation, boolean containsInteresting) {
		StmtItem sItem = new StmtItem(methodLocation, blockLocation, instructionLocation);
		sItem.setContainsInteresting(containsInteresting);
		this.getInstructionTrace().add(sItem);
	}

	public boolean isContainsSink() {
		return containsSink;
	}

//	public void setContainsSink(boolean containsSink) {
//		this.containsSink = containsSink;
//		this.getInstructionTrace().get(this.getInstructionTrace().size() - 1).setContainsSink(containsSink);
//	}
	
	public void setContainsSink(boolean containsSink, Value curIntst) {
		this.containsSink = containsSink;
//		this.getInstructionTrace().get(this.getInstructionTrace().size() - 1).setContainsSink(containsSink);
		this.getInstructionTrace().get(this.getInstructionTrace().size() - 1).setContainsSink(containsSink, curIntst);
	}

	public Stack<CallStackItem> getCallStack() {
		return callStack;
	}

	public void setCallStack(Stack<CallStackItem> callStack) {
		this.callStack = callStack;
	}
	
	public Value getCurInterestedVariable() {
		return curInterestedVariable;
	}

	public void setCurInterestedVariable(Value curInterestedVariable) {
		this.curInterestedVariable = curInterestedVariable;
	}

}
