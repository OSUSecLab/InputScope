package osu.seclab.inputscope.stringvsa.graph;

import java.util.HashSet;
import java.util.List;

import org.json.JSONObject;

import osu.seclab.inputscope.stringvsa.utility.Logger;

public class DGraph {
	HashSet<IDGNode> nodes = new HashSet<IDGNode>();

	public void addNode(IDGNode node) {
		nodes.add(node);
	}

	public void removeNode(IDGNode node) {
		nodes.remove(node);
	}

	public HashSet<IDGNode> getNodes() {
		return nodes;
	}

	public void solve(List<ValuePoint> vps) {
		for (ValuePoint vp : vps)
			this.addNode(vp);

		IDGNode tnode;
		initAllIfNeed();
		while (true) {
			initAllIfNeed();
			tnode = getNextSolvableNode();

			if (hasSolvedAllTarget(vps)) {
				Logger.print("[DONE]: Solved All Targets!");
				return;
			}
//
			if (tnode == null) {
				Logger.print("[DONE]: No Solvable Node Left!");
				if (try2PartiallySolve()) {
					continue;
				} else {
					Logger.print("[DONE]: No PartiallySolvable Node Left!");
					return;
				}
			}

			tnode.solve();

		}
	}

	public void initAllIfNeed() {
		IDGNode whoNeedInit;
		while (true) {
			whoNeedInit = null;
			for (IDGNode tmp : nodes)
				if (!tmp.inited()) {
					whoNeedInit = tmp;
					break;
				}
			if (whoNeedInit == null) {
				return;
			} else {
				whoNeedInit.initIfHavenot();
			}
		}
	}

	private IDGNode getNextSolvableNode() {
		for (IDGNode tmp : nodes) {
			if (tmp.getUnsovledDependentsCount() == 0 && !tmp.hasSolved()) {
				return tmp;
			}
		}
		return null;
	}

	private boolean try2PartiallySolve() {
		for (IDGNode tmp : nodes) {
			if (tmp.canBePartiallySolve()) {
				return true;
			}
		}
		return false;
	}

	private boolean hasSolvedAllTarget(List<ValuePoint> vps) {
		for (ValuePoint vp : vps) {
			if (!vp.hasSolved())
				return false;
		}
		return true;
	}

	public JSONObject toJson() {
		JSONObject result = new JSONObject();
		JSONObject jnodes = new JSONObject();
		JSONObject jedges = new JSONObject();
		for (IDGNode node : nodes) {
			jnodes.put(node.hashCode() + "", node.getClass().getSimpleName());
			for (IDGNode subn : node.getDependents()) {
				jedges.append(node.hashCode() + "", subn.hashCode() + "");
			}
		}
		result.put("nodes", jnodes);
		result.put("edges", jedges);
		return result;
	}
}
