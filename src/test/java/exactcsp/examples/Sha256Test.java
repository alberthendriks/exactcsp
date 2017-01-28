package exactcsp.examples;

import org.junit.Assert;
import org.junit.Test;

public class Sha256Test {

    @Test
    public void testSha256() {
        // integration test
        Assert.assertTrue(new Sha256(16, 8).solve());
        Assert.assertFalse(new Sha256(16, 16).solve());
    }

}
