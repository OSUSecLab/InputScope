package osu.seclab.inputscope.stringvsa.forwardexec;

import java.util.List;

import soot.Unit;
import osu.seclab.inputscope.stringvsa.base.StmtPoint;

public interface StmtPath {

	public Unit getStmtPathHeader();

	public Unit getSuccsinStmtPath(Unit u);

	public Unit getPredsinStmtPath(Unit u);

	public Unit getStmtPathTail();

	public List<StmtPoint> getStmtPath();
}
