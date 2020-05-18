package osu.seclab.inputscope.stringvsa.utility;

import java.util.HashSet;

import osu.seclab.inputscope.stringvsa.base.GlobalStatistics;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.StringConstant;
import soot.jimple.UshrExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JAndExpr;
import soot.jimple.internal.JDivExpr;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JOrExpr;
import soot.jimple.internal.JShlExpr;
import soot.jimple.internal.JShrExpr;
import soot.jimple.internal.JSubExpr;
import soot.jimple.internal.JXorExpr;

public class OtherUtility {

	public static boolean isStrConstant(Object obj) {
		return obj instanceof StringConstant;
	}

	public static boolean isNumConstant(Object obj) {
		return obj instanceof IntConstant || obj instanceof LongConstant || obj instanceof FloatConstant || obj instanceof DoubleConstant;
	}

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		}

		catch (NumberFormatException er) {
			return false;
		}
	}

	public static int string2Int(String i) {
		return Integer.parseInt(i);
	}

	public static long string2Long(String i) {
		return Long.parseLong(i);
	}

	public static float string2Float(String i) {
		return Float.parseFloat(i);
	}

	public static double string2Double(String i) {
		return Double.parseDouble(i);
	}

	public static boolean computeBasedOnType(Value to, BinopExpr exp, HashSet<String> currentValues, HashSet<String> opt2, HashSet<String> newValues) {
		boolean isInt = to.getType().toString().equals("int");
		boolean isLong = to.getType().toString().equals("long");
		boolean isFloat = to.getType().toString().equals("float");
		boolean isDouble = to.getType().toString().equals("double");

		Compute compu = null;
		if (exp instanceof JAddExpr) {
			GlobalStatistics.addExp++;
			compu = new AddCompute();
		} else if (exp instanceof JSubExpr) {
			GlobalStatistics.subExp++;
			compu = new SubCompute();
		} else if (exp instanceof JMulExpr) {
			GlobalStatistics.mulExp++;
			compu = new MulCompute();
		} else if (exp instanceof JDivExpr) {
			GlobalStatistics.divExp++;
			compu = new DivCompute();
		} else if (exp instanceof JAndExpr) {
			GlobalStatistics.andExp++;
			compu = new AndCompute();
		} else if (exp instanceof JOrExpr) {
			GlobalStatistics.orExp++;
			compu = new OrCompute();
		} else if (exp instanceof JShlExpr) {
			GlobalStatistics.shlExp++;
			compu = new ShlCompute();
		} else if (exp instanceof JShrExpr) {
			GlobalStatistics.shrExp++;
			compu = new ShrCompute();
		} else if (exp instanceof JXorExpr) {
			GlobalStatistics.xorExp++;
			compu = new XorCompute();
		} else if (exp instanceof UshrExpr) {
			GlobalStatistics.ushrExp++;
			compu = new UshrCompute();
		} else {
			Logger.printW(String.format("[OtherUtility][W] [unknow compute]: %s", exp));
			return false;
		}

		for (String apd : currentValues) {
			for (String str : opt2) {
				if (isInt) {
					newValues.add(compu.doit(OtherUtility.string2Int(apd), OtherUtility.string2Int(str)));
				} else if (isLong) {
					newValues.add(compu.doit(OtherUtility.string2Long(apd), OtherUtility.string2Long(str)));
				} else if (isFloat) {
					newValues.add(compu.doit(OtherUtility.string2Float(apd), OtherUtility.string2Float(str)));
				} else if (isDouble) {
					newValues.add(compu.doit(OtherUtility.string2Double(apd), OtherUtility.string2Double(str)));
				} else
					newValues.add(apd + str);
			}
		}

		return true;
	}

	interface Compute {
		public <T> String doit(T one, T two);
	}

	static class AddCompute implements Compute {
		public <T> String doit(T one, T two) {
			if (one.getClass() == Integer.class) {
				return (Integer) ((Integer) one + (Integer) two) + "";
			}
			if (one.getClass() == Long.class) {
				return (((Long) one).longValue() + ((Long) two).longValue()) + "";
			}
			if (one.getClass() == Double.class) {
				return (((Double) one).doubleValue() + ((Double) two).doubleValue()) + "";
			}
			if (one.getClass() == Float.class) {
				return (((Float) one).floatValue() + ((Float) two).floatValue()) + "";
			}
			Logger.printW(String.format("[OtherUtility][W] [unknow add]: %s (%s)", one, two));
			return "";
		}
	}

	static class SubCompute implements Compute {
		public <T> String doit(T one, T two) {
			if (one.getClass() == Integer.class) {
				return (Integer) ((Integer) one - (Integer) two) + "";
			}
			if (one.getClass() == Long.class) {
				return (((Long) one).longValue() - ((Long) two).longValue()) + "";
			}
			if (one.getClass() == Double.class) {
				return (((Double) one).doubleValue() - ((Double) two).doubleValue()) + "";
			}
			if (one.getClass() == Float.class) {
				return (((Float) one).floatValue() - ((Float) two).floatValue()) + "";
			}
			Logger.printW(String.format("[OtherUtility][W] [unknow sub]: %s (%s)", one, two));
			return "";
		}
	}

	static class MulCompute implements Compute {
		public <T> String doit(T one, T two) {
			if (one.getClass() == Integer.class) {
				return (Integer) ((Integer) one * (Integer) two) + "";
			}
			if (one.getClass() == Long.class) {
				return (((Long) one).longValue() * ((Long) two).longValue()) + "";
			}
			if (one.getClass() == Double.class) {
				return (((Double) one).doubleValue() * ((Double) two).doubleValue()) + "";
			}
			if (one.getClass() == Float.class) {
				return (((Float) one).floatValue() * ((Float) two).floatValue()) + "";
			}
			Logger.printW(String.format("[OtherUtility][W] [unknow mul]: %s (%s)", one, two));
			return "";
		}
	}

	static class DivCompute implements Compute {
		public <T> String doit(T one, T two) {
			if (one.getClass() == Integer.class) {
				return (Integer) ((Integer) one / (Integer) two) + "";
			}
			if (one.getClass() == Long.class) {
				System.out.println(((Long) one).longValue() + " " + ((Long) two).longValue());
				return (((Long) one).longValue() / ((Long) two).longValue()) + "";
			}
			if (one.getClass() == Double.class) {
				return (((Double) one).doubleValue() / ((Double) two).doubleValue()) + "";
			}
			if (one.getClass() == Float.class) {
				return (((Float) one).floatValue() / ((Float) two).floatValue()) + "";
			}
			Logger.printW(String.format("[OtherUtility][W] [unknow div]: %s (%s)", one, two));
			return "";
		}
	}

	static class AndCompute implements Compute {
		public <T> String doit(T one, T two) {
			if (one.getClass() == Integer.class) {
				return (Integer) ((Integer) one & (Integer) two) + "";
			}
			if (one.getClass() == Long.class) {
				return (((Long) one).longValue() & ((Long) two).longValue()) + "";
			}
			Logger.printW(String.format("[OtherUtility][W] [unknow And]: %s (%s)", one, two));
			return "";
		}
	}

	static class OrCompute implements Compute {
		public <T> String doit(T one, T two) {
			if (one.getClass() == Integer.class) {
				return (Integer) ((Integer) one | (Integer) two) + "";
			}
			if (one.getClass() == Long.class) {
				return (((Long) one).longValue() | ((Long) two).longValue()) + "";
			}
			Logger.printW(String.format("[OtherUtility][W] [unknow Or]: %s (%s)", one, two));
			return "";
		}
	}

	static class ShlCompute implements Compute {
		public <T> String doit(T one, T two) {
			if (one.getClass() == Integer.class) {
				return (Integer) ((Integer) one << (Integer) two) + "";
			}
			if (one.getClass() == Long.class) {
				return (((Long) one).longValue() << ((Long) two).longValue()) + "";
			}
			Logger.printW(String.format("[OtherUtility][W] [unknow Shl]: %s (%s)", one, two));
			return "";
		}
	}

	static class ShrCompute implements Compute {
		public <T> String doit(T one, T two) {
			if (one.getClass() == Integer.class) {
				return (Integer) ((Integer) one >> (Integer) two) + "";
			}
			if (one.getClass() == Long.class) {
				return (((Long) one).longValue() >> ((Long) two).longValue()) + "";
			}
			Logger.printW(String.format("[OtherUtility][W] [unknow Shr]: %s (%s)", one, two));
			return "";
		}
	}

	static class XorCompute implements Compute {
		public <T> String doit(T one, T two) {
			if (one.getClass() == Integer.class) {
				return (Integer) ((Integer) one ^ (Integer) two) + "";
			}
			if (one.getClass() == Long.class) {
				return (((Long) one).longValue() ^ ((Long) two).longValue()) + "";
			}
			Logger.printW(String.format("[OtherUtility][W] [unknow Xor]: %s (%s)", one, two));
			return "";
		}
	}

	static class UshrCompute implements Compute {
		public <T> String doit(T one, T two) {
			if (one.getClass() == Integer.class) {
				return (Integer) ((Integer) one >>> (Integer) two) + "";
			}
			if (one.getClass() == Long.class) {
				return (((Long) one).longValue() >>> ((Long) two).longValue()) + "";
			}
			Logger.printW(String.format("[OtherUtility][W] [unknow Ushr]: %s (%s)", one, two));
			return "";
		}
	}
}
