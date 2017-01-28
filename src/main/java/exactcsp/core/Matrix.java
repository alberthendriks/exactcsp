package exactcsp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class Matrix {

    private Int2IntOpenHashMap rowToSolution;
    private LinkedHashMap<Integer, Int2IntOpenHashMap> rowToCol;
    private HashMap<Integer, IntOpenHashSet> colToRow;

    private int currentRowToAdd = 0;
    private int currentColToAdd = 0;

    private Int2IntOpenHashMap valueAssignments;
    private IntOpenHashSet decisionVariables;
    private ArrayList<Integer> initialDecisionVariables;
    private IntOpenHashSet affectedRows;
    private IntOpenHashSet affectedRowsForPropagate;

    private Int2IntOpenHashMap rowToPivot;

    public Matrix() {
        rowToSolution = new Int2IntOpenHashMap();
        rowToCol = new LinkedHashMap<Integer, Int2IntOpenHashMap>();
        colToRow = new HashMap<Integer, IntOpenHashSet>();
        valueAssignments = new Int2IntOpenHashMap();
        decisionVariables = new IntOpenHashSet();
        int zero = newCol();
        colToRow.put(zero, new IntOpenHashSet());
        setCol(zero, 0);
        int one = newCol();
        colToRow.put(one, new IntOpenHashSet());
        setCol(one, 1);
        rowToPivot = new Int2IntOpenHashMap();

        affectedRows = new IntOpenHashSet();
        affectedRowsForPropagate = new IntOpenHashSet();
    }

    public Matrix(Matrix m) {
        rowToSolution = new Int2IntOpenHashMap(m.rowToSolution);
        rowToCol = new LinkedHashMap<Integer, Int2IntOpenHashMap>();
        for (int row : m.rowToCol.keySet()) {
            rowToCol.put(row, new Int2IntOpenHashMap(m.rowToCol.get(row)));
        }
        colToRow = new HashMap<Integer, IntOpenHashSet>();
        for (int col : m.colToRow.keySet()) {
            colToRow.put(col, new IntOpenHashSet(m.colToRow.get(col)));
        }
        valueAssignments = new Int2IntOpenHashMap(m.valueAssignments);
        decisionVariables = new IntOpenHashSet(m.decisionVariables);
        currentRowToAdd = m.currentRowToAdd;
        currentColToAdd = m.currentColToAdd;
        rowToPivot = new Int2IntOpenHashMap(m.rowToPivot);

        affectedRows = new IntOpenHashSet(m.affectedRows);
        affectedRowsForPropagate = new IntOpenHashSet(m.affectedRowsForPropagate);
    }

    public void addRow(int result, int[] cols, int[] values) {
        int row = currentRowToAdd++;
        affectedRows.add(row);
        rowToSolution.put(row, result);
        Int2IntOpenHashMap r = new Int2IntOpenHashMap();
        rowToCol.put(row, r);
        for (int i = 0; i < cols.length; i++) {
            int value = (getColValue(row, cols[i]) + values[i]) % 3;
            if (value == 0) {
                r.remove(cols[i]);
                if (colToRow.containsKey(cols[i])) {
                    colToRow.get(cols[i]).remove(row);
                }
                continue;
            }
            r.put(cols[i], value);
            if (!colToRow.containsKey(cols[i])) {
                colToRow.put(cols[i], new IntOpenHashSet());
            }
            colToRow.get(cols[i]).add(row);
        }
        redoSetCols();
    }

    public Int2IntOpenHashMap getValueAssignments() {
        return valueAssignments;
    }

    private void redoSetCols() {
        for (int col : valueAssignments.keySet()) {
            setCol(col, valueAssignments.get(col));
        }
    }

    public void setCol(int col, int value) {
        valueAssignments.put(col, value);
        if (!colToRow.containsKey(col)) {
            colToRow.put(col, new IntOpenHashSet());
        }
        for (int row : colToRow.get(col)) {
            affectedRows.add(row);
            affectedRowsForPropagate.add(row);
            int v = rowToCol.get(row).get(col);
            rowToCol.get(row).remove(col);
            rowToSolution.put(row, (rowToSolution.get(row) + 3 - v * value) % 3);
        }
        colToRow.get(col).clear();
    }

    public int newCol() {
        colToRow.put(currentColToAdd, new IntOpenHashSet());
        return currentColToAdd++;
    }

    public boolean propagate() {
        boolean result = prop();
        return result;
    }

    private boolean prop() {
        Boolean prop2 = true;

        while (prop2 != null && prop2 == true) {
            prop2 = null;
            sweep();
            Boolean prop = true;
            while (prop != null && prop == true) {
                prop = checkAndPropagate();
                if (prop != null && prop == false) {
                    return false;
                }
                if (prop != null && prop == true) {
                    prop2 = true;
                }
            }
        }
        return true;
    }

    private Boolean checkAndPropagate() {
        Boolean ret = simpleCheckAndPropagate();
        if (ret != null && ret == false) {
            return false;
        }

        IntOpenHashSet rows = affectedRowsForPropagate;
        affectedRowsForPropagate = new IntOpenHashSet();

        for (int row1 : rows) {
            if (rowToCol.get(row1).size() < 3) {
                continue;
            }
            for (int someCol : new IntOpenHashSet(rowToCol.get(row1).keySet())) {
                if (someCol == rowToPivot.get(row1)) {
                    continue;
                }
                for (int row2 : new IntOpenHashSet(colToRow.get(someCol))) {
                    if (row1 == row2) {
                        continue;
                    }
                    Boolean r = twoCheckAndPropagate(row1, row2);
                    if (r != null && r == false) {
                        return false;
                    }
                    if (r != null && r == true) {
                        ret = true;
                    }
                }
                break;
            }
        }
        return ret;
    }

    private Boolean twoCheckAndPropagate(int row1, int row2) {
        if (rowToCol.get(row2).size() < 3 || rowToCol.get(row1).size() != rowToCol.get(row2).size()) {
            return null;
        }
        Boolean ret = null;
        IntOpenHashSet r1 = new IntOpenHashSet(rowToCol.get(row1).keySet());
        IntOpenHashSet r2 = new IntOpenHashSet(rowToCol.get(row2).keySet());
        r1.remove(rowToPivot.get(row1));
        r2.remove(rowToPivot.get(row2));
        if (!(r1.containsAll(r2) && r2.containsAll(r1))) {
            return null;
        }
        Int2IntOpenHashMap added = new Int2IntOpenHashMap(rowToCol.get(row2));
        int solution = rowToSolution.get(row2);
        for (int i = 0; i < 2; i++) {
            solution = addRow1OnceToRow2(rowToCol.get(row1), rowToSolution.get(row1), added, solution);
            Boolean r = simple2CheckAndPropagate(added, solution);
            if (r != null && r == false) {
                return false;
            }
            if (r != null && r == true) {
                ret = true;
            }
        }
        return ret;
    }

    private Boolean simple2CheckAndPropagate(Int2IntOpenHashMap theRow, int solution) {
        if (theRow.keySet().size() != 2) {
            return null;
        }
        Boolean ret = null;
        int col1 = -1, col2 = -1, v1 = 0, v2 = 0;
        for (int c : theRow.keySet()) {
            if (col1 == -1) {
                col1 = c;
                v1 = theRow.get(c);
            } else {
                col2 = c;
                v2 = theRow.get(c);
            }
        }
        if (v1 == v2) {
            if (solution == 0) {
                setCol(col1, 0);
                setCol(col2, 0);
                ret = true;
            } else if (solution == (v1 + v2) % 3) {
                setCol(col1, 1);
                setCol(col2, 1);
                ret = true;
            }
        } else {
            if (solution == v1) {
                setCol(col1, 1);
                setCol(col2, 0);
                ret = true;
            } else if (solution == v2) {
                setCol(col2, 1);
                setCol(col1, 0);
                ret = true;
            }
        }
        return ret;
    }

    private Boolean simpleCheckAndPropagate() {
        Boolean ret = null;
        for (int row = 0; row < currentRowToAdd; row++) {
            if (rowToCol.get(row).isEmpty()) {
                if (rowToSolution.get(row) != 0) {
                    return false;
                }
                continue;
            }
            int size = rowToCol.get(row).size();
            int solution = rowToSolution.get(row);
            if (size == 1) {
                for (int c : rowToCol.get(row).keySet()) {
                    if (solution == 0) {
                        setCol(c, 0);
                    } else {
                        int val = rowToCol.get(row).get(c);
                        if (val != solution) {
                            return false;
                        }
                        setCol(c, 1);
                    }
                }
            } else if (size == 2) {
                Boolean r = simple2CheckAndPropagate(rowToCol.get(row), solution);
                if (r != null && r == false) {
                    return false;
                }
                if (r != null && r == true) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private void sweep() {
        IntOpenHashSet currentAffectedRows = affectedRows;
        IntOpenHashSet doneRows = new IntOpenHashSet();
        affectedRows = new IntOpenHashSet();
        while (!currentAffectedRows.isEmpty()) {
            for (int row : currentAffectedRows) {
                if (rowToPivot.containsKey(row) && colToRow.get(rowToPivot.get(row)).contains(row)) {
                    continue;
                }
                int col = -1;
                int size = Integer.MAX_VALUE;
                for (int c : rowToCol.get(row).keySet()) {
                    if (colToRow.get(c).size() < size) {
                        col = c;
                        size = colToRow.get(c).size();
                    }
                }
                if (col == -1) {
                    continue;
                }
                sweepWithRowForCol(row, col);
                rowToPivot.put(row, col);
            }
            doneRows.addAll(currentAffectedRows);
            currentAffectedRows = new IntOpenHashSet(affectedRows);
            currentAffectedRows.removeAll(doneRows);
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int r = 0; r < currentRowToAdd; r++) {
            result.append(rowToSolution.get(r) + ": ");
            for (int c = 0; c < currentColToAdd; c++) {
                Integer value = rowToCol.get(r).get(c);
                result.append(value == null ? "  " : (value + " "));
            }
            result.append("\n");
        }
        return new String(result);
    }

    public void sweepWithRowForCol(int row1, int sweepCol) {
        boolean keep = affectedRows.contains(row1);
        affectedRows.addAll(colToRow.get(sweepCol));
        if (!keep) {
            affectedRows.remove(row1);
        }
        keep = affectedRowsForPropagate.contains(row1);
        affectedRowsForPropagate.addAll(colToRow.get(sweepCol));
        if (!keep) {
            affectedRowsForPropagate.remove(row1);
        }
        int sweepColValue = rowToCol.get(row1).get(sweepCol);
        for (int row2 : new LinkedHashSet<Integer>(colToRow.get(sweepCol))) {
            if (row1 == row2) {
                continue;
            }
            if ((sweepColValue + rowToCol.get(row2).get(sweepCol)) % 3 == 0) {
                addRow1OnceToRow2(row1, row2);
            } else {
                addRow1TwiceToRow2(row1, row2);
            }
        }
    }

    private int getColValue(int row, int col) {
        return rowToCol.get(row).containsKey(col) ? rowToCol.get(row).get(col) : 0;
    }

    private void addRow1TwiceToRow2(int row1, int row2) {
        rowToSolution.put(row2, (2 * rowToSolution.get(row1) + rowToSolution.get(row2)) % 3);
        for (int col : rowToCol.get(row1).keySet()) {
            int v1 = getColValue(row1, col);
            int v2 = getColValue(row2, col);
            int newV2 = (2 * v1 + v2) % 3;
            if (newV2 == 0) {
                rowToCol.get(row2).remove(col);
                colToRow.get(col).remove(row2);
            } else {
                rowToCol.get(row2).put(col, newV2);
                colToRow.get(col).add(row2);
            }
        }
    }

    private void addRow1OnceToRow2(int row1, int row2) {
        rowToSolution.put(row2, (rowToSolution.get(row1) + rowToSolution.get(row2)) % 3);
        for (int col : rowToCol.get(row1).keySet()) {
            int v1 = getColValue(row1, col);
            int v2 = getColValue(row2, col);
            int newV2 = (v1 + v2) % 3;
            if (newV2 == 0) {
                rowToCol.get(row2).remove(col);
                colToRow.get(col).remove(row2);
            } else {
                rowToCol.get(row2).put(col, newV2);
                colToRow.get(col).add(row2);
            }
        }
    }

    private int addRow1OnceToRow2(Int2IntOpenHashMap row1, int solution1, Int2IntOpenHashMap row2, int solution2) {
        int solution = (solution1 + solution2) % 3;
        for (int col : row1.keySet()) {
            int v1 = getColValue(row1, col);
            int v2 = getColValue(row2, col);
            int newV2 = (v1 + v2) % 3;
            if (newV2 == 0) {
                row2.remove(col);
            } else {
                row2.put(col, newV2);
            }
        }
        return solution;
    }

    private int getColValue(Int2IntOpenHashMap row, int col) {
        return row.containsKey(col) ? row.get(col) : 0;
    }

    public void addToDecisionVariables(int col) {
        if (initialDecisionVariables == null) {
            initialDecisionVariables = new ArrayList<Integer>();
        }
        initialDecisionVariables.add(col);
        decisionVariables.add(col);
    }

    public IntArrayList getBranchCols() {
        Matrix m = new Matrix(this);
        IntArrayList cols = new IntArrayList();
        m.sweep();
        int col = m.selectCol();
        do {
            m.setCol(col, 0);
            m.decisionVariables.remove(col);
            cols.add(col);
            m.sweep();
            col = m.selectCol();
        } while (col != -1);
        return cols;
    }

    private int selectCol() {
        int bestCol = -1;
        int minCols = Integer.MAX_VALUE;
        for (int col : decisionVariables) {
            for (int row : colToRow.get(col)) {
                if (rowToCol.get(row).keySet().size() < minCols) {
                    bestCol = col;
                    minCols = rowToCol.get(row).keySet().size();
                    if (minCols <= 1) {
                        return bestCol;
                    }
                }
            }
        }
        return bestCol;
    }
    
    public boolean solve() {
        IntArrayList cols = getBranchCols();
        IntArrayList colsAreBranching = new IntArrayList();
        if (! propagate()) {
            return false;
        }
        for (@SuppressWarnings("unused") int col: decisionVariables) {
            colsAreBranching.add(0);
        }
        HashMap<Integer, Matrix> matrices = new HashMap<Integer, Matrix>();
        Matrix m = new Matrix(this);
        matrices.put(0, this);
        int stackIndex=0;
        boolean wentUp = true;
        while (0 <= stackIndex && stackIndex < cols.size()) {
            if (! wentUp) {
                matrices.remove(stackIndex+1);
            }
            System.out.println("Branching depth: " + stackIndex + " / " + cols.size());
            int col = cols.getInt(stackIndex);
            Integer value = m.getValueAssignments().containsKey(col) ? m.getValueAssignments().get(col) : null;
            // @todo improve readability
            if (value == null && (colsAreBranching.getInt(stackIndex) == 0)) {
                m = new Matrix(m);
                m.setCol(col, 0);
                colsAreBranching.set(stackIndex, 1);
                if (m.propagate()) {
                    stackIndex++;
                    wentUp = true;
                    matrices.put(stackIndex, m);
                } else {
                    m = matrices.get(stackIndex);                   
                }
            } else if (colsAreBranching.getInt(stackIndex) == 1) {
                m = new Matrix(m);
                m.setCol(col, 1);
                if (m.propagate()) {
                    colsAreBranching.set(stackIndex, 2);
                    stackIndex++;
                    wentUp = true;
                    matrices.put(stackIndex, m);
                } else {
                    colsAreBranching.set(stackIndex, 0);
                    stackIndex--;
                    wentUp = false;
                    m = matrices.get(stackIndex);
                }               
            } else if (colsAreBranching.getInt(stackIndex) == 2) {
                colsAreBranching.set(stackIndex, 0);
                if (! wentUp) {
                    int swap = cols.getInt(stackIndex);
                    cols.set(stackIndex, cols.getInt(stackIndex+1));
                    cols.set(stackIndex+1, swap);
                }
                stackIndex--;
                wentUp = false;
                m = matrices.get(stackIndex);               
            } else if (colsAreBranching.getInt(stackIndex) == 0) {
                if (wentUp) {
                    stackIndex++;
                    matrices.put(stackIndex, m);
                } else {
                    int swap = cols.getInt(stackIndex);
                    cols.set(stackIndex, cols.getInt(stackIndex+1));
                    cols.set(stackIndex+1, swap);
                    stackIndex--;
                }
            } else {
                System.err.println("err");
                System.exit(0);
            }
        }
        int cnt = 0;
        for (int b: colsAreBranching) {
            if (b!=0) { cnt++; };
        }
        if (stackIndex>0) {
            System.out.println("branched: " + cnt);
            //System.out.println(m);
            m.outputStatistics();
            for (int col: initialDecisionVariables) {
                System.out.print(""+m.getValueAssignments().get(col));
            }
            System.out.println();
        }
        return stackIndex > 0;
    }
    
    public void outputStatistics() {
        IntOpenHashSet rows = new IntOpenHashSet();
        IntOpenHashSet cols = new IntOpenHashSet();
        int totalCols = 0;
        for (int row : rowToCol.keySet()) {
            if (rowToCol.get(row).size() > 1) {
                rows.add(row);
                cols.addAll(rowToCol.get(row).keySet());
                totalCols += rowToCol.get(row).size();
            }
        }
        IntOpenHashSet vrs = new IntOpenHashSet(decisionVariables);
        if (!rows.isEmpty()) {
            System.out.println("rows: " + rows.size() + "; cols: " + cols.size() + "; avg cols per row: "
                    + (totalCols / rows.size()));
        }
        vrs.removeAll(valueAssignments.keySet());
        System.out.println("initial decision variables: " + decisionVariables.size() + "; still undecided: " + vrs.size());
    }

}
