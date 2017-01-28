package exactcsp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import exactcsp.core.ProblemDefinitionTest;
import exactcsp.examples.Sha1Test;
import exactcsp.examples.Sha256Test;
import exactcsp.examples.SubsetSumTest;

@RunWith(Suite.class)
@SuiteClasses({ ProblemDefinitionTest.class, 
        Sha1Test.class, 
        Sha256Test.class,
        SubsetSumTest.class})
public class AllTests {

}
