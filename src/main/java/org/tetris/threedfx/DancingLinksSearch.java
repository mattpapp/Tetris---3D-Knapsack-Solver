package org.tetris.threedfx;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class converts the field of pentominoes into a cover problem format as preparation the dancing links algorithm, and starts the algorithm.
 */

public class DancingLinksSearch {

    private int length, width, height, columns;
    public static int maxVal = 0;
    private int[][][] field;
    //private Pentomino[] pentominos;
    private Builder3D builder3D;
    private static PackageBuilder packageBuilder = new PackageBuilder();
    private ArrayList<double[]> matrix;
    private char[] inputs;
    public static boolean dancingLinksRunning = false;
    //public static UI ui;
    public static long startingTime, total;
    public static int pieceA = 0, pieceB = 0, pieceC = 0;
    public static int counter;
    public DancingLinksSearch(char[] inputs, int length, int width, int height){

        pieceA = 0;
        pieceB = 0;
        pieceC = 0;
        maxVal = 0;
        //ui = new UI(width, height, 50);
        field = new int[length][width][height];

        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                for (int k = 0; k < field[0][0].length; k++) {
                    field[i][j][k] = -1;
                }
            }
        }

        this.inputs = inputs;
        this.length = length;
        this.height = height;
        this.width = width;
        matrix = new ArrayList<>();
        int cols = length * width * height;
        this.columns = cols;
        //pentominos = Pentomino.charsToPentominoArray(inputs);
        
        for(int i = 0; i < inputs.length; i++){
            //System.out.println("Outer loop ran");
            for(int[][][] mutation : builder3D.getMutations3D(packageBuilder.packageList[Package.packageCharToID(inputs[i])].package3D)){
                //System.out.println("Mutation print");
                //System.out.println(Package.packageCharToID(inputs[i]));
                //System.out.println(packageBuilder.packageList[Package.packageCharToID(inputs[i])].package3D);
                convertPackageToMatrix(mutation,packageBuilder.packageList[Package.packageCharToID(inputs[i])].colorID);
                //System.out.println("after calling function");
            }
        }
        //MatrixPrinter.printMatrix(MatrixPrinter.convertArrayListTo2DArray(matrix));
        double[][] newMatrux = MatrixPrinter.convertArrayListTo2DArray(matrix);
        //System.out.println("Created newMatrux");
        startingTime = System.currentTimeMillis();
        new DancingLinks(newMatrux,this).runSolver();
        DancingLinksSearch.dancingLinksRunning = false;
        Platform.runLater(() -> Interface.textDisplay.setText("Solver stopped."));
    }

    public void displaySolution(List<DancingLinks.DancingNode> nodes){
        double[][][] field = new double[Main.length][Main.width][Main.height];
        //System.out.println("Elapsed time: " + (System.currentTimeMillis() - startingTime));
        total+=(System.currentTimeMillis() - startingTime);
        counter++;
        //System.out.println("Average: " + (double)total/(double)counter);
        int pentominoId = -1;
        int value = 0;
        for (DancingLinks.DancingNode n : nodes){
            List<int[]> squaresToFill = new ArrayList<>();
            DancingLinks.DancingNode rcNode = n;

            int val = Integer.parseInt(rcNode.C.name);
            int[] reversed = reverseMatrixColumn(val);
            field[reversed[0]][reversed[1]][reversed[2]] = rcNode.colorId;
            //System.out.println(n.C.name + " " + n.colorId);
            if ((int)n.colorId/1 == 1) {
                value+= 3;
            }
            else if ((int)n.colorId/1 == 2) {
                value+= 4;
            }
            else if ((int)n.colorId/1 == 3){
                value += 5;
            }
            for(DancingLinks.DancingNode tmp = n.R; tmp != n; tmp = tmp.R){
                val = Integer.parseInt(tmp.C.name);
                reversed = reverseMatrixColumn(val);
                field[reversed[0]][reversed[1]][reversed[2]] = rcNode.colorId;
                //System.out.print(" " + val + " " + n.colorId);

                //System.out.println(Integer.parseInt(tmp.C.name));
            }
            //System.out.println("Filling pentomino id: " + pentominoId);
            //System.out.println(pentominos[pentominoId].getChar());
            /*for (int[] sq : squaresToFill){

                field[sq[0]][sq[1]] = Search.characterToID(pentominos[pentominoId].getChar());
            }*/

        }

        System.out.println();
        final double[][][] newfield = field;
        Platform.runLater(() -> Interface.drawUI(newfield));
        int suspend = 1000;
        if(DancingLinks.knapsackSolver){suspend = 50;}
        try {
			Thread.sleep(suspend);
		}catch (Exception e){}
        if (value > maxVal){
            maxVal = value;
        }
        System.out.println("Solution found in: " + (System.currentTimeMillis()-startingTime - 1000));
        System.out.println("Solution value " + value);
        System.out.println("Max value " + maxVal);
        startingTime = System.currentTimeMillis();
        int value1 = value;
        if(DancingLinks.knapsackSolver) Platform.runLater(() -> Interface.textDisplay.setText("Solution #" + DancingLinks.solutions + ", weight: " + value1 + ", max weight: " + maxVal));
    }

    public int getMatrixColumn(int x, int y, int z){
        return y + x * width + z * width * length;
    }

    public int[] reverseMatrixColumn(int column) {
        int z = column / (width * length);
        int remainder = column % (width * length);
        int x = remainder / width;
        int y = remainder % width;

        return new int[]{x, y, z};
    }
    /*public int[] getGridCoords(int idx){
        int subbed = idx - pentominos.length;
        int x = subbed % width;
        int y = subbed / width;
        return new int[]{x,y};
    }*/


    //converts given Package object to the matrix used for the cover problem
    private void convertPackageToMatrix(int[][][] piece, int colorId) {

        //System.out.println(piece.length + " " + piece[0].length + " " + piece[0][0].length);
        //System.out.println(length);
        //System.out.println(width);
        //System.out.println("Limits: " + (length - piece.length + 1) + " " + (width - piece[0].length + 1) + " " + (height - piece[0][0].length + 1));

        int pieceCount = 0;
        switch (colorId) {
            case 1:
                DancingLinksSearch.pieceA++;
                pieceCount = pieceA;
                System.out.println("A: " + DancingLinksSearch.pieceA);
                break;
            case 2:
                DancingLinksSearch.pieceB++;
                pieceCount = pieceB;
                System.out.println("B: " + DancingLinksSearch.pieceB);
                break;
            case 3:
                DancingLinksSearch.pieceC++;
                pieceCount = pieceC;
                System.out.println("C: " + DancingLinksSearch.pieceC);
                break;
        }

        ArrayList<int[]> placedCoordinates;
        for (int x = -10; x < field.length + 10; x++) {
            for (int y = -10; y < field[0].length + 10; y++) {
                for (int z = -10; z < field[0][0].length + 10; z++) {
                    //System.out.println("Inner loop working");
                    if ((placedCoordinates = checkIfPlaceable3D(piece, x, y, z, field)) != null) {
                        double[] row = new double[columns];
                        Arrays.fill(row, 0);

                        // Set the corresponding column for the package
                        //System.out.println("Cube postionion: "+x +" "+ y +" "+ z);
                        for (int[] coordinate : placedCoordinates){
                            //System.out.println("Transforming cooridate x: " + coordinate[0] + " y: " + coordinate[1] + " z: " + coordinate[2]);
                            row[getMatrixColumn(coordinate[0],coordinate[1],coordinate[2])] = (double)colorId + 0.00001 * pieceCount;
                            
                        }

                        
                        //row[width * height * z + width * y + x] = 1; //TODO possible issue

                        // Add the row to the matrix
                        pieceCount++;
                        matrix.add(row);
                        //System.out.println("Added new row to matrix");
                    }
                }
            }
        }

        switch (colorId) {
            case 1:
                DancingLinksSearch.pieceA = pieceCount;
                break;
            case 2:
                DancingLinksSearch.pieceB = pieceCount;
                break;
            case 3:
                DancingLinksSearch.pieceC = pieceCount;
                System.out.println("Piece count: " + pieceCount);
                break;
        }

        //System.out.println("Saved Color ID: " + ((double)colorId + 0.024 * pieceCount));

        //System.out.println("Finished convertPackageToMatrix");
    }

    // checkIfPlaceable from "Search.java" modified for the 3D field
    public static ArrayList<int[]> checkIfPlaceable3D(int[][][] piece, int x, int y, int z, int[][][] field) {
        //System.out.println("Placeable check started");
        ArrayList<int[]> placedCoordinates = new ArrayList<>();
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                for (int k = 0; k < piece[i][j].length; k++) {
                    //System.out.println(piece[i][j][k]);
                    if (piece[i][j][k] != 0) {
                        int newX = x + i;
                        int newY = y + j;
                        int newZ = z + k;

                        //System.out.println(newX + " " + newY + " " + newZ);
                        placedCoordinates.add(new int[]{newX,newY,newZ});
                        if (!isValidPosition(newX, newY, newZ, field)) {
                            // Piece is over the edge
                            //System.out.println("Piece is not placeable");
                            return null;
                        }
                    }
                }

            }

        }
    
        return placedCoordinates;
    }
    
    private static boolean isValidPosition(int x, int y, int z, int[][][] field) {
        return x >= 0 && x < field.length && y >= 0 && y < field[0].length && z >= 0 && z < field[0][0].length;
    }
    

    public static void main(String[] args){
        
        //Used for bigger, more difficult grid tests

        /*UI ui = new UI(12, 5, 60);
        
        char[] inputs = new char[] {'T','W','L','I','Y','Z','N','X','P','F','V','U'};
        char[] inputsNew = new char[inputs.length*25];
        for(int i = 0; i< 25;i++){
            for (int j = 0; j<12;j++){
                inputsNew[i*12+j] = inputs[j];
            }
        }
        new DancingLinksSearch(inputs,33,5, 8);
        System.out.println(counter);*/


        //for(List<Package> combination : packageBuilder.allCombinations){
           /* char[] combinationForDancingLinks = new char[combination.size()];
            int indx = 0;
            for(Package pack : combination){
                combinationForDancingLinks[indx] = pack.packageChar;
                System.out.print(combinationForDancingLinks[indx]);
                indx++;
            }*/
            //2x2x4
            //2x3x4
            //3x3x3
            //System.out.println("Combination:" + combinationForDancingLinks.toString());
            char[] allowedPieces = {'C'};
            new DancingLinksSearch(allowedPieces, 3, 3, 3);

        //}
        
    }
}
