package org.tetris.threedfx;

import java.util.ArrayList;

public class MatrixPrinter {
    public static void printMatrix(double[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            System.out.println("Matrix is empty.");
            return;
        }

        int numRows = matrix.length;
        int numCols = matrix[0].length;

        int maxNumWidth = getMaxNumWidth(matrix);

        // Print column numbers
        System.out.print("   "); // Offset for row numbers
        for (int col = 0; col < numCols; col++) {
            String colHeader = String.format("%" + maxNumWidth + "d", col);
            System.out.print(colHeader + " ");
        }
        System.out.println();

        for (int i = 0; i < numRows; i++) {
            System.out.print(String.format("%" + maxNumWidth + "d", i) + ": ");
            for (int j = 0; j < numCols; j++) {
                String numStr = String.format("%" + maxNumWidth + "d", matrix[i][j]);
                System.out.print(numStr + " ");
            }
            System.out.println(); // Move to the next row
        }
    }

    private static int getMaxNumWidth(double[][] matrix) {
        int max = Integer.MIN_VALUE;
        for (double[] row : matrix) {
            for (double num : row) {
                int numWidth = String.valueOf(num).length();
                if (numWidth > max) {
                    max = numWidth;
                }
            }
        }
        return max;
    }
    public static double[][] convertArrayListTo2DArray(ArrayList<double[]> arrayList) {
        if (arrayList == null || arrayList.isEmpty()) {
            return new double[0][0]; // Return an empty 2D array if the ArrayList is empty or null.
        }

        int numRows = arrayList.size();
        int numCols = arrayList.get(0).length;

        double[][] result = new double[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            if (arrayList.get(i).length != numCols) {
                throw new IllegalArgumentException("All subarrays must have the same length.");
            }
            result[i] = arrayList.get(i);
        }

        return result;
    }
    /*public static void main(String[] args) {
        int[][] matrix = {
                {1, 12, 123},
                {1234, 12345, 123456},
                {1234567, 12345678, 123456789}
        };

        printMatrix(matrix);
    }*/
}
