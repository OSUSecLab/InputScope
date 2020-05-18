package osu.seclab.inputscope.stringvsa.forwardexec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.Constant;
import soot.jimple.DoubleConstant;
import soot.jimple.FieldRef;
import soot.jimple.FloatConstant;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LongConstant;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JAndExpr;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JDivExpr;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JOrExpr;
import soot.jimple.internal.JShlExpr;
import soot.jimple.internal.JShrExpr;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JSubExpr;
import soot.jimple.internal.JUshrExpr;
import soot.jimple.internal.JXorExpr;
import osu.seclab.inputscope.stringvsa.base.GlobalStatistics;
import osu.seclab.inputscope.stringvsa.base.ParameterTransferStmt;
import osu.seclab.inputscope.stringvsa.base.StmtPoint;
import osu.seclab.inputscope.stringvsa.graph.DGraph;
import osu.seclab.inputscope.stringvsa.graph.HeapObject;
import osu.seclab.inputscope.stringvsa.main.ApkContext;
import osu.seclab.inputscope.stringvsa.utility.FunctionUtility;
import osu.seclab.inputscope.stringvsa.utility.Logger;
import osu.seclab.inputscope.stringvsa.utility.OtherUtility;

public class SimulateEngine extends AbstractStmtSwitch {
	DGraph dg;
	StmtPath spath;
	HashMap<Value, HashSet<String>> currentValues = new HashMap<Value, HashSet<String>>();

	public SimulateEngine(DGraph dg, StmtPath spath) {
		this.dg = dg;
		this.spath = spath;
	}

	public StmtPath getSpath() {
		return spath;
	}

	public HashMap<Value, HashSet<String>> getCurrentValues() {
		return currentValues;
	}

	public void setInitValue(Value val, String str, boolean append) {
		HashSet<String> tmp;
		if (!this.getCurrentValues().containsKey(val)) {
			tmp = new HashSet<String>();
			this.getCurrentValues().put(val, tmp);
		} else {
			tmp = this.getCurrentValues().get(val);
		}
		if (!append) {
			tmp.clear();
		}
		tmp.add(str);
	}

	public void setArrValue(Value val, String str) {
		HashSet<String> tmp;
		if (!(val instanceof ArrayRef))
			return;
		if (!this.getCurrentValues().containsKey(((ArrayRef) val).getBase())) {
			tmp = new HashSet<String>();
			this.getCurrentValues().put(((ArrayRef) val).getBase(), tmp);
		} else {
			tmp = this.getCurrentValues().get(((ArrayRef) val).getBase());
		}

		tmp.add("element->" + str);
	}

	@SuppressWarnings("unchecked")
	public void transferValues(Value from, Value to) {
		HashSet<String> vs = null;
		if (from instanceof Constant) {
			vs = getContent(from);
		} else
			vs = this.getCurrentValues().get(from);
		this.getCurrentValues().remove(to);

		if (vs != null) {
			this.getCurrentValues().put(to, (HashSet<String>) vs.clone());
		}
	}
	
	public void transferValuesRightArr(Value from, Value to, String addInfo) {
		
		
		HashSet<String> vs = null;
		if (from instanceof Constant) {
			vs = getContent(from);
		} else
			vs = this.getCurrentValues().get(from);
		
		if (vs == null){
			vs = new HashSet<>();	
		}
		
		vs.add(addInfo);
		
		this.getCurrentValues().remove(to);

		if (vs != null) {
			this.getCurrentValues().put(to, (HashSet<String>) vs.clone());
		}
	}

	@SuppressWarnings("unchecked")
	public void transferValues_compute(Value to, String func, Value... args) {
		HashSet<String> vs = new HashSet<String>();

		if (func.equals("replaceFirst")) {
			HashSet<String> vsB = getContent(args[0]);
			HashSet<String> vs0 = getContent(args[1]);
			HashSet<String> vs1 = getContent(args[2]);

			for (String sb : vsB) {
				for (String s0 : vs0) {
					for (String s1 : vs1) {
						vs.add(sb.replaceFirst(s0, s1));
					}
				}
			}

		} else if (func.equals("replace")) {
			HashSet<String> vsB = getContent(args[0]);
			HashSet<String> vs0 = getContent(args[1]);
			HashSet<String> vs1 = getContent(args[2]);

			for (String sb : vsB) {
				for (String s0 : vs0) {
					for (String s1 : vs1) {
						vs.add(sb.replace(s0, s1));
					}
				}
			}

		}

		this.getCurrentValues().remove(to);
		if (vs != null) {
			this.getCurrentValues().put(to, (HashSet<String>) vs.clone());
		}
	}

	@SuppressWarnings("unchecked")
	public void transferValuesAndAppend(Stmt stmt, Value from, Value to, Value arg, boolean apdontheold,
			boolean delold) {
		if (!this.getCurrentValues().containsKey(from)) {
			this.getCurrentValues().remove(to);
			return;
		}

		HashSet<String> apds = null;
		if (this.getCurrentValues().containsKey(arg)) {
			apds = this.getCurrentValues().get(arg);
		} else if (arg instanceof Constant) {
			apds = new HashSet<String>();
			if (arg instanceof StringConstant) {
				apds.add(((StringConstant) arg).value);
			} else if (arg instanceof IntConstant) {
				apds.add(((IntConstant) arg).value + "");
			} else if (arg instanceof LongConstant) {
				apds.add(((LongConstant) arg).value + "");
			} else if (arg instanceof FloatConstant) {
				apds.add(((FloatConstant) arg).value + "");
			} else if (arg instanceof DoubleConstant) {
				apds.add(((DoubleConstant) arg).value + "");
			}

		} else {
			Logger.printW(String.format("[%s] [SIMULATE][transferValuesAndAppend arg unknow]: %s (%s)", this.hashCode(),
					stmt, arg.getClass()));
			apds = new HashSet<String>();
			apds.add(String.format("[unknown:%s]", arg.getClass()));
			return;
		} //

		HashSet<String> vs = this.getCurrentValues().get(from);
		HashSet<String> newValues = new HashSet<String>();

		for (String apd : apds) {
			for (String str : vs) {
				newValues.add(str + apd);
			}
		}

		if (apdontheold) {
			this.getCurrentValues().put(from, newValues);
		}

		if (delold) {
			this.getCurrentValues().remove(from);
		}

		this.getCurrentValues().put(to, (HashSet<String>) newValues.clone());
	}

	@SuppressWarnings("unchecked")
	public void transferValuesAndCompute(Stmt stmt, Value to, Value op1, Value op2, BinopExpr exp) {

		HashMap<Value, HashSet<String>> ops = new HashMap<Value, HashSet<String>>();

		for (Value value : new Value[] { op1, op2 }) {
			HashSet<String> apds = null;
			if (this.getCurrentValues().containsKey(value)) {
				apds = this.getCurrentValues().get(value);
			} else if (value instanceof Constant) {
				apds = new HashSet<String>();
				if (value instanceof StringConstant) {
					apds.add(((StringConstant) value).value);
				} else if (value instanceof IntConstant) {
					apds.add(((IntConstant) value).value + "");
				} else if (value instanceof LongConstant) {
					apds.add(((LongConstant) value).value + "");
				} else if (value instanceof FloatConstant) {
					apds.add(((FloatConstant) value).value + "");
				} else if (value instanceof DoubleConstant) {
					apds.add(((DoubleConstant) value).value + "");
				}
			} else {
				Logger.printW(String.format("[%s] [SIMULATE][transferValuesAndCompute arg unknow]: %s (%s)",
						this.hashCode(), stmt, value.getClass()));
				apds = new HashSet<String>();
				apds.add(String.format("[unknown:%s]", value.getClass()));
				return;
			}

			ops.put(value, apds);
		}

		HashSet<String> newValues = new HashSet<String>();

		OtherUtility.computeBasedOnType(to, exp, ops.get(op1), ops.get(op2), newValues);

		this.getCurrentValues().put(to, (HashSet<String>) newValues.clone());
	}

	public String getPrintableValues() {
		StringBuilder sb = new StringBuilder();
		for (Value var : this.getCurrentValues().keySet()) {
			sb.append("    ");
			sb.append(var);
			sb.append('(');
			sb.append(var.hashCode());
			sb.append(')');
			sb.append(':');
			for (String content : this.getCurrentValues().get(var)) {
				sb.append(content);
				sb.append(",");
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	public void simulate() {
		Unit lastUnit = getSpath().getStmtPathTail();

		for (StmtPoint stmtSP : getSpath().getStmtPath()) {
			Stmt stmt = (Stmt) stmtSP.getInstruction_location();
			if (stmt == lastUnit) {
				return;
			}
			String oldv = getPrintableValues();
			Logger.print("[SIMULATE]" + this.hashCode() + ": " + stmt + " " + stmt.getClass());
			if (stmt instanceof ParameterTransferStmt) {
				caseAssignStmt((AssignStmt) stmt);
			} else {
				stmt.apply(this);
			}
			String newv = getPrintableValues();
			Logger.print("\n" + oldv + "\n====>\n" + newv);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		// TODO Auto-generated method stub
		Value leftop = stmt.getLeftOp();
		Value rightop = stmt.getRightOp();
		if (leftop instanceof Local || leftop instanceof ParameterRef || leftop instanceof ArrayRef) {
			if (rightop instanceof InvokeExpr) {
				InvokeExpr vie = (InvokeExpr) rightop;
				String msig = vie.getMethod().toString();
				if (msig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")) {
					GlobalStatistics.getInstance().countAppendString();
					transferValuesAndAppend(stmt, ((VirtualInvokeExpr) vie).getBase(), leftop, vie.getArg(0), true,
							false);
				} else if (msig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(int)>")) {
					GlobalStatistics.getInstance().countAppendString();
					transferValuesAndAppend(stmt, ((VirtualInvokeExpr) vie).getBase(), leftop, vie.getArg(0), true,
							false);
				} else if (msig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(char)>")) {
					GlobalStatistics.getInstance().countAppendString();
					transferValuesAndAppend(stmt, ((VirtualInvokeExpr) vie).getBase(), leftop, vie.getArg(0), true,
							false);
				} else if (msig.equals("<android.content.Context: java.lang.String getString(int)>")
						|| msig.equals("<android.content.res.Resources: java.lang.String getString(int)>")) {
					GlobalStatistics.getInstance().countGetString();
					if (vie.getArg(0) instanceof IntConstant) {
						try{
							setInitValue(leftop, ApkContext.getInstance().findResource(((IntConstant) vie.getArg(0)).value),
									false);
						}catch (Exception e) {
							// TODO: handle exception
						}
						
					} else if (this.getCurrentValues().get(vie.getArg(0)).size() > 0) {
						for (String str : (HashSet<String>) this.getCurrentValues().get(vie.getArg(0)).clone()) {
							this.getCurrentValues().remove(leftop);
							if (OtherUtility.isInt(str)) {
								setInitValue(leftop, ApkContext.getInstance().findResource(Integer.parseInt(str)),
										true);
							} else {
								Logger.printW(String.format(
										"[%s] [SIMULATE][arg value not int getString(VirtualInvokeExpr)]: %s (%s)",
										this.hashCode(), stmt, str));
							}
						}
					} else {
						Logger.printW(
								String.format("[%s] [SIMULATE][arg not int getString(VirtualInvokeExpr)]: %s (%s)",
										this.hashCode(), stmt, vie.getArg(0).getClass()));
					}
				} else if (msig.equals("<java.lang.StringBuilder: java.lang.String toString()>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<java.lang.Object: java.lang.String toString()>")) {
					boolean isVir = false;
					try{
						transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
						isVir = true;
					}catch (Exception e) {
						// TODO: handle exception
						
					}
					if (!isVir){
						transferValues(((InterfaceInvokeExpr) vie).getBase(), leftop);
					}
					
				} else if (msig.equals("<java.lang.CharSequence: java.lang.String toString()>")) {
					boolean isVir = false;
					try{
						transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
					}catch (Exception e) {
						// TODO: handle exception
					}
					if (!isVir){
						transferValues(((InterfaceInvokeExpr) vie).getBase(), leftop);
					}
					
				} else if (msig.equals("<java.lang.String: java.lang.String substring(int,int)>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<java.lang.String: java.lang.String trim()>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<java.lang.String: java.lang.String[] split(java.lang.String)>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<android.content.Context: java.lang.String getPackageName()>")) {
					setInitValue(leftop, ApkContext.getInstance().getPackageName(), false);
				} else if (msig.equals(
						"<android.content.res.Resources: int getIdentifier(java.lang.String,java.lang.String,java.lang.String)>")) {
					this.getCurrentValues().remove(leftop);
					for (String p1 : this.getContent(vie.getArg(0))) {
						for (String p2 : this.getContent(vie.getArg(1))) {

							setInitValue(leftop, ApkContext.getInstance().getIdentifier(p1, p2, null), true);

						}
					}

				} else if (msig
						.equals("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>")) {
					GlobalStatistics.getInstance().countFormatString();
					FunctionUtility.String_format(this, leftop, vie, vie.getArg(0), vie.getArg(1));
				} else if (msig.equals("<java.util.UUID: java.lang.String toString()>")) {
					transferValues(((InstanceInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<java.lang.Integer: java.lang.Integer valueOf(int)>")) {
					transferValues(vie.getArg(0), leftop);
				} else if (msig.equals("<android.os.ParcelUuid: android.os.ParcelUuid fromString(java.lang.String)>")) {
					transferValues(vie.getArg(0), leftop);
				} else if (msig.equals(
						"<java.lang.String: java.lang.String format(java.util.Locale,java.lang.String,java.lang.Object[])>")) {
					GlobalStatistics.getInstance().countFormatString();
					FunctionUtility.String_format(this, leftop, vie, vie.getArg(1), vie.getArg(2));
				} else if (msig.equals(
						"<java.lang.String: java.lang.String replaceFirst(java.lang.String,java.lang.String)>")) {
					GlobalStatistics.replace++;
					transferValues_compute(leftop, "replaceFirst", ((VirtualInvokeExpr) vie).getBase(), vie.getArg(0),
							vie.getArg(1));
				} else if (msig.equals(
						"<java.lang.String: java.lang.String replace(java.lang.CharSequence,java.lang.CharSequence)>")) {
					GlobalStatistics.replace++;
					transferValues_compute(leftop, "replace", ((VirtualInvokeExpr) vie).getBase(), vie.getArg(0),
							vie.getArg(1));
				} else if (msig.contains("android.view.View findViewById(int)")) {
					if (((VirtualInvokeExpr) vie).getArg(0).toString().contains("$")) {
						transferValues(vie.getArg(0), leftop);
					} else {
						setInitValue(leftop, ((VirtualInvokeExpr) vie).getArg(0).toString(), true);
					}

				} else if (msig.contains("android.view.View internalFindViewById(int)")) {
					if (((VirtualInvokeExpr) vie).getArg(0).toString().contains("$")) {
						transferValues(vie.getArg(0), leftop);
					} else {
						setInitValue(leftop, ((VirtualInvokeExpr) vie).getArg(0).toString(), true);
					}

				} else if (msig.contains(
						"android.view.LayoutInflater: android.view.View inflate(int,android.view.ViewGroup)")) {
					if (((VirtualInvokeExpr) vie).getArg(0).toString().contains("$")) {
						transferValues(vie.getArg(0), leftop);
					} else {
						setInitValue(leftop, ((VirtualInvokeExpr) vie).getArg(0).toString(), true);
					}

				} else if (msig.contains("android.view.View findRequiredView(java.lang.Object,int,java.lang.String)")) {
					if (((VirtualInvokeExpr) vie).getArg(1).toString().contains("$")) {
						transferValues(vie.getArg(1), leftop);
					} else {
						setInitValue(leftop, ((VirtualInvokeExpr) vie).getArg(1).toString(), true);
					}
					if (((VirtualInvokeExpr) vie).getArg(2).toString().contains("$")) {
						transferValues(vie.getArg(2), leftop);
					} else {
						setInitValue(leftop, ((VirtualInvokeExpr) vie).getArg(2).toString(), true);
					}

				} else if (msig
						.contains("android.view.View inflate(android.content.Context,int,android.view.ViewGroup)")) {

					if (((VirtualInvokeExpr) vie).getArg(1).toString().contains("$")) {
						transferValues(vie.getArg(1), leftop);
					} else {
						setInitValue(leftop, ((VirtualInvokeExpr) vie).getArg(1).toString(), true);
					}

				} else if (msig.contains(
						"android.view.LayoutInflater: android.view.View inflate(int,android.view.ViewGroup,boolean)")) {

					if (((VirtualInvokeExpr) vie).getArg(0).toString().contains("$")) {
						transferValues(vie.getArg(0), leftop);
					} else {
						setInitValue(leftop, ((VirtualInvokeExpr) vie).getArg(0).toString(), true);
					}

				} else if (msig.contains(
						"butterknife.internal.Utils: java.lang.Object findRequiredViewAsType(android.view.View,int,java.lang.String,java.lang.Class)")) {
					if (((VirtualInvokeExpr) vie).getArg(1).toString().contains("$")) {
						transferValues(vie.getArg(1), leftop);
					} else {
						setInitValue(leftop, ((VirtualInvokeExpr) vie).getArg(1).toString(), true);
					}
					if (((VirtualInvokeExpr) vie).getArg(2).toString().contains("$")) {
						transferValues(vie.getArg(2), leftop);
					} else {
						setInitValue(leftop, ((VirtualInvokeExpr) vie).getArg(2).toString(), true);
					}			

				} else if (msig.equals("<android.content.Intent: android.os.Bundle getExtras()>")) {
					setInitValue(leftop, "Intent_GetExtra->Bundle", true);
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.contains("<android.content.Intent") &&  msig.contains("get") && msig.contains("Extra")){
					setInitValue(leftop, "Intent_GetExtra" + ((VirtualInvokeExpr) vie).getType() + "->" + ((VirtualInvokeExpr) vie).getArg(0).toString(), true);
				} else if (msig.contains("<android.os.Bundle") && msig.contains("get") && !msig.contains("getClipData")) {
					setInitValue(leftop, "Bundle_GetExtra" + ((VirtualInvokeExpr) vie).getType() + "->" + ((VirtualInvokeExpr) vie).getArg(0).toString(), true);
				} else if (msig.equals(
						"<android.preference.PreferenceManager: android.content.SharedPreferences getDefaultSharedPreferences(android.content.Context)>")) {
					setInitValue(leftop, "SharedPreferences_GetDefault", true);
					transferValues(((VirtualInvokeExpr) vie).getArg(0), leftop);
				} else if (msig.equals("<android.content.SharedPreferences: java.lang.String getString(java.lang.String,java.lang.String)>")) {
					setInitValue(leftop, "SharedPreferences_GetString->" + ((InterfaceInvokeExpr) vie).getArg(0).toString(), true);
				} else if (msig.contains("<android.content.SharedPreferences") && msig.contains("get")) {
					setInitValue(leftop, "SharedPreferences_GetExtra" + ((VirtualInvokeExpr) vie).getType() + "->" + ((VirtualInvokeExpr) vie).getArg(0).toString(), true);
				} else if (msig.contains("android.database.sqlite.SQLiteDatabase") && msig.contains("open") && msig.contains("Database")) {
					setInitValue(leftop, "SQLiteDatabase_GetOpen->" + ((VirtualInvokeExpr) vie).getArg(0).toString(), true);
				} else if (msig.contains("android.database.Cursor") && msig.contains("rawQuery") && !msig.contains("WithFactory")) {
					setInitValue(leftop, "SQLiteDatabase_GetQuery->" + ((VirtualInvokeExpr) vie).getArg(0).toString(), true);
				}	else if (msig.contains("android.database.Cursor") && msig.contains("rawQuery") && msig.contains("WithFactory")) {
					setInitValue(leftop, "SQLiteDatabase_GetQuery->" + ((VirtualInvokeExpr) vie).getArg(1).toString(), true);
				}
				
				/*else if (msig.equals("<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>")) {
					setInitValue(leftop, "Intent_GetExtra->" + ((VirtualInvokeExpr) vie).getArg(0).toString(), false);
				} else if (msig.equals("<android.os.Bundle: boolean containsKey(java.lang.String)>")) {
					setInitValue(leftop, "Bundle_GetKey->" + ((VirtualInvokeExpr) vie).getArg(0).toString(), false);
				} else if (msig.contains("<android.os") && msig.contains("Bundle")
						&& msig.contains("java.lang.String getString(java.lang.String)>")) {
					setInitValue(leftop, "Bundle_GetString->" + ((VirtualInvokeExpr) vie).getArg(0).toString(), false);
				} else if (msig.equals(
						"<android.content.SharedPreferences: java.lang.String getString(java.lang.String,java.lang.String)>")) {
					setInitValue(leftop,
							"SharedPreferences_GetString->" + ((InterfaceInvokeExpr) vie).getArg(0).toString(), false);
				} else if (msig.contains("android.content.SharedPreferences getSharedPreferences")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.contains(
						"android.content.Context: android.database.sqlite.SQLiteDatabase openOrCreateDatabase")) {
					setInitValue(leftop, "SQLiteDatabase->" + ((VirtualInvokeExpr) vie).getArg(0).toString(), false);
				} else if (msig.equals(
						"<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQuery(java.lang.String,java.lang.String[])>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<android.database.Cursor: java.lang.String getString(int)>")) {
					transferValues(((InterfaceInvokeExpr) vie).getBase(), leftop);
				} */else if (msig.contains("android.widget.EditText: android.text.Editable getText()")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.contains("java.io.File: java.lang.String getName()")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} /*else if (msig.contains("android.app.Dialog: android.view.Window getWindow()")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.contains("android.widget.TextView: java.lang.CharSequence getText()")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("java.lang.String: int lastIndexOf(int)>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<java.lang.String: boolean equals(java.lang.Object)>")) {
					transferValues(((VirtualInvokeExpr) vie).getArg(0), leftop);
				} else if (msig.equals("<java.lang.String: boolean contains(java.lang.CharSequence)>")) {
					transferValues(((VirtualInvokeExpr) vie).getArg(0), leftop);
				} */else if (msig.equals("<java.lang.String: java.lang.String toUpperCase(java.util.Locale)>")) {
					transferValues(((VirtualInvokeExpr) vie).getArg(0), leftop);
				} else if (msig.equals("<java.util.Iterator: java.lang.Object next()>")) {
					transferValues(((InterfaceInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<java.lang.String: java.lang.String toLowerCase()>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals(
						"<butterknife.ButterKnife$Finder: android.view.View findOptionalView(java.lang.Object,int)>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<java.lang.Class: java.lang.Object cast(java.lang.Object)>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<java.util.UUID: java.util.UUID fromString(java.lang.String)>")) {
					transferValues(((StaticInvokeExpr) vie).getArg(0), leftop);
				} /*else if (msig.equals("<android.widget.LinearLayout: android.view.View getChildAt(int)>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				}*/ else {
					Logger.printW(String.format("[%s] [SIMULATE][right unknown(VirtualInvokeExpr)]: %s (%s)",
							this.hashCode(), stmt, rightop.getClass()));
				}

			} else if (rightop instanceof NewExpr) {
				if (rightop.getType().toString().equals("java.lang.StringBuilder")) {
					setInitValue(leftop, "", false);
				} else if (rightop.getType().toString().contains("android.widget.EditText")) {
					setInitValue(leftop, "New<->EditText", false);
				} else if (rightop.getType().toString().contains("java.util.ArrayList")) {

				} else {
					Logger.printW(String.format("[%s] [SIMULATE][right unknown(NewExpr)]: %s (%s)", this.hashCode(),
							stmt, rightop.getClass()));
				}
			} else if (rightop instanceof FieldRef) {
				HeapObject ho = HeapObject.getInstance(dg, ((FieldRef) rightop).getField());
				if (ho != null) {
					if (ho.inited() && ho.hasSolved()) {
						HashSet<String> nv = new HashSet<String>();
						ArrayList<HashMap<Integer, HashSet<String>>> hoResult = ho.getResult();
						for (HashMap<Integer, HashSet<String>> var : hoResult) {
							nv.addAll(var.get(-1));
						}
						this.getCurrentValues().put(leftop, nv);
					} else {
						Logger.printW(String.format("[%s] [SIMULATE][HeapObject not inited or Solved]: %s (%s)",
								this.hashCode(), stmt, ho.inited()));
					}
				} else {
					Logger.printW(String.format("[%s] [SIMULATE][HeapObject not found]: %s (%s)", this.hashCode(), stmt,
							rightop.getClass()));
				}
			} else if (rightop instanceof Local) {
				transferValues(stmt.getRightOp(), stmt.getLeftOp());
			} else if (rightop instanceof JCastExpr) {
				transferValues(((JCastExpr) rightop).getOp(), stmt.getLeftOp());
			} else if (rightop instanceof StringConstant) {
				
				if (leftop instanceof ArrayRef) {
					setArrValue(leftop, ((StringConstant) rightop).value);
				} else {
					setInitValue(leftop, ((StringConstant) rightop).value, false);
				}
			} else if (rightop instanceof IntConstant) {
				setInitValue(leftop, ((IntConstant) rightop).value + "", false);
			} else if (rightop instanceof LongConstant) {
				setInitValue(leftop, ((LongConstant) rightop).value + "", false);
			} else if (rightop instanceof FloatConstant) {
				setInitValue(leftop, ((FloatConstant) rightop).value + "", false);
			} else if (rightop instanceof DoubleConstant) {
				setInitValue(leftop, ((DoubleConstant) rightop).value + "", false);
			} else if (rightop instanceof NewArrayExpr) {
				setInitValue(leftop, ((NewArrayExpr) rightop).getSize() + "", false);
			} else if (rightop instanceof JAddExpr || rightop instanceof JSubExpr || rightop instanceof JMulExpr
					|| rightop instanceof JDivExpr || rightop instanceof JAndExpr || rightop instanceof JOrExpr
					|| rightop instanceof JShlExpr || rightop instanceof JShrExpr || rightop instanceof JUshrExpr
					|| rightop instanceof JXorExpr) {
				transferValuesAndCompute(stmt, leftop, ((BinopExpr) rightop).getOp1(), ((BinopExpr) rightop).getOp2(),
						(BinopExpr) rightop);
			} else if (rightop instanceof NewArrayExpr) {
				setInitValue(leftop, ((NewArrayExpr) rightop).getSize() + "", false);
			} else if (rightop instanceof ArrayRef) {
				
				transferValuesRightArr(((ArrayRef) rightop).getBase(), leftop, "ListCmp");
			} else {
				Logger.printW(String.format("[%s] [SIMULATE][right unknown]: %s", this.hashCode(),
						leftop.getType().toString()));
				Logger.printW(String.format("[%s] [SIMULATE][right unknown]: %s (%s)", this.hashCode(), stmt,
						rightop.getClass()));
			}
		} else {
			Logger.printW(
					String.format("[%s] [SIMULATE][left unknown]: %s (%s)", this.hashCode(), stmt, leftop.getClass()));
		}
	}

	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {

		InvokeExpr vie = stmt.getInvokeExpr();
		String msig = vie.getMethod().toString();
		if (msig.contains("java.io.File: void <init>(java.lang.String)")) {
			setInitValue(((SpecialInvokeExpr) vie).getBase(), ((SpecialInvokeExpr) vie).getArg(0).toString(), false);
		} /*else if (msig.contains("android.widget.EditText: void setHint(java.lang.CharSequence)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		} else if (msig.contains("android.widget.EditText: void setHint(int)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		} else if (msig.contains("android.widget.EditText: void setText(java.lang.CharSequence)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		} else if (msig.contains("android.widget.TextView: void setText(java.lang.CharSequence)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		} else if (msig.contains("android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setMessage(int)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		} else if (msig.contains("android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setMessage(java.lang.CharSequence)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		} else if (msig.contains(
				"android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setTitle(java.lang.CharSequence)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		} else if (msig.contains("android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setTitle(int)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		} else if (msig.contains(
				"android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setView(android.view.View)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		} else if (msig.contains("android.app.Dialog: void setContentView(int)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		}*/ else if (msig
				.contains("android.view.LayoutInflater: android.view.View inflate(int,android.view.ViewGroup)")) {
			transferValues(((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getArg(0));
		} else if (msig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")) {
			GlobalStatistics.getInstance().countAppendString();
			transferValuesAndAppend(stmt, ((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getBase(),
					((VirtualInvokeExpr) vie).getArg(0), true, false);
		} else if (msig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(int)>")) {
			GlobalStatistics.getInstance().countAppendString();
			transferValuesAndAppend(stmt, ((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getBase(),
					((VirtualInvokeExpr) vie).getArg(0), true, false);
		} else if (msig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.Object)>")) {
			GlobalStatistics.getInstance().countAppendString();
			transferValuesAndAppend(stmt, ((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getBase(),
					((VirtualInvokeExpr) vie).getArg(0), true, false);
		} else if (msig.equals("<java.lang.StringBuffer: java.lang.StringBuffer append(char)>")) {
			GlobalStatistics.getInstance().countAppendString();
			transferValuesAndAppend(stmt, ((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getBase(),
					((VirtualInvokeExpr) vie).getArg(0), true, false);
		} else if (msig.equals("<android.os.ParcelUuid: void <init>(java.util.UUID)>")) {
			transferValues(((SpecialInvokeExpr) vie).getArg(0), ((SpecialInvokeExpr) vie).getBase() );
		} else {
			Logger.printW(String.format("[%s] [SIMULATE][Can't Handle InvokeStmt]: %s", this.hashCode(), stmt));
		}

	}

	public void caseIdentityStmt(IdentityStmt stmt) {
		// TODO Auto-generated method stub
		transferValues(stmt.getRightOp(), stmt.getLeftOp());
	}

	@Override
	public void defaultCase(Object obj) {
		// TODO Auto-generated method stub
		Logger.printW(String.format("[%s] [SIMULATE][Can't Handle]: %s (%s)", this.hashCode(), obj, obj.getClass()));
	}

	public HashSet<String> getContent(Value valu) {
		HashSet<String> vs = new HashSet<String>();
		if (this.getCurrentValues().containsKey(valu)) {
			return this.getCurrentValues().get(valu);
		} else if (valu instanceof StringConstant) {
			vs.add(((StringConstant) valu).value);
		} else if (valu instanceof IntConstant) {
			vs.add(((IntConstant) valu).value + "");
		} else if (valu instanceof LongConstant) {
			vs.add(((LongConstant) valu).value + "");
		} else if (valu instanceof DoubleConstant) {
			vs.add(((DoubleConstant) valu).value + "");
		} else if (valu instanceof FloatConstant) {
			vs.add(((FloatConstant) valu).value + "");
		}
		return vs;
	}

}
