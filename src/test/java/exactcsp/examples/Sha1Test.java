package exactcsp.examples;

import org.junit.Assert;
import org.junit.Test;

public class Sha1Test {

    @Test
    public void testSha1() {
        // integration test
        Assert.assertTrue(new Sha1(13, 8).solve());
        Assert.assertFalse(new Sha1(13, 13).solve());
    }

}
