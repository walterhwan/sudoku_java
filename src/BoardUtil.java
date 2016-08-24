import java.awt.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a utility class for swapping or randomize the board while still
 * maintain it as a valid solution (Assuming that the board given is correct and complete)
 *
 * @author Hsuanchen Wan(Walter).
 */
public final class BoardUtil {
    // make the class cannot be instantiated
    private BoardUtil(){
    }

    /**
     * BoardUtil two cellsJButton
     *
     * @param x1 x coordinate of cell 1
     * @param y1 y coordinate of cell 1
     * @param x2 x coordinate of cell 2
     * @param y2 y coordinate of cell 2
     */
    public static void swapCells(Map<Point, Cell> cells, int x1, int y1, int x2, int y2) {
        Point key1 = new Point(x1, y1);
        Point key2 = new Point(x2, y2);
        Cell cell = cells.get(key1);
        cells.put(key1, cells.get(key2));
        cells.put(key2, cell);
    }

    /**
     * BoardUtil two rows in the board
     *
     * @param cells the board data
     * @param row1 the first row
     * @param row2 the second row
     */
    public static void swapRows(Map<Point, Cell> cells, int row1, int row2) {
        // do not allow swap if rows do not belong to the same row block
//        if ((row1-1)/3 != (row2-1)/3) {
//            throw new AssertionError("rows do not belong to the same row block.");
//        }

        for (int indRow = 1; indRow <= 9; indRow++) {
            swapCells(cells, indRow, row1, indRow, row2);
        }
    }

    /**
     * BoardUtil two columns in the board
     *
     * @param cells the board data
     * @param col1 the first column
     * @param col2 the second column
     */
    public static void swapCols(Map<Point, Cell> cells, int col1, int col2) {
        // do not allow swap if cols do not belong to the same column block
//        if ((col1 - 1) / 3 != (col2 - 1) / 3) {
//            throw new AssertionError("Columns do not belong to the same column block.");
//        }

        for (int indCol = 1; indCol <= 9; indCol++) {
            swapCells(cells, col1, indCol, col2, indCol);
        }
    }

    /**
     * BoardUtil the whole row block (i.e. swap row 1,2,3 and row 4,5,6)
     *
     * @param cells the board data
     * @param rb1 row block 1
     * @param rb2 row block 2
     */
    public static void swapRowBlocks(Map<Point, Cell> cells, int rb1, int rb2) {
        // for each row in the row block
        for (int row = 1; row <= 3; row++) {
            // swap each row
            swapRows(cells, (rb1 - 1) * 3 + row, (rb2 - 1) * 3 + row);
        }
    }

    /**
     * BoardUtil the whole column block (i.e. swap column 7,8,9 and column 4,5,6)
     *
     * @param cells the board data
     * @param cb1 column block 1
     * @param cb2 column block 2
     */
    public static void swapCOlBlocks(Map<Point, Cell> cells, int cb1, int cb2) {
        // for each row in the row block
        for (int col = 1; col <= 3; col++) {
            // swap each column
            swapCols(cells, (cb1 - 1) * 3 + col, (cb2 - 1) * 3 + col);
        }
    }

    /**
     * Randomly swapping rows
     *
     * @param cells the board data
     * @param times the method will swap this many times
     */
    private static void swapRandomRows(Map<Point, Cell> cells, int times) {
        int randRowBlock;
        ArrayList<Integer> randRow;

        for (int i = 0; i < times; i++) {
            randRow = new ArrayList<>();
            randRow.add(1);
            randRow.add(2);
            randRow.add(3);
            Collections.shuffle(randRow);
            randRowBlock = ThreadLocalRandom.current().nextInt(1, 3 + 1);

            swapRows(cells, randRowBlock * 3 + randRow.get(0),
                    randRowBlock * 3 + randRow.get(1));
        }
    }

    /**
     * Randomly swapping colunms
     *
     * @param cells the board data
     * @param times the method will swap this many times
     */
    private static void swapRandomCols(Map<Point, Cell> cells, int times) {
        int randColBlock;
        ArrayList<Integer> randCols;

        for (int i = 0; i < times; i++) {
            randCols = new ArrayList<>();
            randCols.add(1);
            randCols.add(2);
            randCols.add(3);
            Collections.shuffle(randCols);
            randColBlock = ThreadLocalRandom.current().nextInt(1, 3 + 1);

            swapCols(cells, randColBlock * 3 + randCols.get(0),
                    randColBlock * 3 + randCols.get(1));
        }
    }

    /**
     * BoardUtil random row blocks
     *
     * @param cells the board data
     * @param times the method will swap this many times
     */
    private static void swapRandomRowBlocks(Map<Point, Cell> cells, int times) {
        ArrayList<Integer> randRowBlocks;

        for (int i = 0; i < times; i++) {
            randRowBlocks = new ArrayList<>();
            randRowBlocks.add(1);
            randRowBlocks.add(2);
            randRowBlocks.add(3);
            Collections.shuffle(randRowBlocks);

            swapRowBlocks(cells, randRowBlocks.get(0), randRowBlocks.get(1));
        }
    }

    /**
     * BoardUtil random column blocks
     *
     * @param cells the board data
     * @param times the method will swap this many times
     */
    private static void swapRandomColBlocks(Map<Point, Cell> cells, int times) {
        ArrayList<Integer> randColBlocks;

        for (int i = 0; i < times; i++) {
            randColBlocks = new ArrayList<>();
            randColBlocks.add(1);
            randColBlocks.add(2);
            randColBlocks.add(3);
            Collections.shuffle(randColBlocks);

            swapCOlBlocks(cells, randColBlocks.get(0), randColBlocks.get(1));
        }
    }

    /**
     * Roatate the board 90 degree clockwise
     * @param cells the board data
     */
    private static void rotateBoard(Map<Point, Cell> cells) {
        Map<Point, Cell> newCells = new HashMap<>();
        int counter = 0;
        for (int x = 9; x >= 1; x--) {
            for (int y = 1; y <= 9; y++) {
                newCells.put(new Point(x, y),
                        cells.get(new Point(counter % 9 + 1, (counter / 9 + 1) )));
                counter++;
            }
        }
        cells = newCells;
    }

    /**
     * Use swapRowBlocks, swapRandomRows, swapRandomRowBlocks, swapRandomColBlocks
     * and rotateBoard to randomize the board
     * @param cells the board data
     */
    public static void randomizeBoard(Map<Point, Cell> cells) {
        for (int i = 0; i < 20; i++) {
            swapRandomRows(cells, 10);
            swapRandomColBlocks(cells, 1);
            swapRandomCols(cells, 10);
            swapRandomRowBlocks(cells, 1);
            rotateBoard(cells);
        }
    }
}
