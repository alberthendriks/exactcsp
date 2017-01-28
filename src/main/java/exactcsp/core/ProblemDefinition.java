package exactcsp.core;

import java.math.BigInteger;

public class ProblemDefinition {
	
	private Matrix matrix;
	
	public ProblemDefinition(Matrix newMatrix) {
		matrix = newMatrix;
	}
	
	public int newCol() {
		return matrix.newCol();
	}
	
	public int[] newVar(int bits, boolean addToDecisionVariables) {
		int[] result = new int[bits];
		for (int i=0; i<bits; i++) {
			result[i] = newCol();
		}
		if (addToDecisionVariables) {
			for (int col: result) {
				matrix.addToDecisionVariables(col);
			}
		}
		return result;
	}

	public void setCol(int col, int value) {
		matrix.setCol(col, value);
	}
	
	public Matrix getMatrix() {
		return matrix;
	}
	
	public int[] newConst(int nrOfBits, BigInteger value) {
		int[] result = new int[nrOfBits];
		for (int i=0; i<value.bitLength(); i++) {
			result[i] = value.testBit(i) ? 1 : 0;
		}
		return result;
	}
	
	public void fix(int[] var, BigInteger value) {
		for (int i=0; i<var.length; i++) {
			setCol(var[i], value.testBit(i) ? 1 : 0);
		}
	}

	public int[] getSelector(int nrOfBits, BigInteger value, boolean addToDecisionVariables) {
		int[] result = new int[nrOfBits];
		int selector = newCol();
		if (addToDecisionVariables) {
			matrix.addToDecisionVariables(selector);
		}
		for (int i=0; i<value.bitLength(); i++) {
			result[i] = value.testBit(i) ? selector : 0;
		}
		return result;
	}

	public int xor(int a, int b) {
		int result = matrix.newCol();
		int helper = matrix.newCol();
		matrix.addRow(0, new int[] {a, b, helper, result}, new int[] {1, 1, 1, 2});
		int helper2 = matrix.newCol();
		matrix.addRow(0, new int[] {helper, helper2, result}, new int[] {1, 2, 1});
		return result;
	}
	
	public int[] and(int[] a, int[] b) {
		int[] result = new int[a.length];
		for (int i=0; i<result.length; i++) {
			int r = matrix.newCol();
			int helper = matrix.newCol();
			result[i] = r;
			matrix.addRow(0, new int[] {a[i], b[i], helper, r}, new int[] {1, 1, 2, 1});
			helper = newCol();
			matrix.addRow(0, new int[] {a[i], helper, r}, new int[] {1, 2, 2});
			helper = newCol();
			matrix.addRow(0, new int[] {b[i], helper, r}, new int[] {1, 2, 2});
		}
		return result;
	}
	
	public int[] not(int[] var) {
		int[] result = newVar(var.length, false);
		for (int i=0; i<result.length; i++) {
			matrix.addRow(1, new int[] {var[i], result[i]}, new int[] {1, 1});
		}
		return result;		
	}

	public int[] xor(int[] a, int[] b) {
		int[] result = new int[a.length];
		for (int i=0; i<result.length; i++) {
			int r = matrix.newCol();
			int helper = matrix.newCol();
			result[i] = r;
			matrix.addRow(0, new int[] {a[i], b[i], helper, r}, new int[] {1, 1, 1, 2});
			int helper2 = matrix.newCol();
			matrix.addRow(0, new int[] {helper, helper2, r}, new int[] {1, 2, 1});
		}
		return result;
	}
	
	public int[] add(int[]a, int[] b) {
		int[] result = new int[a.length];
		for (int i=0; i<result.length; i++) {
			result[i] = newCol();
		}
		return add(a, b, result);
	}
	
	public int[] add(int[]a, int[] b, int[] result) {
		int[] carry = new int[result.length];
		for (int i=0; i<carry.length; i++) {
			carry[i] = newCol();
		}
		matrix.addRow(0, new int[] {a[0], b[0], carry[0], result[0]}, new int[] {1, 1, 1, 2});
		int helper = newCol();
		matrix.addRow(0, new int[] {helper, carry[0], result[0]}, new int[] {2, 1, 1});
		
		for (int i=1; i<carry.length; i++) {
			matrix.addRow(0, new int[] {a[i], b[i], carry[i-1], carry[i], result[i]}, new int[] {1, 1, 1, 1, 2});
			helper = newCol();
			matrix.addRow(1, new int[] {a[i], b[i], carry[i], helper}, new int[] {1, 1, 2, 1});
			helper = newCol();
			matrix.addRow(1, new int[] {a[i], carry[i-1], carry[i], helper}, new int[] {1, 1, 2, 1});
			helper = newCol();
			matrix.addRow(1, new int[] {b[i], carry[i-1], carry[i], helper}, new int[] {1, 1, 2, 1});
		}		
		return result;
	}

    public boolean solve()
    {
        return matrix.solve();
    }
	
 }