package pkg2pc.assign01;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import jdk.nashorn.internal.ir.BlockLexicalContext;

/**
 * Assignment 01 - Implement parallel maze solving algorithm using JAVA Concurrent API
 * 
 * use maze.isNorth(x,y), maze.isSouth(x,y), etc to check if there is wall in exact direction
 * use maze.setVisited(x,y) and maze.isVisited(x,y) to check is you already have been at particular cell in the maze
 * @author Janis
 */
public class MazeSolver {
    // Class containing information about a spot: 
    private class SpotInfo{
        // ID of parent thread
        final private Long main;
        // ID of thread itself
        final private Long sub;
        // coordinates of spot
        private int x;
        private int y;

        private SpotInfo(Long main, Long sub, int x, int y){
            this.main = main;
            this.sub = sub;
            this.x = x;
            this.y = y;
        }
      
        public Long getMain() {
            return main;
        }
      
        public Long getSub() {
            return sub;
        }
      
        public int getX() {
            return x;
        }
     
        public int getY() {
            return y;
        }
    }
    
    Maze maze;
    
    //Current position
    int x;
    int y;
    // Variable that allows to detect, if it is a first move
    boolean first = true;
    // A thread-safe variant of ArrayList, containing spots
    CopyOnWriteArrayList<SpotInfo> spotList = new CopyOnWriteArrayList<SpotInfo>();

    public MazeSolver(Maze m){
        this.maze = m;
    }

    // Solves the maze by recursively looking for places to go
    public void solve(int x, int y){
        if(first){
            first = false;
            move(x, y);
        }else{  
            doNorth(x, y);
            doEast(x, y);  
            doSouth(x, y); 
            doWest(x, y);
        }
    }

    // Checks if it is possible to move North and moves if it is
    private void doNorth(int x, int y){
        if(!maze.isNorth(x, y) && !maze.isVisited(x, y + 1)){
            move(x, y + 1); 
        }
    }
    
    // Checks if it is possible to move East and moves if it is
    private void doEast(int x, int y){
        if(!maze.isEast(x, y) && !maze.isVisited(x + 1, y)){
            move(x + 1, y);
        }
    }

    // Checks if it is possible to move South and moves if it is
    private void doSouth(int x, int y){
        if(!maze.isSouth(x, y) && !maze.isVisited(x, y - 1)){
            move(x, y - 1);
        }
    }

    // Checks if it is possible to move West and moves if it is
    private void doWest(int x, int y){
        if(!maze.isWest(x, y) && !maze.isVisited(x - 1, y)){
            move(x - 1, y); 
        }
    }

    // Moves to given coordinates (sets and draws the spot)
    private void move(int x, int y){
        Long main = Thread.currentThread().getId();
        try{
            Thread thread = new Thread(){
                public void run(){
                    maze.setVisited(x, y);
                    drawMove(x, y);
                    spotList.add(new SpotInfo(main, this.getId(), x, y));
                    if(x == maze.getN() && y == maze.getN()){
                        finalSolution(spotList);
                        System.out.println("FINISHED");
                    }
                    if(!solutionFound()){
                        solve(x, y);
                    }
                    try{
                        join();
                    }catch(Exception e){
                        System.out.println(e);
                    }
                }
            };
            thread.start();
        }catch(Exception e){
            System.out.println(e);
        }
    }

    // Checks whether final spot has been reached
    private boolean solutionFound(){
        return maze.isVisited(maze.getN(), maze.getN());
    }

    // Draws final solution
    private void finalSolution(CopyOnWriteArrayList<SpotInfo> spotList){
        Collections.reverse(spotList);
        SpotInfo temp = new SpotInfo(0L, 0L, 0, 0);
        // Finds thread ID of final spot
        for(SpotInfo var : spotList){
            if(var.getX() == maze.getN() && var.getY() == maze.getN()){
                temp = new SpotInfo(var.getMain(), var.getSub(), var.getX(), var.getY());
                drawSolution(temp.getX(), temp.getY());
                spotList.remove(var);
                break;
            }
        }
        // Goes through spotList array and draws solution based on thread IDs
        while(true){
            for(SpotInfo var : spotList){
                if(var.getSub().equals(temp.getMain())){
                    temp = new SpotInfo(var.getMain(), var.getSub(), var.getX(), var.getY());
                    drawSolution(temp.getX(), temp.getY());
                    spotList.remove(var);
                    break;
                }
            }
            if( temp.getX() == 1 && temp.getY() == 1){
                break;
            }
        }
    }

    // Draws solution spot
    private void drawSolution(int x, int y){
        StdDraw.setPenColor(StdDraw.GREEN);
        StdDraw.filledCircle(x + 0.5, y + 0.5, 0.3);
        //sleeps for 10 ms
        StdDraw.show(10);
    }

    // Use theses lines where necessary to draw movement of the solver
    private void drawMove(int x, int y){
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.filledCircle(x + 0.5, y + 0.5, 0.25);
        //StdDraw.setPenColor(StdDraw.BLACK);
        //StdDraw.text(x + 0.8, y + 0.8, ""+Thread.currentThread().getId());
        //sleeps for 30 ms
        StdDraw.show(30);
    }

    /**
     * Solve maze starting from initial positions
     * @param x starting position in x direction
     * @param y starting position in y direction
     */
    public void solveMaze(int x, int y){
        //Set current position to initial position
        this.x = x;
        this.y = y;
        System.out.println("SEARCHING");
        solve(x, y);
    }
}
