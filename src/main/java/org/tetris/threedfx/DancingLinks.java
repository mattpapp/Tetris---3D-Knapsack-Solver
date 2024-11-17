package org.tetris.threedfx;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.application.Platform;

public class DancingLinks{

    static final boolean verbose = true;

    class DancingNode{
        DancingNode L, R, U, D;
        ColumnNode C;

        double colorId;

        // hooks node n1 `below` current node
        DancingNode hookDown(DancingNode n1){
            assert (this.C == n1.C);
            n1.D = this.D;
            n1.D.U = n1;
            n1.U = this;
            this.D = n1;
            return n1;
        }

        // hooke a node n1 to the right of `this` node
        DancingNode hookRight(DancingNode n1){
            n1.R = this.R;
            n1.R.L = n1;
            n1.L = this;
            this.R = n1;
            return n1;
        }

        void unlinkLR(){
            this.L.R = this.R;
            this.R.L = this.L;
            updates++;
        }

        void relinkLR(){
            this.L.R = this.R.L = this;
            updates++;
        }

        void unlinkUD(){
            this.U.D = this.D;
            this.D.U = this.U;
            updates++;
        }

        void relinkUD(){
            this.U.D = this.D.U = this;
            updates++;
        }

        public DancingNode(double colorId){
            L = R = U = D = this;
            this.colorId = colorId;
        }

        public DancingNode(ColumnNode c, double colorId){
            this(colorId);
            C = c;
        }
    }

    class ColumnNode extends DancingNode{
        int size; // number of ones in current column
        String name;

        public ColumnNode(String n){
            super(-1);
            size = 0;
            name = n;
            C = this;
        }

        void cover(){
            unlinkLR();
            for(DancingNode i = this.D; i != this; i = i.D){
                for(DancingNode j = i.R; j != i; j = j.R){
                    j.unlinkUD();
                    j.C.size--;
                }
            }
            header.size--; // not part of original
        }

        void uncover(){
            for(DancingNode i = this.U; i != this; i = i.U){
                for(DancingNode j = i.L; j != i; j = j.L){
                    j.C.size++;
                    j.relinkUD();
                }
            }
            relinkLR();
            header.size++; // not part of original
        }
    }

    private ColumnNode header;
    public static int solutions = 0;
    private int updates = 0;
    private List<DancingNode> answer;
    private DancingLinksSearch handler;

    // Heart of the algorithm
    private void search(int k){
        if(stopDancingLinks) {
            Platform.runLater(() -> Interface.resetUI());
            return;
        }

        if (header.R == header){ // all the columns removed
            if(verbose){
                System.out.println("-----------------------------------------");
                System.out.println("Solution #" + solutions + "\n");
            }
            handler.displaySolution(answer);
            if(!DancingLinks.knapsackSolver) Platform.runLater(() -> Interface.textDisplay.setText("Solution #" + solutions));
            //handler.handleSolution(answer);
            if(verbose){
                System.out.println("-----------------------------------------");
            }
            solutions++;
        } else{

            ColumnNode c = selectColumnNodeHeuristic();
            c.cover();

            for(DancingNode r = c.D; r != c; r = r.D){
                answer.add(r);

                for(DancingNode j = r.R; j != r; j = j.R){
                    j.C.cover();
                }
                

                if(timer() >= timeoutInSeconds/2 && solutions == 0){
                    Platform.runLater(() -> Interface.textDisplay.setText("Solver taking its time..."));
                }

                if(timer() > timeoutInSeconds && solutions == 0){
                    Platform.runLater(() -> Interface.textDisplay.setText("Solver timed out after " + timeoutInSeconds + "s, possibly no solution."));
                    try{Thread.sleep(50);}catch(Exception e){};
                    Platform.runLater(() -> Interface.resetUI());
                    Main.myThread = null;
                    return;
                }

                search(k + 1);

                r = answer.remove(answer.size() - 1);
                c = r.C;

                for(DancingNode j = r.L; j != r; j = j.L){
                    j.C.uncover();
                }
            }
            c.uncover();
        }
    }

    private ColumnNode selectColumnNodeHeuristic(){
        int min = Integer.MAX_VALUE;
        ColumnNode ret = null;
        for(ColumnNode c = (ColumnNode) header.R; c != header; c = (ColumnNode) c.R){
            if (c.size < min){
                min = c.size;
                ret = c;
            }
        }
        return ret;
    }


    // grid is a grid of 0s and 1s to solve the exact cover for
    // returns the root column header node
    private ColumnNode makeDLXBoard(double[][] grid) {
        if (grid.length == 0 || grid[0].length == 0) {
            // Handle the case where the grid is empty
            throw new IllegalArgumentException("Grid must not be empty");
        }
    
        final int COLS = grid[0].length;
        final int ROWS = grid.length;
    
        ColumnNode headerNode = new ColumnNode("header");
        ArrayList<ColumnNode> columnNodes = new ArrayList<ColumnNode>();
    
        for (int i = 0; i < COLS; i++) {
            ColumnNode n = new ColumnNode(Integer.toString(i));
            columnNodes.add(n);
            headerNode = (ColumnNode) headerNode.hookRight(n);
        }
        headerNode = headerNode.R.C;
    
        for (int i = 0; i < ROWS; i++) {
            DancingNode prev = null;
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] != 0) {
                    ColumnNode col = columnNodes.get(j);
                    DancingNode newNode = new DancingNode(col,grid[i][j]);
                    if (prev == null)
                        prev = newNode;
                    col.U.hookDown(newNode);
                    prev = prev.hookRight(newNode);
                    col.size++;
                }
            }
        }
    
        headerNode.size = COLS;
    
        return headerNode;
    }

    private void showInfo(){
        System.out.println("Number of updates: " + updates);
    }

    // Grid consists solely of 1s and 0s. Undefined behaviour otherwise
    //public DancingLinks(int[][] grid){
    //    this(grid);
   // }

    public DancingLinks(double[][] grid, DancingLinksSearch search){
        header = makeDLXBoard(grid);
        handler = search;
    }

    public long timer(){
        timeTaken = System.currentTimeMillis() / 1000 - timeStarted;
        //System.out.println("Time taken:" + timeTaken);
        return timeTaken;
    }

    public void runSolver(){
        solutions = 0;
        updates = 0;
        answer = new LinkedList<DancingNode>();
        timeStarted = System.currentTimeMillis() / 1000;
        search(0);
        if(verbose) showInfo();
    }

    //private volatile boolean timeoutOccurred = false;
    public static boolean stopDancingLinks = false;
    public static long timeTaken;
    public static long timeStarted;
    long timeoutInSeconds = 10;
    public static boolean knapsackSolver = false;

}