package solve;

import java.util.*;


class DirCostPair {
    public int dir;
    public int cost;

    public DirCostPair(int dir, int cost) {
        this.dir = dir;
        this.cost = cost;
    }
}
class Constants {
    public static int width;
    public static int height;
    public static int INF = Integer.MAX_VALUE;
    public int[][] grid;
    public static Map<Integer, List<Integer>> occupyingPos = new HashMap();
    public static Map<Integer, Node> idToNode = new HashMap();


    private static int fromxyToNum(int x, int y) {
        return x + (y*width);
    }
    public static void update(int x, int y, int id) {
        int position = fromxyToNum(x, y);
        if (occupyingPos.containsKey(position)) {
            List<Integer> idsAtPos = occupyingPos.get(position);
            idsAtPos.add(id);
            return;
        }
        List<Integer> newList = new ArrayList();
        newList.add(id);
        occupyingPos.put(position, newList);
    }

    public static boolean hasKey(int x, int y) {
        int position = fromxyToNum(x, y);
        return occupyingPos.containsKey(position);
    }

    public static List<Integer> getOccupyers(int x, int y) {
        int position = fromxyToNum(x, y);
        return occupyingPos.get(position);
    }
}



class Node {
    public int id;
    public int posX;
    public int posY;
    public int height;
    public int[][] grid;
    // 0 means EAST, 1 means NORTH, 2 means WEST, and 3 means SOUTH
    public boolean[] canDirection = new boolean[4];
    public boolean[] collisions = new boolean[4];
    public boolean canFall = true;
    interface LambdaFunc {
        public void run(int x, int y, int dir);
    }

    public Node(int id, int posX, int posY, int height, int[][] grid) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.height = height;
        this.grid = grid;
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < Constants.width && y >= 0 && y < Constants.height;
    }

    public void doForDirection(int dir, LambdaFunc lf) {
        if (dir == 0) {
            for (int x = posX+1; x < posX + height; x++) {
                lf.run(x, posY, dir);
            }
        }
        // checkNorth 1
        if (dir == 1) {
            for (int y = 1+posY-height; y < posY; y++) {
                lf.run(posX, y, dir);
            }
        }
        // checkWest 2
        if (dir == 2) {
            for (int x = 1+posX-height; x < posX; x++) {
                lf.run(x, posY, dir);
            }
        }
        // checkSouth 3
        if (dir == 3) {
            for (int y = posY+1; y < posY+height; y++) {
                lf.run(posX, y, dir);
            }
        }
    }

    public void checkPossibleFellings() {
        // init canDirection arr
        for (int i = 0; i < 4; i++) {
            canDirection[i] = true;
        }

        for (int i = 0; i < 4; i++) {
            doForDirection(i, (x, y, dir) -> {
                if (!(isInside(x, y) && grid[x][y] == 0)) {
                    canDirection[dir] = false;
                }
            });
        }
    }

    public void markFellingsInMap() {
        for (int i = 0; i < 4; i++) {
            if (canDirection[i]) {
                doForDirection(i, (x, y, dir) -> {
                    Constants.update(x, y, id);
                });
            }
        };
    }

    public void markCollisions() {
        for (int i = 0; i < 4; i++) {
            if (canDirection[i]) {
                doForDirection(i, (x, y, dir) -> {
                    List<Integer> others = Constants.getOccupyers(x, y);
                    // others are marking this spot as a possible felling
                    if (others.size() > 1) {
                        collisions[dir] = true;
                    }
                });
            }
        }
    }

    public int checkFreeFellingandFall() {
        for(int i = 0; i < collisions.length; i++) {
            if (canDirection[i] && !collisions[i]) {
                return i;
            }
        }
        return -1;
    }

    public List<DirCostPair> minBlockList() {
        List<DirCostPair> xs = new ArrayList();

        for (int i = 0; i < 4; i++) {
            if (collisions[i]) {
                Set<Integer> onBranch = new HashSet();

                doForDirection(i, (x, y, dir) -> {
                    List<Integer> others = Constants.getOccupyers(x, y);

                    for (Integer o : others) {
                        // don't add myself
                        if (o != id) {
                            onBranch.add(o);
                        }
                    }
                });
                int sm = 0;
                for (Integer o : onBranch) {
                    sm += Constants.idToNode.get(o).height;
                }
                xs.add(new DirCostPair(i, sm));
            }
        }

        // sort by min
        xs.sort((x1, x2) -> x1.cost -x2.cost);
        return xs;
    }



    public int fellWithMinBlock() {
        List<DirCostPair> xs = minBlockList();

        for (DirCostPair dcp : xs) {
            doForDirection(dcp.dir, (x, y, dir) -> {
                if (grid[x][y] == -1) {
                    canFall = false;
                }
            });

            if (canFall) {
                doForDirection(dcp.dir, (x, y, dir) -> {
                    grid[x][y] = -1;
                });
                return dcp.dir;
            }
            canFall = true;
        }
        // can't fall anywhere
        return -1;
    }
}



public class YourSolution implements Solver {

    // width and height of yard is passed,
    // where width is west-east and height is north-south

    public void setup(int width, int height) {
        Constants.width = width;
        Constants.height = height;
    }

    public int[][] transposeMatrix(int[][] matrix) {
        int width = matrix[0].length;
        int height = matrix.length;
        int[][] transposed = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                transposed[i][j] = matrix[j][i];
            }
        }
        return transposed;
    }

    public int[][] solve(int[][] matrix) {
        List<List<Integer>> res = new ArrayList<List<Integer>>();

        matrix = transposeMatrix(matrix);
        List<Node> nodes = new ArrayList();

        int currId = 0;

        for (int x = 0; x < Constants.width; x++) {
            for (int y = 0; y < Constants.height; y++) {
                if (matrix[x][y] > 1) {
                    Node newNode = new Node(currId, x, y, matrix[x][y], matrix);
                    nodes.add(newNode);
                    Constants.idToNode.put(currId, newNode);
                    currId++;
                }
            }
        }

        for (Node n : nodes) {
            n.checkPossibleFellings();
            n.markFellingsInMap();
        }

        for (Node n : nodes) {
            n.markCollisions();
        }

        List<Node> collided = new ArrayList();

        // this opt does not make a big diff
        for (Node n : nodes) {
            int dir = n.checkFreeFellingandFall();
            if (dir != -1) {
                res.add(List.of(n.posY, n.posX, dir));
            } else {
                collided.add(n);
            }
        }


        collided.sort((n1, n2) -> n2.height -n1.height);
        for (Node n : collided) {
            int dir = n.fellWithMinBlock();
            if (dir != -1) {
                res.add(List.of(n.posY, n.posX, dir));
            }
        }

        /*for (List<Integer> ans : res) {
            String s = "";
            for (Integer x : ans) {
                s += String.valueOf(x) + " ";
            }
            System.out.println(s);
        }*/

        return resFromList(res);
    }

    public int[][] resFromList(List<List<Integer>> res) {
        int[][] resIntArr = new int[res.size()][3];
        int i = 0;
        for (List<Integer> arr: res) {
            int j = 0;
            for (Integer x : arr) {
                resIntArr[i][j] = x;
                j+=1;
            }
            i+=1;
        }
        return resIntArr;
    }
}