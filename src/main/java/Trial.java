import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * Trial round.
 */
public class Trial {

    private static final String SMALL = "./trial/small.in";
    private static final String MEDIUM = "./trial/medium.in";
    private static final String BIG = "./trial/big.in";

    public static void main(String[] args) {
        System.out.println("Welcome to the trial round!");

        Pizza pizza = readFile(SMALL);

    }

    private static Pizza readFile(String resPath) {

        URL resource = ClassLoader.getSystemClassLoader().getResource(resPath);

        BufferedReader br;
        Pizza pizza = new Pizza();
        try {
            br = new BufferedReader(new FileReader(resource.getFile()));
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
