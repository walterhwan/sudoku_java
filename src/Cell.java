import java.util.ArrayList;

/**
 * Cell class represent a cell in a Sudoku game
 *
 * @author Hsuanchen Wan(Walter).
 */
public class Cell implements Cloneable {
    // number in the cell that is correct
    private int num;
    // store possible number that can be put in the cell
    public ArrayList<Integer> possibleNumbers = new ArrayList<>();

    public Cell() {
        this(0);
        resetPossibility();
    }

    /**
     * Constructor
     *
     * @param num known confirmed number in the cell
     *            if one does not know the answer yet, use Cell()
     */
    Cell(int num) {
        this.num = num;
    }

    /**
     * Get the cell number.
     *
     * @return the cell number
     */
    public int getNum() {
        return num;
    }

    /**
     * Set the number
     *
     * @param num set this number (1~9)
     */
    public boolean setNum(int num) {
        if (num == 0) {
            this.num = num;
            return true;
        } else if (num > 0 && num <= 9) {
            this.num = num;
            return true;
        }
        return false;
    }

    /**
     * Remove a possibility in possibleNumbers ArrayList
     * num use Integer instead of int since it uses "remove Object" version of "remove()" in ArrayList
     *
     * @param num the number set to remove
     * @return false if the number do not exist
     */
    public boolean removePossibility(Integer num) {
        return possibleNumbers.remove(num);
    }

    /**
     * Add a possibility to the possibleNumbers ArrayList
     *
     * @param num the number to be added
     * @return true if successful
     */
    public boolean addPossibility(Integer num) {
        return possibleNumbers.add(num);
    }

    /**
     * reset all possibility of a cell
     */
    public void resetPossibility() {
        while (!possibleNumbers.isEmpty()) {
            possibleNumbers.remove(0);
        }
        for (Integer i = 1; i <= 9; i++) {
            possibleNumbers.add(i);
        }
    }

    /**
     * Check if a specific number possible to write in the cell
     *
     * @param num number to check
     * @return false if the number don't exist in possibleNumbers
     */
    public boolean doesPossibilityExist(int num) {
        for (Integer possibleNumber : possibleNumbers) {
            if (possibleNumber == num) {
                return true;
            }
        }
        return false;
    }

    /**
     * For cloning the Cell object
     * @return the cloned Cell object
     */
    @Override
    public Cell clone() {
        Cell cloned = new Cell(this.getNum());
        cloned.possibleNumbers = new ArrayList<>(this.possibleNumbers);

        return cloned;
    }

    /**
     * Test the Cell class, nothing to see here
     *
     * @param args n/a
     */
    public static void main(String[] args) {

        Cell a = new Cell(3);
        if (!a.removePossibility(1)) {
            System.out.println("failed to remove.");
        }
        a.possibleNumbers.forEach(System.out::println);
    }

}
