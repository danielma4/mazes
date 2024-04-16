import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javalib.worldimages.*;
import java.awt.Color;
import tester.*;
import javalib.impworld.*;

interface ITile {
  static final Color TILE_COLOR = Color.LIGHT_GRAY;
  static final Color WALL_COLOR = Color.DARK_GRAY;
}

abstract class ATile implements ITile{
  
  protected Color tileColor;
  
  ATile(Color tileColor) {
    this.tileColor = tileColor;
  }
  
  ATile() {
    this.tileColor = ITile.TILE_COLOR;
  }
  
  ATile findRep(HashMap<ATile, ATile> reps) {
    ATile rep = reps.get(this);
    if (rep.equals(this)) {
      return rep;
    } else {
      return rep.findRep(reps);
    }
  }
  
  abstract void breakTile(ATile other);
  
  void breakRect(RectTile other) {
    throw new IllegalArgumentException("Incompatible tile types.");
  }
  
  void breakHex(HexTile other) {
    throw new IllegalArgumentException("Incompatible tile types.");
  }
  
  void moveTo() {
    this.tileColor = new Color(61, 118, 204);
  }
  
  void moveFrom() { 
    this.tileColor = new Color(145, 184, 242);
  }
}

class RectTile extends ATile{
  
  private boolean upWall;
  private boolean downWall;
  private boolean rightWall;
  private boolean leftWall;
  
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
  
  RectTile(Color tileColor) {
    this(true, true, true, true, tileColor);
  }
  
  RectTile() {
    this(true, true, true, true, ITile.TILE_COLOR);
  }
  
  void setUp(RectTile up) {
    this.up = up;
  }
  
  void setDown(RectTile down) {
    this.down = down;
  }
  
  void setRight(RectTile right) {
    this.right = right;
  }
  
  void setLeft(RectTile left) {
    this.left = left;
  }
  
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
  
  String getDirection(RectTile neighbor) {
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
  
  void breakTile(ATile other) {
    other.breakRect(this);
  }
  
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
  
  void breakUp() {
    this.upWall = false;
  }
  
  void breakDown() {
    this.downWall = false;
  }
  
  void breakRight() {
    this.rightWall = false;
  }
  
  void breakLeft() {
    this.leftWall = false;
  }
  
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

class HexTile extends ATile{
  
  private boolean leftWall;;
  private boolean rightWall;
  private boolean rightUpWall;
  private boolean rightDownWall;
  private boolean leftUpWall;
  private boolean leftDownWall;
  
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
  
  HexTile(Color tileColor) {
    this(true, true, true, true, true, true, tileColor);
  }
  
  HexTile() {
    this(true, true, true, true, true, true, ITile.TILE_COLOR);
  }
  
  void setLeft(HexTile left) {
    this.left = left;
  }
  
  void setRight(HexTile right) {
    this.right = right;
  }
  
  void setRightUp(HexTile rightUp) {
    this.rightUp = rightUp;
  }
  
  void setRightDown(HexTile rightDown) {
    this.rightDown = rightDown;
  }
  
  void setLeftUp(HexTile leftUp) {
    this.leftUp = leftUp;
  }
  
  void setLeftDown(HexTile leftDown) {
    this.leftDown = leftDown;
  }
  
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
  String getDirection(HexTile neighbor) {
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
  
  void breakTile(ATile other) {
    other.breakHex(this);
  }
  
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
  
  void breakLeft() {
    this.leftWall = false;
  }
  
  void breakRight() {
    this.rightWall = false;
  }
  
  void breakRightUp() {
    this.rightUpWall = false;
  }
  
  void breakRightDown() {
    this.rightDownWall = false;
  }
  
  void breakLeftUp() {
    this.leftUpWall = false;
  }
  
  void breakLeftDown() {
    this.leftDownWall = false;
  }
  
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

class Edge {
  
  private final ATile topLeft;
  private final ATile botRight; 
  private final int weight;

  Edge(ATile topLeft, ATile botRight, int weight) {
    this.topLeft = topLeft;
    this.botRight = botRight;
    this.weight = weight;
  }

  Edge(ATile topLeft, ATile botRight) {
    this.topLeft = topLeft;
    this.botRight = botRight;
    this.weight = (int) (Math.random() * 100 * 60);
  }
  
  void breakEdge() {
    this.topLeft.breakTile(this.botRight);
    this.botRight.breakTile(this.topLeft);
  }
  
  int compareWeight(Edge that) {
    return that.compareWeight(this.weight);
  }
  
  int compareWeight(int thatWeight) {
    return Integer.compare(thatWeight, this.weight);
  }
  
  boolean sameReps(HashMap<ATile, ATile> reps) {
    return this.topLeft.findRep(reps).equals(this.botRight.findRep(reps));
  }
  
  void unionReps(HashMap<ATile, ATile> reps) {
    reps.put(this.topLeft.findRep(reps), this.botRight.findRep(reps));
  }
}

class WeightComparator implements Comparator<Edge> {
  public int compare(Edge e1, Edge e2) {
    return e1.compareWeight(e2);
  }
}

abstract class AMaze {
  abstract void showShortestPath();
  
  abstract WorldImage render();
  
  abstract void move(String s);
  
  abstract boolean won();

  abstract void bfsTick();
  
  abstract void dfsTick();

  abstract void stickLeftTick();
}

class RectMaze extends AMaze {
  private final int width;
  private final int height;
  private final int tileSize;
  private final ArrayList<ArrayList<RectTile>> grid;
  private final ArrayList<RectTile> shortestPath;
  private int xPos;
  private int yPos;
  
  private final ArrayList<RectTile> workList;
  private final ArrayList<RectTile> seenList;
  private boolean hasWon;
  
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
    this.grid.get(0).get(0).moveTo();
    this.shortestPath = new ArrayList<RectTile>();
    this.shortestPath.add(grid.get(0).get(0));
    this.workList = new ArrayList<RectTile>();
    this.workList.add(this.grid.get(0).get(0));
    this.seenList = new ArrayList<RectTile>();
    this.hasWon = false;
    this.leftHand = "a";
  }
  
  RectMaze(ArrayList<ArrayList<RectTile>> grid) {
    if (grid.size() > 60 || grid.size() < 1) {
      throw new IllegalArgumentException("Height must be between 1 and 60");
    }
    if (grid.get(0).size() > 100 || grid.get(0).size() < 1) {
      throw new IllegalArgumentException("Width must be between 1 and 100");
    }
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
  
  ArrayList<ArrayList<RectTile>> buildTiles() {
    ArrayList<ArrayList<RectTile>> tiles = new ArrayList<ArrayList<RectTile>>();
    for (int row = 0; row < this.height; row++) {
      ArrayList<RectTile> acc = new ArrayList<RectTile>();
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
  
  ArrayList<Edge> getEdges() {
    ArrayList<Edge> edges = new ArrayList<Edge>();
    for (int row = 0; row < this.height; row++) {
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
  
  ArrayList<Edge> buildTree() {
    ArrayList<Edge> edges = this.getEdges();
    Collections.sort(edges, new WeightComparator());
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    
    HashMap<ATile, ATile> representatives = new HashMap<ATile, ATile>();
    for (ArrayList<RectTile> row : grid) {
      for (RectTile t : row) {
        representatives.put(t, t);
      }
    }
    
    while (!edges.isEmpty()) {
      Edge currEdge = edges.remove(0);
      if (!currEdge.sameReps(representatives)) {
        edgesInTree.add(currEdge);
        currEdge.unionReps(representatives);
      }
    }
    
    return edgesInTree;
  }
  
  void breakTreeWalls() {
    ArrayList<Edge> tree = this.buildTree();
    for (Edge edge : tree) {
      edge.breakEdge();
    }
  }
  
  WorldImage render() {
    WorldImage img = new EmptyImage();
    for (ArrayList<RectTile> row : this.grid) {
      WorldImage rowImg = new EmptyImage();
      for (RectTile t : row) {
        rowImg = new BesideImage(rowImg, t.render(this.tileSize));
      }
      img = new AboveImage(img, rowImg);
    }
    return img;
  }
  
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
  
  boolean won() {
    this.hasWon = this.hasWon || this.xPos == this.width - 1 && this.yPos == this.height - 1;
    return this.hasWon;
  }
  
  void showShortestPath() {
    for (RectTile t : this.shortestPath) {
      t.moveTo();
    }
  }
  
  void dfsTick() {
    if (!this.workList.isEmpty()) {
      RectTile curr = workList.remove(0);
      if (curr == this.grid.get(height - 1).get(width - 1)) {
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
        for (RectTile neighbor : curr.accessibleNeighbors()) {
          this.workList.add(0, neighbor);
        }
        this.seenList.add(0, curr);
      }
    }
  }
  
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
        for (int i = curr.accessibleNeighbors().size() - 1; i >= 0; i--) {
          RectTile neighbor = curr.accessibleNeighbors().get(i);
          this.workList.add(neighbor);
        }
        this.seenList.add(0, curr);
      }
    }
  }
  
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
  
  void rotateLeft() {
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
  
  void rotateRight() {
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

class HexMaze extends AMaze {
  private final int sideLength;
  private final int tileSize;
  private final ArrayList<ArrayList<HexTile>> grid;
  private final ArrayList<HexTile> shortestPath;
  private int rowPos;
  private int colPos;
  
  private final ArrayList<HexTile> workList;
  private final ArrayList<HexTile> seenList;
  private boolean hasWon;
  
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
  
  ArrayList<ArrayList<HexTile>> buildTiles() {
    ArrayList<ArrayList<HexTile>> tiles = new ArrayList<ArrayList<HexTile>>();
    int maxRows = this.sideLength * 2 - 1;
    for (int row = 0; row < maxRows; row++) {
      ArrayList<HexTile> acc = new ArrayList<HexTile>();
      int maxCols;
      if (row < this.sideLength) {
        maxCols = this.sideLength + row;
      } else {
        maxCols = 3 * this.sideLength - 2 - row;
      }
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
  
  ArrayList<Edge> getEdges() {
    ArrayList<Edge> edges = new ArrayList<Edge>();
    int maxRows = this.sideLength * 2 - 1;
    for (int row = 0; row < maxRows; row++) {
      int maxCols;
      if (row < this.sideLength) {
        maxCols = this.sideLength + row;
      } else {
        maxCols = 3 * this.sideLength - 2 - row;
      }
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
  
  ArrayList<Edge> buildTree() {
    ArrayList<Edge> edges = this.getEdges();
    Collections.sort(edges, new WeightComparator());
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    
    HashMap<ATile, ATile> representatives = new HashMap<ATile, ATile>();
    for (ArrayList<HexTile> row : grid) {
      for (HexTile t : row) {
        representatives.put(t, t);
      }
    }
    
    while (!edges.isEmpty()) {
      Edge currEdge = edges.remove(0);
      if (!currEdge.sameReps(representatives)) {
        edgesInTree.add(currEdge);
        currEdge.unionReps(representatives);
      }
    }
    
    return edgesInTree;
  }
  
  void breakTreeWalls() {
    ArrayList<Edge> tree = this.buildTree();
    for (Edge edge : tree) {
      edge.breakEdge();
    }
  }
  
  WorldImage render() {
    WorldImage img = new EmptyImage();
    for (HexTile t : this.grid.get(0)) {
      img = new BesideImage(img, t.render(this.tileSize));
    }
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
  
  boolean won() {
    this.hasWon = this.hasWon || this.rowPos == this.grid.size() - 1 && this.colPos == this.grid.get(this.grid.size() - 1).size() - 1;
    return this.hasWon;
  }
  
  void showShortestPath() {
    for (HexTile t : this.shortestPath) {
      t.moveTo();
    }
  }
  
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
        for (HexTile neighbor : curr.accessibleNeighbors()) {
          this.workList.add(0, neighbor);
        }
        this.seenList.add(0, curr);
      }
    }
  }
  
  void bfsTick() {
    if (!this.workList.isEmpty()) {
      HexTile curr = workList.remove(0);
      if (curr == this.grid.get(this.grid.size() - 1).get(this.grid.get(this.grid.size() - 1).size() - 1)) {
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
  
  void rotateLeft() {
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
  
  void rotateRight() {
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

class Game extends World {
  
  private final AMaze maze;
  private int width;
  private int height;
  private int tileSize;
  
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
  
  public WorldScene makeScene() {
    WorldImage mazeImage = this.maze.render();
    WorldScene scene = new WorldScene(this.width + this.tileSize, this.height + this.tileSize);
    scene.placeImageXY(mazeImage, (this.width + this.tileSize) / 2, (this.height + this.tileSize) / 2);
    return scene;
  }
  
  public void onKeyEvent(String key) {
    this.maze.move(key);
  }
  
  public boolean shouldWorldEnd() {
    return this.maze.won();
  }
  
  public void onTick() {
    this.maze.stickLeftTick();
  }
  
  public WorldScene lastScene(String msg) {
    this.maze.showShortestPath();
    WorldImage mazeImage = maze.render();
    WorldScene scene = new WorldScene(this.width + this.tileSize, this.height + this.tileSize);
    scene.placeImageXY(mazeImage, (this.width + this.tileSize) / 2, (height + this.tileSize) / 2);
    return scene;
  }
  
}

class ExamplesMazes {
  Game m = new Game(3);
  
  void testStuff(Tester t) {
    m.bigBang(1500, 800, 2);
  }
  

}
