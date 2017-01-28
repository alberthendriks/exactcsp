package exactcsp.examples;

import java.math.BigInteger;

import exactcsp.core.Matrix;
import exactcsp.core.ProblemDefinition;

public class Sha1 extends ProblemDefinition {

	private final static int ROUNDS = 13;
	private final static int FIX_WS = 8;
	
	private int[][] w;
	
    public static void main( String[] args )
    {
    	long startTime = System.currentTimeMillis();
    	Sha1 sha1 = new Sha1(ROUNDS, FIX_WS);
    	if (sha1.solve()) {
    		System.out.println("has solution");
    	} else {
    		System.out.println("NO solution");
    	}
    	System.out.println("time: " + (System.currentTimeMillis()-startTime) + " ms");
    }
	
	public Sha1(int rounds, int fixedWs) {
	    super(new Matrix());
	    
		w = new int[rounds][];
		for (int i=0; i<Math.min(16, rounds); i++) {
			w[i] = newVar(32, true);
		}
		
		for (int i=0; i<fixedWs; i++) {
			fix(w[i], new BigInteger("0"));
		}
		
		int[] h0 = newConst(32, BigInteger.valueOf(0x67452301));
		int[] h1 = newConst(32, BigInteger.valueOf(0xEFCDAB89));
		int[] h2 = newConst(32, BigInteger.valueOf(0x10325476));
		int[] h3 = newConst(32, BigInteger.valueOf(0x10325476));
		int[] h4 = newConst(32, BigInteger.valueOf(0xC3D2E1F0));

		// outer loop
		
	    int[] a = h0;
	    int[] b = h1;
	    int[] c = h2;
	    int[] d = h3;
	    int[] e = h4;
	    
	    int[] temp;

	    for (int i=16; i<rounds; i++) {
	    	w[i] = rightRotate(xor(w[i-3], xor(w[i-8], xor(w[i-14], w[i-16]))), 31);
	    }
	    
	    int f[];
	    int k[];
	    
	    for (int i=0; i<rounds; i++) {
	    	if (i<20) {
	    		f = select(b, c ,d);
	    		k = newConst(32, BigInteger.valueOf(0x5A827999));
	    	} else if (i<40) {
	    		f = xor(b, xor(c,d));
	    		k = newConst(32, BigInteger.valueOf(0x6ED9EBA1));
	    	} else if (i<60) {
	    		f = majo(b, c, d);
	    		k = newConst(32, BigInteger.valueOf(0x8F1BBCDC));
	    	} else {
	    		f = xor(b, xor(c,d));
	    		k = newConst(32, BigInteger.valueOf(0xCA62C1D6));
	    	}
	    	temp = add(rightRotate(a, 27), add(f, add(e, add(k, w[i])))); 
	    	
		    e = d; 	
		    d = c;
		    c = rightRotate(b, 2);
		    b = a;
		    a = temp;
	    }
	    
	    h0 = add(h0, a);
	    h1 = add(h1, b);
	    h2 = add(h2, c);
	    h3 = add(h3, d);
	    h4 = add(h4, e);
	    // END OF OUTER LOOP
	    
	    // Output to be cracked
	    fix(h0, new BigInteger("0"));
	    fix(h1, new BigInteger("0"));
	    fix(h2, new BigInteger("0"));
	    fix(h3, new BigInteger("0"));
	    fix(h4, new BigInteger("0"));
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
