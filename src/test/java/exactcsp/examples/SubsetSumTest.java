package exactcsp.examples;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

public class SubsetSumTest {

    @Test
    public void testSubsetSum() {
        Assert.assertTrue(new SubsetSum(16,
                BigInteger.valueOf(1489),
                convertToBigInteger(500, 200, 333, 489, 295, 287, 500) )
            .solve());
        Assert.assertFalse(new SubsetSum(16,
                BigInteger.valueOf(800),
                convertToBigInteger(500, 200, 333, 489, 295, 287, 500) )
            .solve());
    }
    
    private BigInteger[] convertToBigInteger(int... nrs) {
        BigInteger[] out = new BigInteger[nrs.length];
        for (int i=0; i<nrs.length; i++) {
            out[i] = BigInteger.valueOf(nrs[i]);
        }
        return out;
    }

}
