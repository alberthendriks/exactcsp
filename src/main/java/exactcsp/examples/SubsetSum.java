package exactcsp.examples;

import java.math.BigInteger;
import java.util.Random;

import exactcsp.core.Matrix;
import exactcsp.core.ProblemDefinition;

public class SubsetSum extends ProblemDefinition 
{	
	private final static int NUM_BITS = 128;
	private final static int MAX_BITS = 138;
	private final static int NUM_NRS = 16;
	
    public static void main( String[] args )
    {
        long startTime = System.currentTimeMillis();
        
        BigInteger[] items = new BigInteger[NUM_NRS];
        Random rnd = new Random();
        for (int i=0; i<items.length; i++) {
            items[i] = new BigInteger(NUM_BITS, rnd); 
        }

        // 0: solution is possbile. 1 or other: solution is probably not possible.
        BigInteger subsetSum = new BigInteger("0");
        for (BigInteger item: items) {
            if (rnd.nextBoolean()) {
                subsetSum = subsetSum.add(item);
            }
        }
        System.out.println("target sum: " + subsetSum);
        if (new SubsetSum(MAX_BITS, subsetSum, items).solve()) {
            System.out.println("hasSolution");
        } else {
            System.out.println("NO Solution");
        }
        System.out.println((System.currentTimeMillis()-startTime) + " ms");
    }
    
    public SubsetSum(int maxBits, BigInteger subsetSum, BigInteger[] items) {
        super(new Matrix());
               
        int[] prev = getSelector(maxBits, items[0], true);
        
        for (int i=1; i<items.length-1; i++) {
            prev = add(prev, getSelector(maxBits, items[i], true));
        }
        
        add(prev, getSelector(maxBits, items[items.length-1], true), newConst(maxBits, subsetSum));
        
    }
}
