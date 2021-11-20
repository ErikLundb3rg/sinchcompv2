package solve;

import solve.YourSolution;

public class Main {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        int[][] example1 = {
                {0,0,0,0,0,0},
                {0,0,0,3,0,0},
                {0,0,3,0,0,0},
                {2,0,0,0,3,0},
                {0,0,0,3,0,0},
                {0,0,0,0,0,0},
        };
        int width = example1[0].length;
        int height = example1.length;

        YourSolution ys = new YourSolution();
        ys.setup(width, height);
        ys.solve(example1);

    }

}
