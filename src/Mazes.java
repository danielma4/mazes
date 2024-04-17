import java.util.*;

import javalib.worldimages.*;
import java.awt.Color;
import tester.*;
import javalib.impworld.*;

//represents a Tile in a maze
interface ITile {
  static final Color TILE_COLOR = Color.LIGHT_GRAY;
  static final Color WALL_COLOR = Color.DARK_GRAY;
}

//represents an abstract Tile in a maze
abstract class ATile implements ITile {
  //access needed for rendering
  //we also need to change tileColors when creating a heat map
  protected Color tileColor;
  
  ATile(Color tileColor) {
    this.tileColor = tileColor;
  }
  
  ATile() {
    this.tileColor = ITile.TILE_COLOR;
  }

  //finds the Tile representative of this ATile
  ATile findRep(HashMap<ATile, ATile> reps) {
    ATile rep = reps.get(this);
    if (rep.equals(this)) {
      return rep;
    } else {
      return rep.findRep(reps);
    }
  }

  //breaks the wall between this ATile and other ATile
  abstract void breakTile(ATile other);

  //breaks the wall between this ATile and other RectTile
  void breakRect(RectTile other) {
    throw new IllegalArgumentException("Incompatible tile types.");
  }

  //breaks the wall between this ATile and other HexTile
  void breakHex(HexTile other) {
    throw new IllegalArgumentException("Incompatible tile types.");
  }

  //mutates this ATile's color to indicate current Tile
  void moveTo() {
    this.tileColor = new Color(61, 118, 204);
  }

  //mutates this ATile's color to indicate that it's been visited
  void moveFrom() { 
    this.tileColor = new Color(145, 184, 242);
  }
}

class RectTile extends ATile{
  //these fields are not final because we need to break them when making the maze
  private boolean upWall;
  private boolean downWall;
  private boolean rightWall;
  private boolean leftWall;

  //fields are cyclic and therefore need mutation
  private RectTile up;
  private RectTile down;
  private RectTile right;
  private RectTile left;

  RectTile(boolean upWall, boolean downWall, boolean rightWall, boolean leftWall,
      RectTile up, RectTile down, RectTile right, RectTile left, Color tileColor) {
    super(tileColor);
    this.upWall = upWall;
    this.downWall = downWall;
    this.rightWall = rightWall;
    this.leftWall = leftWall;
    this.up = up;
    this.down = down;
    this.right = right;
    this.left = left;
  }

  //sets this RectTile's neighbors to null
  RectTile(boolean upWall, boolean downWall, boolean rightWall, boolean leftWall, Color tileColor) {
    super(tileColor);
    this.upWall = upWall;
    this.downWall = downWall;
    this.rightWall = rightWall;
    this.leftWall = leftWall;
    this.up = null;
    this.down = null;
    this.right = null;
    this.left = null;
  }

  //sets this RectTile's walls to true
  RectTile(Color tileColor) {
    this(true, true, true, true, tileColor);
  }

  //sets this RectTile's wall to true and this color to a default color
  RectTile() {
    this(true, true, true, true, ITile.TILE_COLOR);
  }

  //sets this RectTile's up field to RectTile up
  void setUp(RectTile up) {
    this.up = up;
  }

  //sets this RectTile's down field to RectTile down
  void setDown(RectTile down) {
    this.down = down;
  }

  //sets this RectTile's right field to RectTile right
  void setRight(RectTile right) {
    this.right = right;
  }

  //sets this RectTile's left field to RectTile left
  void setLeft(RectTile left) {
    this.left = left;
  }

  //renders this RectTile as a WorldImage
  WorldImage render(int size) {
    WorldImage walls = new RectangleImage(size, size, "solid", ITile.WALL_COLOR);
    WorldImage innerTile = new RectangleImage(size - 2, size - 2, "solid", this.tileColor);
    WorldImage tile = new OverlayImage(innerTile, walls);
    if (!this.upWall) {
      tile = new OverlayOffsetImage(innerTile, 0, 1, tile);
    }
    if (!this.downWall) {
      tile = new OverlayOffsetImage(innerTile, 0, -1, tile);
    }
    if (!this.rightWall) {
      tile = new OverlayOffsetImage(innerTile, -1, 0, tile);
    }
    if (!this.leftWall) {
      tile = new OverlayOffsetImage(innerTile, 1, 0, tile);
    }
    return tile;
  }

  //finds where a neighbor RectTile is in relation to this RectTile
  private String getDirection(RectTile neighbor) {
    if (neighbor.equals(this.up)) {
      return "up";
    } else if (neighbor.equals(this.down)) {
      return "down";
    } else if (neighbor.equals(this.right)) {
      return "right";
    } else if (neighbor.equals(this.left)) {
      return "left";
    } else {
      throw new IllegalArgumentException("Tile is not a neighbor");
    }
  }

  //breaks this RectTile and other ATile's walls between
  void breakTile(ATile other) {
    other.breakRect(this);
  }

  //breaks the wall between this and that RectTile
  void breakRect(RectTile that) {
    String s = this.getDirection(that);
    switch (s) {
      case "up":
        this.breakUp();
        break;
      case "down":
        this.breakDown();
        break;
      case "right":
        this.breakRight();
        break;
      case "left":
        this.breakLeft();
        break;
      default:
        throw new IllegalArgumentException("Invalid direction: " + s);
    }
  }

  //sets this RectTile's up wall to false
  private void breakUp() {
    this.upWall = false;
  }

  //sets this RectTile's down wall to false
  private void breakDown() {
    this.downWall = false;
  }

  //sets this RectTile's right wall to false
  private void breakRight() {
    this.rightWall = false;
  }

  //sets this RectTile's left wall to false
  private void breakLeft() {
    this.leftWall = false;
  }

  //determines if this RectTile can move in a given direction
  boolean canMove(String direction) {
    switch (direction) {
      case "w":
      case "up":
        return !this.upWall;
      case "s":
      case "down":
        return !this.downWall;
      case "d":
      case "right":
        return !this.rightWall;
      case "a":
      case "left":
        return !this.leftWall;
      default:
        throw new IllegalArgumentException("Invalid direction: " + direction);
    }
  }

  //finds all the RectTile's which this RectTile can access
  ArrayList<RectTile> accessibleNeighbors() {
    ArrayList<RectTile> neighbors = new ArrayList<RectTile>();
    if (!this.leftWall && this.left != null) {
      neighbors.add(this.left);
    }
    if (!this.upWall && this.up != null) {
      neighbors.add(this.up);
    }
    if (!this.downWall && this.down != null) {
      neighbors.add(this.down);
    }
    if (!this.rightWall && this.right != null) {
      neighbors.add(this.right);
    }
    return neighbors;
  }
}

//represents a Hexagonal Tile in a maze
class HexTile extends ATile{
  //these fields are not final because they need to be broken for maze creation
  private boolean leftWall;;
  private boolean rightWall;
  private boolean rightUpWall;
  private boolean rightDownWall;
  private boolean leftUpWall;
  private boolean leftDownWall;
  //these fields are not final because this data is cyclic and needs to be mutated
  private HexTile left;
  private HexTile right;
  private HexTile rightUp;
  private HexTile rightDown;
  private HexTile leftUp;
  private HexTile leftDown;
  
  HexTile(boolean leftWall, boolean rightWall, boolean rightUpWall,
      boolean rightDownWall, boolean leftUpWall, boolean leftDownWall,
      HexTile left, HexTile right, HexTile rightUp, HexTile rightDown,
      HexTile leftUp, HexTile leftDown, Color tileColor) {
    super(tileColor);
    this.leftWall = leftWall;
    this.rightWall = rightWall;
    this.rightUpWall = rightUpWall;
    this.rightDownWall = rightDownWall;
    this.leftUpWall = leftUpWall;
    this.leftDownWall = leftDownWall;
    this.left = left;
    this.right = right;
    this.rightUp = rightUp;
    this.rightDown = rightDown;
    this.leftUp = leftUp;
    this.leftDown = leftDown;
  }

  //sets this HexTile's neighbors to null
  HexTile(boolean leftWall, boolean rightWall, boolean rightUpWall,
      boolean rightDownWall, boolean leftUpWall, boolean leftDownWall, Color tileColor) {
    super(tileColor);
    this.leftWall = leftWall;
    this.rightWall = rightWall;
    this.rightUpWall = rightUpWall;
    this.rightDownWall = rightDownWall;
    this.leftUpWall = leftUpWall;
    this.leftDownWall = leftDownWall;
    this.left = null;
    this.right = null;
    this.rightUp = null;
    this.rightDown = null;
    this.leftUp = null;
    this.leftDown = null;
  }

  //sets this HexTile's walls to true
  HexTile(Color tileColor) {
    this(true, true, true, true, true, true, tileColor);
  }

  //sets this HexTile's walls to true and the color to a default color
  HexTile() {
    this(true, true, true, true, true, true, ITile.TILE_COLOR);
  }

  //sets this HexTile's left neighbor
  void setLeft(HexTile left) {
    this.left = left;
  }

  //sets this HexTile's right neighbor
  void setRight(HexTile right) {
    this.right = right;
  }

  //sets this HexTile's rightUp neighbor
  void setRightUp(HexTile rightUp) {
    this.rightUp = rightUp;
  }

  //sets this HexTile's rightDown neighbor
  void setRightDown(HexTile rightDown) {
    this.rightDown = rightDown;
  }

  //sets this HexTile's leftUp neighbor
  void setLeftUp(HexTile leftUp) {
    this.leftUp = leftUp;
  }

  //sets this HexTile's leftDown neighbor
  void setLeftDown(HexTile leftDown) {
    this.leftDown = leftDown;
  }

  //renders this HexTile as a WorldImage
  WorldImage render(int sideLength) {
    WorldImage walls = new HexagonImage(sideLength, "solid", ITile.WALL_COLOR);
    WorldImage innerTile = new HexagonImage(sideLength - 2, "solid", this.tileColor);
    WorldImage tile = new RotateImage(new OverlayImage(innerTile, walls), 90);
    WorldImage vertRect = new RectangleImage(sideLength - 4,
        (int)((sideLength - 2) * Math.sqrt(3)), "solid",  this.tileColor);
    WorldImage horzRect =new RotateImage(vertRect, 90);
    WorldImage topLeft = new RotateImage(vertRect, -30);
    WorldImage topRight = new RotateImage(vertRect, 30);
    if (!this.rightWall) {
      tile = new OverlayOffsetImage(horzRect, -2.5, 0, tile);
    }
    if (!this.leftWall) {
      tile = new OverlayOffsetImage(horzRect, 2.5, 0, tile);
    }
    if (!this.rightUpWall) {
      tile = new OverlayOffsetImage(topRight, 1.5 * Math.cos(Math.PI / 3) - 1, 1.5 * Math.sin(Math.PI / 3) + 0.5, tile);
    }
    if (!this.rightDownWall) {
      tile = new OverlayOffsetImage(topLeft, 1.5 * Math.cos(Math.PI / 3) - 2, -1.5 * Math.sin(Math.PI / 3) - 1, tile);
    }
    if (!this.leftUpWall) {
      tile = new OverlayOffsetImage(topLeft, -1.5 * Math.cos(Math.PI / 3) + 0.5, 1.5 * Math.sin(Math.PI / 3) + 0.5, tile);
    }
    if (!this.leftDownWall) {
      tile = new OverlayOffsetImage(topRight, -1.5 * Math.cos(Math.PI / 3) + 1.5, -1.5 * Math.sin(Math.PI / 3) - 0.5, tile);
    }
    return tile;
  }

  //returns which direction a HexTile neighbor is with respect to this HexTile
  private String getDirection(HexTile neighbor) {
    if (neighbor.equals(this.left)) {
      return "left";
    } else if (neighbor.equals(this.right)) {
      return "right";
    } else if (neighbor.equals(this.rightUp)) {
      return "rightUp";
    } else if (neighbor.equals(this.rightDown)) {
      return "rightDown";
    } else if (neighbor.equals(this.leftUp)) {
      return "leftUp";
    } else if (neighbor.equals(this.leftDown)) {
      return "leftDown";
    } else {
      throw new IllegalArgumentException("Tile is not a neighbor");
    }
  }

  //breaks the wall between this HexTile and that ATile
  void breakTile(ATile other) {
    other.breakHex(this);
  }

  //breaks the wall between this HexTile and that HexTile
  void breakHex(HexTile that) {
    String s = this.getDirection(that);
    switch (s) {
      case "left":
        this.breakLeft();
        break;
      case "right":
        this.breakRight();
        break;
      case "rightUp":
        this.breakRightUp();
        break;
      case "rightDown":
        this.breakRightDown();
        break;
      case "leftUp":
        this.breakLeftUp();
        break;
      case "leftDown":
        this.breakLeftDown();
        break;
      default:
        throw new IllegalArgumentException("Invalid direction: " + s);
    }
  }

  //sets this HexTile's left wall to false
  private void breakLeft() {
    this.leftWall = false;
  }

  //sets this HexTile's right wall to false
  private void breakRight() {
    this.rightWall = false;
  }

  //sets this HexTile's right up wall to false
  private void breakRightUp() {
    this.rightUpWall = false;
  }

  //sets this HexTile's right down wall to false
  private void breakRightDown() {
    this.rightDownWall = false;
  }

  //sets this HexTile's left up wall to false
  private void breakLeftUp() {
    this.leftUpWall = false;
  }

  //sets this HexTile's left down wall to false
  private void breakLeftDown() {
    this.leftDownWall = false;
  }

  //determines if this HexTile has access to the HexTile in a given direction
  boolean canMove(String direction) {
    switch (direction) {
      case "a":
        return !this.leftWall;
      case "d":
        return !this.rightWall;
      case "e":
        return !this.rightUpWall;
      case "x":
        return !this.rightDownWall;
      case "w":
        return !this.leftUpWall;
      case "z":
        return !this.leftDownWall;
      default:
        throw new IllegalArgumentException("Invalid direction: " + direction);
    }
  }

  //gathers all HexTiles which this HexTile has immediate access to
  ArrayList<HexTile> accessibleNeighbors() {
    ArrayList<HexTile> neighbors = new ArrayList<HexTile>();
    if (!this.rightUpWall && this.rightUp != null) {
      neighbors.add(this.rightUp);
    }
    if (!this.leftUpWall && this.leftUp != null) {
      neighbors.add(this.leftUp);
    }
    if (!this.leftWall && this.left != null) {
      neighbors.add(this.left);
    }
    if (!this.leftDownWall && this.leftDown != null) {
      neighbors.add(this.leftDown);
    }
    if (!this.rightDownWall && this.rightDown != null) {
      neighbors.add(this.rightDown);
    }
    if (!this.rightWall && this.right != null) {
      neighbors.add(this.right);
    }
    return neighbors;
  }
}

//represents a connection between two ATiles
class Edge {
  
  private final ATile topLeft;
  private final ATile botRight; 
  private final int weight;

  Edge(ATile topLeft, ATile botRight, int weight) {
    this.topLeft = topLeft;
    this.botRight = botRight;
    this.weight = weight;
  }

  //randomly sets weights of edges
  Edge(ATile topLeft, ATile botRight) {
    this.topLeft = topLeft;
    this.botRight = botRight;
    this.weight = (int) (Math.random() * 100 * 60);
  }

  //breaks the wall which this Edge represents
  void breakEdge() {
    this.topLeft.breakTile(this.botRight);
    this.botRight.breakTile(this.topLeft);
  }

  //compares this Edge weight and that Edge weight
  int compareWeight(Edge that) {
    return that.compareWeight(this.weight);
  }

  //compares this Edge weight to int thatWeight
  private int compareWeight(int thatWeight) {
    return Integer.compare(thatWeight, this.weight);
  }

  //determines if the ATiles connected by this Edge have the same represetatives
  boolean sameReps(HashMap<ATile, ATile> reps) {
    return this.topLeft.findRep(reps).equals(this.botRight.findRep(reps));
  }

  //sets the representatives of this Edge's ATiles to be the same
  void unionReps(HashMap<ATile, ATile> reps) {
    reps.put(this.topLeft.findRep(reps), this.botRight.findRep(reps));
  }
}

//represents a comparator which compares Edge weights
class WeightComparator implements Comparator<Edge> {
  //determines if Edge e1 has greater or equal weight that Edge e2
  public int compare(Edge e1, Edge e2) {
    return e1.compareWeight(e2);
  }
}

//represents an abstract maze of ATiles
abstract class AMaze {
  //displays the shortest path from the top left ATile to the bottom right ATile
  abstract void showShortestPath();

  //Renders this AMaze as a WorldImage
  abstract WorldImage render();

  //moves the current tile in a given direction
  abstract void move(String s);

  //determines if this AMaze has been solved
  abstract boolean won();

  //traverses this AMaze breadth first
  abstract void bfsTick();

  //traverses this AMaze depth first
  abstract void dfsTick();

  //traverses this maze by greedily moving left
  abstract void stickLeftTick();
}

//represents a maze consisting of RectTiles
class RectMaze extends AMaze {
  private final int width;
  private final int height;
  private final int tileSize;
  //the grid must be reassigned when creating the heat map
  private ArrayList<ArrayList<RectTile>> grid;
  private final ArrayList<RectTile> shortestPath;
  //the x and y positions are changing with the current position
  private int xPos;
  private int yPos;
  
  private final ArrayList<RectTile> workList;
  private final ArrayList<RectTile> seenList;
  //changes when the maze has been solved
  private boolean hasWon;
  private final HashMap<RectTile, Integer> timeIn;

  //changes as walls are encountered
  private String leftHand;
  
  RectMaze(int width, int height, int tileSize) {
    if (width > 100 || width < 1) {
      throw new IllegalArgumentException("Width must be between 1 and 100");
    }
    if (height > 60 || height < 1) {
      throw new IllegalArgumentException("Height must be between 1 and 60");
    }
    this.width = width;
    this.height = height;
    this.tileSize = tileSize;
    this.grid = this.buildTiles();
    this.breakTreeWalls();
    this.xPos = 0;
    this.yPos = 0;
    this.workList = new ArrayList<RectTile>();
    this.seenList = new ArrayList<RectTile>();
    this.hasWon = false;
    this.leftHand = "a";
    this.timeIn = new HashMap<>();
    this.reconstructGrid(1);
    this.grid.get(0).get(0).moveTo();
    this.shortestPath = new ArrayList<RectTile>();
    this.shortestPath.add(grid.get(0).get(0));
  }

  //creates the RectMaze given the grid
  RectMaze(ArrayList<ArrayList<RectTile>> grid) {
    if (grid.size() > 60 || grid.size() < 1) {
      throw new IllegalArgumentException("Height must be between 1 and 60");
    }
    if (grid.get(0).size() > 100 || grid.get(0).size() < 1) {
      throw new IllegalArgumentException("Width must be between 1 and 100");
    }
    this.timeIn = new HashMap<>();
    this.width = grid.get(0).size();
    this.height = grid.size();
    this.tileSize = Math.min(1500 / this.width, 800 / this.height);
    this.grid = grid;
    this.breakTreeWalls();
    this.xPos = 0;
    this.yPos = 0;
    this.grid.get(0).get(0).moveTo();
    this.shortestPath = new ArrayList<RectTile>();
    this.shortestPath.add(grid.get(0).get(0));
    this.workList = new ArrayList<RectTile>();
    this.workList.add(this.grid.get(0).get(0));
    this.seenList = new ArrayList<RectTile>();
    this.hasWon = false;
    this.leftHand = "a";
  }

  //formulates the grid of RectTiles
  private ArrayList<ArrayList<RectTile>> buildTiles() {
    ArrayList<ArrayList<RectTile>> tiles = new ArrayList<ArrayList<RectTile>>();
    //iterates through rows desired
    for (int row = 0; row < this.height; row++) {
      ArrayList<RectTile> acc = new ArrayList<RectTile>();
      //iterates through columns in row
      for (int col = 0; col < this.width; col++) {
        RectTile tile;
        if (row == 0 && col == 0) {
          tile = new RectTile(new Color(31, 128, 70));
        } else if (row == this.height - 1 && col == this.width - 1) {
          tile = new RectTile(new Color(106, 34, 128));
        } else {
          tile = new RectTile();
        }
        acc.add(tile);
      }
      tiles.add(acc);
    }
    //sets the RectTile's neighbors
    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {
        RectTile t = tiles.get(row).get(col);
        if (row != 0) {
          t.setUp(tiles.get(row - 1).get(col));
        }
        if (row != this.height - 1) {
          t.setDown(tiles.get(row + 1).get(col));
        }
        if (col != 0) {
          t.setLeft(tiles.get(row).get(col - 1));
        }
        if (col != this.width - 1) {
          t.setRight(tiles.get(row).get(col + 1));
        }
      }
    }
    return tiles;
  }

  //gets all of the edges between RectTiles in this RectMaze
  private ArrayList<Edge> getEdges() {
    ArrayList<Edge> edges = new ArrayList<Edge>();
    //iterates through the rows in this RectMaze
    for (int row = 0; row < this.height; row++) {
      //iterates through the columns in the row
      for (int col = 0; col < this.width; col++) {
        if (col != this.width - 1) {
          edges.add(new Edge(this.grid.get(row).get(col),
                  this.grid.get(row).get(col + 1)));
        }
        if (row != this.height - 1) {
          edges.add(new Edge(this.grid.get(row).get(col),
                  this.grid.get(row + 1).get(col)));
        }
      }
    }
    return edges;
  }

  //uses Kruskal's algorithm to get the minimum spanning tree (maze)
  private ArrayList<Edge> buildTree() {
    ArrayList<Edge> edges = this.getEdges();
    Collections.sort(edges, new WeightComparator());
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    
    HashMap<ATile, ATile> representatives = new HashMap<ATile, ATile>();
    //iterates through the rows in this RectMaze grid
    for (ArrayList<RectTile> row : grid) {
      //iterates through the tiles in the row, setting tile representatives to themselves
      for (RectTile t : row) {
        representatives.put(t, t);
      }
    }
    //continue until we have processed all edges in the tree
    while (!edges.isEmpty()) {
      Edge currEdge = edges.remove(0);
      if (!currEdge.sameReps(representatives)) {
        edgesInTree.add(currEdge);
        currEdge.unionReps(representatives);
      }
    }
    
    return edgesInTree;
  }

  //breaks all the walls corresponding to edges in the min spanning tree
  private void breakTreeWalls() {
    ArrayList<Edge> tree = this.buildTree();
    //break the wall between all MST edges
    for (Edge edge : tree) {
      edge.breakEdge();
    }
  }

  //renders this RectMaze as a WorldImage
  WorldImage render() {
    WorldImage img = new EmptyImage();
    //iterates through the rows in this RectMaze's grid
    for (ArrayList<RectTile> row : this.grid) {
      WorldImage rowImg = new EmptyImage();
      //iterates through all the tiles in the row
      for (RectTile t : row) {
        rowImg = new BesideImage(rowImg, t.render(this.tileSize));
      }
      img = new AboveImage(img, rowImg);
    }
    return img;
  }

  //moves the current position to a given adjacent and accessible position
  void move(String s) {
    int dx = 0;
    int dy = 0;
    boolean validDirection;
    switch (s) {
      case "w":
      case "up":
        validDirection = true;
        dy--;
        break;
      case "s":
      case "down":
        validDirection = true;
        dy++;
        break;
      case "d":
      case "right":
        validDirection = true;
        dx++;
        break;
      case "a":
      case "left":
        validDirection = true;
        dx--;
        break;
      default:
        validDirection = false;
        break;
    }
    if (validDirection && this.grid.get(this.yPos).get(this.xPos).canMove(s)) {
      RectTile oldTile = this.grid.get(this.yPos).get(this.xPos);
      this.xPos += dx;
      this.yPos += dy;
      RectTile newTile = this.grid.get(this.yPos).get(this.xPos);
      oldTile.moveFrom();
      newTile.moveTo();
      if (this.shortestPath.size() > 1 && this.shortestPath.get(1).equals(newTile)) {
        this.shortestPath.remove(0);
      } else {
        shortestPath.add(0, newTile);
      }
    }
  }

  //determines if this RectMaze has been solved
  boolean won() {
    this.hasWon = this.hasWon || this.xPos == this.width - 1 && this.yPos == this.height - 1;
    return this.hasWon;
  }

  //displays the shortest path from start to end of this RectMaze
  void showShortestPath() {
    //iterates through the shortestPath ArrayList
    for (RectTile t : this.shortestPath) {
      t.moveTo();
    }
  }

  //traverses this RectMaze depth first
  void dfsTick() {
    if (!this.workList.isEmpty()) {
      RectTile curr = workList.remove(0);
      if (curr == this.grid.get(height - 1).get(width - 1)) {
        this.hasWon = true;
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
      } else if(this.seenList.contains(curr)) {
        this.dfsTick();
      } else {
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
        //adds all adjacent accessible neighbors to the head of the worklist
        for (RectTile neighbor : curr.accessibleNeighbors()) {
          this.workList.add(0, neighbor);
        }
        this.seenList.add(0, curr);
      }
    }
  }

  //calculates the time in which a tile is accessed
  void dfsTimeIn() {
    int timer = 1;
    //continues traversing until we have accessed every tile
    while (!this.workList.isEmpty()
            && this.seenList.size() < this.grid.size() * this.grid.get(0).size()) {
      RectTile curr = workList.remove(0);
      if (!this.seenList.contains(curr)) {
        //new tile, dfs
        this.timeIn.put(curr, timer);
        timer += 1;
        //adds all adjacent and accessible RectTile neighbors to the head of the worklist
        for (RectTile neighbor : curr.accessibleNeighbors()) {
          this.workList.add(0, neighbor);
        }
        this.seenList.add(0, curr);
      }
    }
  }

  //creates and colors the grid to represent a heatmap of which tiles get accessed first
  void reconstructGrid(int startFrom) {
    //if start from is ge 0, then start from start
    //else start from end
    if (startFrom >= 0) {
      this.workList.add(this.grid.get(0).get(0));
    } else {
      this.workList.add(this.grid.get(this.grid.size() - 1)
              .get(this.grid.get(0).size() - 1));
    }

    ArrayList<ArrayList<RectTile>> tiles = new ArrayList<ArrayList<RectTile>>();
    this.dfsTimeIn();
    this.workList.clear();
    this.seenList.clear();
    for (int row = 0; row < this.height; row++) {
      ArrayList<RectTile> acc = new ArrayList<RectTile>();
      for (int col = 0; col < this.width; col++) {
        RectTile tile;
        if (row == 0 && col == 0) {
          tile = new RectTile(new Color(31, 128, 70));
        } else if (row == this.height - 1 && col == this.width - 1) {
          tile = new RectTile(new Color(106, 34, 128));
        } else {
          int currTimeIn = this.timeIn.get(this.grid.get(row).get(col));
          //divide by 24 to account for max time in (100 x 60)
          int redBlue = currTimeIn / 24;
          tile = new RectTile(new Color(255 - redBlue, 0, redBlue));
        }
        acc.add(tile);
      }
      tiles.add(acc);
    }
    //iterates and sets neighbors
    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {
        RectTile t = tiles.get(row).get(col);
        if (row != 0) {
          t.setUp(tiles.get(row - 1).get(col));
        }
        if (row != this.height - 1) {
          t.setDown(tiles.get(row + 1).get(col));
        }
        if (col != 0) {
          t.setLeft(tiles.get(row).get(col - 1));
        }
        if (col != this.width - 1) {
          t.setRight(tiles.get(row).get(col + 1));
        }
      }
    }
    this.grid = tiles;
    this.workList.add(this.grid.get(0).get(0));
    this.breakTreeWalls();
  }


  //traverses through this RectMaze breadth first
  void bfsTick() {
    if (!this.workList.isEmpty()) {
      RectTile curr = workList.remove(0);
      if (curr == this.grid.get(height - 1).get(width - 1)) {
        this.hasWon = true;
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
      } else if(this.seenList.contains(curr)){
        this.bfsTick();
      } else {
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
        //adds all accessible neighbors to the tail of the worklist
        for (int i = curr.accessibleNeighbors().size() - 1; i >= 0; i--) {
          RectTile neighbor = curr.accessibleNeighbors().get(i);
          this.workList.add(neighbor);
        }
        this.seenList.add(0, curr);
      }
    }
  }

  //traverses this RectMaze by going left whenever possible
  void stickLeftTick() {
    RectTile currTile = this.grid.get(yPos).get(xPos);
    if (currTile.canMove(this.leftHand)) {
      this.move(this.leftHand);
      this.rotateLeft();
    } else {
      this.rotateRight();
      this.stickLeftTick();
    }
  }

  //changes the direction of the current RectTile so that we can continue to traverse left
  private void rotateLeft() {
    switch (this.leftHand) {
      case "a":
        this.leftHand = "s";
        break;
      case "s":
        this.leftHand = "d";
        break;
      case "d":
        this.leftHand = "w";
        break;
      case "w":
        this.leftHand = "a";
        break;
      default:
        throw new IllegalArgumentException("Invalid direction: " + leftHand);
    }
  }

  //changes the direction of the current RectTile so that we can continue to traverse left
  private void rotateRight() {
    switch (this.leftHand) {
      case "a":
        this.leftHand = "w";
        break;
      case "s":
        this.leftHand = "a";
        break;
      case "d":
        this.leftHand = "s";
        break;
      case "w":
        this.leftHand = "d";
        break;
      default:
        throw new IllegalArgumentException("Invalid direction: " + leftHand);
    }
  }
}

//represents a maze consisting of HexTiles
class HexMaze extends AMaze {
  private final int sideLength;
  private final int tileSize;
  private final ArrayList<ArrayList<HexTile>> grid;
  private final ArrayList<HexTile> shortestPath;
  //these fields will be updated as we move our current HexTile
  private int rowPos;
  private int colPos;
  
  private final ArrayList<HexTile> workList;
  private final ArrayList<HexTile> seenList;
  //this field changes as we determine if we have won or not
  private boolean hasWon;

  //this field changes as we rotate or HexTile orientation
  private String leftHand;
  
  HexMaze(int sideLength, int tileSize) {
    if (sideLength > 10 || sideLength < 1) {
      throw new IllegalArgumentException("Side length must be between 1 and 10");
    }
    this.sideLength = sideLength;
    this.tileSize = tileSize;
    this.grid = this.buildTiles();
    this.breakTreeWalls();
    this.rowPos = 0;
    this.colPos = 0;
    this.grid.get(0).get(0).moveTo();
    this.shortestPath = new ArrayList<HexTile>();
    this.shortestPath.add(grid.get(0).get(0));
    this.workList = new ArrayList<HexTile>();
    this.workList.add(this.grid.get(0).get(0));
    this.seenList = new ArrayList<HexTile>();
    this.hasWon = false;
    this.leftHand = "a";
  }
  
//  RectMaze(ArrayList<ArrayList<RectTile>> grid) {
//    if (grid.size() > 60 || grid.size() < 0) {
//      throw new IllegalArgumentException("Height must be between 0 and 60");
//    }
//    if (grid.get(0).size() > 100 || grid.get(0).size() < 0) {
//      throw new IllegalArgumentException("Width must be between 0 and 100");
//    }
//    this.width = grid.get(0).size();
//    this.height = grid.size();
//    this.tileSize = Math.min(1500 / this.width, 800 / this.height);
//    this.grid = grid;
//    this.breakTreeWalls();
//    this.xPos = 0;
//    this.yPos = 0;
//    this.grid.get(0).get(0).moveTo();
//    this.shortestPath = new ArrayList<RectTile>();
//    this.shortestPath.add(grid.get(0).get(0));
//  }

  //formulates the grid of HexTiles which comprise this HexMaze
  private ArrayList<ArrayList<HexTile>> buildTiles() {
    ArrayList<ArrayList<HexTile>> tiles = new ArrayList<ArrayList<HexTile>>();
    int maxRows = this.sideLength * 2 - 1;
    //iterates through the desired rows
    for (int row = 0; row < maxRows; row++) {
      ArrayList<HexTile> acc = new ArrayList<HexTile>();
      int maxCols;
      if (row < this.sideLength) {
        maxCols = this.sideLength + row;
      } else {
        maxCols = 3 * this.sideLength - 2 - row;
      }
      //iterates through the desired columns
      for (int col = 0; col < maxCols; col++) {
        HexTile tile;
        if (row == 0 && col == 0) {
          tile = new HexTile(new Color(31, 128, 70));
        } else if (row == maxRows - 1 && col == maxCols - 1) {
          tile = new HexTile(new Color(106, 34, 128));
        } else {
          tile = new HexTile();
        }
        acc.add(tile);
      }
      tiles.add(acc);
    }
    for (int row = 0; row < maxRows; row++) {
      int maxCols;
      if (row < this.sideLength) {
        maxCols = this.sideLength + row;
      } else {
        maxCols = 3 * this.sideLength - 2 - row;
      }
      //iterates through and sets neighbors
      for (int col = 0; col < maxCols; col++) {
        HexTile t = tiles.get(row).get(col);
        if (col != 0) {
          t.setLeft(tiles.get(row).get(col - 1));
        }
        if (col != maxCols - 1) {
          t.setRight(tiles.get(row).get(col + 1));
        }
        if (row != 0 && (col != maxCols - 1 || row >= this.sideLength)) {
          if (row < this.sideLength) {
            t.setRightUp(tiles.get(row - 1).get(col));
          } else {
            t.setRightUp(tiles.get(row - 1).get(col + 1));
          }
        }
        if (row != 0 && (col != 0 || row >= this.sideLength)) {
          if (row < this.sideLength) {
            t.setLeftUp(tiles.get(row - 1).get(col - 1));
          } else {
            t.setLeftUp(tiles.get(row - 1).get(col));
          }
        }
        if (row != maxRows - 1 && (col != maxCols - 1 || row < this.sideLength - 1)) {
          if (row >= this.sideLength - 1) {
            t.setRightDown(tiles.get(row + 1).get(col));
          } else {
            t.setRightDown(tiles.get(row + 1).get(col + 1));
          }
        }
        if (row != maxRows - 1 && (col != 0 || row < this.sideLength - 1)) {
          if (row >= this.sideLength - 1) {
            t.setLeftDown(tiles.get(row + 1).get(col - 1));
          } else {
            t.setLeftDown(tiles.get(row + 1).get(col));
          }
        }
      }
    }
    return tiles;
  }

  //gets all of the Edges between any two HexTiles in the HexMaze
  private ArrayList<Edge> getEdges() {
    ArrayList<Edge> edges = new ArrayList<Edge>();
    int maxRows = this.sideLength * 2 - 1;
    //iterates through rows
    for (int row = 0; row < maxRows; row++) {
      int maxCols;
      if (row < this.sideLength) {
        maxCols = this.sideLength + row;
      } else {
        maxCols = 3 * this.sideLength - 2 - row;
      }
      //iteartes through columns and formulates edges
      for (int col = 0; col < maxCols; col++) {
        if (col != maxCols - 1) {
          edges.add(new Edge(this.grid.get(row).get(col),
              this.grid.get(row).get(col + 1)));
        }
        if (row != maxRows - 1 && (col != maxCols - 1 || row < this.sideLength - 1)) {
          if (row >= this.sideLength - 1) {
            edges.add(new Edge(grid.get(row).get(col),
                grid.get(row + 1).get(col)));
          } else {
            edges.add(new Edge(grid.get(row).get(col),
                grid.get(row + 1).get(col + 1)));
          }
        }
        if (row != maxRows - 1 && (col != 0 || row < this.sideLength - 1)) {
          if (row >= this.sideLength - 1) {
            edges.add(new Edge(grid.get(row).get(col),
                this.grid.get(row + 1).get(col - 1)));
          } else {
            edges.add(new Edge(grid.get(row).get(col),
                this.grid.get(row + 1).get(col)));
          }
        }
      }
    }
    return edges;
  }

  //uses Kruskal's algorithm to gather the edges in the minimum spanning tree (maze)
  private ArrayList<Edge> buildTree() {
    ArrayList<Edge> edges = this.getEdges();
    Collections.sort(edges, new WeightComparator());
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    
    HashMap<ATile, ATile> representatives = new HashMap<ATile, ATile>();
    //iterates through and sets HexTile representatives to themselves
    for (ArrayList<HexTile> row : grid) {
      for (HexTile t : row) {
        representatives.put(t, t);
      }
    }
    //provess all edges in maze and formulates MST
    while (!edges.isEmpty()) {
      Edge currEdge = edges.remove(0);
      if (!currEdge.sameReps(representatives)) {
        edgesInTree.add(currEdge);
        currEdge.unionReps(representatives);
      }
    }
    
    return edgesInTree;
  }

  //breaks all the walls corresponding to edges in the MST
  private void breakTreeWalls() {
    ArrayList<Edge> tree = this.buildTree();
    //iterates through the edges in MST and breaks corresponding walls
    for (Edge edge : tree) {
      edge.breakEdge();
    }
  }

  //renders this HexMaze as a WorldImage
  WorldImage render() {
    WorldImage img = new EmptyImage();
    //iterates thrugh the tiles in the first row
    for (HexTile t : this.grid.get(0)) {
      img = new BesideImage(img, t.render(this.tileSize));
    }
    //iterates through the tiles in the grid
    for (int row = 1; row < this.grid.size(); row ++) {
      WorldImage rowImg = new EmptyImage();
      ArrayList<HexTile> rowTiles = this.grid.get(row);
      for (HexTile t : rowTiles) {
        rowImg = new BesideImage(rowImg, t.render(this.tileSize));
      }
      double currHeight = img.getHeight();
      double nextHeight = currHeight + 1.5 * this.tileSize;
      double shift = nextHeight - (currHeight / 2) - (rowImg.getHeight() / 2);
      img = new OverlayOffsetImage(img, 0, shift - 1, rowImg);
    }
    return img;
  }

  //moves the current position to a given accessible position
  void move(String s) {
    int drow = 0;
    int dcol = 0;
    boolean validDirection;
    int maxRows = this.sideLength * 2 - 1;
    int maxCols;
    if (rowPos < this.sideLength) {
      maxCols = this.sideLength + rowPos;
    } else {
      maxCols = 3 * this.sideLength - 2 - rowPos;
    }
    switch (s) {
      case "a":
        validDirection = true;
        dcol--;
        break;
      case "d":
        validDirection = true;
        dcol++;
        break;
      case "e":
        validDirection = true;
        if (rowPos != 0 && (colPos != maxCols - 1 || rowPos >= this.sideLength)) {
          if (rowPos < this.sideLength) {
            drow--;
          } else {
            drow--;
            dcol++;
          }
        }
        break;
      case "x":
        validDirection = true;
        if (rowPos != maxRows - 1 && (colPos != maxCols - 1 || rowPos < this.sideLength - 1)) {
          if (rowPos >= this.sideLength - 1) {
            drow++;
          } else {
            drow++;
            dcol++;
          }
        }
        break;
      case "w":
        validDirection = true;
        if (rowPos != 0 && (colPos != 0 || rowPos >= this.sideLength)) {
          if (rowPos < this.sideLength) {
            drow--;
            dcol--;
          } else {
            drow--;
          }
        }
        break;
      case "z":
        validDirection = true;
        if (rowPos != maxRows - 1 && (colPos != 0 || rowPos < this.sideLength - 1)) {
          if (rowPos >= this.sideLength - 1) {
            drow++;
            dcol--;
          } else {
            drow++;
          }
        }
        break;
      default:
        validDirection = false;
        break;
    }
    if (validDirection && this.grid.get(this.rowPos).get(this.colPos).canMove(s)) {
      HexTile oldTile = this.grid.get(this.rowPos).get(this.colPos);
      this.colPos += dcol;
      this.rowPos += drow;
      HexTile newTile = this.grid.get(this.rowPos).get(this.colPos);
      oldTile.moveFrom();
      newTile.moveTo();
      if (this.shortestPath.size() > 1 && this.shortestPath.get(1).equals(newTile)) {
        this.shortestPath.remove(0);
      } else {
        shortestPath.add(0, newTile);
      }
    }
  }

  //determines if this HexMaze has been solved
  boolean won() {
    this.hasWon = this.hasWon || this.rowPos == this.grid.size() - 1
            && this.colPos == this.grid.get(this.grid.size() - 1).size() - 1;
    return this.hasWon;
  }

  //displays the shortest path from start HexTile to end HexTile
  void showShortestPath() {
    for (HexTile t : this.shortestPath) {
      t.moveTo();
    }
  }

  //traverses this HexTile depth first
  void dfsTick() {
    if (!this.workList.isEmpty()) {
      HexTile curr = workList.remove(0);
      if (curr == this.grid.get(this.grid.size() - 1).get(this.grid.get(this.grid.size() - 1).size() - 1)) {
        this.hasWon = true;
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
      } else if(this.seenList.contains(curr)){
        this.dfsTick();
      } else {
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
        //traverses the accessible neighbors of curr and adds them to the head of worklist
        for (HexTile neighbor : curr.accessibleNeighbors()) {
          this.workList.add(0, neighbor);
        }
        this.seenList.add(0, curr);
      }
    }
  }

  //traverses the HexMaze breadth first
  void bfsTick() {
    if (!this.workList.isEmpty()) {
      HexTile curr = workList.remove(0);
      if (curr == this.grid.get(this.grid.size() - 1).get(this.grid.get(this.grid.size() - 1).size() - 1)) {
        this.hasWon = true;
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
      } else if(this.seenList.contains(curr)) {
        this.bfsTick();
      } else {
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
        //traverses the accessible neighbors of curr and adds them to the tail of worklist
        for (int i = curr.accessibleNeighbors().size() - 1; i >= 0; i--) {
          HexTile neighbor = curr.accessibleNeighbors().get(i);
          this.workList.add(neighbor);
        }
        this.seenList.add(0, curr);
      }
    }
  }
  
  // TODO: fix
  void stickLeftTick() {
    HexTile currTile = this.grid.get(rowPos).get(colPos);
    if (currTile.canMove(this.leftHand)) {
      this.move(this.leftHand);
      this.rotateLeft();
    } else {
      this.rotateRight();
      this.stickLeftTick();
    }
  }

  //rotates the orientation to continue moving left
  private void rotateLeft() {
    switch (this.leftHand) {
      case "a":
        this.leftHand = "z";
        break;
      case "z":
        this.leftHand = "x";
        break;
      case "x":
        this.leftHand = "d";
        break;
      case "d":
        this.leftHand = "e";
        break;
      case "e":
        this.leftHand = "w";
        break;
      case "w":
        this.leftHand = "a";
        break;
      default:
        throw new IllegalArgumentException("Invalid direction: " + leftHand);
    }
  }

  //rotates the orientation to continue moving left
  private void rotateRight() {
    switch (this.leftHand) {
      case "a":
        this.leftHand = "w";
        break;
      case "z":
        this.leftHand = "a";
        break;
      case "x":
        this.leftHand = "z";
        break;
      case "d":
        this.leftHand = "x";
        break;
      case "e":
        this.leftHand = "d";
        break;
      case "w":
        this.leftHand = "e";
        break;
      default:
        throw new IllegalArgumentException("Invalid direction: " + leftHand);
    }
  }
}

//represents the game of solving the maze
class Game extends World {
  
  private final AMaze maze;
  private final int width;
  private final int height;
  private final int tileSize;
  
  Game(int width, int height) {
    this.tileSize = Math.min(1400 / width, 700 / height);
    this.maze = new RectMaze(width, height, this.tileSize);
    this.width = width * this.tileSize;
    this.height = height * this.tileSize;
  }
  
  Game(int sideLength) {
    this.tileSize = 250 / sideLength;
    this.maze = new HexMaze(sideLength, this.tileSize);
    this.width = (sideLength * 2 - 2) * this.tileSize * 2;
    this.height = (sideLength * 2 - 2) * this.tileSize * 2;
  }

  //renders the game as a WorldScene
  public WorldScene makeScene() {
    WorldImage mazeImage = this.maze.render();
    WorldScene scene = new WorldScene(this.width + this.tileSize, this.height + this.tileSize);
    scene.placeImageXY(mazeImage, (this.width + this.tileSize) / 2, (this.height + this.tileSize) / 2);
    return scene;
  }

  //moves the current tile given a key command
  public void onKeyEvent(String key) {
    this.maze.move(key);
  }

  //determines if the game should end
  public boolean shouldWorldEnd() {
    return this.maze.won();
  }

  //traverses per tick
  public void onTick() {
    this.maze.dfsTick();
  }

  //displays a scene after the game has ended
  public WorldScene lastScene(String msg) {
    this.maze.showShortestPath();
    WorldImage mazeImage = maze.render();
    WorldScene scene = new WorldScene(this.width + this.tileSize, this.height + this.tileSize);
    scene.placeImageXY(mazeImage, (this.width + this.tileSize) / 2, (height + this.tileSize) / 2);
    return scene;
  }
}

class ExamplesMazes {
  Game m = new Game(50, 50);

 /* void testStuff(Tester t) {
    m.bigBang(1500, 800, .1);
  }*/

  boolean testATileAndEdge(Tester t) {
    ATile middle = new RectTile();
    ATile left = new RectTile(Color.RED);
    ATile right = new RectTile();
    ATile up = new RectTile();
    ATile down = new RectTile();
    ATile hex = new HexTile();
    HashMap<ATile, ATile> reps = new HashMap<>();
    reps.put(middle, middle);
    reps.put(left, left);
    reps.put(right, middle);
    reps.put(up, right);
    reps.put(down, down);

    boolean testFindRep = t.checkExpect(middle.findRep(reps),
            middle)
            && t.checkExpect(down.findRep(reps),
            down)
            && t.checkExpect(up.findRep(reps),
            middle)
            && t.checkExpect(right.findRep(reps),
            middle);

    up.moveTo();
    down.moveFrom();

    boolean testMoveToFrom = t.checkExpect(up.tileColor,
            new Color(61, 118, 204))
            && t.checkExpect(down.tileColor,
            new Color(145, 184, 242));

    return testFindRep && testMoveToFrom;
  }

  boolean testRectTile(Tester t) {
    RectTile middle = new RectTile();
    RectTile left = new RectTile(Color.RED);
    RectTile right = new RectTile();
    RectTile up = new RectTile();
    RectTile down = new RectTile();

    middle.setUp(up);
    up.setDown(middle);
    middle.setLeft(left);
    left.setRight(middle);
    middle.setRight(right);
    right.setLeft(middle);
    middle.setDown(down);
    down.setUp(middle);

    middle.breakTile(left);
    left.breakTile(middle);
    up.breakRect(middle);
    middle.breakRect(up);

    boolean testSettingAndBreakingAndCanMove = t.checkExpect(middle.canMove("up"),
            true)
            && t.checkExpect(middle.canMove("left"), true)
            && t.checkExpect(middle.canMove("right"), false)
            && t.checkExpect(middle.canMove("down"), false);

    boolean checkExceptions = t.checkException(new IllegalArgumentException(
            "Invalid direction: hi"), middle, "canMove", "hi")
            && t.checkException(
                    new IllegalArgumentException("Tile is not a neighbor"),
            left, "breakRect", right);

    boolean testAccessibleNeighbors = t.checkExpect(middle.accessibleNeighbors(),
            new ArrayList<>(Arrays.asList(left, up)))
            && t.checkExpect(left.accessibleNeighbors(),
            new ArrayList<>(List.of(middle)))
            && t.checkExpect(down.accessibleNeighbors(),
            new ArrayList<>());

    return testSettingAndBreakingAndCanMove && checkExceptions && testAccessibleNeighbors;
  }

  boolean EdgeAndWeightComparator(Tester t) {
    RectTile middle = new RectTile();
    RectTile left = new RectTile(Color.RED);
    RectTile right = new RectTile();
    RectTile up = new RectTile();
    RectTile down = new RectTile();

    middle.setUp(up);
    up.setDown(middle);
    middle.setLeft(left);
    left.setRight(middle);
    middle.setRight(right);
    right.setLeft(middle);
    middle.setDown(down);
    down.setUp(middle);

    HashMap<ATile, ATile> reps = new HashMap<>();
    reps.put(middle, middle);
    reps.put(left, left);
    reps.put(right, middle);
    reps.put(up, right);
    reps.put(down, down);

    Edge e1 = new Edge(right, middle, 10);
    Edge e2 = new Edge(left, middle, 5);
    Edge e3 = new Edge(right, up, 5);

    e1.breakEdge();
    e2.breakEdge();

    boolean testBreakEdge = t.checkExpect(right.canMove("left"),
            true)
            && t.checkExpect(middle.canMove("right"),
            true)
            && t.checkExpect(left.canMove("right"),
            true)
            && t.checkExpect(middle.canMove("left"),
            true)
            && t.checkExpect(middle.canMove("up"),
            false);

    boolean testCompareWeight = t.checkExpect(e1.compareWeight(e2),
            1)
            && t.checkExpect(e2.compareWeight(e1),
            -1)
            && t.checkExpect(e2.compareWeight(e3),
            0);

    boolean testFind = t.checkExpect(e1.sameReps(reps),
            true)
            && t.checkExpect(e2.sameReps(reps),
            false);

    e2.unionReps(reps);

    boolean testUnion = t.checkExpect(e2.sameReps(reps),
            true);

    HexTile mid = new HexTile();
    HexTile l = new HexTile();
    HexTile r = new HexTile();
    HexTile rightup = new HexTile();
    HexTile rightdown = new HexTile();
    HexTile leftup = new HexTile();
    HexTile leftdown = new HexTile();

    mid.setLeft(l);
    l.setRight(mid);
    mid.setLeftUp(leftup);
    leftup.setRightDown(mid);
    mid.setLeftDown(leftdown);
    leftdown.setRightUp(mid);
    mid.setRight(r);
    r.setLeft(mid);
    mid.setRightDown(rightdown);
    rightdown.setLeftUp(mid);
    mid.setRightUp(rightup);
    rightup.setLeftDown(mid);

    HashMap<ATile, ATile> hexReps = new HashMap<>();
    hexReps.put(mid, mid);
    hexReps.put(l, mid);
    hexReps.put(r, l);
    hexReps.put(leftup, leftup);

    Edge hexEdge1 = new Edge(mid, l, 10);
    Edge hexEdge2 = new Edge(mid, leftup, 10);
    Edge hexEdge3 = new Edge(mid, right, 100);

    hexEdge1.breakEdge();
    hexEdge2.breakEdge();

    boolean testHexBreakEdge = t.checkExpect(mid.canMove("a"),
            true)
            && t.checkExpect(l.canMove("d"),
            true)
            && t.checkExpect(mid.canMove("w"),
            true)
            && t.checkExpect(leftup.canMove("x"),
            true)
            && t.checkExpect(mid.canMove("x"),
            false);

    boolean testCompareHexWeight = t.checkExpect(hexEdge1.compareWeight(hexEdge2),
            0)
            && t.checkExpect(hexEdge1.compareWeight(hexEdge3),
            -1)
            && t.checkExpect(hexEdge3.compareWeight(hexEdge1),
            1);

    boolean testFindHex = t.checkExpect(hexEdge1.sameReps(hexReps),
            true)
            && t.checkExpect(hexEdge2.sameReps(hexReps),
            false);

    hexEdge2.unionReps(hexReps);

    boolean testUnionHex = t.checkExpect(hexEdge2.sameReps(hexReps),
            true);

    WeightComparator comp = new WeightComparator();
    boolean testComp = t.checkExpect(comp.compare(hexEdge1, hexEdge2),
            0)
            && t.checkExpect(comp.compare(hexEdge3, hexEdge2),
            1)
            && t.checkExpect(comp.compare(hexEdge2, hexEdge3),
            -1)
            && t.checkExpect(comp.compare(e1, e2),
            1)
            && t.checkExpect(comp.compare(e2, e3),
            0)
            && t.checkExpect(comp.compare(e2, e1),
            -1);

    return testBreakEdge && testCompareWeight && testFind
            && testUnion && testHexBreakEdge && testCompareHexWeight
            && testFindHex && testUnionHex && testComp;
  }

  boolean testHexTile(Tester t) {
    HexTile middle = new HexTile();
    HexTile left = new HexTile();
    HexTile right = new HexTile();
    HexTile rightup = new HexTile();
    HexTile rightdown = new HexTile();
    HexTile leftup = new HexTile();
    HexTile leftdown = new HexTile();

    middle.setLeft(left);
    left.setRight(middle);
    middle.setLeftUp(leftup);
    leftup.setRightDown(middle);
    middle.setLeftDown(leftdown);
    leftdown.setRightUp(middle);
    middle.setRight(right);
    right.setLeft(middle);
    middle.setRightDown(rightdown);
    rightdown.setLeftUp(middle);
    middle.setRightUp(rightup);
    rightup.setLeftDown(middle);

    middle.breakTile(right);
    right.breakTile(middle);
    middle.breakHex(leftdown);
    leftdown.breakHex(middle);

    boolean testSetandBreakByCanMove = t.checkExpect(middle.canMove("a"),
            false)
            && t.checkExpect(middle.canMove("d"), true)
            && t.checkExpect(right.canMove("a"), true)
            && t.checkExpect(middle.canMove("z"), true)
            && t.checkExpect(leftdown.canMove("e"), true)
            && t.checkExpect(rightdown.canMove("w"), false);

    boolean testExceptions = t.checkException(
            new IllegalArgumentException("Invalid direction: 1"),
            middle,
            "canMove",
            "1")
            && t.checkException(
                    new IllegalArgumentException("Tile is not a neighbor"),
            left,
            "breakHex",
            right);

    boolean testAccessibleNeighbors = t.checkExpect(middle.accessibleNeighbors(),
            new ArrayList<>(Arrays.asList(leftdown, right)))
            && t.checkExpect(right.accessibleNeighbors(),
            new ArrayList<>(List.of(middle)))
            && t.checkExpect(leftup.accessibleNeighbors(),
            new ArrayList<>());

    return testSetandBreakByCanMove && testExceptions && testAccessibleNeighbors;
  }

}
