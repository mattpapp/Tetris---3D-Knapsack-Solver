package org.tetris.threedfx;

import java.util.ArrayList;
import java.util.List;

public class PackageBuilder {

    Package packageA; // 2x2x4
    Package packageB; // 2x3x4
    Package packageC; // 3x3x3
    Package pentominoL, pentominoP, pentominoT;
    Package[] packageList;
    List<List<Package>> allCombinations;
    int targetVolume = 1320; // volume of the truck

    ArrayList<Package> usedPackages; //list of packages that are actually used and need a list of mutations generated

    public PackageBuilder() {

        packageA = new Package(2, 2, 4, 'A', 1);
        packageB = new Package(2, 3, 4, 'B', 2);
        packageC = new Package(3, 3, 3, 'C', 3);


        pentominoL = new Package(1,4,2,'L',1);
        pentominoL.setPackage3D(Pentomino3D.pentominoList[0]);
        pentominoP = new Package(1,3,2,'P',2);
        pentominoP.setPackage3D(Pentomino3D.pentominoList[1]);
        pentominoT = new Package(1,3,3,'T',3);
        pentominoT.setPackage3D(Pentomino3D.pentominoList[2]);
        fillPackages();
        List<Package> packages = new ArrayList<>();
        packages.add(packageA);
        packages.add(packageB);
        packages.add(packageC);
        packages.add(pentominoL);
        packages.add(pentominoP);
        packages.add(pentominoT);
        //allCombinations = CombinationFinderPackage.findCombinations(packages, targetVolume);

    }

    // Fill in package 3D arrays with the color ID
    public void fillPackages(){

        if(packageA == null || packageB == null || packageC == null){
            System.out.println("Packages null, skipping");
        }
        else{

            packageList = new Package[]{packageA, packageB, packageC};

            for(Package pack : packageList){

                int[][][] tempShape = new int[pack.length][pack.width][pack.height];

                for (int x = 0; x < pack.length; x++) {
                    for (int y = 0; y < pack.width; y++) {
                        for (int z = 0; z < pack.height; z++) {
                        tempShape[x][y][z] = pack.colorID; 
                        }
                    }
                }

                pack.setPackage3D(tempShape);

            }
            packageList = new Package[]{packageA, packageB, packageC,pentominoL,pentominoP,pentominoT};
        }

    }

}
