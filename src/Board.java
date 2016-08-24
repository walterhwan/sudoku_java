import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Board class represent a board of 9x9 cells in a Sudoku game
 * <p>
 * It uses several different well-established method to solve the puzzle when user ask for tip
 * reference: https://www.sudokuoftheday.com/techniques/
 *
 * @author Hsuanchen Wan(Walter).
 */
public class Board implements Cloneable {
    // a HashMap object that store all the cells
    // it use java.awt.Point to specify coordinates of cells
    public Map<Point, Cell> cells = new HashMap<>();
    public Map<Point, Cell> originalCells = new HashMap<>();
    public Cell testCell;

    /**
     * A complete Sudoku have three rules. Any rows, columns, and blocks should contain all the digits from 1 to 9.
     */
    public enum RULE {
        ROW, COL, BLOCK
    }

    /**
     * Constructor
     * it sets all cells to 0's (i.e. an empty board)
     */
    Board() {
        // initialize cells
        for (int y = 1; y <= 9; y++) {
            for (int x = 1; x <= 9; x++) {
                cells.put(new Point(x, y), new Cell());
            }
        }
        for (int y = 1; y <= 9; y++) {
            for (int x = 1; x <= 9; x++) {
                originalCells.put(new Point(x, y), new Cell());
            }
        }
    }

    /**
     * Fill the board with a fixed board that satisfy all the game rules
     *
     * @param puzzleLevel the difficulty of the puzzle (1~5)
     * @param puzzleNum   a specific puzzle in the file (1~10000)
     */
    public void getPuzzleFromFIle(int puzzleLevel, int puzzleNum) {
        String fileName = "level" + puzzleLevel + "Puzzles.txt";
        String puzzle;

        // Try different ways of reading the file.
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(System.getProperty("user.dir") + fileName));
            for (int i = 0; i < puzzleNum; i++) {
                puzzle = reader.readLine();
                listNumToCells(puzzle);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            try {
                BufferedReader reader = new BufferedReader(
                        new FileReader(fileName));
                for (int i = 0; i < puzzleNum; i++) {
                    puzzle = reader.readLine();
                    listNumToCells(puzzle);
                }
                reader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("the file is not in [working dir]/src/HW5");
            e.printStackTrace();
        }

        // eliminate possibilities for the whole puzzle
        eliminate();
    }

    /**
     * Read from a String of numbers and return a 2D int array
     *
     * @param listNum list of numbers
     */
    public void listNumToCells(String listNum) {
        int x = 0, y = 0;
        Cell cell;
        for (int i = 0; i < listNum.length(); i++) {
            cell = cells.get(new Point(x + 1, y + 1));
            cell.setNum(Character.getNumericValue(listNum.charAt(i)));
            cell.resetPossibility();
            originalCells.get(new Point(x + 1, y + 1)).setNum(Character.getNumericValue(listNum.charAt(i)));
            x++;
            if (x > 8) {
                x = 0;
                y++;
            }
        }

    }

    /**
     * Check if a row, column, or a block is legal.
     * i.e. isNumLegal(RULE.COL, 7) : check if column 7 have two of the same number
     * block number is represented as
     * |---|---|---|
     * | 1 | 2 | 3 |
     * |---|---|---|
     * | 4 | 5 | 6 |
     * |---|---|---|
     * | 7 | 8 | 9 |
     * |---|---|---|
     * <p>
     * if one choose block 6, it means checking the cells in row 4~6 and column 7~9
     *
     * @param rule choose which rule to check
     * @param num  a row, column, or block num
     * @return false if there are
     */
    public boolean isLegal(RULE rule, int num) {
        ArrayList<Integer> listCells = new ArrayList<>();
        boolean isLegal = true;

        // depend on the "rule" specified, use different ways to assemble the list
        if (rule == RULE.ROW) {
            // put a row of cells to listCells
            for (int col = 1; col <= 9; col++) {
                listCells.add(cells.get(new Point(col, num)).getNum());
            }
        } else if (rule == RULE.COL) {
            // put a column of cells to listCells
            for (int row = 1; row <= 9; row++) {
                listCells.add(cells.get(new Point(num, row)).getNum());
            }
        } else if (rule == RULE.BLOCK) {
            // put a block of cells to listCells
            int[] blockPo = blockNumberToBlockRowCol(num);
            for (int row = (blockPo[0] - 1) * 3 + 1; row < (blockPo[0] * 3) + 1; row++) {
                for (int col = (blockPo[1] - 1) * 3 + 1; col < (blockPo[1] * 3) + 1; col++) {
                    listCells.add(cells.get(new Point(col, row)).getNum());
                }
            }
        }

        // check if legal
        for (int i = 0; i < 9; i++) {
            for (int j = i + 1; j < 9; j++) {
                // if two numbers are the same, except the number is zero
                if (listCells.get(i).equals(listCells.get(j)) && !listCells.get(i).equals(0)) {
                    isLegal = false;
                }
            }
        }

        return isLegal;
    }

    /**
     * Eliminate all possibilities associated with a cell
     *
     * @param correctCell the Point where the correct cell is located
     */
    public void eliminate(Point correctCell) {
        eliminate(RULE.ROW, correctCell, cells.get(correctCell).getNum());
        eliminate(RULE.COL, correctCell, cells.get(correctCell).getNum());
        eliminate(RULE.BLOCK, correctCell, cells.get(correctCell).getNum());
    }

    /**
     * Eliminate possibilities for a row, column, or block based on a correct cell.
     *
     * @param rule        choose which rule to apply
     * @param correctCell the Point where the correct cell is located
     * @param num         number to be eliminated
     */
    public void eliminate(RULE rule, Point correctCell, int num) {
        if (num == 0) {
            return;
        }
        int col = correctCell.x;
        int row = correctCell.y;

        if (rule == RULE.ROW) {
            for (int i = 1; i <= 9; i++) {
                cells.get(new Point(i, row)).removePossibility(num);
            }
        } else if (rule == RULE.COL) {
            for (int i = 1; i <= 9; i++) {
                cells.get(new Point(col, i)).removePossibility(num);
            }
        } else if (rule == RULE.BLOCK) {
            int br = ((row - 1) / 3) + 1;
            int bc = ((col - 1) / 3) + 1;
            for (int j = (br - 1) * 3 + 1; j < (br * 3) + 1; j++) {
                for (int i = (bc - 1) * 3 + 1; i < (bc * 3) + 1; i++) {
                    cells.get(new Point(i, j)).removePossibility(num);
                }
            }
        }
    }

    /**
     * Eliminate board-wise.
     * It is used usually when a new board is created
     */
    private void eliminate() {
        Cell cell;
        for (int j = 1; j <= 9; j++) {
            for (int i = 1; i <= 9; i++) {
                cell = cells.get(new Point(i, j));
                if (cell.getNum() != 0) {
                    cell.possibleNumbers = new ArrayList<>();
                    eliminate(new Point(i, j));
                }
            }
        }
    }

    /**
     * Translate from "block number" to the block row and column number.
     * <p>
     * block number is represented as
     * |---|---|---|
     * | 1 | 2 | 3 |
     * |---|---|---|
     * | 4 | 5 | 6 |
     * |---|---|---|
     * | 7 | 8 | 9 |
     * |---|---|---|
     * <p>
     * input of block 6 will translate to block in row 2, column 3
     * return "new int[]{2, 3}"
     *
     * @param blockNum block number
     * @return an array consists of row and column
     */
    public static int[] blockNumberToBlockRowCol(int blockNum) {
        return new int[]{(blockNum - 1) / 3 + 1, (blockNum - 1) % 3 + 1};
    }

    /**
     * solveCell fill in a number to the board and eliminate the possibility
     * of other related cells on that number
     *
     * @param point   the point to be filled
     * @param cellNum the number to be filled
     */
    public void solveCell(Point point, int cellNum) {
        Cell cell = cells.get(point);
        cell.setNum(cellNum);

        while (!cell.possibleNumbers.isEmpty()) {
            cell.possibleNumbers.remove(0);
        }
        // eliminate the possibility of other cells
        eliminate(point);
    }

    /**
     * Undo a solved cell
     *
     * @param point the Point coordinate of the cell
     */
    public void undoSolved(Point point) {
        Cell cell = cells.get(point);
        cell.setNum(0);

        // refresh all cells possibility number
        for (int col = 1; col <= 9; col++) {
            for (int row = 1; row <= 9; row++) {
                cells.get(new Point(col, row)).resetPossibility();
            }
        }
        eliminate();
    }

    /**
     * Inspect every cell and find cells with only one possibility and assign it.
     * at the same time eliminate the possibility of other associate cells
     *
     * @param num solve number of times
     * @return false if nothing can be solved
     */
    public boolean solveSingleSolution(int num) {
        int solveCount = 0;
        boolean solvable = true;

        // Continue to solve until the solveCount is bigger than num
        // or until nothing can be solve after the whole array has been inspected.
        while (solvable) {
            solvable = false;
            for (int j = 1; j <= 9; j++) {
                for (int i = 1; i <= 9; i++) {
                    // if the cell exist only one possibility
                    if (cells.get(new Point(i, j)).possibleNumbers.size() == 1) {
                        solveCell(new Point(i, j), cells.get(new Point(i, j)).possibleNumbers.get(0));
                        solveCount++;
                        solvable = true;
                        if (solveCount >= num) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Try to solve a cell on column col
     *
     * @param col column number
     * @return false if fail to solve any cell
     */
    public boolean solveColSingleCandidate(int col) {
        int foundNumRow = 0;
        int numCount;
        for (int cellNum = 1; cellNum <= 9; cellNum++) {
            numCount = 0;
            for (int row = 1; row <= 9; row++) {
                if (cells.get(new Point(col, row)).doesPossibilityExist(cellNum)) {
                    numCount++;
                    foundNumRow = row;
                }
            }
            if (numCount == 1) {
                solveCell(new Point(col, foundNumRow), cellNum);
                return true;
            }
        }
        return false;
    }

    /**
     * Solve all columns
     *
     * @return false if fail to solve any cell
     */
    public boolean solveALLColsSingleCandidate() {
        for (int i = 1; i <= 9; i++) {
            if (solveColSingleCandidate(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to solve a cell on a row
     *
     * @param row row number
     * @return false if fail to solve any cell
     */
    public boolean solveRowSingleCandidate(int row) {
        int foundNumCol = 0;
        int numCount;
        for (int cellNum = 1; cellNum <= 9; cellNum++) {
            numCount = 0;
            for (int col = 1; col <= 9; col++) {
                if (cells.get(new Point(col, row)).doesPossibilityExist(cellNum)) {
                    numCount++;
                    foundNumCol = col;
                }
            }
            if (numCount == 1) {
                solveCell(new Point(foundNumCol, row), cellNum);
                return true;
            }
        }
        return false;
    }

    /**
     * Solve all rows
     *
     * @return false if fail to solve any cell
     */
    public boolean solveALLRowsSingleCandidate() {
        for (int i = 1; i <= 9; i++) {
            if (solveRowSingleCandidate(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Solve all blocks
     *
     * @return false if fail to solve any cell
     */
    public boolean solveAllBlockSingleCandidate() {
        for (int i = 1; i <= 9; i++) {
            if (solveBlockSingleCandidate(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Solve a blocks
     *
     * @param blockNum the block number
     * @return false if fail to solve any cell
     */
    public boolean solveBlockSingleCandidate(int blockNum) {
        int[] blockPo = Board.blockNumberToBlockRowCol(blockNum);
        int foundNumCol = 0;
        int foundNumRow = 0;

        int numCount;
        for (int cellNum = 1; cellNum <= 9; cellNum++) {
            numCount = 0;
            for (int row = (blockPo[0] - 1) * 3 + 1; row < (blockPo[0] * 3) + 1; row++) {
                for (int col = (blockPo[1] - 1) * 3 + 1; col < (blockPo[1] * 3) + 1; col++) {
                    if (cells.get(new Point(col, row)).doesPossibilityExist(cellNum)) {
                        numCount++;
                        foundNumCol = col;
                        foundNumRow = row;
                    }
                }
            }
            if (numCount == 1) {
                solveCell(new Point(foundNumCol, foundNumRow), cellNum);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the puzzle is solved.
     *
     * @return false if not solved
     */
    public boolean isSolved() {
        // check if all number is not zero
        for (int j = 1; j <= 9; j++) {
            for (int i = 1; i <= 9; i++) {
                if (cells.get(new Point(i, j)).getNum() == 0) {
                    return false;
                }
            }
        }
        // check if all rows, columns, and blocks
        for (int i = 1; i <= 9; i++) {
            if (!isLegal(RULE.ROW, i) || !isLegal(RULE.COL, i) || !isLegal(RULE.BLOCK, i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get a hash map object that contain all places where the num can be placed
     *
     * @param num the number
     * @return the hash map object
     */
    public HashMap<Point, Cell> getPossibilityMap(int num) {
        HashMap<Point, Cell> newCells = new HashMap<>();
        for (int y = 1; y <= 9; y++) {
            for (int x = 1; x <= 9; x++) {
                if (cells.get(new Point(x, y)).doesPossibilityExist(num)) {
                    newCells.put(new Point(x, y), new Cell(num));
                }
            }
        }
        return newCells;
    }

    /**
     * Print all possible places where specified number can be placed
     *
     * @param num number to look at
     */
    public void printPossibilityMap(int num) {
        boolean isAllZero = true;
        for (int j = 1; j <= 9; j++) {
            for (int i = 1; i <= 9; i++) {
                if (cells.get(new Point(i, j)).doesPossibilityExist(num)) {
                    isAllZero = false;
                }
            }
        }
        // don't show anything if the map is all zeros
        if (isAllZero) {
            return;
        }

        Map<Point, Cell> newCells = getPossibilityMap(num);
        printBoardOnConsole(newCells);
    }

    /**
     * Print a row of dash line.
     */
    private void printRowDashLine() {
        System.out.println("|-----------------------|");
    }

    /**
     * Show the entire board in console.
     *
     * @param cells the Map<Point, Cell> object containing cells of a board
     */
    public void printBoardOnConsole(Map<Point, Cell> cells) {
        System.out.println("");
        printRowDashLine();
        for (int y = 1; y <= 9; y++) {
            System.out.printf("| ");
            for (int x = 1; x <= 9; x++) {
                int printInt;
                printInt = cells.get(new Point(x, y)).getNum();
                if (printInt != 0) {
                    System.out.printf(printInt + " ");
                } else {
                    System.out.printf("  ");
                }
                if (x == 3 || x == 6) {
                    System.out.printf("| ");
                }
            }
            System.out.printf("| ");
            System.out.println("");
            if (y == 3 || y == 6) {
                printRowDashLine();
            }

        }
        printRowDashLine();
        System.out.println("");
    }

    /**
     * Actually make assumptions on a point
     *
     * @param board  the board to solve
     * @param points points that needed to make assumptions
     * @param num    number to fill in
     * @return false if making assumption cannot solve the board
     * @throws CloneNotSupportedException
     */
    public static boolean makeAssumption(Board board, List<Point> points, int num) throws CloneNotSupportedException {
        Board cloneBoard = board.clone();

        // assume the first one is true
        cloneBoard.solveCell(points.get(0), num);
        // continue to solve the puzzle
        while (cloneBoard.solveSingleSolution(1) || cloneBoard.solveALLRowsSingleCandidate()) {
            cloneBoard.solveALLColsSingleCandidate();
        }
        // check if solved
        if (cloneBoard.isSolved()) {
            // solve the real board
            board.solveCell(points.get(0), num);
            return true;
        } else {
            // check if there exist any cell with no possibility but no number assigned
            for (int y = 1; y <= 9; y++) {
                for (int x = 1; x <= 9; x++) {
                    Cell cell = cloneBoard.cells.get(new Point(x, y));
                    // if the assumption is wrong, delete that assumption
                    if (cell.getNum() == 0 && cell.possibleNumbers.size() == 0 && points.size() != 1) {
                        board.cells.get(points.get(0)).removePossibility(num);
                        board.eliminate();
                        return true;
                    }
                }
            }
            // if all legal but still not solved
            // make the second assumption
            if (points.size() > 1) {
                points.remove(0);
                return makeAssumption(board, points, num);
            }
        }
        return false;
    }

    /**
     * Solve a solution by making assumption
     *
     * @param board the board intended to solve
     * @return false if not solvable
     * @throws CloneNotSupportedException
     */
    public static boolean solveByAssumption(Board board) throws CloneNotSupportedException {
        HashMap<Point, Cell> possibilityMap;
        List<Point> points;
        Cell cell;

        for (int num = 1; num <= 9; num++) {
            possibilityMap = board.getPossibilityMap(num);
            // scan rows
            for (int row = 1; row <= 9; row++) {
                points = new ArrayList<>();
                for (int col = 1; col <= 9; col++) {
                    cell = possibilityMap.get(new Point(col, row));
                    if (cell != null && cell.getNum() == num) {
                        points.add(new Point(col, row));
                    }
                }
                if (points.size() == 2) {
                    return makeAssumption(board, points, num);
                }
            }

            // scan cols
            for (int col = 1; col <= 9; col++) {
                points = new ArrayList<>();
                for (int row = 1; row <= 9; row++) {
                    cell = possibilityMap.get(new Point(col, row));
                    if (cell != null && cell.getNum() == num) {
                        points.add(new Point(col, row));
                    }
                }
                if (points.size() == 2) {
                    return makeAssumption(board, points, num);
                }
            }

            // scan blocks
            for (int blockNum = 1; blockNum <= 9; blockNum++) {
                int[] blockPo = blockNumberToBlockRowCol(blockNum);

                for (int row = (blockPo[0] - 1) * 3 + 1; row < (blockPo[0] * 3) + 1; row++) {
                    points = new ArrayList<>();
                    for (int col = (blockPo[1] - 1) * 3 + 1; col < (blockPo[1] * 3) + 1; col++) {
                        cell = possibilityMap.get(new Point(col, row));
                        if (cell != null && cell.getNum() == num) {
                            points.add(new Point(col, row));
                        }
                    }
                    if (points.size() == 2) {
                        return makeAssumption(board, points, num);
                    }
                }
            }
        }

        return false;
    }

    /**
     * this is for cloning the Board object
     *
     * @return the cloned Board
     */
    @Override
    public Board clone() {
        Board clonedBoard = new Board();
        Point point;

        for (int row = 1; row <= 9; row++) {
            for (int col = 1; col <= 9; col++) {
                point = new Point(col, row);
                clonedBoard.cells.get(point).setNum(cells.get(point).getNum());
            }
        }
        clonedBoard.eliminate();

        return clonedBoard;
    }

    /**
     * for testing board solver on console
     *
     * @param args N/
     */
    public static void main(String[] args) {
        Board board;
//        board = new Board();
//        board.getPuzzleFromFIle(1, 9118);
//        board.printBoardOnConsole(board.cells);
//
//        while (!board.isSolved()) {
//            while (board.solveSingleSolution(1) || board.solveALLRowsSingleCandidate() || board.solveALLColsSingleCandidate()) {
//            }
//            if (!board.isSolved()) {
//                try {
//                    solveByAssumption(board);
//                } catch (CloneNotSupportedException e) {
//                    e.printStackTrace();
//                }
////            System.out.println("Print");
////            for (int i = 0; i < 10; i++) {
////                board.printPossibilityMap(i);
////            }
//            }
//        }
//
//        System.out.println("a.isSolved() = " + board.isSolved());
//        board.printBoardOnConsole(board.cells);


        // find not solvable puzzle on all level
        // 2728 7434 9118 Level3: 1460
        int puzzleNum = 1;
        do {
            System.out.println("puzzleNum = " + puzzleNum);
            board = new Board();
            board.getPuzzleFromFIle(2, puzzleNum);
            int counter = 0;

            while (!board.isSolved()) {
                while (board.solveSingleSolution(1) || board.solveALLRowsSingleCandidate() || board.solveALLColsSingleCandidate()) {
                    board.solveAllBlockSingleCandidate();
                }
                try {
                    if (solveByAssumption(board)) {
                        System.out.println("solved a point by assumption.");
                    }
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                counter++;

                if (!board.isSolved() && counter == 3) {
                    board.printBoardOnConsole(board.originalCells);
                    System.out.println("puzzleNum = " + puzzleNum);
                    System.out.println("a.isSolved() = " + board.isSolved());
                    board.printBoardOnConsole(board.cells);
                    for (int b = 1; b < 10; b++) {
                        board.printPossibilityMap(b);
                    }
                }
            }

            if (!board.isSolved()) {
                board.printBoardOnConsole(board.originalCells);
                System.out.println("puzzleNum = " + puzzleNum);
                System.out.println("a.isSolved() = " + board.isSolved());
                board.printBoardOnConsole(board.cells);
                for (int b = 1; b < 10; b++) {
                    board.printPossibilityMap(b);
                }
            }

            puzzleNum++;
        } while (board.isSolved());
    }
}
