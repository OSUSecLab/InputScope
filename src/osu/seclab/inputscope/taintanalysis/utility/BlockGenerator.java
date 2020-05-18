package osu.seclab.inputscope.taintanalysis.utility;


import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import soot.Body;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.CompleteBlockGraph;

public class BlockGenerator {
	static BlockGenerator bg = new BlockGenerator();

	public static BlockGenerator getInstance() {
		return bg;
	}

	private BlockGenerator() {
	}

	Hashtable<Body, CompleteBlockGraph> ht = new Hashtable<Body, CompleteBlockGraph>();

	public CompleteBlockGraph generate(Body b) {
		if (!ht.containsKey(b)) {
			ht.put(b, new CompleteBlockGraph(b));
		}
		return ht.get(b);
	}

	public static boolean isCircle(Block b, Block current, CompleteBlockGraph cbg, HashSet<Block> history) {
		if (history.contains(current)) {
			return false;
		}
		boolean isc = false;

		history.add(current);
		for (Block blk : cbg.getPredsOf(current)) {
			if (b == blk)
				isc = true;
			else
				isc |= isCircle(b, blk, cbg, history);
			if (isc)
				return isc;
		}
		history.remove(current);
		return isc;
	}

	public static void removeCircleBlocks(List<Block> bs, Block current, CompleteBlockGraph cbg) {
		HashSet<Block> rem = new HashSet<Block>();

		for (Block blk : bs) {
			if (isCircle(current, blk, cbg, new HashSet<Block>())) {
				rem.add(blk);
			}
		}
		for (Block blk : rem) {
			bs.remove(blk);
		}

	}
}
