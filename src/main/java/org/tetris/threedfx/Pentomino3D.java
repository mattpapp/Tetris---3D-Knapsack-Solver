package org.tetris.threedfx;

public class Pentomino3D {

    public static int[][][][] pentominoList = {
        {
        {{1,0},
         {1,0},
         {1,0},
         {1,1}}},
        {
        {{1,1},
         {1,1},
         {1,0}}},
        {
        {{1,1,1},
         {0,1,0},
         {0,1,0}}}
    };

    int[][][] L, P, T;

    public Pentomino3D(){
        L = pentominoList[0];
        P = pentominoList[1];
        T = pentominoList[2]; 
    };
}
