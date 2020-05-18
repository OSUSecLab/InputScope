package osu.seclab.inputscope.taintanalysis.main;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import osu.seclab.inputscope.taintanalysis.solver.SimulationContext;
import osu.seclab.inputscope.taintanalysis.solver.StmtItem;
import soot.Unit;

public class CrossPath {

	Unit u;
	List<SimulationContext> inputs;
	List<SimulationContext> arrays;

	public CrossPath(Unit u, List<SimulationContext> inputs, List<SimulationContext> arrays) {
		super();
		this.u = u;
		this.inputs = inputs;
		this.arrays = arrays;
	}

	public List<SimulationContext> getInputs() {
		return inputs;
	}

	public List<SimulationContext> getArrays() {
		return arrays;
	}

	public JSONObject toJson() {

		JSONObject tor = new JSONObject(); 
		
		
		JSONObject input = new JSONObject();
		JSONObject array = new JSONObject();

		input.put("length", 0);
		array.put("length", 0);
		if (inputs != null && inputs.size() > 0) {
			ArrayList<StmtItem> stmts = inputs.get(0).getInstructionTrace();
			input.put("length", stmts.size());
			for (int i = 0; i < stmts.size(); i++) {
				input.put(i + "", stmts.get(i).toJson(true));
			}
		}
		
		if (arrays != null && arrays.size() > 0) {
			ArrayList<StmtItem> stmts = arrays.get(0).getInstructionTrace();
			array.put("length", stmts.size());
			for (int i = 0; i < stmts.size(); i++) {
				array.put(i + "", stmts.get(i).toJson(true));
			}
		}

		tor.put("input", input);
		tor.put("array", array);
		
		return tor;
		
	}

}
