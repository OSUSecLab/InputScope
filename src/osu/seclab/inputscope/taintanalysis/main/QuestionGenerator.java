package osu.seclab.inputscope.taintanalysis.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import osu.seclab.inputscope.taintanalysis.base.SinkMethod;
import osu.seclab.inputscope.taintanalysis.base.SourcePoint;
import osu.seclab.inputscope.taintanalysis.base.TaintQuestion;
import osu.seclab.inputscope.taintanalysis.solver.SimulationContext;
import osu.seclab.inputscope.taintanalysis.solver.StmtItem;
import osu.seclab.inputscope.taintanalysis.solver.TaintQuestionSolver;
import osu.seclab.inputscope.taintanalysis.utility.BlockGenerator;
import osu.seclab.inputscope.taintanalysis.utility.ListUtility;
import osu.seclab.inputscope.taintanalysis.utility.Logger;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JArrayRef;
import soot.toolkits.graph.Block;

public class QuestionGenerator {
	ArrayList<TaintQuestion> tqInput;
	ArrayList<TaintQuestion> tqArray;

	List<TaintQuestion> solvedInput = new ArrayList<TaintQuestion>();
	List<TaintQuestion> solvedArray = new ArrayList<TaintQuestion>();

	public QuestionGenerator generateInputQuestions() {

		ArrayList<String> inputMetds = new ArrayList<String>();
		inputMetds.add("<android.widget.EditText: android.text.Editable getText()>");
		inputMetds.add("<android.widget.EditText: android.text.Editable getEditableText()>");
		inputMetds.add("<android.text.Editable: java.lang.String toString()>");

		tqInput = new ArrayList<TaintQuestion>();
		tqArray = new ArrayList<TaintQuestion>();

		for (SootClass sclas : Scene.v().getClasses()) {
			for (SootMethod smthd : ListUtility.clone(sclas.getMethods())) {
				if (!smthd.isConcrete())
					continue;

				Body body = smthd.retrieveActiveBody();
				if (body == null)
					continue;

				List<Block> bs = BlockGenerator.getInstance().generate(body).getBlocks();

				for (Block block : bs) {
					for (Unit unit : block) {
						if (unit instanceof AssignStmt) {
							if (((AssignStmt) unit).getRightOp() instanceof InvokeExpr) {

								if (inputMetds.contains(((InvokeExpr) ((AssignStmt) unit).getRightOp()).getMethodRef().getSignature()))
									generateOneQuestion(tqInput, smthd, block, unit, ((AssignStmt) unit).getLeftOp());

							} 
						}
					}
				}
			}
		}

		return this;
	}

	private void generateOneQuestion(ArrayList<TaintQuestion> tqs, SootMethod smthd, Block block, Unit unit, Value value) {

		ArrayList<String> sinkMetds = new ArrayList<String>();
		sinkMetds.add("<android.text.TextUtils: boolean equals(java.lang.CharSequence,java.lang.CharSequence)>");
		sinkMetds.add("<java.lang.Object: boolean equals(java.lang.Object)>");
		sinkMetds.add("<java.lang.String: boolean equals(java.lang.Object)>");
		sinkMetds.add("<java.lang.String: boolean equals(java.lang.Object)>");
		sinkMetds.add("<java.lang.String: boolean contains(java.lang.CharSequence)>");
		sinkMetds.add("<java.lang.String: boolean contentEquals(java.lang.StringBuffer)>");
		sinkMetds.add("<java.lang.String: boolean contentEquals(java.lang.CharSequence)>");
		sinkMetds.add("<java.lang.String: boolean equalsIgnoreCase(java.lang.String)>");
		sinkMetds.add("<java.lang.String: int indexOf(java.lang.String)>");
//		sinkMetds.add("<java.lang.String: int indexOf(java.lang.String,int)>");
		sinkMetds.add("<java.lang.String: int lastIndexOf(java.lang.String)>");
//		sinkMetds.add("<java.lang.String: int lastIndexOf(java.lang.String,int)>");
		sinkMetds.add("<java.lang.StringBuffer: int indexOf(java.lang.String)>");
//		sinkMetds.add("<java.lang.StringBuffer: int indexOf(java.lang.String,int)>");
		sinkMetds.add("<java.lang.StringBuffer: int lastIndexOf(java.lang.String)>");
//		sinkMetds.add("<java.lang.StringBuffer: int lastIndexOf(java.lang.String,int)>");
//		sinkMetds.add("<java.lang.StringBuffer: int lastIndexOf(java.lang.String,int)>");
		sinkMetds.add("<java.util.HashMap: boolean containsKey(java.lang.Object)>");
		sinkMetds.add("<java.util.Map: java.lang.Object get(java.lang.Object)>");
		

		SourcePoint sp = new SourcePoint(smthd, block, unit, value);

		TaintQuestion tq = new TaintQuestion(sp);
		SinkMethod sm;

		for (String str : sinkMetds) {
			try {
				sm = new SinkMethod(Scene.v().getMethod(str));
				tq.addSinks(sm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		tqs.add(tq);

		print();
	}

	public HashSet<String> solveInputQuestions(boolean gArrayQuestions) {

		solvedInput = new ArrayList<TaintQuestion>();

		TaintQuestionSolver tgSolver = new TaintQuestionSolver();
		int i = 0;
		int positive = 0;
		for (TaintQuestion tq : getTqInput()) {
//			System.out.println("Start Solving Q-"+i);
//			System.out.println(tq.getSourcep().getMethodLocation());
//			System.out.println(tq.getSourcep().getBlockLocation());
//			System.out.println("End Solving Q-"+i);
			tgSolver.solve(tq);
//			System.out.println("End Solving Q-" + i++);

			if (tq.isPositive()) {
				solvedInput.add(tq);
//				System.out.println("Question Q-"+ i + " is positive!\n");
				positive++;
			}else{
//				System.out.println("Question Q-"+ i + " is negative!\n");
			}
		}

		System.out.println("Statistic: Total Questions:" + i + " Total Positive: " + positive);

		return TResSolve.saveSolved(solvedInput);

	}

	@SuppressWarnings("unchecked")
	public QuestionGenerator generateArrayQuestions(List<TaintQuestion> solved) {
		HashSet<SootMethod> sms = new HashSet<SootMethod>();

		ArrayList<StmtItem> stmts;
		for (TaintQuestion tq : solved) {
			for (SimulationContext sc : tq.getsContexts()) {
				if (sc.isContainsSink()) {

					stmts = (ArrayList<StmtItem>) sc.getInstructionTrace().clone();

					while (!stmts.get(stmts.size() - 1).isContainsInteresting())
						stmts.remove(stmts.size() - 1);

					for (StmtItem stmt : stmts)
						sms.add(stmt.getSm());
				}
			}
		}

		for (SootMethod smthd : sms) {
			if (!smthd.isConcrete())
				continue;
			Body body = smthd.retrieveActiveBody();
			if (body == null)
				continue;

			List<Block> bs = BlockGenerator.getInstance().generate(body).getBlocks();

			for (Block block : bs) {
				for (Unit unit : block) {
					if (unit instanceof AssignStmt) {
						if (((AssignStmt) unit).getRightOp() instanceof JArrayRef) {
							if (((JArrayRef) ((AssignStmt) unit).getRightOp()).getType().toString().equals("java.lang.String"))
								generateOneQuestion(tqArray, smthd, block, unit, ((AssignStmt) unit).getLeftOp());
						}
					}
				}
			}
		}

		return this;
	}

	public void solveArrayQuestions() {

		TaintQuestionSolver tgSolver = new TaintQuestionSolver();
		int i = 0;
		int positive = 0;
		for (TaintQuestion tq : getTqArray()) {
			System.out.println("aaa");
			System.out.println(tq.getSourcep().getMethodLocation());
			System.out.println(tq.getSourcep().getBlockLocation());
			System.out.println("aaa");
			tgSolver.solve(tq);
			System.out.println("ddd " + i++);

			if (tq.isPositive()) {
				solvedArray.add(tq);
				System.out.println("positive!");
				positive++;
			}
		}

		System.out.println("ddd " + i + " " + positive);

	}

	public ArrayList<CrossPath> checkOverlappingAndGetResults() {
		ArrayList<CrossPath> paths = new ArrayList<CrossPath>();
		if (solvedInput.size() > 0 && solvedArray.size() > 0) {
			Hashtable<Unit, List<SimulationContext>> pointsInput = new Hashtable<Unit, List<SimulationContext>>();
			Hashtable<Unit, List<SimulationContext>> pointsArray = new Hashtable<Unit, List<SimulationContext>>();

			for (TaintQuestion tq : solvedInput) {
				for (SimulationContext sc : tq.getsContexts()) {
					if (sc.isContainsSink()) {
						for (StmtItem stmt : sc.getInstructionTrace()) {
							if (stmt.isContainsSink()) {
								if (!pointsInput.containsKey(stmt.getU()))
									pointsInput.put(stmt.getU(), new ArrayList<SimulationContext>());
								pointsInput.get(stmt.getU()).add(sc);
							}
						}
					}
				}
			}

			for (TaintQuestion tq : solvedArray) {
				for (SimulationContext sc : tq.getsContexts()) {
					if (sc.isContainsSink()) {
						for (StmtItem stmt : sc.getInstructionTrace()) {
							if (stmt.isContainsSink()) {
								if (!pointsArray.containsKey(stmt.getU()))
									pointsArray.put(stmt.getU(), new ArrayList<SimulationContext>());
								pointsArray.get(stmt.getU()).add(sc);
							}
						}
					}
				}
			}

			for (Unit u : pointsInput.keySet()) {
				if (pointsArray.containsKey(u)) {
					paths.add(new CrossPath(u, pointsInput.get(u), pointsArray.get(u)));
				}
			}

		}

		System.out.println(paths.size());

		return paths;
	}

	public QuestionGenerator print() {
		Logger.print("tqInput size:" + tqInput.size());
		Logger.print("tqArray size:" + tqArray.size());
		return this;
	}

	public ArrayList<TaintQuestion> getTqInput() {
		return tqInput;
	}

	public ArrayList<TaintQuestion> getTqArray() {
		return tqArray;
	}

}
