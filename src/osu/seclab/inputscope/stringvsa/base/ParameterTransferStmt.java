package osu.seclab.inputscope.stringvsa.base;

import java.util.ArrayList;
import java.util.List;

import soot.Unit;
import soot.UnitBox;
import soot.UnitPrinter;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.VariableBox;
import soot.tagkit.Host;
import soot.tagkit.Tag;
import soot.util.Switch;

public class ParameterTransferStmt implements AssignStmt {

	private static final long serialVersionUID = 1L;
	Value left;
	Value right;

	public ParameterTransferStmt(Value left, Value right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("%s = %s", left, right);
	}

	@Override
	public Value getLeftOp() {
		// TODO Auto-generated method stub
		return left;
	}

	@Override
	public Value getRightOp() {
		// TODO Auto-generated method stub
		return right;
	}

	@Override
	public ValueBox getLeftOpBox() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueBox getRightOpBox() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void toString(UnitPrinter up) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsInvokeExpr() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InvokeExpr getInvokeExpr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueBox getInvokeExprBox() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsArrayRef() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayRef getArrayRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueBox getArrayRefBox() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsFieldRef() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FieldRef getFieldRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueBox getFieldRefBox() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ValueBox> getUseBoxes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ValueBox> getDefBoxes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UnitBox> getUnitBoxes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UnitBox> getBoxesPointingToThis() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBoxPointingToThis(UnitBox b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeBoxPointingToThis(UnitBox b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearUnitBoxes() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ValueBox> getUseAndDefBoxes() {
		
		List<ValueBox> ret = new ArrayList<ValueBox>();
		ret.add(new LinkedVariableBox(left));
		ret.add(new LinkedVariableBox(right));
		return ret;
	}

	private static class LinkedVariableBox extends VariableBox {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private LinkedVariableBox(Value v) {
			super(v);
		}

		public boolean canContainValue(Value v) {

			return true;
		}
	}

	@Override
	public boolean fallsThrough() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean branches() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void redirectJumpsToThisTo(Unit newLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void apply(Switch sw) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Tag> getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tag getTag(String aName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTag(Tag t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeTag(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasTag(String aName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAllTags() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAllTagsOf(Host h) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getJavaSourceStartLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getJavaSourceStartColumnNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLeftOp(Value variable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRightOp(Value rvalue) {
		// TODO Auto-generated method stub

	}

	public ParameterTransferStmt clone() {
		return new ParameterTransferStmt(this.getLeftOp(), this.getRightOp());
	}

}
