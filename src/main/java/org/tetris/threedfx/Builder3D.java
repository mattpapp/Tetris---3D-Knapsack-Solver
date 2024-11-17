package org.tetris.threedfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Methods to generate mutations and such of all 3D pieces - packages and pentominoes both should work

public class Builder3D {
    public static int[][][] copy3DArray(int[][][] original) {
        int depth = original.length;
        int rows = original[0].length;
        int cols = original[0][0].length;

        // Create a new 3D array with the same dimensions
        int[][][] copy = new int[depth][rows][cols];

        // Iterate through the original array and copy values
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < cols; k++) {
                    copy[i][j][k] = original[i][j][k];
                }
            }
        }

        return copy;
    }
    public static List<int[][][]> getMutations3D(int[][][] shape) {
        
        List<int[][][]> mutations = new ArrayList<>();

        mutations.add(shape);
        int[][][] xAxRot = shape;
        int[][][] yAxRot, zAxRot;
        for(int i = 0; i < 3; i++){
            xAxRot = rotate90Degrees(xAxRot,'x');
            mutations.add(copy3DArray(xAxRot));
            yAxRot = copy3DArray(xAxRot);
            for(int j = 0; j < 3;j++){
                yAxRot = rotate90Degrees(yAxRot,'y');
                mutations.add(copy3DArray(yAxRot));
                zAxRot = copy3DArray(yAxRot);
                for(int k = 0; k < 3; k++){
                    zAxRot = rotate90Degrees(zAxRot,'z');
                    mutations.add(copy3DArray(zAxRot));
                }
            }
        }
        //mutations.add(rotate90Degrees(shape,'y'));
        //mutations.add(rotate90Degrees(rotate90Degrees(shape,'y'),'y'));
        //mutations.add(rotate90Degrees(rotate90Degrees(rotate90Degrees(shape,'y'),'y'),'y'));
        //mutations.add(rotate(shape,2));
        //mutations.add(rotate3DArray(shape,'y'));
        //mutations.add(rotate3DArray(shape,'z'));
        /*int[][][] arr = shape;
        for (int i = 0; i < 4; i++) {
            // Rotate along the x-axis
            mutations.add(rotate3DArray(arr, 'x'));

            // Rotate along the y-axis
            for (int j = 0; j < 4; j++) {
                mutations.add(rotate3DArray(arr, 'y'));
                arr = rotate3DArray(arr, 'y');
            }

            // Rotate along the z-axis
            mutations.add(rotate3DArray(arr, 'z'));

            // Rotate the original array for the next iteration
            arr = rotate3DArray(arr, 'x');
        }*/
        // 90-degree rotations

        /*mutations.add(rotate3DArray(shape, 'y'));
        mutations.add(rotate3DArray(shape, 'z'));

        // 180-degree rotations
        mutations.add(rotate180Degrees(shape, 'x'));
        mutations.add(rotate180Degrees(shape, 'y'));
        mutations.add(rotate180Degrees(shape, 'z'));*/

        // Flipped versions
        //mutations.add(flip(shape, 'x'));
        //mutations.add(flip(shape, 'y'));
        //mutations.add(flip(shape, 'z'));

        return mutations;
    }

    public static int[][][] rotate90Degrees(int[][][] matrix,char axis) {
        // Check if the matrix is empty
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return null;
        }
        switch (axis){
            case 'z':
            {
                int rows = matrix.length;
                int cols = matrix[0].length;

                // Create a new matrix to store the rotated values
                int[][][] rotatedMatrix = new int[cols][rows][matrix[0][0].length];

                // Iterate through each element in the original matrix and rotate
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        for(int l = 0; l < matrix[0][0].length; l++){
                            rotatedMatrix[j][rows - 1 - i][l] = matrix[i][j][l];
                        }

                    }
                }
                return rotatedMatrix;
            }
            case 'x':{
                int rows = matrix.length;
                int cols = matrix[0].length;
                int depth = matrix[0][0].length;

                // Create a new matrix to store the rotated values
                int[][][] rotatedMatrix = new int[rows][depth][cols];

                // Iterate through each element in the original matrix and rotate
                for (int i = 0; i < cols; i++) {
                    for (int j = 0; j < depth; j++) {
                        for(int l = 0; l < rows; l++){
                            rotatedMatrix[l][j][cols - 1 - i] = matrix[l][i][j];
                        }

                    }
                }
                return rotatedMatrix;
            }
            case 'y':{
                int rows = matrix.length;
                int cols = matrix[0].length;
                int depth = matrix[0][0].length;

                // Create a new matrix to store the rotated values
                int[][][] rotatedMatrix = new int[depth][cols][rows];

                // Iterate through each element in the original matrix and rotate
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < depth; j++) {
                        for(int l = 0; l < cols; l++){
                            rotatedMatrix[j][l][rows - 1 - i] = matrix[i][l][j];
                        }

                    }
                }
                return rotatedMatrix;
            }
        }
        return null;

    }

    private static int[][][] flip(int[][][] shape, char axis) {
        int[][][] flippedShape = new int[shape.length][shape[0].length][shape[0][0].length];

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[0].length; j++) {
                for (int k = 0; k < shape[0][0].length; k++) {
                    switch (axis) {
                        case 'x':
                            flippedShape[i][j][k] = shape[shape.length - i - 1][j][k];
                            break;
                        case 'y':
                            flippedShape[i][j][k] = shape[i][shape[0].length - j - 1][k];
                            break;
                        case 'z':
                            flippedShape[i][j][k] = shape[i][j][shape[0][0].length - k - 1];
                            break;
                    }
                }
            }
        }

        flippedShape = trimEmptyLines(flippedShape);
        return flippedShape;
    }

    private static int[][][] trimEmptyLines(int[][][] shape) {
        int minI = Integer.MAX_VALUE;
        int maxI = Integer.MIN_VALUE;
        int minJ = Integer.MAX_VALUE;
        int maxJ = Integer.MIN_VALUE;
        int minK = Integer.MAX_VALUE;
        int maxK = Integer.MIN_VALUE;
    
        // Find the bounding box of non-empty cells
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[0].length; j++) {
                for (int k = 0; k < shape[0][0].length; k++) {
                    if (shape[i][j][k] != 0) {
                        minI = Math.min(minI, i);
                        maxI = Math.max(maxI, i);
                        minJ = Math.min(minJ, j);
                        maxJ = Math.max(maxJ, j);
                        minK = Math.min(minK, k);
                        maxK = Math.max(maxK, k);
                    }
                }
            }
        }
    
        // Create a new trimmed shape
        int newLength = maxI - minI + 1;
        int newWidth = maxJ - minJ + 1;
        int newHeight = maxK - minK + 1;
    
        int[][][] trimmedShape = new int[newLength][newWidth][newHeight];
    
        // Copy non-empty cells to the trimmed shape
        for (int i = minI; i <= maxI; i++) {
            for (int j = minJ; j <= maxJ; j++) {
                for (int k = minK; k <= maxK; k++) {
                    trimmedShape[i - minI][j - minJ][k - minK] = shape[i][j][k];
                }
            }
        }
    
        return trimmedShape;
    }

}
