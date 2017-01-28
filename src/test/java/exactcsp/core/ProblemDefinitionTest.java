package exactcsp.core;

import static org.mockito.Mockito.*;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import exactcsp.core.Matrix;
import exactcsp.core.ProblemDefinition;

@RunWith(MockitoJUnitRunner.class)
public class ProblemDefinitionTest {

    @InjectMocks
    private ProblemDefinition problemDefinition;

    @Mock
    private Matrix matrixMock;
    
    @Captor
    ArgumentCaptor<int[]> vars;

    @Captor
    ArgumentCaptor<int[]> values;

    @Test
    public void testNewVar() {
        int[] var = problemDefinition.newVar(32, true);
        Assert.assertEquals(32, var.length);
        verify(matrixMock, times(32)).newCol();
        verify(matrixMock, times(32)).addToDecisionVariables(anyInt());        
    }

    @Test
    public void testNewConst() {
        int[] var = problemDefinition.newConst(6, BigInteger.valueOf(7));
        Assert.assertArrayEquals(new int[] {1,1,1,0,0,0}, var);   
    }

    @Test
    public void testFix() {
        int[] var = new int[] {3,4,5}; 
        problemDefinition.fix(var, BigInteger.valueOf(7));
        verify(matrixMock, times(3)).setCol(anyInt(), eq(1));
    }

    @Test
    public void testGetSelector() {
        int[] var = problemDefinition.getSelector(32, BigInteger.valueOf(123456), true);
        Assert.assertEquals(32, var.length);
        verify(matrixMock, times(1)).newCol();
        verify(matrixMock, times(1)).addToDecisionVariables(anyInt()); 
    }

    @Test
    public void testXorIntInt() {
        when(matrixMock.newCol()).thenReturn(5).thenReturn(6).thenReturn(7);
        problemDefinition.xor(3, 4);
        verify(matrixMock, times(2)).addRow(eq(0), vars.capture(), values.capture());
        Assert.assertArrayEquals(new int[] {3,4,6,5}, vars.getAllValues().get(0));
        Assert.assertArrayEquals(new int[] {1,1,1,2}, values.getAllValues().get(0));
        Assert.assertArrayEquals(new int[] {6,7,5}, vars.getAllValues().get(1));
        Assert.assertArrayEquals(new int[] {1,2,1}, values.getAllValues().get(1));
    }

    @Test
    public void testAndOk1() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newVar(3, true);
        pd.fix(a, BigInteger.valueOf(6));
        int[] b = pd.newConst(3, BigInteger.valueOf(7));
        int[] c = pd.and(a, b);
        pd.fix(c, BigInteger.valueOf(6));
        Assert.assertTrue(pd.solve());
    }
    
    @Test
    public void testAndOk2() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newConst(3, BigInteger.valueOf(6));
        int[] b = pd.newVar(3, true);
        pd.setCol(b[0], 1);
        pd.setCol(b[2], 1);
        int[] c = pd.and(a, b);
        pd.fix(c, BigInteger.valueOf(6));
        Assert.assertTrue(pd.solve());
    }
    
    @Test
    public void testAndFail() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newConst(2, BigInteger.valueOf(3));
        int[] b = pd.newVar(2, true);
        pd.setCol(b[0], 0);
        int[] c = pd.and(a, b);
        pd.fix(c, BigInteger.valueOf(3));
        Assert.assertFalse(pd.solve());
    }

    @Test
    public void testNotOk() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newVar(2, true);
        pd.fix(a, BigInteger.valueOf(2));
        int[] b = pd.not(a);
        pd.fix(b, BigInteger.valueOf(1));
        Assert.assertTrue(pd.solve());
    }
    
    @Test
    public void testNotFail() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newVar(2, true);
        pd.fix(a, BigInteger.valueOf(2));
        int[] b = pd.not(a);
        pd.fix(b, BigInteger.valueOf(2));
        Assert.assertFalse(pd.solve());
    }

    @Test
    public void testXorIntArrayIntArrayOk() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newVar(2, true);
        pd.fix(a, BigInteger.valueOf(1));
        int[] b = pd.not(a);
        pd.fix(b, BigInteger.valueOf(2));
        Assert.assertTrue(pd.solve());
    }

    @Test
    public void testXorIntArrayIntArrayFail() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newVar(2, true);
        pd.fix(a, BigInteger.valueOf(2));
        int[] b = pd.not(a);
        pd.fix(b, BigInteger.valueOf(2));
        Assert.assertFalse(pd.solve());
    }

    @Test
    public void testAddIntArrayIntArrayOk() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newVar(32, true);
        pd.fix(a, BigInteger.valueOf(100000000));
        int[] b = pd.newConst(32, BigInteger.valueOf(100000000));
        int[] c = pd.add(a, b);
        pd.fix(c, BigInteger.valueOf(200000000));
        Assert.assertTrue(pd.solve());
    }
    
    @Test
    public void testAddIntArrayIntArrayFail() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newVar(32, true);
        pd.fix(a, BigInteger.valueOf(100000000));
        int[] b = pd.newConst(32, BigInteger.valueOf(100000002));
        int[] c = pd.add(a, b);
        pd.fix(c, BigInteger.valueOf(200000000));
        Assert.assertFalse(pd.solve());
    }

    @Test
    public void testAddIntArrayIntArrayIntArrayOk() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newVar(32, true);
        pd.fix(a, BigInteger.valueOf(100000000));
        int[] b = pd.newConst(32, BigInteger.valueOf(100000000));
        int[] c = pd.newConst(32, BigInteger.valueOf(200000000));
        pd.add(a,b,c);
        Assert.assertTrue(pd.solve());
    }
    
    @Test
    public void testAddIntArrayIntArrayIntArrayFail() {
        // integration test
        ProblemDefinition pd = new ProblemDefinition(new Matrix());
        int[] a = pd.newVar(32, true);
        pd.fix(a, BigInteger.valueOf(100000000));
        int[] b = pd.newConst(32, BigInteger.valueOf(100000004));
        int[] c = pd.newConst(32, BigInteger.valueOf(200000000));
        pd.add(a,b,c);
        Assert.assertFalse(pd.solve());
    }
}
