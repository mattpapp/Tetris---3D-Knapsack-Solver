package org.tetris.threedfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class Main extends Application{

    
    public static int length = 33; // Truck length in meters/0.5
    public static int width = 5; // Truck width
    public static int height = 8; // Truck height
    static DancingLinksSearch dancingLinks;

    public static int counter = 0;
    static int[][][] field;
    static char[] allowedPieces;

    public static Thread myThread;
    public static boolean runAlgorithms;

    private final float WINDOW_HEIGHT = 500, WINDOW_WIDTH = 800;


    public static void assignButtonActions(){

        Interface.setSelection.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Selected Option: " + newValue);
            String selectedOption = (String) newValue;
            // Add separate handlers for each option
            switch (selectedOption) {
                case "Packages: A, B, C":
                    System.out.println("Packages selected");
                    Main.allowedPieces = new char[]{'A','B','C'};
                    Interface.textDisplay.setText("Selected Packages");
                    break;
                case "Pentominoes: L, P, T":
                    System.out.println("Pentomino selected");
                    Main.allowedPieces = new char[]{'P','L','T'};
                    Interface.textDisplay.setText("Selected Pentominoes");
                    break;
                default:
                    System.out.println("S");
                    break;
            }
            Interface.setSelection.getButtonCell().setText(selectedOption);
        });
        
        Interface.buttonTestField.setOnAction(e -> {
            System.out.println("Pressed to generate field");;
            //Interface.drawUI(Main.generateTestField());
        });

        Interface.buttonCoverSolver.setOnAction(e -> {
            if(allowedPieces != null && myThread == null && DancingLinksSearch.dancingLinksRunning == false){
                myThread = new Thread(() -> {
                    Platform.runLater(() -> Interface.textDisplay.setText("Solver Started, Preparing..."));
                    DancingLinks.stopDancingLinks = false;
                    DancingLinksSearch.dancingLinksRunning = true;
                    Main.dancingLinks = new DancingLinksSearch(allowedPieces, length, width, height);
                });
    
                myThread.start();
            }
            else if(myThread != null || DancingLinksSearch.dancingLinksRunning == true){
                Interface.textDisplay.setText("Solver instance already running, stop first");
            }else{
                Interface.textDisplay.setText("Please select used pieces");
            }


            //insert what happens when cover solver is called
        });
        
        Interface.buttonKnapsackSolver.setOnAction(e -> {
            if(allowedPieces != null && myThread == null && DancingLinksSearch.dancingLinksRunning == false){
                myThread = new Thread(() -> {
                    Platform.runLater(() -> Interface.textDisplay.setText("Solver Started, Preparing..."));
                    if(Arrays.equals(new char[]{'A','B','C'}, allowedPieces)){System.out.println("Using packages, resizing"); width = 4; Interface.width = 4;}
                    DancingLinks.stopDancingLinks = false;
                    DancingLinksSearch.dancingLinksRunning = true;
                    DancingLinks.knapsackSolver = true;
                    Main.dancingLinks = new DancingLinksSearch(allowedPieces, length, width, height);
                });
    
                myThread.start();
            }
            else if(myThread != null || DancingLinksSearch.dancingLinksRunning == true){
                Interface.textDisplay.setText("Solver instance already running, stop first");
            }else{
                Interface.textDisplay.setText("Please select used pieces");
            }

        });

        Interface.buttonStop.setOnAction(e -> {
            if(myThread != null) Platform.runLater(() -> Interface.textDisplay.setText("Solver stopping..."));
            else Platform.runLater(() -> Interface.textDisplay.setText("Solver not running."));
            DancingLinks.stopDancingLinks = true;
            myThread = null;
            DancingLinks.knapsackSolver = false;
            Interface.width = 5;
            width = 5;
        });

    }

    @Override
    public void start(Stage truckWindow){
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        Interface.constructWindow3D((float)screenSize.getWidth(), (float)screenSize.getHeight(), length, height, width, truckWindow);
        System.out.println("Created ui, program still running (Good sign)");
    }

    public static void main(String[] args) {
        launch();
    }

    public static int[][][] generateTestField(){
        List<int[][][]> mutations = Builder3D.getMutations3D(new PackageBuilder().packageList[Package.packageCharToID('P')].package3D);

        int[][][] testField = new int[length][width][height];
        //System.out.println(Arrays.deepToString(new PackageBuilder().packageList[Package.packageCharToID('B')].package3D));
        drawPackage(mutations.get(counter),testField);
        // Iterate through the 3D array and set values between -1 and 2
        /*for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < height; k++) {
                    int randomValue = (int) (Math.random() * 4) - 1;
                    testField[i][j][k] = randomValue;
                }
            }
        }*/


        System.out.println("Created test field");
        Main.field = testField;
        counter++;
        return testField;
    }

    public static void drawPackage(int[][][] shape,int[][][] field){
        clear(field,-1);
        for(int i = 0; i < shape.length; i++){
            for(int j = 0; j < shape[0].length; j++){
                for(int k = 0; k < shape[0][0].length; k++){
                    if (shape[i][j][k] == 1){
                        field[i][j][k] = 1;
                    }
                }
            }
        }
    }
    
    public static void clear(int[][][] array3D, int fillValue) {
        for (int i = 0; i < array3D.length; i++) {
            for (int j = 0; j < array3D[i].length; j++) {
                for (int k = 0; k < array3D[i][j].length; k++) {
                    array3D[i][j][k] = fillValue;
                }
            }
        }
    }


}
