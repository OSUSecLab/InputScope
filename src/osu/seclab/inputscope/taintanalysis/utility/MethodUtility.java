package osu.seclab.inputscope.taintanalysis.utility;

import java.util.Iterator;

import org.json.JSONObject;

import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ParameterRef;

public class MethodUtility {
	public static ParameterRef getParameterRef(SootMethod sm, int i) {
		for (ValueBox box : sm.retrieveActiveBody().getUseAndDefBoxes()) {
			if (box.getValue() instanceof ParameterRef) {
				if (((ParameterRef) box.getValue()).getIndex() == i)
					return (ParameterRef) box.getValue();
			}
		}

		return null;
	}

	public static Value findValueByString(SootMethod sm, String i) {
		for (ValueBox box : sm.retrieveActiveBody().getUseAndDefBoxes()) {
			Logger.print(box.getValue().toString() + " " + sm.retrieveActiveBody().getUseAndDefBoxes().size());
			if (box.getValue().toString().equals(i)) {
				return box.getValue();
			}
		}
		return null;
	}

	public static int getUnitIndex(SootMethod sm, Unit i) {
		PatchingChain<Unit> units = sm.retrieveActiveBody().getUnits();
		int index = 0;
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			if (iter.next().equals(i))
				return index;
			index++;
		}
		return -1;
	}

	public static JSONObject toJson(SootMethod sm) {
		JSONObject ret = new JSONObject();
		ret.put("sig", sm);
		ret.put("class", sm.getDeclaringClass().getName());
		ret.put("classdetails", class2Json(sm.getDeclaringClass()));
		ret.put("name", sm.getName());
		ret.put("isStatic", sm.isStatic());
		ret.put("argCount", sm.getParameterCount());
		int i = 0;
		for (Type type : sm.getParameterTypes()) {
			ret.put("args" + i, type);
			i++;
		}

		return ret;
	}

	public static JSONObject class2Json(SootClass cls) {
		JSONObject ret = new JSONObject();
		ret.put("name", cls.getName());

		for (SootMethod sm : cls.getMethods()) {
			if (!sm.isConstructor())
				continue;
			JSONObject constr = new JSONObject();
			ret.append("constructor", constr);

			constr.put("argCount", sm.getParameterCount());
			int i = 0;
			for (Type type : sm.getParameterTypes()) {
				constr.put("args" + i, type);
				i++;
			}

		}

		return ret;
	}
}
