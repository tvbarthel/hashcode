import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Trial round.
 */
public class Trial {

    private static final String EXAMPLE_IN = "files/trial_2017/example.in";
    private static final String EXAMPLE_OUT = "files/trial_2017/example.out";

    private static final String SMALL_IN = "files/trial_2017/small.in";
    private static final String SMALL_OUT = "files/trial_2017/small.out";

    private static final String MEDIUM_IN = "files/trial_2017/medium.in";
    private static final String MEDIUM_OUT = "files/trial_2017/medium.out";

    private static final String BIG_IN = "files/trial_2017/big.in";
    private static final String BIG_OUT = "files/trial_2017/big.out";


    public static void main(String[] args) {
        System.out.println("Welcome to the trial round!");

        cutPizza(EXAMPLE_IN, EXAMPLE_OUT);
        cutPizza(SMALL_IN, SMALL_OUT);
        cutPizza(MEDIUM_IN, MEDIUM_OUT);
        cutPizza(BIG_IN, BIG_OUT);
    }

    private static void cutPizza(String in, String out) {
        Pizza pizza = readFile(in);
        SlicesPool slicesPool = new SlicesPool();
        slicesPool.initCells(pizza.rows, pizza.columns);

        int rows = pizza.rows;
        int columns = pizza.columns;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {


                if (slicesPool.cells[i][j] == (byte) 1) {
                    // cell already used
                    continue;
                }

                // get a slice shape.
                int sliceCol = 2;
                int sliceRow = pizza.maxCells / sliceCol;
                int countTomato = 0;
                int countMushroom = 0;
                boolean cellsAlreadyUsed = false;

                // valid shape
                int rowBoundary = Math.min(rows, sliceRow + i);
                int colBoundary = Math.min(columns, sliceCol + j);
                innerLoop:
                for (int k = i; k < rowBoundary; k++) {
                    for (int l = j; l < colBoundary; l++) {
                        if (slicesPool.cells[k][l] == (byte) 1) {
                            cellsAlreadyUsed = true;
                            break innerLoop;
                        }
                        if (pizza.cells[k][l] == (byte) 1) {
                            countTomato++;
                        } else {
                            countMushroom++;
                        }
                    }
                }

                if (!cellsAlreadyUsed && countTomato >= pizza.minIngredient && countMushroom >= pizza.minIngredient) {
                    for (int k = i; k < rowBoundary; k++) {
                        for (int l = j; l < colBoundary; l++) {
                            slicesPool.cells[k][l] = (byte) 1;
                        }
                    }
                    slicesPool.slices.add(new int[]{i, j, rowBoundary - 1, colBoundary - 1});
                }
            }
        }

        slicesPool.writeFile(out);

        System.out.println(out + " : " + slicesPool.computeScore());
    }

    private static Pizza readFile(String resPath) {

        BufferedReader br;
        Pizza pizza = new Pizza();
        try {
            br = new BufferedReader(new FileReader(resPath));
            String line;
            line = br.readLine();

            String[] params = line.split(" ");
            int rows = Integer.parseInt(params[0]);
            pizza.rows = rows;
            int columns = Integer.parseInt(params[1]);
            pizza.columns = columns;
            pizza.minIngredient = Integer.parseInt(params[2]);
            pizza.maxCells = Integer.parseInt(params[3]);

            byte[][] cells = new byte[rows][columns];

            for (int i = 0; i < rows; i++) {
                line = br.readLine();
                for (int j = 0; j < columns; j++) {
                    if (line.charAt(j) == 'M') {
                        cells[i][j] = (byte) 1;
                    } else {
                        cells[i][j] = (byte) 2;
                    }
                }
            }
            pizza.cells = cells;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pizza;
    }

    static class SlicesPool {

        ArrayList<int[]> slices;
        byte[][] cells; // 1 used | 2 unused
        private int rows;
        private int columns;

        public SlicesPool() {
            slices = new ArrayList<int[]>();
        }

        public void initCells(int r, int c) {
            rows = r;
            columns = c;
            cells = new byte[r][c];
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    cells[i][j] = (byte) 0;
                }
            }
        }

        public int computeScore() {
            int score = 0;
            for (int[] slice : slices) {
                score += (1 + slice[2] - slice[0]) * (1 + slice[3] - slice[1]);
            }
            return score;
        }

        private void writeFile(String resPath) {

            try {
                PrintWriter writer = new PrintWriter(resPath);
                int numberOfSlices = slices.size();

                writer.println(numberOfSlices);
                for (int i = 0; i < numberOfSlices; i++) {
                    int[] slice = slices.get(i);
                    writer.println(slice[0] + " " + slice[1] + " " + slice[2] + " " + slice[3]);
                }

                writer.close();
            } catch (IOException e) {
                System.out.println("error writing file " + e.getMessage());
            }
        }
    }

    static class Pizza {
        int rows;
        int columns;
        int minIngredient; // min amount of each ingredient per slices.
        int maxCells; // max cell per slices.
        byte[][] cells; // 1 M | 2 T

        @Override
        public String toString() {
            return "Pizza{" +
                    "rows=" + rows +
                    ", columns=" + columns +
                    ", minIngredient=" + minIngredient +
                    ", maxCells=" + maxCells +
                    ", cells=" + Arrays.deepToString(cells) +
                    '}';
        }
    }
}
