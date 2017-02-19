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

        Collections.sort(shapes, new Comparator<int[]>() {

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

            SlicesPool pool = new SlicesPool(pizza);
            pool.initCells(pizza.rows, pizza.columns);
            slicePizza(pizza, shape, pool);

            // fill gap with others shape.
            for (int k = 0; k < size; k++) {
                slicePizza(pizza, shapes.get(k), pool);
            }

            pool.slices = splitSlices(pool);
            pool.slices = expandSlices(pool);

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

    private static List<int[]> expandSlices(SlicesPool pool) {
        List<int[]> slices = pool.slices;
        ArrayList<int[]> expandedSlices = new ArrayList<int[]>();
        for (int[] slice : slices) {
            expandedSlices.add(expandSlice(pool, slice));
        }
        return expandedSlices;
    }

    private static int[] expandSlice(SlicesPool pool, int[] slice) {
        boolean didExpand = false;
        int rowTop = slice[0] - 1;
        if (canExpandToRow(pool, rowTop, slice)) {
            didExpand = true;
            markRowAsNotAvailable(pool, rowTop, slice[1], slice[3]);
            slice = new int[]{rowTop, slice[1], slice[2], slice[3]};
        }

        int rowBottom = slice[2] + 1;
        if (canExpandToRow(pool, rowBottom, slice)) {
            didExpand = true;
            markRowAsNotAvailable(pool, rowBottom, slice[1], slice[3]);
            slice = new int[]{slice[0], slice[1], rowBottom, slice[3]};
        }

        int colLeft = slice[1] - 1;
        if (canExpandToCol(pool, colLeft, slice)) {
            didExpand = true;
            markColAsNotAvailable(pool, colLeft, slice[0], slice[2]);
            slice = new int[]{slice[0], colLeft, slice[2], slice[3]};
        }

        int colRight = slice[3] + 1;
        if (canExpandToCol(pool, colRight, slice)) {
            didExpand = true;
            markColAsNotAvailable(pool, colRight, slice[0], slice[2]);
            slice = new int[]{slice[0], slice[1], slice[2], colRight};
        }

        if (!didExpand) {
            return slice;
        }

        return expandSlice(pool, slice);
    }


    private static void markColAsNotAvailable(SlicesPool pool, int col, int startRow, int stopRow) {
        for (int row = startRow; row <= stopRow; row++) {
            pool.cells[row][col] = (byte) 1;
        }
    }

    private static boolean canExpandToCol(SlicesPool pool, int col, int[] slice) {
        if (col >= pool.columns || col < 0) {
            return false;
        }

        int startRow = slice[0];
        int stopRow = slice[2];
        int sliceHeight = 1 + stopRow - startRow;
        int expandedSize = getSliceSize(slice) + sliceHeight;
        if (pool.pizza.maxCells < expandedSize) {
            return false;
        }

        for (int row = startRow; row <= stopRow; row++) {
            if (pool.cells[row][col] == (byte) 1) {
                return false;
            }
        }

        return true;
    }


    private static void markRowAsNotAvailable(SlicesPool pool, int row, int startCol, int stopCol) {
        for (int col = startCol; col <= stopCol; col++) {
            pool.cells[row][col] = (byte) 1;
        }
    }

    private static boolean canExpandToRow(SlicesPool pool, int row, int[] slice) {
        if (row >= pool.rows || row < 0) {
            return false;
        }

        int startCol = slice[1];
        int stopCol = slice[3];
        int sliceWidth = 1 + stopCol - startCol;
        int expandedSize = getSliceSize(slice) + sliceWidth;
        if (pool.pizza.maxCells < expandedSize) {
            return false;
        }

        for (int col = startCol; col <= stopCol; col++) {
            if (pool.cells[row][col] == (byte) 1) {
                return false;
            }
        }

        return true;
    }

    private static List<int[]> splitSlices(SlicesPool pool) {
        List<int[]> oldSlices = pool.slices;
        List<int[]> newSlices = new ArrayList<int[]>();
        Pizza pizza = pool.pizza;

        for (int[] slice : oldSlices) {
            List<int[]> splitSlices = splitSlice(pizza, slice);
            newSlices.addAll(splitSlices);
        }

        return newSlices;
    }

    private static List<int[]> splitSlice(Pizza pizza, int[] slice) {
        int sliceSize = getSliceSize(slice);
        boolean canBeSplit = sliceSize >= pizza.minIngredient * 4;
        if (!canBeSplit) {
            ArrayList<int[]> splitSlices = new ArrayList<int[]>(1);
            splitSlices.add(slice);
            return splitSlices;
        }

        int sliceHeight = 1 + slice[2] - slice[0];
        int sliceWidth = 1 + slice[3] - slice[1];
        if (sliceWidth >= sliceHeight) {
            return splitSliceVertically(pizza, slice);
        } else {
            return splitSlicesHorizontally(pizza, slice);
        }
    }

    private static List<int[]> splitSlicesHorizontally(Pizza pizza, int[] slice) {
        ArrayList<int[]> splitSlices = new ArrayList<int[]>();

        for (int row = slice[0]; row < slice[2]; row++) {
            int[] splitSlice1 = new int[]{slice[0], slice[1], row, slice[3]};
            int[] splitSlice2 = new int[]{row + 1, slice[1], slice[2], slice[3]};
            if (areValidSlices(pizza, splitSlice1, splitSlice2)) {
                splitSlices.addAll(splitSlice(pizza, splitSlice1));
                splitSlices.addAll(splitSlice(pizza, splitSlice2));
                return splitSlices;
            }
        }

        splitSlices.add(slice);
        return splitSlices;
    }

    private static ArrayList<int[]> splitSliceVertically(Pizza pizza, int[] slice) {
        ArrayList<int[]> splitSlices = new ArrayList<int[]>();

        for (int col = slice[1]; col < slice[3]; col++) {
            int[] splitSlice1 = new int[]{slice[0], slice[1], slice[2], col};
            int[] splitSlice2 = new int[]{slice[0], col + 1, slice[2], slice[3]};
            if (areValidSlices(pizza, splitSlice1, splitSlice2)) {
                splitSlices.addAll(splitSlice(pizza, splitSlice1));
                splitSlices.addAll(splitSlice(pizza, splitSlice2));
                return splitSlices;
            }
        }

        splitSlices.add(slice);
        return splitSlices;
    }

    private static boolean areValidSlices(Pizza pizza, int[] splitSlice1, int[] splitSlice2) {
        return isSliceValid(pizza, splitSlice1) && isSliceValid(pizza, splitSlice2);
    }

    private static boolean isSliceValid(Pizza pizza, int[] slice) {
        int countTomato = 0;
        int countMushroom = 0;

        for (int k = slice[0]; k <= slice[2]; k++) {
            for (int l = slice[1]; l <= slice[3]; l++) {
                if (pizza.cells[k][l] == (byte) 1) {
                    countTomato++;
                } else {
                    countMushroom++;
                }
            }
        }

        return countTomato >= pizza.minIngredient && countMushroom >= pizza.minIngredient;
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
            int numberOfTomato = 0;
            int numberOfMushroom = 0;

            for (int i = 0; i < rows; i++) {
                line = br.readLine();
                for (int j = 0; j < columns; j++) {
                    if (line.charAt(j) == 'M') {
                        cells[i][j] = (byte) 1;
                        numberOfMushroom++;
                    } else {
                        cells[i][j] = (byte) 2;
                        numberOfTomato++;
                    }
                }
            }
            pizza.cells = cells;

            System.out.println("Pizza with " + numberOfMushroom + " mushrooms and " + numberOfTomato + " tomatoes");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pizza;
    }

    static class SlicesPool {

        List<int[]> slices;
        byte[][] cells; // 1 used | 2 unused
        private int rows;
        private int columns;
        private final Pizza pizza;

        public SlicesPool(Pizza pizza) {
            this.pizza = pizza;
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
                score += getSliceSize(slice);
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

    private static int getSliceSize(int[] slice) {
        return (1 + slice[2] - slice[0]) * (1 + slice[3] - slice[1]);
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
