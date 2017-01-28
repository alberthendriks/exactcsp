package exactcsp.examples;

import java.math.BigInteger;

import exactcsp.core.Matrix;
import exactcsp.core.ProblemDefinition;

// @todo, check if this is a correct implementation of sha-2 (big endian vs little endian)
public class Sha256 extends ProblemDefinition {

	private final static int ROUNDS = 16;
	private final static int FIX_WS = 8;
	
	private final static long[] k = {
			   0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
			   0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
			   0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
			   0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
			   0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
			   0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
			   0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
			   0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};
	
	private int[][] w;
	
    public static void main( String[] args )
    {
    	long startTime = System.currentTimeMillis();
    	Sha256 sha256 = new Sha256(ROUNDS, FIX_WS);
    	if (sha256.solve()) {
    		System.out.println("has solution");
    	} else {
    		System.out.println("NO solution");
    	}
    	System.out.println("time: " + (System.currentTimeMillis()-startTime) + " ms");
    }
	
	public Sha256(int rounds, int fixWs) {
	    super(new Matrix());
	    
		w = new int[rounds][];
		for (int i=0; i<Math.min(16, rounds); i++) {
			w[i] = newVar(32, true); // true: add to decisionVariables
		}
		
		for (int i=0; i<fixWs; i++) {
			fix(w[i], new BigInteger("0"));
		}
		
		int[][] constK = new int[rounds][];
		for (int i=0; i<rounds; i++) {
			constK[i] = newConst(32, BigInteger.valueOf(k[i]));
		}
		
		
		int[] h0 = newConst(32, BigInteger.valueOf(0x6a09e667));
		int[] h1 = newConst(32, BigInteger.valueOf(0xbb67ae85));
		int[] h2 = newConst(32, BigInteger.valueOf(0x3c6ef372));
		int[] h3 = newConst(32, BigInteger.valueOf(0xa54ff53a));
		int[] h4 = newConst(32, BigInteger.valueOf(0x510e527f));
		int[] h5 = newConst(32, BigInteger.valueOf(0x9b05688c));
		int[] h6 = newConst(32, BigInteger.valueOf(0x1f83d9ab));
		int[] h7 = newConst(32, BigInteger.valueOf(0x5be0cd19));

		// outer loop
		
	    int[] a = h0;
	    int[] b = h1;
	    int[] c = h2;
	    int[] d = h3;
	    int[] e = h4;
	    int[] f = h5;
	    int[] g = h6;
	    int[] h = h7;
	    
	    int[] S1;
	    int[] ch;
	    int[] temp1;
	    int[] S0;
	    int[] maj;
	    int[] temp2;

	    for (int i=16; i<rounds; i++) {
	    	int[] s0 = xor(rightRotate(w[i-15], 7), xor(rightRotate(w[i-15], 18), rightShift(w[i-15], 3)));
	    	int[] s1 = xor(rightRotate(w[i-2], 17), xor(rightRotate(w[i-2], 19), rightShift(w[i-2], 10)));
	    	w[i] = add(w[i-16], add(s0, add(w[i-7], s1)));
	    }
	    
	    for (int i=0; i<rounds; i++) {
		    S1 = xor(rightRotate(e, 6), xor(rightRotate(e, 11), rightRotate(e, 25)));
		    ch = select(e, f, g);
		    temp1 = add(h, add(S1, add(ch, add(constK[i], w[i]))));
		    S0 = xor(rightRotate(a, 2), xor(rightRotate(a, 13), rightRotate(a, 22)));
		    maj = majo(a, b, c);
		    temp2 = add(S0, maj);
		    
		    h = g;
		    g = f;
		    f = e;
		    e = add(d, temp1);
		    d = c;
		    c = b;
		    b = a;
		    a = add(temp1, temp2);
	    }
	    
	    h0 = add(h0, a);
	    h1 = add(h1, b);
	    h2 = add(h2, c);
	    h3 = add(h3, d);
	    h4 = add(h4, e);
	    h5 = add(h5, f);
	    h6 = add(h6, g);
	    h7 = add(h7, h);
	    // END OF OUTER LOOP
	    
	    // Output to be cracked
	    fix(h0, new BigInteger("0"));
	    fix(h1, new BigInteger("0"));
	    fix(h2, new BigInteger("0"));
	    fix(h3, new BigInteger("0"));
	    fix(h4, new BigInteger("0"));
	    fix(h5, new BigInteger("0"));
	    fix(h6, new BigInteger("0"));
	    fix(h7, new BigInteger("0"));
	}

	private int[] rightShift(int[] var, int positions) {
		int[] result = new int[var.length];
		for (int i=positions; i<var.length; i++) {
			result[i] = var[i-positions];
		}
		return result;
	}

	private int[] majo(int[] a, int[] b, int[] c) {
		return xor(and(a, b), xor(and(b, c), and(a, c)));
	}

	private int[] select(int[] e, int[] f, int[] g) {
		return xor(and(e, f), and(not(e), g));
	}

	private int[] rightRotate(int[] e, int positions) {
		int[] result = new int[e.length];
		for (int i=0; i<e.length; i++) {
			result[(i+positions) % e.length] = e[i];
		}
		return result;
	}
}
