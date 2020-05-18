package osu.seclab.inputscope.stringvsa.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.javatuples.Pair;

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
		HashSet<Block> toRemove = new HashSet<Block>();

		for (Block blk : bs) {

			if (isCircle(current, blk, cbg, new HashSet<Block>())) {
				toRemove.add(blk);
			}
		}
		for (Block blk : toRemove) {
			bs.remove(blk);
			System.out.println("removed!!!");
			System.out.println(blk);
		}

	}

	public static boolean isCircle(Block blk, Block current, CompleteBlockGraph cbg) {
		// args order is correct
		return isCircle(current, blk, cbg, new HashSet<Block>());
	}

	public static List<Block> removeBlocksThatHaveBeenVisitedTwice(ArrayList<Pair<Block, Integer>> bks, List<Block> targets) {


		ArrayList<Pair<Block, Integer>> hit = new ArrayList<Pair<Block, Integer>>();

		hit.add(bks.get(0));

		Pair<Block, Integer> lastone = bks.get(0);
		int index = 1;
		int blen = bks.size();
		boolean iwasInOthersubcall = false;
		while (index < blen) {

			if (bks.get(index).getValue1() < lastone.getValue1())
				break;

			if (bks.get(index).getValue1() != lastone.getValue1()) {
				iwasInOthersubcall = true;
			} else {
				if (iwasInOthersubcall) {
					iwasInOthersubcall = false;
					if (bks.get(index).getValue0() != lastone.getValue0()) {
						Logger.printW("looks like call stack not even");
					}
				} else {
					lastone = bks.get(index);
					hit.add(lastone);
				}
			}
			index++;
		}

		if (iwasInOthersubcall)
			Logger.printW("looks like call stack not evenb");

		List<Block> toRet = new ArrayList<Block>();
		for (Block current : targets) {
			int visitedTimes = 0;
			Body myBody = current.getBody();
			for (Pair<Block, Integer> block : hit) {
				if (!block.getValue0().getBody().equals(myBody)) {
					Logger.printW("looks like call stack not evenc");
					break;
				}

				if (block.getValue0().getIndexInMethod() == current.getIndexInMethod())
					visitedTimes++;
			}
			if (visitedTimes < 2)
				toRet.add(current);
		}

		return toRet;
	}

}
