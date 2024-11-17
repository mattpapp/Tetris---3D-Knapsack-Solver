package org.tetris.threedfx;

public class Package {

    public int length, width, height, volume;
    public int colorID;
    char packageChar;

    public int [][][] package3D;

    public Package(int length, int width, int height, char packageChar, int colorID){
        this.length = length;
        this.height = height;
        this.width = width;
        this.colorID = colorID;
        this.volume = length*height*width;
        this.packageChar = packageChar;

        package3D = new int[length][width][height];
    }

    public void setPackage3D(int[][][] shape){

        this.package3D = shape;

    }

    public static int packageCharToID(char packageCharRep){
        switch (packageCharRep){
            case 'A': return 0;
            case 'B': return 1;
            case 'C': return 2;
            case 'L': return 3;
            case 'P': return 4;
            case 'T': return 5;
        }
        System.out.println("Received incorrect package char representation");
        return -1;
    }

}
