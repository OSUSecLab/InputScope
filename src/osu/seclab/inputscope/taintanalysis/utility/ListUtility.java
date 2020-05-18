package osu.seclab.inputscope.taintanalysis.utility;

import java.util.ArrayList;
import java.util.List;

import soot.PatchingChain;
import soot.Unit;

public class ListUtility {

	public static List<Unit> chain2List(PatchingChain<Unit> us) {
		List<Unit> ls = new ArrayList<Unit>();
		for (Unit inst : us) {
			ls.add(inst);
		}
		return ls;
	}

	public static <T> List<T> Array2List(T[] ts) {
		List<T> ls = new ArrayList<T>();
		for (T inst : ts) {
			ls.add(inst);
		}
		return ls;
	}

	public static <T> List<T> clone(List<T> ls) {
		List<T> list = new ArrayList<T>();
		for (T t : ls) {
			list.add(t);
		}
		return list;
	}
}
