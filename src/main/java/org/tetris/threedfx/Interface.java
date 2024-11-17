package org.tetris.threedfx;

import java.math.BigDecimal;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Constructs the 3D view, contains methods for updating the 3D view of the truck
 */
public class Interface{

    // Set window dimensions
    private static float WINDOW_WIDTH;
    private static float WINDOW_HEIGHT;

    // Buttons. Public because listeners are added in Main.java
    public static Button buttonTestField;
    public static Button buttonCoverSolver;
    public static Button buttonKnapsackSolver;
    public static Button buttonStop;
    public static ComboBox setSelection;
    public static Label textDisplay;
    private static Label textDisplayInfo;

    private static Scene scene;
    private static Stage controlWindow;
    private static HBox solverControls;
    private static Timeline displayRotation;

    // Set used field dimensions, truck dimensions depend on these
    public static int length = 33, width = 5, height = 8;

    // Truck building blocks
    private static int buildingBlockSize = 60; // Set the dimensions of the blocks, truck outline scales with this variable

    // Variables for camera and mouse controls
    private static PerspectiveCamera camera;
    private static final DoubleProperty zoom = new SimpleDoubleProperty(1.0);
    private static double anchorX, anchorY;
    private static double anchorAngleX = 0;
    private static double anchorAngleY = 0;
    private static final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private static final DoubleProperty angleY = new SimpleDoubleProperty(0);
    private static final DoubleProperty angleZ = new SimpleDoubleProperty(0);
    private static int cameraXoffset = 0;
    private static int cameraYoffset = -450;
    private static int cameraZoffset = 0; 
    private static int cameraZoomOut = -4000;

    // Different scene groups
    public static Group displayGroup; 
    private static Group truckGroup; // Truck is colored here
    private static Group axisGroup;
    private static Box truckOutline;

    // Variables to play around with for visuals. If you see lime green boxes, something went wrong (color ids in field are incorrect)
    private static double blockOpacity = 1.0; // 1.0 - 0.0 --> opaque - transparent
    private static int axisWidth = 6;
    private static int colorCount = 4; // Adjust as you add new colors
    private static String backgroundColor = "#22223b"; // Royal blue
    private static String controlBackgroundColor = "#10101b";

    public static String buttonColor = "#22223b";
    private static String buttonBorderColor = "#f7e1d7";
    private static String buttonBorderWidth = "2px";
    private static String buttonBorderRadius = "6px";
    private static String buttonPadding = "0px";

    private static String buttonColorPressed = "#f7af9f";
    private static String buttonBorderColorPressed = "#f7e1d7";
    private static String buttonBorderWidthPressed = "2px";
    private static String buttonBorderRadiusPressed = "6px";
    private static String buttonPaddingPressed = "0px";

    private static String buttonColorHover = "#2c2c4b";
    private static String buttonBorderColorHover = "#f7e1d7";
    private static String buttonBorderWidthHover = "2px";
    private static String buttonBorderRadiusHover = "6px";
    private static String buttonPaddingHover = "0px";

    private static String textColor = "#f7e1d7";
    public static String parcelAColor = "#b5179e"; // Wine red
    public static String parcelBColor = "#ff1d23";
    public static String parcelCColor = "#ffc800";
    private static String pentominoTColor = "";
    private static String pentominoPColor = "";
    private static String pentominoLColor = "";

    public static void constructWindow3D(float WINDOW_WIDTH, float WINDOW_HEIGHT, int length, int height, int width, Stage mainStage){

        Interface.WINDOW_WIDTH = WINDOW_WIDTH;
        Interface.WINDOW_HEIGHT = WINDOW_HEIGHT;
        Interface.length = length;
        Interface.height = height;
        Interface.width = width;
        Interface.displayGroup = new Group();
        Interface.truckGroup = new Group();

        createAxis();
        createTruck();

        Interface.displayGroup.getChildren().add(truckOutline);
        Interface.displayGroup.getChildren().add(axisGroup);

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        mainStage.setOnCloseRequest(event -> {
            DancingLinks.stopDancingLinks = true;
            Platform.exit();
        });

        Interface.camera = new PerspectiveCamera(false);
        Interface.camera.setDepthTest(DepthTest.ENABLE);
        Interface.camera.setTranslateY(WINDOW_HEIGHT/-4 + cameraYoffset);

        Interface.scene = new Scene(displayGroup,WINDOW_WIDTH, WINDOW_HEIGHT, true);
        Interface.scene.setFill(Color.valueOf(backgroundColor));

        Interface.scene.setCamera(camera);
        Interface.camera.translateZProperty().bind(zoom.multiply(cameraZoomOut)); // Zooms camera out on start
        Interface.initMouseControl(displayGroup, scene, mainStage);

        createButtons();
        Main.assignButtonActions();

        Interface.controlWindow = new Stage();
        Interface.controlWindow.initStyle(StageStyle.UTILITY);
        Interface.controlWindow.setAlwaysOnTop(true);
        Interface.controlWindow.setTitle("Controls");
        Scene controlScene = new Scene(solverControls, primaryScreenBounds.getWidth() / 6, 100);
        //controlScene.getStylesheets().add(Interface.class.getResource("styles.css").toExternalForm());
        controlWindow.setScene(controlScene);
        Interface.controlWindow.setWidth(primaryScreenBounds.getWidth()); 
        Interface.controlWindow.setHeight(primaryScreenBounds.getHeight()/7);
        Interface.controlWindow.setOnCloseRequest(event -> {
            DancingLinks.stopDancingLinks = true;
            Platform.exit(); 
        });
        Interface.controlWindow.setX(primaryScreenBounds.getMinX());
        Interface.controlWindow.setY(primaryScreenBounds.getMinY());

        drawUI(null);

        Rotate rotate = new Rotate(1, new Point3D(0, 1, 0));
        Interface.displayGroup.getTransforms().add(rotate);
        displayRotation = new Timeline(
            new KeyFrame(Duration.seconds(0.01), event -> {
                rotate.setAngle(((Rotate) rotate).getAngle() + 0.1); // Adjust the increment as needed
            })
        );
        displayRotation.setCycleCount(Timeline.INDEFINITE);
        displayRotation.play();

        mainStage.setTitle("Solver View");
        mainStage.setScene(scene);
        mainStage.show();

        Interface.controlWindow.show();
    }

    private static Box createBox(int width, int height, int depth, double colorID) {
        String color = "#ffb703";
        //System.out.println((int) (colorID/1) );
        double darken = 1;
        double darkenMultiplier = 10;
        switch ((int)(colorID/1)) {
            case 1:
                color = parcelAColor;
                darken = extractAndConvertToDouble(colorID) * darkenMultiplier;
                break;
            case 2:
                color = parcelBColor;
                darken = extractAndConvertToDouble(colorID) * darkenMultiplier;
                break;
            case 3:
                color = parcelCColor;
                darken = extractAndConvertToDouble(colorID) * darkenMultiplier ;
                break;
        }
        //System.out.println("Color ID: "  + (int)(colorID/1) + " Darken: " + darken);
        //if(color.equals("#06d6a0")) System.out.println("Incorrect color ID at createBox. ID received: " + colorID);
        Box box = new Box(width, height, depth);
        PhongMaterial material = new PhongMaterial();
        Color colorConverted = Color.web(adjustColor(color, darken));
        material.setDiffuseColor(new Color(colorConverted.getRed(), colorConverted.getGreen(), colorConverted.getBlue(), Interface.blockOpacity));
        material.setSpecularColor(Color.BLACK);
        box.setMaterial(material);
        return box;
    }

    public static double extractAndConvertToDouble(double number) {
        // Convert the double to a string
        String numberString = Double.toString(number);

        // Check if the string contains a decimal point
        int decimalIndex = numberString.indexOf('.');
        if (decimalIndex != -1 && decimalIndex < numberString.length() - 1) {
            // Extract the decimal part as a substring
            String decimalPartString = numberString.substring(decimalIndex + 1);

            // Convert the decimal part to a double using BigDecimal for precision
            BigDecimal decimalPart = new BigDecimal("0." + decimalPartString);

            //System.out.println("Converted " + number + " into " + decimalPart);
            return decimalPart.doubleValue();
        } else {
            // No decimal part
            return 0.0;
        }
    }

    public static String adjustColor(String hexColor, double factor) {
        // Parse the hex color to RGB components
        int red = Integer.parseInt(hexColor.substring(1, 3), 16);
        int green = Integer.parseInt(hexColor.substring(3, 5), 16);
        int blue = Integer.parseInt(hexColor.substring(5, 7), 16);

        // Adjust RGB components
        red = (int) (red * factor);
        green = (int) (green * factor);
        blue = (int) (blue * factor);

        // Ensure the values are within the valid range (0 to 255)
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        // Convert back to hex format
        String adjustedHexColor = String.format("#%02X%02X%02X", red, green, blue);

        return adjustedHexColor;
    }

    private static void colorTruck(double[][][] field) {
        if (field == null) {
            System.out.println("Given field is null at colorTruck");
        } else {
    
            Interface.truckGroup.getChildren().clear();  // Clears existing children
    
            for (int z = 0; z < Interface.height; z++) {
                for (int x = 0; x < Interface.length; x++) {
                    for (int y = 0; y < Interface.width; y++) {
                        double colorID = field[x][y][z];
                        //System.out.println(colorID);
                        if (colorID == -1) continue;
                        if (colorID > colorCount - 1) {
                            //System.out.println("Wrong color ID at field position X:" + x + " Y:" + y + " Z:" + z + " at colorTruck");
                            //continue;
                        }
    
                        Box block = createBox(buildingBlockSize, buildingBlockSize, buildingBlockSize, colorID);
                        Box outline = createOutline(buildingBlockSize, buildingBlockSize, buildingBlockSize);
                        if (block != null) {
                            block.setTranslateX((buildingBlockSize / 2) + x * buildingBlockSize);
                            block.setTranslateY((buildingBlockSize / 2) + z * buildingBlockSize);
                            block.setTranslateZ((buildingBlockSize / 2) + y * buildingBlockSize);
                            outline.setTranslateX((buildingBlockSize / 2) + x * buildingBlockSize);
                            outline.setTranslateY((buildingBlockSize / 2) + z * buildingBlockSize);
                            outline.setTranslateZ((buildingBlockSize / 2) + y * buildingBlockSize);
                            //System.out.println("added" + colorID);
                            Interface.truckGroup.getChildren().addAll(block, outline);
                        }
                    }
                }
            }
        }

    }

    private static Box createOutline(double width, double height, double depth) {
        Box outline = new Box(width + 1, height + 1, depth + 1);
        PhongMaterial outlineMaterial = new PhongMaterial();
        outlineMaterial.setDiffuseColor(Color.BLACK);
        outline.setMaterial(outlineMaterial);
        outline.setDrawMode(DrawMode.LINE);
        return outline;
    }
    
    private static void resetCoords(Group groupToReset){
        groupToReset.setTranslateX(0);
        groupToReset.setTranslateY(0);
        groupToReset.setTranslateZ(0);
    }

    private static void center(Group groupToCenter){
        groupToCenter.setTranslateX(groupToCenter.getTranslateX() - Interface.length*buildingBlockSize*0.5);
        groupToCenter.setTranslateZ(groupToCenter.getTranslateZ() - Interface.width*buildingBlockSize*0.5);
        groupToCenter.setTranslateY(groupToCenter.getTranslateY() - Interface.height * buildingBlockSize);
    }
    
    public static void drawUI(double[][][] field){

        displayGroup.getChildren().remove(truckGroup);
        colorTruck(field);
        resetCoords(Interface.truckGroup);
        center(Interface.truckGroup);
        if(width == 4) Interface.truckGroup.setTranslateZ(width-width/2 - buildingBlockSize*1.5);
        Interface.displayGroup.getChildren().add(Interface.truckGroup);https://app.diagrams.net/
        Interface.displayGroup.setTranslateX(length*buildingBlockSize/-2);
        Interface.camera.setTranslateX(Interface.displayGroup.getTranslateX()/2 - length*buildingBlockSize/2);
        //camera.setTranslateY(displayGroup.getTranslateY());
        //camera.setTranslateZ(displayGroup.getTranslateZ());
        truckGroup.setScaleZ(-1);
        Interface.scene.setRoot(Interface.displayGroup); //refreshes the displayed truck on the UI
        //System.out.println("truckOutline Translation: " + truckOutline.getTranslateX() + ", " + truckOutline.getTranslateY() + ", " + truckOutline.getTranslateZ());
        //System.out.println("truckGroup Translation: " + truckGroup.getTranslateX() + ", " + truckGroup.getTranslateY() + ", " + truckGroup.getTranslateZ());

    }

    public static void resetUI(){

            displayGroup.getChildren().remove(truckGroup);
            //colorTruck(field);
            resetCoords(Interface.truckGroup);
            center(Interface.truckGroup);
            Interface.displayGroup.getChildren().add(new Group());
            Interface.displayGroup.setTranslateX(length*buildingBlockSize/-2);
            Interface.camera.setTranslateX(Interface.displayGroup.getTranslateX()/2 - length*buildingBlockSize/2);
            //camera.setTranslateY(displayGroup.getTranslateY());
            //camera.setTranslateZ(displayGroup.getTranslateZ());
            Interface.scene.setRoot(Interface.displayGroup); //refreshes the displayed truck on the UI
            //System.out.println("truckOutline Translation: " + truckOutline.getTranslateX() + ", " + truckOutline.getTranslateY() + ", " + truckOutline.getTranslateZ());
            //System.out.println("truckGroup Translation: " + truckGroup.getTranslateX() + ", " + truckGroup.getTranslateY() + ", " + truckGroup.getTranslateZ());
    
        
    }

    // Just creates the axis lines and adds to axisGroup 
    private static void createAxis(){

        int axisLength = 30 * Interface.buildingBlockSize;

        Box xAxis = new Box(axisWidth, axisWidth, axisLength*1.6);
        Box yAxis = new Box(axisWidth, axisWidth, axisLength/1.5);
        Box zAxis = new Box(axisWidth, axisWidth, axisLength/1.5);

        PhongMaterial red = new PhongMaterial(Color.RED);
        PhongMaterial lightGreen = new PhongMaterial(Color.LIGHTGREEN);
        PhongMaterial blue = new PhongMaterial(Color.BLUE);

        red.setDiffuseMap(createColorImage(Color.RED));
        lightGreen.setDiffuseMap(createColorImage(Color.LIGHTGREEN));
        blue.setDiffuseMap(createColorImage(Color.BLUE));

        xAxis.setMaterial(red);
        yAxis.setMaterial(lightGreen);
        zAxis.setMaterial(blue);

        xAxis.setTranslateX(axisLength/2*1.6 + -1*length*buildingBlockSize/2);
        xAxis.setTranslateY(0);
        xAxis.setTranslateZ(width*buildingBlockSize*0.5);

        yAxis.setTranslateX(-1*length*buildingBlockSize/2);
        yAxis.setTranslateY(axisLength/1.5/-2);
        yAxis.setTranslateZ(width*buildingBlockSize*0.5);

        zAxis.setTranslateX(-1*length*buildingBlockSize/2);
        zAxis.setTranslateY(0);
        zAxis.setTranslateZ(axisLength/-2/1.5 + width*buildingBlockSize*0.5);

        yAxis.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        xAxis.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));

        xAxis.setDrawMode(DrawMode.FILL);
        yAxis.setDrawMode(DrawMode.FILL);
        zAxis.setDrawMode(DrawMode.FILL);

        Interface.axisGroup = new Group();
        Interface.axisGroup.getChildren().add(xAxis);
        Interface.axisGroup.getChildren().add(yAxis);
        Interface.axisGroup.getChildren().add(zAxis);
    }

    private static Image createColorImage(Color color) {
        int size = 1; // 1x1 pixel image
        WritableImage image = new WritableImage(size, size);
        PixelWriter pixelWriter = image.getPixelWriter();
        pixelWriter.setColor(0, 0, color);
        return image;
    }

    /**
     * Creates the truck outline used in 3d view
     */
    private static void createTruck(){
        // TODO: Update with lines instead of box
        Interface.truckOutline = new Box(33 * buildingBlockSize, 8 * buildingBlockSize, 5 * buildingBlockSize);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.RED);
        //material.setSpecularColor(Color.TRANSPARENT);
        truckOutline.setDrawMode(DrawMode.LINE);
        truckOutline.setMaterial(material);
        //truckOutline.translateXProperty().set(length*buildingBlockSize*0.5);
        truckOutline.translateYProperty().set(8*buildingBlockSize*-0.5);
        truckOutline.translateZProperty().set(5*buildingBlockSize*0.5 - 0.5*5*buildingBlockSize);
    }

    /**
     * 
     */
    private static void createButtons(){

        buttonTestField = new Button("Test Field");
        buttonCoverSolver = new Button("Cover Solver");
        buttonKnapsackSolver = new Button("Knapsack Solver");
        buttonStop = new Button("Stop Solver");
        textDisplay = new Label("Please select the pieces and choose solver");
        textDisplayInfo = new Label("Output:");
        setSelection = new ComboBox<>();
        setSelection.getItems().addAll("Packages: A, B, C", "Pentominoes: L, P, T");
        setSelection.setValue("Select Used Pieces");


        String buttonStyle = 
            "-fx-background-color: " + buttonColor + "; " + 
            "-fx-border-color: " + buttonBorderColor + "; " + 
            "-fx-border-width: " + buttonBorderWidth + "; " + 
            "-fx-border-radius: " + buttonBorderRadius + "; " + 
            "-fx-background-radius: " + buttonBorderRadius + "; " +
            "-fx-padding: " + buttonPadding + "; " +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-alignment: center;";

        String buttonStylePressed = 
            "-fx-background-color: " + buttonColorPressed + "; " + 
            "-fx-border-color: " + buttonBorderColorPressed + "; " + 
            "-fx-border-width: " + buttonBorderWidthPressed + "; " + 
            "-fx-border-radius: " + buttonBorderRadiusPressed + "; " +
            "-fx-background-radius: " + buttonBorderRadius + "; " + 
            "-fx-padding: " + buttonPaddingPressed + "; " +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-alignment: center;";

        String buttonStyleHover = 
            "-fx-background-color: " + buttonColorHover + "; " + 
            "-fx-border-color: " + buttonBorderColorHover + "; " + 
            "-fx-border-width: " + buttonBorderWidthHover + "; " + 
            "-fx-border-radius: " + buttonBorderRadiusHover + "; " + 
            "-fx-background-radius: " + buttonBorderRadius + "; " +
            "-fx-padding: " + buttonPaddingHover + "; " +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-alignment: center;";

        String textDisplayStyle = 
            "-fx-background-color: " + buttonColor + "; " + 
            "-fx-border-color: " + buttonBorderColorHover + "; " + 
            "-fx-border-width: " + buttonBorderWidthHover + "; " + 
            "-fx-border-radius: " + buttonBorderRadiusHover + "; " + 
            "-fx-background-radius: " + buttonBorderRadius + "; " +
            "-fx-padding: " + buttonPaddingHover + "; " +
            "-fx-text-fill: " + buttonColorPressed + ";" +
            "-fx-alignment: center;";
        

        buttonCoverSolver.setStyle(buttonStyle);
        buttonTestField.setStyle(buttonStyle);
        buttonKnapsackSolver.setStyle(buttonStyle);
        buttonStop.setStyle(buttonStyle);
        textDisplayInfo.setStyle(textDisplayStyle);
        textDisplay.setStyle(
            "-fx-background-color: " + controlBackgroundColor + "; " + 
            "-fx-border-color: " + buttonBorderColor + "; " + 
            "-fx-border-width: " + buttonBorderWidth + "; " + 
            "-fx-border-radius: " + buttonBorderRadius + "; " + 
            "-fx-background-radius: " + buttonBorderRadius + "; " +
            "-fx-padding: " + buttonPadding + "; " +
            "-fx-text-fill: " + buttonColorPressed + ";" +
            "-fx-alignment: center;"
        );
        setSelection.setStyle(
            "-fx-background-color: " + buttonColor + "; " + 
            "-fx-border-color: " + buttonBorderColor + "; " + 
            "-fx-border-width: " + buttonBorderWidth + "; " + 
            "-fx-border-radius: " + buttonBorderRadius + "; " +
            "-fx-background-radius: " + buttonBorderRadius + 2 + "; " + 
            "-fx-padding: " + buttonPadding + "; " +
            "-fx-text-fill: " + textColor + "; " +
            "-fx-alignment: center;"
        );

        setSelection.setButtonCell(new ListCell<>() {
            {
                setStyle(
                    "-fx-background-radius: " + buttonBorderRadius + "; " +
                    "-fx-padding: " + buttonPadding + "; " +
                    "-fx-text-fill: " + textColor + ";" +
                    "-fx-alignment: center;"
                );
                
            }
        });

        setSelection.setCellFactory(param -> new ComboBoxListCell<String>() {
            {
                setStyle(buttonStyle);

                setOnMouseEntered(event -> {
                    setStyle(buttonStyleHover);
                });
                setOnMouseExited(event -> {
                    setStyle(buttonStyle);
                });
                setOnMousePressed(event -> {
                    setStyle(buttonStylePressed);
                });
                setOnMouseReleased(event -> {
                    setStyle(buttonStyle);
                });
            }
        });

        buttonTestField.setOnMousePressed(e -> buttonTestField.setStyle(buttonStylePressed));
        buttonTestField.setOnMouseReleased(e -> buttonTestField.setStyle(buttonStyleHover));
        buttonTestField.setOnMouseEntered(e -> buttonTestField.setStyle(buttonStyleHover));
        buttonTestField.setOnMouseExited(e -> buttonTestField.setStyle(buttonStyle));

        buttonCoverSolver.setOnMousePressed(e -> buttonCoverSolver.setStyle(buttonStylePressed));
        buttonCoverSolver.setOnMouseReleased(e -> buttonCoverSolver.setStyle(buttonStyleHover));
        buttonCoverSolver.setOnMouseEntered(e -> buttonCoverSolver.setStyle(buttonStyleHover));
        buttonCoverSolver.setOnMouseExited(e -> buttonCoverSolver.setStyle(buttonStyle));

        buttonKnapsackSolver.setOnMousePressed(e -> buttonKnapsackSolver.setStyle(buttonStylePressed));
        buttonKnapsackSolver.setOnMouseReleased(e -> buttonKnapsackSolver.setStyle(buttonStyleHover));
        buttonKnapsackSolver.setOnMouseEntered(e -> buttonKnapsackSolver.setStyle(buttonStyleHover));
        buttonKnapsackSolver.setOnMouseExited(e -> buttonKnapsackSolver.setStyle(buttonStyle));

        buttonStop.setOnMousePressed(e -> buttonStop.setStyle(buttonStylePressed));
        buttonStop.setOnMouseReleased(e -> buttonStop.setStyle(
            "-fx-background-color: " + "#ef233c" + "; " + 
            "-fx-border-color: " + buttonBorderColorHover + "; " + 
            "-fx-border-width: " + buttonBorderWidthHover + "; " + 
            "-fx-border-radius: " + buttonBorderRadiusHover + "; " + 
            "-fx-background-radius: " + buttonBorderRadius + "; " +
            "-fx-padding: " + buttonPaddingHover + "; " +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-alignment: center;"
        ));
        buttonStop.setOnMouseEntered(e -> buttonStop.setStyle(
            "-fx-background-color: " + "#ef233c" + "; " + 
            "-fx-border-color: " + buttonBorderColorHover + "; " + 
            "-fx-border-width: " + buttonBorderWidthHover + "; " + 
            "-fx-border-radius: " + buttonBorderRadiusHover + "; " + 
            "-fx-background-radius: " + buttonBorderRadius + "; " +
            "-fx-padding: " + buttonPaddingHover + "; " +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-alignment: center;"
        ));
        buttonStop.setOnMouseExited(e -> buttonStop.setStyle(buttonStyle));

        setSelection.setOnMousePressed(e -> setSelection.setStyle(buttonStylePressed));
        setSelection.setOnMouseReleased(e -> setSelection.setStyle(buttonStyleHover));
        setSelection.setOnMouseEntered(e -> setSelection.setStyle(buttonStyleHover));
        setSelection.setOnMouseExited(e -> setSelection.setStyle(buttonStyle));

        buttonCoverSolver.setPrefSize(120, 40);
        buttonTestField.setPrefSize(120, 40);
        buttonKnapsackSolver.setPrefSize(120, 40);
        buttonStop.setPrefSize(100, 40);
        setSelection.setPrefSize(170, 40);
        textDisplayInfo.setPrefSize(70, 40);
        textDisplay.setPrefSize(WINDOW_WIDTH - 120*3 - 170 - 10*5 - 60, 40);

        Interface.solverControls = new HBox(10);
        Interface.solverControls.getChildren().addAll(Interface.setSelection, Interface.buttonCoverSolver, Interface.buttonKnapsackSolver, Interface.buttonStop, Interface.textDisplayInfo, Interface.textDisplay);
        solverControls.setStyle(
            "-fx-background-color: " + controlBackgroundColor + "; " +
            "-fx-border-color: " + buttonBorderColor + "; " +
            "-fx-border-width: 1px;" + 
            "-fx-border-radius: " + buttonBorderRadius + "; " +
            "-fx-border-style: solid;"
        );
        solverControls.setPadding(new javafx.geometry.Insets(10));

    }

    public static void initMouseControl(Group group, Scene scene, Stage stage) {
        Rotate xRotate;
        Rotate yRotate;
        group.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
            displayRotation.pause();
        });

        scene.setOnMouseReleased(event -> {
           displayRotation.play(); 
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + (anchorX - event.getSceneX()));
            angleZ.set(angleZ.get() + (event.getSceneX() - anchorX) / 2.0);
            //System.out.println("Camera dragged");

        });

        stage.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            zoom.set(zoom.get() + delta / 1000.0);

            zoom.set(Math.max(0.1, Math.min(zoom.get(), 3.0)));
        });
    }

    

}

