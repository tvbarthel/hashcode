import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

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

    private static void slicePizza(Pizza pizza, int[] shape, SlicesPool slicesPool) {

        int rows = pizza.rows;
        int columns = pizza.columns;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {


                if (slicesPool.cells[i][j] == (byte) 1) {
                    // cell already used
                    continue;
                }

                // get a slice shape.
                int sliceRow = shape[0];
                int sliceCol = shape[1];
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
    }

    private static void cutPizza(String in, String out) {
        Pizza pizza = readFile(in);

        ArrayList<int[]> shapes = new ArrayList<int[]>();
        for (int i = pizza.minIngredient * 2; i <= pizza.maxCells; i++) {
            for (int j = 1; j <= i; j++) {
                if (i % j == 0) {
                    shapes.add(new int[]{j, i / j});
                }
            }

        }

        Collections.sort(shapes, new Comparator<int[]>(){

            public int compare(int[] o1, int[] o2) {
                int a1 = o1[0] * o1[1];
                int a2 = o2[0] * o2[1];
                return a2 - a1;
            }
        });


        SlicesPool bestPool = null;
        int bestScore = -1;
        int size = shapes.size();
        for (int i = 0; i < size; i++) {
            int[] shape = shapes.get(i);

            SlicesPool pool = new SlicesPool();
            pool.initCells(pizza.rows, pizza.columns);
            slicePizza(pizza, shape, pool);

            // fill gap with others shape.
            for (int k = 0; k < size; k++) {
                slicePizza(pizza, shapes.get(k), pool);
            }


            int newScore = pool.computeScore();
            if (newScore > bestScore) {
                bestPool = pool;
                bestScore = newScore;
                System.out.println(out + " new best score : " + newScore + "/" + (pizza.rows * pizza.columns) + " with shape : " + Arrays.toString(shape));
            }
        }

        if (bestPool != null) {
            bestPool.writeFile(out);
            bestPool.drawSlices(out + ".draw");
            System.out.println(out + " : " + bestPool.computeScore());
        }

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

        private void drawSlices(String resPath) {

            try {
                PrintWriter writer = new PrintWriter(resPath);

                int numberOfSlice = slices.size();
                int decimal = 1;
                while (numberOfSlice / 10 > 10) {
                    decimal++;
                    numberOfSlice = numberOfSlice / 10;
                }

                StringBuilder builder = new StringBuilder();
                builder.append("[");
                for (int i = 0; i < decimal; i++) {
                    builder.append("#");
                }
                builder.append("]");

                NumberFormat format = new DecimalFormat(builder.toString());

                String[][] chars = new String[rows][columns];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        chars[i][j] = format.format(0);
                    }
                }

                for (int h = 0; h < slices.size(); h++) {
                    int[] slice = slices.get(h);
                    for (int i = slice[0]; i <= slice[2]; i++) {
                        for (int j = slice[1]; j <= slice[3]; j++) {
                            chars[i][j] = format.format(h + 1);
                        }
                    }
                }

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        writer.print(chars[i][j]);
                    }
                    writer.println("");
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
