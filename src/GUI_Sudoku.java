import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a Sudoku game.
 * "The objective of Sudoku is to fill a 9x9 grid with digits so that each column,
 * each row, and each of the nine 3x3 sub-grids that compose the grid also called
 * "blocks" contains all of the digits from 1 to 9.
 * The puzzle setter provides a partially completed grid,
 * which for a well-posed puzzle has a unique solution." --- Wikipedia.org
 * <p>
 * All you have to do to play this game is to select a number to be fill and click on the board.
 * <p>
 * This program has the ability to detect "obvious" wrong answer and show it on red color.
 * It also have the ability to solve a cell at a time for the player.
 * <p>
 * The puzzle (in text files) is generated by https://www.sudokuoftheday.com/techniques/
 * There are ways to generate puzzles(it can even select difficulty).
 * But it is too complex for this project.
 *
 * @author Hsuanchen Wan(Walter).
 */
public class GUI_Sudoku extends JFrame implements ActionListener {

    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private static final int FRAME_X_ORIGIN = 500; // -800; //
    private static final int FRAME_Y_ORIGIN = 50;

    // set colors
    private static final Color GREEN = new Color(17, 140, 78);
    private static final Color ORANGE = new Color(255, 144, 9);
    private static final Color RED = new Color(125, 0, 0);
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color BLACK = new Color(0, 0, 0);
    private static final Color BLUE = new Color(0, 70, 135);
    private static final Color LIGHT_GRAY = new Color(188, 188, 188);

    // set font
    private static final Font FONT_DIALOG = new Font("Dialog", Font.BOLD, 15);
    private static final Font FONT_PUZZLE_CELLS = new Font("Arial", Font.BOLD, 28);
    private static final Font FONT_SELECTED_NUM = new Font("Arial", Font.BOLD, 20);

    // set board swing elements
    private Board board = new Board();
    private ArrayList<Point> history = new ArrayList<>();
    private int puzzleLevel = 1;
    private int puzzleNum = 0;
    private int selectedNum;
    private boolean solveAllMode = false;
    private boolean[][] puzzleCells = new boolean[9][9];
    private boolean[][] oldPuzzleCells = new boolean[9][9];
    private JButton[][] cellsJButton = new JButton[9][9];
    private ActionListener[][] al = new ActionListener[9][9];

    /**
     * Check if the number to be written on the puzzle(toBeWrittenNum) is legal
     * meaning if the number violated one of the rules
     *
     * @param rule           types of rule (ROW, COL, BLOCK)
     * @param num            (ROW: check row [num]) (COL: check column [num]) (BLOCK: check block [num])
     * @param toBeWrittenNum number to be written on the puzzle
     * @return false if the rule is violated
     */
    private boolean isNumLegal(Board.RULE rule, int num, int toBeWrittenNum) {
        ArrayList<Integer> listCells = new ArrayList<>();
        boolean isLegal = true;

        // depend on the "rule" specified, use different ways to assemble the list
        if (rule == Board.RULE.ROW) {
            // put a row of cells to listCells
            for (int col = 1; col <= 9; col++) {
                listCells.add(board.cells.get(new Point(col, num)).getNum());
            }
        } else if (rule == Board.RULE.COL) {
            // put a column of cells to listCells
            for (int row = 1; row <= 9; row++) {
                listCells.add(board.cells.get(new Point(num, row)).getNum());
            }
        } else if (rule == Board.RULE.BLOCK) {
            // put a block of cells to listCells
            int[] blockPo = Board.blockNumberToBlockRowCol(num);
            for (int row = (blockPo[0] - 1) * 3 + 1; row < (blockPo[0] * 3) + 1; row++) {
                for (int col = (blockPo[1] - 1) * 3 + 1; col < (blockPo[1] * 3) + 1; col++) {
                    listCells.add(board.cells.get(new Point(col, row)).getNum());
                }
            }
        }

        // check if legal
        for (Integer i : listCells) {
            if (toBeWrittenNum == i) {
                isLegal = false;
            }
        }

        return isLegal;
    }

    /**
     * Translate between cell row and col number to block number
     *
     * @param row row number of a cell
     * @param col column number of a cell
     * @return block number
     */
    private int rowColToBlockNum(int row, int col) {
        int blockNum = 0;
        if (4 <= row && row <= 6) {
            blockNum += 3;
        } else if (7 <= row && row <= 9) {
            blockNum += 6;
        }

        if (1 <= col && col <= 3) {
            blockNum += 1;
        } else if (4 <= col && col <= 6) {
            blockNum += 2;
        } else if (7 <= col && col <= 9) {
            blockNum += 3;
        }
        return blockNum;
    }

    /**
     * Show a pop-up window and ask user if they want to solve another puzzle
     * if No, close the game.
     */
    private void popUpPuzzleSolved() {
        makeAllCellsWhite();

//        UIManager.put("OptionPane.font", new FontUIResource(FONT_DIALOG));
        int n = JOptionPane.showConfirmDialog(
                this,
                "Congratulation! You solved the puzzle."
                        + "\n Do you want to solve a new puzzle? ",
                "",
                JOptionPane.YES_NO_OPTION);
        // Yes == 0, No == 1 ( it is weird but it is default)
        if (n == 0) {
            // get a new puzzle
            getNewPuzzle();
        } else if (n == 1) {
            setVisible(false); //you can't see me!
            dispose();
        }
    }

    /**
     * Pick a random puzzle and reset the board
     */
    public void getNewPuzzle() {
        puzzleNum = ThreadLocalRandom.current().nextInt(1, 1000 + 1);
        resetBoard();
    }

    /**
     * Make all cells while
     * If it is RED (meaning that it is wrong) set the text to empty
     */
    private void makeAllCellsWhite() {
        JButton tempCell;
        for (int rowIn = 0; rowIn < 9; rowIn++) {
            for (int colIn = 0; colIn < 9; colIn++) {
                tempCell = cellsJButton[colIn][rowIn];
                if (tempCell.getBackground() == RED) {
                    tempCell.setBackground(WHITE);
                    tempCell.setText("");
                } else if (tempCell.getBackground() == BLUE) {
                    tempCell.setBackground(WHITE);
                }
            }
        }
    }

    /**
     * Solve the puzzle for the user. If the user is REALLY lazy.
     */
    public void solveAll() throws CloneNotSupportedException{
        solveAllMode = true;
        int count = 0;
        while (true) {
            if (count > 82) {
                return;
            } else {
                autoSolveACell();
            }
            count++;
        }
    }

    /**
     * Solve a cell for the user. If the user is too lazy.
     * The solver is not perfect. It haven't implement all the solving method.
     *
     * @throws CloneNotSupportedException
     * @return false if failed to solve
     */
    public boolean autoSolveACell() throws CloneNotSupportedException {
        makeAllCellsWhite();

        boolean solvedOne;
        // if one solver solved a solution, stop. Otherwise, try another method.
        if (board.solveSingleSolution(1) || board.solveALLRowsSingleCandidate()
                || board.solveALLColsSingleCandidate() || board.solveAllBlockSingleCandidate()
                || Board.solveByAssumption(board)) {
            // if one of the solver can solve it without making assumption
            // identify which one has been solved and make it as blue background.
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    Point point = new Point(col + 1, row + 1);
                    Cell cell = board.cells.get(point);
                    // check which cell is solved by by computer
                    if (!oldPuzzleCells[col][row]
                            && cell.getNum() != 0) {
                        cellsJButton[col][row].setText(String.valueOf(cell.getNum()));
                        cellsJButton[col][row].setBackground(BLUE);
                        cellsJButton[col][row].setForeground(BLACK);
                        oldPuzzleCells[col][row] = true;
                        history.add(point);
                        // check if the puzzle is solved
                        if (board.isSolved() && !solveAllMode) {
                            // pop-up asking if user want to solve another
                            popUpPuzzleSolved();
                        }
                    }
                }
            }
            solvedOne = true;
        } else {
            System.out.println("Auto solve failed");
            solvedOne = false;
        }
        // for debug
        board.printBoardOnConsole(board.cells);
        return solvedOne;
    }

    /**
     * When player solves a cell
     * @param e action event object
     */
    private void manualSolveACell(ActionEvent e) {
        makeAllCellsWhite();

        Object src = e.getSource();
        JButton button = (JButton) src;
        int selCol = Integer.parseInt(((JButton) src).getName().substring(0, 1));
        int selRow = Integer.parseInt(((JButton) src).getName().substring(1, 2));
        int selBlock = rowColToBlockNum(selRow, selCol);
        if (selectedNum != 0 && board.cells.get(new Point(selCol, selRow)).getNum() == 0) {
            // if the number selected is legal to enter into the cell
            if (isNumLegal(Board.RULE.BLOCK, selBlock, selectedNum)
                    && isNumLegal(Board.RULE.ROW, selRow, selectedNum)
                    && isNumLegal(Board.RULE.COL, selCol, selectedNum)) {
                System.out.println("correct");
                button.setForeground(BLACK);
                button.setBackground(WHITE);
                // actually put the number into Board object
                Point point = new Point(selCol, selRow);
                board.solveCell(point, selectedNum);
                button.setText(String.valueOf(selectedNum));
                oldPuzzleCells[selCol - 1][selRow - 1] = true;
                history.add(point);
                // check if the puzzle is solved
                if (board.isSolved()) {
                    // pop-up asking if user want to solve another
                    popUpPuzzleSolved();
                }
            } else {
                // for debug
                System.out.println("incorrect");
                button.setBackground(RED);
                button.setText(String.valueOf(selectedNum));
            }
        }
        // for debug
        board.printBoardOnConsole(board.cells);
    }

    /**
     * Undo the last solved cell. Including the one solved by the program
     */
    public void undoLastMove() {
        Point lastMove;
        if (!history.isEmpty()) {
            lastMove = history.remove(history.size() - 1);
            board.undoSolved(lastMove);

            // display
            makeAllCellsWhite();
            JButton button = cellsJButton[lastMove.x - 1][lastMove.y - 1];
            button.setBackground(WHITE);
            button.setText("");
            oldPuzzleCells[lastMove.x - 1][lastMove.y - 1] = false;
            // for debug
            board.printBoardOnConsole(board.cells);
        }
    }

    /**
     * Reset the board so it return to the original board. Discard all solved cells by user
     */
    public void resetBoard() {
        // clear the board
        board.getPuzzleFromFIle(puzzleLevel, puzzleNum);
        for (boolean[] row : puzzleCells)
            Arrays.fill(row, false);
        for (boolean[] row : oldPuzzleCells)
            Arrays.fill(row, false);
        int size = history.size();
        for (int i = 0; i < size; i++) {
            history.remove(0);
        }

        // set every cell in 9x9
        int tempInt;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                tempInt = board.cells.get(new Point(col + 1, row + 1)).getNum();
                cellsJButton[col][row].setForeground(GREEN);
                cellsJButton[col][row].setBackground(WHITE);
                cellsJButton[col][row].setFocusPainted(false);
                // if the cell is empty
                if (tempInt == 0) {
                    cellsJButton[col][row].setText("");
                    cellsJButton[col][row].setName(String.valueOf(col + 1) + String.valueOf(row + 1));
                    cellsJButton[col][row].removeActionListener(al[col][row]);
                    // setting ActionListener for every cells
                    al[col][row] = this::manualSolveACell;
                    cellsJButton[col][row].addActionListener(al[col][row]);
                    // if the cell has number
                } else {
                    cellsJButton[col][row].setText(String.valueOf(tempInt));
                    puzzleCells[col][row] = true;
                    oldPuzzleCells[col][row] = true;
                }
            }
        }
    }

    /**
     * Constructor. It include all the construction of the JFrame setting and its
     * elements
     */
    public GUI_Sudoku() {
        // Setting the board
        // set the default properties for JFrame
        setTitle("Play Sudoku! by Hsuanchen Wan (Walter)");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocation(FRAME_X_ORIGIN, FRAME_Y_ORIGIN);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Setting UI color and type
        Border greenLine, grayLine;
        grayLine = BorderFactory.createLineBorder(LIGHT_GRAY, 1);
        greenLine = BorderFactory.createLineBorder(GREEN, 3);

        GridBagLayout mainLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        GridLayout outerGridLayout = new GridLayout(3, 3);
        GridLayout innerGridLayout = new GridLayout(3, 3);
        JPanel paneCenter = new JPanel();
        JPanel paneRight = new JPanel();
        JPanel[] blocks = new JPanel[9];

        // setting up blocks and cells, for center grid
        setLayout(mainLayout);
        constraints.ipadx = 0;
        paneCenter.setLayout(outerGridLayout);
        int[] blockPo;
        for (int i = 0; i < 9; i++) {
            blocks[i] = new JPanel(innerGridLayout);
            blocks[i].setBorder(greenLine);
            blocks[i].setVisible(true);
            blockPo = Board.blockNumberToBlockRowCol(i + 1);
            for (int row = (blockPo[0] - 1) * 3; row < (blockPo[0] * 3); row++) {
                for (int col = (blockPo[1] - 1) * 3; col < (blockPo[1] * 3); col++) {
                    cellsJButton[col][row] = new JButton();
                    cellsJButton[col][row].setBorder(grayLine);
                    cellsJButton[col][row].setBackground(WHITE);
                    cellsJButton[col][row].setFont(FONT_PUZZLE_CELLS);
                    blocks[i].add(cellsJButton[col][row]);
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            paneCenter.add(blocks[i]);
        }
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        constraints.weightx = 0.93;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(paneCenter, constraints);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        constraints.weightx = 0.07;
        constraints.gridx = 1;
        constraints.gridy = 0;
        add(paneRight, constraints);

        // setting up panels for right grid
        paneRight.setLayout(new GridLayout(4, 1, 10, 10));
        JPanel selectButtonPanel = new JPanel();
        JPanel utilityButtonPanel = new JPanel();
        paneRight.add(selectButtonPanel);
        paneRight.add(utilityButtonPanel);

        // upper buttons in right grid
        TitledBorder titledBorder = new TitledBorder("Select a number to fill");
        titledBorder.setTitleFont(FONT_DIALOG);
        selectButtonPanel.setLayout(new GridBagLayout());
        selectButtonPanel.setBorder(titledBorder);
        JButton[] selectNumButtons = new JButton[9];
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.33;
        int counter = 0;
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                constraints.gridx = i;
                constraints.gridy = j;
                selectNumButtons[counter] = new JButton(String.valueOf(counter + 1));
                selectButtonPanel.add(selectNumButtons[counter], constraints);
                selectNumButtons[counter].setBackground(WHITE);
                selectNumButtons[counter].setFocusPainted(false);
                selectNumButtons[counter].setFont(FONT_SELECTED_NUM);
                selectNumButtons[counter].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Object src = e.getSource();
                        JButton button = (JButton) src;
                        for (int k = 0; k < 9; k++) {
                            selectNumButtons[k].setBackground(WHITE);
                        }
                        button.setBackground(ORANGE);
                        selectedNum = Integer.parseInt(button.getText());
                    }
                });
                counter++;
            }
        }

        // middle buttons in right grid
        utilityButtonPanel.setLayout(new GridLayout(4, 2, 5, 2));
        utilityButtonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        utilityButtonPanel.setVisible(true);
        JPanel blankPanel[] = new JPanel[6];
        for (int i = 0; i < 6; i++) {
            blankPanel[i] = new JPanel();
        }
        // Select Level button
        JLabel selectLevelLabel = new JLabel("Select level: ");
        selectLevelLabel.setHorizontalAlignment(SwingConstants.CENTER);
        selectLevelLabel.setFont(FONT_DIALOG);
        utilityButtonPanel.add(selectLevelLabel);
        String[] dropDownStrings = new String[]{"1", "2", "3",
                "4", "5"};
        JComboBox<String> levelList = new JComboBox<>(dropDownStrings);
        levelList.setFont(FONT_DIALOG);
        levelList.setSelectedIndex(0);
        levelList.addActionListener(e -> {
            JComboBox cb = (JComboBox) e.getSource();
            puzzleLevel = Integer.parseInt((String) cb.getSelectedItem());
            getNewPuzzle();
        });
        utilityButtonPanel.add(levelList);
        // Undo button
        JButton undoButton = new JButton("Undo");
        undoButton.setFocusPainted(false);
        undoButton.addActionListener(e -> undoLastMove());
        utilityButtonPanel.add(undoButton);
        // Reset button
        JButton resetButton = new JButton("Reset");
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetBoard());
        utilityButtonPanel.add(resetButton);
        // Solve button
        JButton solveButton = new JButton("Solve one");
        solveButton.setFocusPainted(false);
        solveButton.addActionListener(e -> {
            solveAllMode = false;
            try {
                autoSolveACell();
            } catch (CloneNotSupportedException e1) {
                e1.printStackTrace();
            }
        });
        utilityButtonPanel.add(solveButton);
        // solve all button
        JButton solveAllButton = new JButton("Solve All");
        solveAllButton.setFocusPainted(false);
        solveAllButton.addActionListener(e -> {
            try {
                solveAll();
            } catch (CloneNotSupportedException e1) {
                e1.printStackTrace();
            }
        });
        utilityButtonPanel.add(solveAllButton);

        // blank panels
        utilityButtonPanel.add(blankPanel[1]);
        utilityButtonPanel.add(blankPanel[2]);

        // Fill the board with a puzzle
        getNewPuzzle();
        board.printBoardOnConsole(board.cells);
    }

    /**
     * Somehow required, don't know why
     *
     * @param e the triggered element
     */
    @Override
    public void actionPerformed(ActionEvent e) {

    }

    public static void main(String[] args) {
        GUI_Sudoku frame = new GUI_Sudoku();
        frame.setVisible(true);
    }
}