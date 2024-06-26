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
  //All fields are protected for use in render methods of subclasses
  protected final Color tileColor;
  //Not final because heatColor changes depending on whether it is measured from the start or end
  protected Color heatColor;
  //Not final because all tiles start out neither visited nor visiting, and update to true as 
  //the user or search algorithm process them
  protected boolean visited;
  protected boolean visiting;

  // Creates a tile of the given color
  ATile(Color tileColor) {
    this.tileColor = tileColor;
    this.visiting = false;
    this.visited = false;
  }

  // Creates a tile with the default tile color
  ATile() {
    this.tileColor = ITile.TILE_COLOR;
  }

  // Sets the heat color to the given color
  void setHeat(Color color) {
    this.heatColor = color;
  }

  //finds the deepest Tile representative of this ATile
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
  protected void breakRect(RectTile other) {
    throw new IllegalArgumentException("Incompatible tile types.");
  }

  //breaks the wall between this ATile and other HexTile
  protected void breakHex(HexTile other) {
    throw new IllegalArgumentException("Incompatible tile types.");
  }

  //mutates this ATile's visiting field to true, to show that it is currently being processed.
  void moveTo() {
    this.visiting = true;
  }

  //mutates this ATile's visiting field to false, and visited field to true,
  //to show that it has been processed.
  void moveFrom() {
    this.visiting = false;
    this.visited = true;
  }

  //Sets both the visiting and visited fields of this tile to false
  void resetVistStatus() {
    this.visited = false;
    this.visiting = false;
  }

  // Renders this tile in the given size, with the color given by:
  // visiting > visited (if showVisited) > heatColor (if heatMode) > tileColor
  abstract WorldImage render(int tileSize, boolean heatMode, boolean showVisited);

  // Checks if this tile does not have a wall in the given direction
  abstract boolean canMove(String direction);

  // Returns an ArrayList<ATile> containing this tile's neighbors that are not separated by a wall
  abstract ArrayList<ATile> accessibleNeighbors();

  // Assigns this tile's neighbors to be the neighboring indices of the given grid,
  // at the given position
  abstract void assignNeighbors(ArrayList<ArrayList<ATile>> grid, int rowPos, int colPos);

  // Appends half of this tile's neighbors as edges to the given ArrayList, width the edges created
  // in accordance with the given bias. The edges created represent the lower and right neighbors
  // of this tile
  abstract void appendHalfEdges(ArrayList<Edge> edges, boolean vertBias, boolean horzBias);
}

// Represents a tile in a Rectangular maze
class RectTile extends ATile {
  //these fields are not final because we need to break them when making the maze
  private boolean upWall;
  private boolean downWall;
  private boolean rightWall;
  private boolean leftWall;

  //Not final because these fields are cyclic and therefore need mutation
  private ATile up;
  private ATile down;
  private ATile right;
  private ATile left;

  // Creates a RectTile with the given walls, neighbors, and color
  RectTile(boolean upWall, boolean downWall, boolean rightWall, boolean leftWall,
           RectTile up, RectTile down, RectTile right, RectTile left, Color tileColor) {
    super(tileColor);
    this.upWall = upWall;
    this.downWall = downWall;
    this.rightWall = rightWall;
    this.leftWall = leftWall;
    this.up = up;
    up.setDown(this);
    this.down = down;
    down.setUp(this);
    this.right = right;
    right.setLeft(this);
    this.left = left;
    left.setRight(this);
  }

  //Creates a RectTile witht the given walls and color, and the neighbors set to null
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

  // Creates a RectTile of the given color, with all walls set to true and neighbors set to null
  RectTile(Color tileColor) {
    this(true, true, true, true, tileColor);
  }

  // Creates a RectTile of the default color, with all walls set to true and neighbors set to null
  RectTile() {
    this(true, true, true, true, ITile.TILE_COLOR);
  }

  //sets this RectTile's up field to ATile up
  void setUp(ATile up) {
    this.up = up;
  }

  //sets this RectTile's down field to ATile down
  void setDown(ATile down) {
    this.down = down;
  }

  //sets this RectTile's right field to ATile right
  void setRight(ATile right) {
    this.right = right;
  }

  //sets this RectTile's left field to ATile left
  void setLeft(ATile left) {
    this.left = left;
  }

  //renders this RectTile as a square of the given size, with the color given by:
  // visiting > visited (if showVisited) > heatColor (if heatMode) > tileColor
  WorldImage render(int size, boolean heatMode, boolean showVisited) {
    Color renderColor;
    if (this.visiting) {
      renderColor = new Color(61, 118, 204);
    } else if (this.visited && showVisited) {
      renderColor = new Color(145, 184, 242);
    } else if (heatMode) {
      renderColor = this.heatColor;
    } else {
      renderColor = this.tileColor;
    }
    WorldImage walls = new RectangleImage(size, size, "solid", ITile.WALL_COLOR);
    WorldImage innerTile = new RectangleImage(size - 2, size - 2, "solid", renderColor);
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
  protected void breakRect(RectTile that) {
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

  //determines if there is not a wall in the given direction from this RectTile
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

  //finds all the neighboring tiles that are not separated by a wall
  ArrayList<ATile> accessibleNeighbors() {
    ArrayList<ATile> neighbors = new ArrayList<ATile>();
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

  // Assigns this tile's neighbors to be the neighboring indices of the given grid,
  // at the given position
  void assignNeighbors(ArrayList<ArrayList<ATile>> grid, int rowPos, int colPos) {
    if (rowPos != 0) {
      this.setUp(grid.get(rowPos - 1).get(colPos));
    }
    if (rowPos != grid.size() - 1) {
      this.setDown(grid.get(rowPos + 1).get(colPos));
    }
    if (colPos != 0) {
      this.setLeft(grid.get(rowPos).get(colPos - 1));
    }
    if (colPos != grid.get(rowPos).size() - 1) {
      this.setRight(grid.get(rowPos).get(colPos + 1));
    }
  }

  // Appends this tile's right and down neighbors as edges to the given ArrayList,
  // with the edges created in accordance with the given bias.
  void appendHalfEdges(ArrayList<Edge> edges, boolean vertBias, boolean horzBias) {
    if (this.right != null) {
      edges.add(new Edge(this, this.right, horzBias));
    }
    if (this.down != null) {
      edges.add(new Edge(this, this.down, vertBias));
    }
  }
}

//represents a tile in a Hexagonal maze
class HexTile extends ATile {

  //these fields are not final because they need to be broken for maze creation
  private boolean leftWall;
  private boolean rightWall;
  private boolean rightUpWall;
  private boolean rightDownWall;
  private boolean leftUpWall;
  private boolean leftDownWall;

  //these fields are not final because this data is cyclic and needs to be mutated
  private ATile left;
  private ATile right;
  private ATile rightUp;
  private ATile rightDown;
  private ATile leftUp;
  private ATile leftDown;

  // Creates a HexTile with the given walls, neighbors, and color
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
    left.setRight(this);
    this.right = right;
    right.setLeft(this);
    this.rightUp = rightUp;
    rightUp.setLeftDown(this);
    this.rightDown = rightDown;
    rightDown.setLeftUp(this);
    this.leftUp = leftUp;
    leftUp.setRightDown(this);
    this.leftDown = leftDown;
    leftDown.setRightUp(this);
  }

  // Creates a HexTile with the given walls and color, and neighbors set to null
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

  // Creates a HexTile of the given color, with all walls set to true and neighbors set to null
  HexTile(Color tileColor) {
    this(true, true, true, true, true, true, tileColor);
  }

  // Creates a HexTile of the default color, with all walls set to true and neighbors set to null
  HexTile() {
    this(true, true, true, true, true, true, ITile.TILE_COLOR);
  }

  //sets this HexTile's left neighbor
  void setLeft(ATile left) {
    this.left = left;
  }

  //sets this HexTile's right neighbor
  void setRight(ATile right) {
    this.right = right;
  }

  //sets this HexTile's rightUp neighbor
  void setRightUp(ATile rightUp) {
    this.rightUp = rightUp;
  }

  //sets this HexTile's rightDown neighbor
  void setRightDown(ATile rightDown) {
    this.rightDown = rightDown;
  }

  //sets this HexTile's leftUp neighbor
  void setLeftUp(ATile leftUp) {
    this.leftUp = leftUp;
  }

  //sets this HexTile's leftDown neighbor
  void setLeftDown(ATile leftDown) {
    this.leftDown = leftDown;
  }

  //renders this RectTile as a hexagon of the given size, with the color given by:
  // visiting > visited (if showVisited) > heatColor (if heatMode) > tileColor
  WorldImage render(int sideLength, boolean heatMode, boolean showVisited) {
    Color renderColor;
    if (this.visiting) {
      renderColor = new Color(61, 118, 204);
    } else if (this.visited && showVisited) {
      renderColor = new Color(145, 184, 242);
    } else if (heatMode) {
      renderColor = this.heatColor;
    } else {
      renderColor = this.tileColor;
    }
    WorldImage walls = new HexagonImage(sideLength, "solid", ITile.WALL_COLOR);
    WorldImage innerTile = new HexagonImage(sideLength - 2, "solid", renderColor);
    WorldImage tile = new RotateImage(new OverlayImage(innerTile, walls), 90);
    WorldImage vertRect = new RectangleImage(sideLength - 4,
            (int) ((sideLength - 2) * Math.sqrt(3)), "solid", renderColor);
    WorldImage horzRect = new RotateImage(vertRect, 90);
    WorldImage topLeft = new RotateImage(vertRect, -30);
    WorldImage topRight = new RotateImage(vertRect, 30);
    if (!this.rightWall) {
      tile = new OverlayOffsetImage(horzRect, -2.5, 0, tile);
    }
    if (!this.leftWall) {
      tile = new OverlayOffsetImage(horzRect, 2.5, 0, tile);
    }
    if (!this.rightUpWall) {
      tile = new OverlayOffsetImage(topRight, 1.5 * Math.cos(Math.PI / 3) - 1,
              1.5 * Math.sin(Math.PI / 3) + 0.5, tile);
    }
    if (!this.rightDownWall) {
      tile = new OverlayOffsetImage(topLeft, 1.5 * Math.cos(Math.PI / 3) - 2,
              -1.5 * Math.sin(Math.PI / 3) - 1, tile);
    }
    if (!this.leftUpWall) {
      tile = new OverlayOffsetImage(topLeft, -1.5 * Math.cos(Math.PI / 3) + 0.5,
              1.5 * Math.sin(Math.PI / 3) + 0.5, tile);
    }
    if (!this.leftDownWall) {
      tile = new OverlayOffsetImage(topRight, -1.5 * Math.cos(Math.PI / 3) + 1.5,
              -1.5 * Math.sin(Math.PI / 3) - 0.5, tile);
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
  protected void breakHex(HexTile that) {
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

  //determines if there is not a wall in the given direction
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

  //finds all the neighboring tiles that are not separated by a wall
  ArrayList<ATile> accessibleNeighbors() {
    ArrayList<ATile> neighbors = new ArrayList<ATile>();
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

  // Assigns this tile's neighbors to be the neighboring indices of the given grid,
  // at the given position
  void assignNeighbors(ArrayList<ArrayList<ATile>> grid, int rowPos, int colPos) {
    if (colPos != 0) {
      this.setLeft(grid.get(rowPos).get(colPos - 1));
    }
    if (colPos != grid.get(rowPos).size() - 1) {
      this.setRight(grid.get(rowPos).get(colPos + 1));
    }
    if (rowPos != 0 &&
            (colPos != grid.get(rowPos).size() - 1 || rowPos >= grid.get(0).size())) {
      if (rowPos < grid.get(0).size()) {
        this.setRightUp(grid.get(rowPos - 1).get(colPos));
      } else {
        this.setRightUp(grid.get(rowPos - 1).get(colPos + 1));
      }
    }
    if (rowPos != 0 && (colPos != 0 || rowPos >= grid.get(0).size())) {
      if (rowPos < grid.get(0).size()) {
        this.setLeftUp(grid.get(rowPos - 1).get(colPos - 1));
      } else {
        this.setLeftUp(grid.get(rowPos - 1).get(colPos));
      }
    }
    if (rowPos != grid.size() - 1 &&
            (colPos != grid.get(rowPos).size() - 1 || rowPos < grid.get(0).size() - 1)) {
      if (rowPos >= grid.get(0).size() - 1) {
        this.setRightDown(grid.get(rowPos + 1).get(colPos));
      } else {
        this.setRightDown(grid.get(rowPos + 1).get(colPos + 1));
      }
    }
    if (rowPos != grid.size() - 1 && (colPos != 0 || rowPos < grid.get(0).size() - 1)) {
      if (rowPos >= grid.get(0).size() - 1) {
        this.setLeftDown(grid.get(rowPos + 1).get(colPos - 1));
      } else {
        this.setLeftDown(grid.get(rowPos + 1).get(colPos));
      }
    }
  }

  // Appends this tile's right, rightDown, and leftDown neighbors as edges to the given ArrayList,
  // with the edges created in accordance with the given bias.
  void appendHalfEdges(ArrayList<Edge> edges, boolean diagBias, boolean horzBias) {
    if (this.right != null) {
      edges.add(new Edge(this, this.right, horzBias));
    }
    if (this.rightDown != null) {
      edges.add(new Edge(this, this.rightDown, diagBias));
    }
    if (this.leftDown != null) {
      edges.add(new Edge(this, this.leftDown, diagBias));
    }
  }
}

//represents a connection between two ATiles
class Edge {

  private final ATile tile1;
  private final ATile tile2;
  private final int weight;

  // Creates an edge between the given tiles with the given weights
  Edge(ATile tile1, ATile tile2, int weight) {
    this.tile1 = tile1;
    this.tile2 = tile2;
    this.weight = weight;
  }

  //randomly sets the weight of this edge, biased towards lower weights if bias == true
  Edge(ATile tile1, ATile tile2, boolean bias) {
    this.tile1 = tile1;
    this.tile2 = tile2;
    if (bias) {
      this.weight = (int) (Math.random() * 100 * 60 / 2);
    } else {
      this.weight = (int) (Math.random() * 100 * 60);
    }
  }

  //randomly sets the weight of this edge
  Edge(ATile tile1, ATile tile2) {
    this.tile1 = tile1;
    this.tile2 = tile2;
    this.weight = (int) (Math.random() * 100 * 60);
  }

  //breaks the wall which this Edge represents
  void breakEdge() {
    this.tile1.breakTile(this.tile2);
    this.tile2.breakTile(this.tile1);
  }

  //compares this Edge weight and that Edge weight
  int compareWeight(Edge that) {
    return that.compareWeight(this.weight);
  }

  //compares this Edge weight to int thatWeight
  private int compareWeight(int thatWeight) {
    return Integer.compare(thatWeight, this.weight);
  }

  //determines if the ATiles connected by this Edge have the same representatives
  boolean sameReps(HashMap<ATile, ATile> reps) {
    return this.tile1.findRep(reps).equals(this.tile2.findRep(reps));
  }

  //sets the representatives of this Edge's ATiles to be the same
  void unionReps(HashMap<ATile, ATile> reps) {
    reps.put(this.tile1.findRep(reps), this.tile2.findRep(reps));
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

  private final TileUtils utils;
  private final int height;
  private final int firstRowWidth;
  private final ArrayList<Edge> tree;
  private final ArrayList<ATile> solutionPath;
  private final ArrayList<ATile> workList;
  private final ArrayList<ATile> seenList;
  // Protected due to subclasses needing the grid for rendering and the stickLeft algorithm
  protected final ArrayList<ArrayList<ATile>> grid;
  // protected due to subclasses needing the size for rendering
  protected final int tileSize;
  //changes when the maze has been solved
  private boolean hasWon;
  //the x and y positions are changing with the current position,
  //and are needed in subclasses for moving
  protected int colPos;
  protected int rowPos;
  // Not final because it starts as true and swaps to false
  // once all the walls have been knocked down
  private boolean inConstruction;
  // Not final because the user can choose whether to display heat or paths.
  // Protected because both are needed in subclasses for rendering
  protected boolean heatMode;
  protected boolean showPath;
  // Not final because the leftHand direction changes as the algorithm rotates.
  // Protected because needed in subclasses for stickLeft algorithm
  protected String leftHand;

  // Creates an AMaze of the given size with the given biases, using the provided TileUtils for
  // calculating row width and generating the appropriate tiles
  AMaze(TileUtils utils, int height, int firstRowWidth, int tileSize,
        boolean vertBias, boolean horzBias) {
    this.utils = utils;
    this.height = height;
    this.tileSize = tileSize;
    this.firstRowWidth = firstRowWidth;
    this.grid = this.buildTiles();
    this.tree = this.buildTree(vertBias, horzBias);
    this.inConstruction = true;
    this.grid.get(0).get(0).moveTo();
    this.hasWon = false;
    this.colPos = 0;
    this.rowPos = 0;
    this.solutionPath = new ArrayList<ATile>();
    this.solutionPath.add(grid.get(0).get(0));
    this.workList = new ArrayList<ATile>();
    this.workList.add(this.grid.get(0).get(0));
    this.seenList = new ArrayList<ATile>();
    this.heatMode = false;
    this.showPath = true;
    this.leftHand = "a";
  }

  //formulates the grid of ATiles which comprise this AMaze, using this.utils to
  //calculate row widths and generate the appropriate tiles
  private ArrayList<ArrayList<ATile>> buildTiles() {
    ArrayList<ArrayList<ATile>> tiles = new ArrayList<ArrayList<ATile>>();
    //iterates through the maze's rows
    for (int row = 0; row < this.height; row++) {
      ArrayList<ATile> acc = new ArrayList<ATile>();
      int width = this.utils.calculateWidth(row, this.firstRowWidth);
      // iterates through the maze's columns and creates Tiles
      for (int col = 0; col < width; col++) {
        ATile tile;
        if (row == 0 && col == 0) {
          tile = this.utils.generateTile(new Color(31, 128, 70));
        } else if (row == this.height - 1 && col == width - 1) {
          tile = this.utils.generateTile(new Color(106, 34, 128));
        } else {
          tile = this.utils.generateTile();
        }
        acc.add(tile);
      }
      tiles.add(acc);
    }
    // iterates through the mazes rows
    for (int row = 0; row < this.height; row++) {
      int width = this.utils.calculateWidth(row, this.firstRowWidth);
      //iterates through and sets neighbors
      for (int col = 0; col < width; col++) {
        ATile t = tiles.get(row).get(col);
        t.assignNeighbors(tiles, row, col);
      }
    }
    return tiles;
  }

  // Returns an ArrayList with all possible edges between tiles in the grid,
  // with the given weight biases
  private ArrayList<Edge> getEdges(boolean vertBias, boolean horzBias) {
    ArrayList<Edge> edges = new ArrayList<Edge>();
    //iterates through rows
    for (int row = 0; row < this.height; row++) {
      int width = this.utils.calculateWidth(row, this.firstRowWidth);
      //iterates through columns and formulates edges
      for (int col = 0; col < width; col++) {
        this.grid.get(row).get(col).appendHalfEdges(edges, vertBias, horzBias);
      }
    }
    return edges;
  }

  //uses Kruskal's algorithm to gather the edges in the minimum spanning tree (maze)
  private ArrayList<Edge> buildTree(boolean vertBias, boolean horzBias) {
    ArrayList<Edge> edges = this.getEdges(vertBias, horzBias);
    Collections.sort(edges, new WeightComparator());
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();

    HashMap<ATile, ATile> representatives = new HashMap<ATile, ATile>();
    //iterates through and sets HexTile representatives to themselves
    for (ArrayList<ATile> row : this.grid) {
      for (ATile t : row) {
        representatives.put(t, t);
      }
    }
    //Iterates through all of the edges and checks their representatives to create the MST
    while (!edges.isEmpty()) {
      Edge currEdge = edges.remove(0);
      if (!currEdge.sameReps(representatives)) {
        edgesInTree.add(currEdge);
        currEdge.unionReps(representatives);
      }
    }

    return edgesInTree;
  }

  //breaks the first wall in the MST
  void breakFirstWall() {
    if (!this.tree.isEmpty()) {
      this.tree.remove(0).breakEdge();
    }
  }

  // Finds the solution path for this AMaze
  void findPath() {
    // Iterates through until the solution path is found
    while (!this.won()) {
      this.stickLeftTick();
    }
    this.restart();
  }

  //displays the path from start to end of this AMaze
  void showSolutionPath() {
    //iterates through the solutionPath ArrayList
    for (ATile t : this.solutionPath) {
      t.moveTo();
    }
  }

  //One tick of traversal of this AMaze, depth first
  void dfsTick() {
    if (!this.workList.isEmpty()) {
      ATile curr = this.workList.remove(0);
      if (curr == this.grid.get(this.grid.size() - 1)
              .get(this.grid.get(this.grid.size() - 1).size() - 1)) {
        this.hasWon = true;
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
      } else if (this.seenList.contains(curr)) {
        this.dfsTick();
      } else {
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
        //traverses the accessible neighbors of curr and adds them to the head of worklist
        for (ATile neighbor : curr.accessibleNeighbors()) {
          this.workList.add(0, neighbor);
        }
        this.seenList.add(0, curr);
      }
    }
  }

  //One tick of traversal of this AMaze, breadth first
  void bfsTick() {
    if (!this.workList.isEmpty()) {
      ATile curr = this.workList.remove(0);
      if (curr == this.grid.get(this.grid.size() - 1)
              .get(this.grid.get(this.grid.size() - 1).size() - 1)) {
        this.hasWon = true;
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
      } else if (this.seenList.contains(curr)) {
        this.bfsTick();
      } else {
        curr.moveTo();
        if (!this.seenList.isEmpty()) {
          this.seenList.get(0).moveFrom();
        }
        //traverses the accessible neighbors of curr and adds them to the tail of worklist
        for (int i = curr.accessibleNeighbors().size() - 1; i >= 0; i--) {
          ATile neighbor = curr.accessibleNeighbors().get(i);
          this.workList.add(neighbor);
        }
        this.seenList.add(0, curr);
      }
    }
  }

  //Assigns each tiles "heat" (distance from either entrance or exit)
  void assignHeats(boolean startFromExit) {
    ATile startTile;
    if (startFromExit) {
      startTile = this.grid.get(this.height - 1).get(this.firstRowWidth - 1);
    } else {
      startTile = this.grid.get(0).get(0);
    }
    HashMap<ATile, Integer> heatMap = new HashMap<ATile, Integer>();
    heatMap.put(startTile, 0);
    int maxHeat = 0;
    ArrayList<ATile> heatWorkList = new ArrayList<ATile>();
    heatWorkList.add(startTile);
    ArrayList<ATile> heatSeenList = new ArrayList<ATile>();
    //continues traversing until we have accessed every tile
    while (!heatWorkList.isEmpty()) {
      ATile curr = heatWorkList.remove(0);
      if (!heatSeenList.contains(curr)) {
        //adds all accessible neighbors to the tail of the worklist
        for (int i = curr.accessibleNeighbors().size() - 1; i >= 0; i--) {
          ATile neighbor = curr.accessibleNeighbors().get(i);
          if (!heatSeenList.contains(neighbor)) {
            heatWorkList.add(neighbor);
            heatMap.put(neighbor, heatMap.get(curr) + 1);
          }
        }
        maxHeat = heatMap.get(curr);
        heatSeenList.add(0, curr);
      }
    }
    // Iterate through every tile in the grid and set the appropriate heat
    for (ArrayList<ATile> row : this.grid) {
      for (ATile tile : row) {
        int blueValue = (int) (255.0 * heatMap.get(tile) / maxHeat);
        int redValue = 255 - blueValue;
        tile.setHeat(new Color(redValue, 0, blueValue));
      }
    }
  }

  // Toggles whether to display all visited tiles
  void togglePath() {
    this.showPath = !this.showPath;
  }

  // Toggles whether to display tiles heat
  void toggleHeat() {
    this.heatMode = !this.heatMode;
  }

  // Checks and returns if this maze is still being constructed
  boolean inConstruction() {
    this.inConstruction = this.inConstruction && !this.tree.isEmpty();
    return this.inConstruction;
  }

  //Checks and returns if this maze has been won
  boolean won() {
    this.hasWon = this.hasWon || this.rowPos == this.grid.size() - 1
            && this.colPos == this.grid.get(this.grid.size() - 1).size() - 1;
    return this.hasWon;
  }

  // Resets this maze so that it can be solved again
  void restart() {
    this.colPos = 0;
    this.rowPos = 0;
    this.workList.clear();
    this.seenList.clear();
    this.leftHand = "a";
    this.hasWon = false;
    // Iterates through every tile in the grid
    for (ArrayList<ATile> row : this.grid) {
      for (ATile tile : row) {
        tile.resetVistStatus();
      }
    }
    this.workList.add(this.grid.get(0).get(0));
    this.grid.get(0).get(0).moveTo();
  }

  //Moves from the current position in the given direction if possible, by the given amounts
  void move(String dir, int dcol, int drow) {
    if (this.grid.get(this.rowPos).get(this.colPos).canMove(dir)) {
      ATile oldTile = this.grid.get(this.rowPos).get(this.colPos);
      this.colPos += dcol;
      this.rowPos += drow;
      ATile newTile = this.grid.get(this.rowPos).get(this.colPos);
      oldTile.moveFrom();
      newTile.moveTo();
      if (this.solutionPath.size() > 1 && this.solutionPath.get(1).equals(newTile)) {
        this.solutionPath.remove(0);
      } else {
        this.solutionPath.add(0, newTile);
      }
    }
  }

  //moves from the current tile in a given direction, if possible
  abstract void move(String s);

  //Renders this AMaze as a WorldImage
  abstract WorldImage render();

  //traverses this maze by sticking to the leftHand wall
  abstract void stickLeftTick();
}

//represents a Rectangle-shaped maze consisting of RectTiles
class RectMaze extends AMaze {

  // Creates a RectMaze of the given dimensions and size, with the given biases towards edges
  RectMaze(int width, int height, int tileSize, boolean vertBias, boolean horzBias) {
    super(new RectUtils(), height, width, tileSize, vertBias, horzBias);
    if (width > 100 || width < 1) {
      throw new IllegalArgumentException("Width must be between 1 and 100");
    }
    if (height > 60 || height < 1) {
      throw new IllegalArgumentException("Height must be between 1 and 60");
    }
  }

  //renders this RectMaze as a WorldImage
  WorldImage render() {
    WorldImage img = new EmptyImage();
    //iterates through the rows in this RectMaze's grid
    for (ArrayList<ATile> row : this.grid) {
      WorldImage rowImg = new EmptyImage();
      //iterates through all the tiles in the row
      for (ATile t : row) {
        rowImg = new BesideImage(rowImg, t.render(this.tileSize, this.heatMode, this.showPath));
      }
      img = new AboveImage(img, rowImg);
    }
    return img;
  }

  //moves the current position to a given adjacent and accessible position
  void move(String s) {
    int dcol = 0;
    int drow = 0;
    boolean validDirection;
    switch (s) {
      case "w":
      case "up":
        validDirection = true;
        drow--;
        break;
      case "s":
      case "down":
        validDirection = true;
        drow++;
        break;
      case "d":
      case "right":
        validDirection = true;
        dcol++;
        break;
      case "a":
      case "left":
        validDirection = true;
        dcol--;
        break;
      default:
        validDirection = false;
        break;
    }
    if (validDirection) {
      this.move(s, dcol, drow);
    }
  }


  //traverses this RectMaze by sticking to the leftHand wall
  void stickLeftTick() {
    ATile currTile = this.grid.get(this.rowPos).get(this.colPos);
    if (currTile.canMove(this.leftHand)) {
      this.move(this.leftHand);
      this.rotateLeft();
    } else {
      this.rotateRight();
      this.stickLeftTick();
    }
  }

  // Rotates the current leftHand direction to the left
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
        throw new IllegalArgumentException("Invalid direction: " + this.leftHand);
    }
  }

  // Rotates the current leftHand direction to the right
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
        throw new IllegalArgumentException("Invalid direction: " + this.leftHand);
    }
  }

}

//represents a regular Hexagon-shaped maze consisting of HexTiles
class HexMaze extends AMaze {
  private final int sideLength;

  HexMaze(int sideLength, int tileSize, boolean vertBias, boolean horzBias) {
    super(new HexUtils(), sideLength * 2 - 1, sideLength, tileSize, vertBias, horzBias);
    if (sideLength > 25 || sideLength < 1) {
      throw new IllegalArgumentException("Sidelength must be between 1 and 23");
    }
    this.sideLength = sideLength;
  }

  //renders this HexMaze as a WorldImage
  WorldImage render() {
    WorldImage img = new EmptyImage();
    //iterates through and draws the tiles in the first row
    for (ATile t : this.grid.get(0)) {
      img = new BesideImage(img, t.render(this.tileSize, this.heatMode, this.showPath));
    }
    //iterates through and draws each row of the grid, adding it to the overall image
    for (int row = 1; row < this.grid.size(); row++) {
      WorldImage rowImg = new EmptyImage();
      ArrayList<ATile> rowTiles = this.grid.get(row);
      // Iterates through and draws the tiles in the row
      for (ATile t : rowTiles) {
        rowImg = new BesideImage(rowImg, t.render(this.tileSize, this.heatMode, this.showPath));
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
    if (this.rowPos < this.sideLength) {
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
        if (this.rowPos != 0 &&
                (this.colPos != maxCols - 1 || this.rowPos >= this.sideLength)) {
          if (this.rowPos < this.sideLength) {
            drow--;
          } else {
            drow--;
            dcol++;
          }
        }
        break;
      case "x":
        validDirection = true;
        if (this.rowPos != maxRows - 1 &&
                (this.colPos != maxCols - 1 || this.rowPos < this.sideLength - 1)) {
          if (this.rowPos >= this.sideLength - 1) {
            drow++;
          } else {
            drow++;
            dcol++;
          }
        }
        break;
      case "w":
        validDirection = true;
        if (this.rowPos != 0 &&
                (this.colPos != 0 || this.rowPos >= this.sideLength)) {
          if (this.rowPos < this.sideLength) {
            drow--;
            dcol--;
          } else {
            drow--;
          }
        }
        break;
      case "z":
        validDirection = true;
        if (this.rowPos != maxRows - 1 &&
                (this.colPos != 0 || this.rowPos < this.sideLength - 1)) {
          if (this.rowPos >= this.sideLength - 1) {
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
    if (validDirection) {
      this.move(s, dcol, drow);
    }
  }

  //traverses this HexMaze by sticking to the leftHand wall
  void stickLeftTick() {
    ATile currTile = this.grid.get(this.rowPos).get(this.colPos);
    if (currTile.canMove(this.leftHand)) {
      this.move(this.leftHand);
      this.rotateLeft();
      this.rotateLeft();
    } else {
      this.rotateRight();
      this.stickLeftTick();
    }
  }

  // Rotates the current leftHand direction to the left
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
        throw new IllegalArgumentException("Invalid direction: " + this.leftHand);
    }
  }

  // Rotates the current leftHand direction to the right
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
        throw new IllegalArgumentException("Invalid direction: " + this.leftHand);
    }
  }
}

// Utility methods for mazes of different tiles types
abstract class TileUtils {

  // Calculates the width of a row in a maze
  // based off of its index number and the length of the first row
  abstract Integer calculateWidth(Integer currRow, Integer firstRowLength);

  // Generates a Tile of the given color
  abstract ATile generateTile(Color color);

  // Generates a Tile of the default color
  abstract ATile generateTile();
}

// Utility methods for RectMazes
class RectUtils extends TileUtils {

  // The width of a RectMaze is constant, so return the firstRowLength
  Integer calculateWidth(Integer currRow, Integer firstRowLength) {
    return firstRowLength;
  }

  // Generate a RectTile of the given color
  ATile generateTile(Color color) {
    return new RectTile(color);
  }

  // Generates a RectTile of the default color
  ATile generateTile() {
    return new RectTile();
  }
}

// Utility methods for HexMazes
class HexUtils extends TileUtils {

  // Calculates the width of a row in a HexMaze
  // based off of its index number and the length of the first row
  Integer calculateWidth(Integer currRow, Integer firstRowLength) {
    int rowLength;
    if (currRow < firstRowLength) {
      rowLength = firstRowLength + currRow;
    } else if (currRow < 2 * firstRowLength - 1) {
      rowLength = 3 * firstRowLength - 2 - currRow;
    } else {
      throw new IllegalArgumentException(
              "currRow (" + currRow + ") out of bounds for sideLength (" + firstRowLength + ")");
    }
    return rowLength;
  }

  // Generates a HexTile of the given color
  ATile generateTile(Color color) {
    return new HexTile(color);
  }

  // Generates a HexTile of the default color
  ATile generateTile() {
    return new HexTile();
  }
}

//represents the game of solving mazes
class Game extends World {

  // None of the fields are final as they are all subject to change based on user input, e.g.
  // creating a new maze of a different size, toggling the paths and heat, pausing the game, etc.
  private AMaze maze;
  private int tileSize;
  private boolean paused;
  private String renderMode;
  private String tickMode;
  private boolean showConstruction;
  private boolean vertBias;
  private boolean horzBias;

  // Creates a Game with a RectMaze of the given size, where width and height are in number of tiles
  Game(int width, int height) {
    this.tileSize = Math.min(250, Math.min(1400 / width, 700 / height));
    this.vertBias = false;
    this.horzBias = false;
    this.maze = new RectMaze(width, height, this.tileSize, this.vertBias, this.horzBias);
    this.renderMode = "normal";
    this.tickMode = "construction";
    this.showConstruction = true;
  }

  // Creates a Game with a HexMaze of the given sideLength, where sideLength is in number of tiles
  Game(int sideLength) {
    this.tileSize = 250 / sideLength;
    this.vertBias = false;
    this.horzBias = false;
    this.maze = new HexMaze(sideLength, this.tileSize, this.vertBias, this.horzBias);
    this.renderMode = "normal";
    this.tickMode = "construction";
    this.showConstruction = true;
  }

  // Creates a Game, randomly choosing to have either a RectMaze or a HexMaze
  Game() {
    this.vertBias = false;
    this.horzBias = false;
    this.newRandomMaze();
    this.renderMode = "normal";
    this.showConstruction = true;
  }

  //renders the game as a WorldScene
  public WorldScene makeScene() {
    WorldImage mazeImage = this.maze.render();
    int width = (int) mazeImage.getWidth();
    int height = (int) mazeImage.getHeight();
    WorldScene scene = new WorldScene(width + this.tileSize, height + this.tileSize);
    scene.placeImageXY(mazeImage, (width + this.tileSize) / 2, (height + this.tileSize) / 2);
    return scene;
  }

  //moves the current tile based on a key command
  // See UserGuide.txt for detailed info on how to play the game
  public void onKeyEvent(String key) {
    switch (key) {
      case " ":
        this.paused = !this.paused;
        break;
      case "c":
        this.showConstruction = !this.showConstruction;
        break;
      case "k":
        this.vertBias = !this.vertBias;
        break;
      case "K":
        this.horzBias = !this.horzBias;
        break;
      case "n":
        this.newRandomMaze();
        break;
      default:
        break;
    }
    if (!this.tickMode.equals("construction")) {
      switch (key) {
        case "p":
          this.maze.togglePath();
          break;
        case "r":
          this.maze.restart();
          if (this.tickMode.equals("won")) {
            this.tickMode = "manual";
          }
          break;
        case "h":
          this.maze.assignHeats(false);
          if (!this.renderMode.equals("exit heat map")) {
            this.maze.toggleHeat();
          }
          if (this.renderMode.equals("start heat map")) {
            this.renderMode = "normal";
          } else {
            this.renderMode = "start heat map";
          }
          break;
        case "H":
          this.maze.assignHeats(true);
          if (!this.renderMode.equals("start heat map")) {
            this.maze.toggleHeat();
          }
          if (this.renderMode.equals("exit heat map")) {
            this.renderMode = "normal";
          } else {
            this.renderMode = "exit heat map";
          }
          break;
        case "M":
          if (!this.tickMode.equals("manual")) {
            this.tickMode = "manual";
            this.maze.restart();
          }
          break;
        case "D":
          if (!this.tickMode.equals("dfs")) {
            this.tickMode = "dfs";
            this.maze.restart();
          }
          break;
        case "B":
          if (!this.tickMode.equals("bfs")) {
            this.tickMode = "bfs";
            this.maze.restart();
          }
          break;
        case "L":
          if (!this.tickMode.equals("lhs")) {
            this.tickMode = "lhs";
            this.maze.restart();
          }
          break;
        default:
          if (this.tickMode.equals("manual") && !this.paused) {
            this.maze.move(key);
          }
          break;
      }
    }
  }

  // Updates the game each tick,
  // based on whether it has been won, is in construction, and the Game's tickMode
  public void onTick() {
    if (!this.paused) {
      if (this.maze.won()) {
        this.tickMode = "won";
      }
      switch (this.tickMode) {
        case "construction":
          if (this.showConstruction && this.maze.inConstruction()) {
            this.maze.breakFirstWall();
          } else {
            // If the maze is in construction and the user doesn't want to see it,
            // construct the whole maze in this tick
            while (this.maze.inConstruction()) {
              this.maze.breakFirstWall();
            }
          }
          if (!this.maze.inConstruction()) {
            this.maze.findPath();
            this.tickMode = "manual";
          }
          break;
        case "dfs":
          this.maze.dfsTick();
          break;
        case "bfs":
          this.maze.bfsTick();
          break;
        case "lhs":
          this.maze.stickLeftTick();
          break;
        case "won":
          this.maze.showSolutionPath();
          break;
        default:
          break;
      }
    }
  }

  // Generates either a RectMaze or HexMaze of random size, and replaces the current maze with it
  private void newRandomMaze() {
    if (Math.random() > 0.5) {
      int width = (int) (Math.random() * 100) + 1;
      int height = (int) (Math.random() * 60) + 1;
      this.tileSize = Math.min(250, Math.min(1400 / width, 700 / height));
      this.maze = new RectMaze(width, height, this.tileSize, this.vertBias, this.horzBias);
    } else {
      int sideLength = (int) (Math.random() * 23) + 1;
      this.tileSize = 250 / sideLength;
      this.maze = new HexMaze(sideLength, this.tileSize, this.vertBias, this.horzBias);
    }
    this.tickMode = "construction";
    this.renderMode = "normal";
  }
  
  // Checks whether the provided value is equal to the field of the provided name.
  // This method is for testing purposes only, not for use in any actual maze logic.
  <T> boolean checkField(String field, T value) {
    switch (field) {
      case "paused":
        return value.equals(this.paused);
      case "renderMode":
        return value.equals(this.renderMode);
      case "tickMode":
        return value.equals(this.tickMode);
      case "showConstruction":
        return value.equals(this.showConstruction);
      case "vertBias":
        return value.equals(this.vertBias);
      case "horzBias":
        return value.equals(this.horzBias);
      case "tileSize":
        return value.equals(this.tileSize);
      default:
        throw new IllegalArgumentException("Not a field: " + field);
    }
  }
}

// Examples and tests
class ExamplesMazes {
  Game m = new Game();
  
  /*
  void testBigBang(Tester t) {
    m.bigBang(1500, 800, 0.0000001);
  }
  */

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

    boolean testMoveToFrom = t.checkExpect(up.visiting,
            true)
            && t.checkExpect(down.visiting,
            false)
            && t.checkExpect(up.visited,
            false)
            && t.checkExpect(down.visited,
            true);

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

  boolean testRectMazes(Tester t) {
    AMaze unbiasedRectMaze = new RectMaze(3, 3, 40, false, false);
    AMaze vertBiasedRectMaze = new RectMaze(2, 2, 10, true, false);
    AMaze horzBiasedRectMaze = new RectMaze(4, 4, 15, false, true);

    boolean checkConstructor = t.checkConstructorException(
            new IllegalArgumentException("Width must be between 1 and 100"),
            "RectMaze", 101, 1, 10, true, true)
            && t.checkConstructorException(
            new IllegalArgumentException("Height must be between 1 and 60"),
            "RectMaze", 10, 61, 10, false, false);


    boolean testInConstruction = t.checkExpect(unbiasedRectMaze.inConstruction(),
            true)
            && t.checkExpect(vertBiasedRectMaze.inConstruction(),
            true);


    //4 tiles, 3 edges in MST
    vertBiasedRectMaze.breakFirstWall();
    vertBiasedRectMaze.breakFirstWall();
    vertBiasedRectMaze.breakFirstWall();


    unbiasedRectMaze.breakFirstWall();
    unbiasedRectMaze.breakFirstWall();
    unbiasedRectMaze.breakFirstWall();
    unbiasedRectMaze.breakFirstWall();
    unbiasedRectMaze.breakFirstWall();
    unbiasedRectMaze.breakFirstWall();
    unbiasedRectMaze.breakFirstWall();
    unbiasedRectMaze.breakFirstWall();


    boolean testInConstructionAndBreakFirstWall = t.checkExpect(vertBiasedRectMaze.inConstruction(),
            false)
            && t.checkExpect(unbiasedRectMaze.inConstruction(),
            false);


    boolean testWon = t.checkExpect(vertBiasedRectMaze.won(),
            false)
            && t.checkExpect(unbiasedRectMaze.won(),
            false);


    //4 tiles, dfs tick 4 times guaranteed to visit all
    vertBiasedRectMaze.dfsTick();
    vertBiasedRectMaze.dfsTick();
    vertBiasedRectMaze.dfsTick();
    vertBiasedRectMaze.dfsTick();


    unbiasedRectMaze.bfsTick();
    unbiasedRectMaze.bfsTick();
    unbiasedRectMaze.bfsTick();
    unbiasedRectMaze.bfsTick();
    unbiasedRectMaze.bfsTick();
    unbiasedRectMaze.bfsTick();
    unbiasedRectMaze.bfsTick();
    unbiasedRectMaze.bfsTick();
    unbiasedRectMaze.bfsTick();
    unbiasedRectMaze.bfsTick();


    boolean checkWonDFSBFS = t.checkExpect(vertBiasedRectMaze.won(),
            true)
            && t.checkExpect(unbiasedRectMaze.won(),
            true);


    vertBiasedRectMaze.showSolutionPath();


    boolean testSolutionDisplayed = t.checkExpect(
            vertBiasedRectMaze.grid.get(0).get(0).visiting,
            true)
            && t.checkExpect(vertBiasedRectMaze.grid.get(1).get(1).visiting,
            true)
            && t.checkExpect(vertBiasedRectMaze.grid.get(0).get(1).visited
            || vertBiasedRectMaze.grid.get(1).get(0).visited, true);


    vertBiasedRectMaze.restart();
    unbiasedRectMaze.restart();

    boolean testRestart = t.checkExpect(vertBiasedRectMaze.inConstruction(),
            false)
            && t.checkExpect(vertBiasedRectMaze.won(),
            false)
            && t.checkExpect(unbiasedRectMaze.won(),
            false);

    horzBiasedRectMaze.move("right", 1, 0);

    //move only if no wall
    boolean testMove = t.checkExpect(horzBiasedRectMaze.grid.get(0).get(0).visiting
            || horzBiasedRectMaze.grid.get(0).get(1).visiting, true);


    unbiasedRectMaze.assignHeats(true);
    vertBiasedRectMaze.assignHeats(false);


    vertBiasedRectMaze.bfsTick();


    //if we visit right before down, that tile color should be less blue
    //end should also be more blue
    //vice versa when starting from exit
    boolean testStartFromStartHeats = vertBiasedRectMaze.grid.get(0).get(1).visiting ?
            t.checkExpect(vertBiasedRectMaze.grid.get(0).get(1).tileColor.getBlue()
                    <= vertBiasedRectMaze.grid.get(1).get(0).tileColor.getBlue(), true)
            : t.checkExpect(vertBiasedRectMaze.grid.get(1).get(0).tileColor.getBlue()
            <= vertBiasedRectMaze.grid.get(0).get(1).tileColor.getBlue(), true)
            && t.checkExpect(vertBiasedRectMaze.grid.get(0).get(0).tileColor.getBlue()
            <= vertBiasedRectMaze.grid.get(1).get(1).tileColor.getBlue(), true);


    boolean testStartFromExitHeats = t.checkExpect(
            unbiasedRectMaze.grid.get(0).get(0).tileColor.getRed()
                    <= unbiasedRectMaze.grid.get(2).get(2).tileColor.getRed(), true);


    //findPath has no side effects (restart)


    vertBiasedRectMaze.restart();


    boolean testTogglesFirst = t.checkExpect(vertBiasedRectMaze.showPath, true)
            && t.checkExpect(vertBiasedRectMaze.heatMode, false);


    vertBiasedRectMaze.togglePath();
    vertBiasedRectMaze.toggleHeat();


    boolean testTogglesAgain = t.checkExpect(vertBiasedRectMaze.showPath, false)
            && t.checkExpect(vertBiasedRectMaze.heatMode, true);


    unbiasedRectMaze.stickLeftTick();


    //either rotate left or right
    boolean testLeftTick = t.checkExpect(unbiasedRectMaze.leftHand.equals("w")
            || unbiasedRectMaze.leftHand.equals("d"), true);


    return checkConstructor && testLeftTick && testInConstruction
            && testInConstructionAndBreakFirstWall && testWon && checkWonDFSBFS
            && testSolutionDisplayed && testRestart && testMove
            && testStartFromExitHeats && testStartFromStartHeats
            && testTogglesFirst && testTogglesAgain;
  }

  boolean testHexMazes(Tester t) {
    AMaze unbiasedHexMaze = new HexMaze(3, 10, false, false);
    AMaze vertBiasedHexMaze = new HexMaze(2, 5, true, false);
    AMaze horzBiasedHexMaze = new HexMaze(4, 5, false, true);

    boolean testConstructor = t.checkConstructorException(
            new IllegalArgumentException("Sidelength must be between 1 and 23"),
            "HexMaze", 34, 10, true, true);


    boolean testInConstruction = t.checkExpect(unbiasedHexMaze.inConstruction(),
            true)
            && t.checkExpect(vertBiasedHexMaze.inConstruction(),
            true);


    vertBiasedHexMaze.breakFirstWall();
    vertBiasedHexMaze.breakFirstWall();
    vertBiasedHexMaze.breakFirstWall();
    vertBiasedHexMaze.breakFirstWall();
    vertBiasedHexMaze.breakFirstWall();
    vertBiasedHexMaze.breakFirstWall();


    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();
    unbiasedHexMaze.breakFirstWall();


    boolean testConstructionAndBreakFirstWall = t.checkExpect(vertBiasedHexMaze.inConstruction(),
            false)
            && t.checkExpect(unbiasedHexMaze.inConstruction(),
            false);


    boolean testWon = t.checkExpect(vertBiasedHexMaze.won(),
            false)
            && t.checkExpect(unbiasedHexMaze.won(),
            false);


    vertBiasedHexMaze.dfsTick();
    vertBiasedHexMaze.dfsTick();
    vertBiasedHexMaze.dfsTick();
    vertBiasedHexMaze.dfsTick();
    vertBiasedHexMaze.dfsTick();
    vertBiasedHexMaze.dfsTick();
    vertBiasedHexMaze.dfsTick();


    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();
    unbiasedHexMaze.bfsTick();

    boolean testWonAgain = t.checkExpect(vertBiasedHexMaze.won(),
            true)
            && t.checkExpect(unbiasedHexMaze.won(),
            true);


    vertBiasedHexMaze.showSolutionPath();


    boolean testSolutionDisplayed = t.checkExpect(
            vertBiasedHexMaze.grid.get(0).get(0).visiting,
            true)
            && t.checkExpect(vertBiasedHexMaze.grid.get(vertBiasedHexMaze.grid.size() - 1)
            .get(vertBiasedHexMaze.grid.get(0).size() - 1).visiting, true);


    vertBiasedHexMaze.restart();
    unbiasedHexMaze.restart();


    boolean testRestart = t.checkExpect(vertBiasedHexMaze.won(),
            false)
            && t.checkExpect(unbiasedHexMaze.won(),
            false);


    vertBiasedHexMaze.move("d", 1, 0);


    boolean testMove = t.checkExpect(vertBiasedHexMaze.grid.get(0).get(1).visiting
            || vertBiasedHexMaze.grid.get(0).get(0).visiting, true);


    unbiasedHexMaze.assignHeats(true);
    vertBiasedHexMaze.assignHeats(false);


    vertBiasedHexMaze.bfsTick();


    boolean testStartFromStartHeats = vertBiasedHexMaze.grid.get(0).get(1).visiting ?
            t.checkExpect(vertBiasedHexMaze.grid.get(0).get(1).tileColor.getBlue()
                            <= vertBiasedHexMaze.grid.get(1).get(1).tileColor.getBlue()
                            && vertBiasedHexMaze.grid.get(0).get(1).tileColor.getBlue()
                            <= vertBiasedHexMaze.grid.get(0).get(1).tileColor.getBlue(),
                    true)
            : t.checkExpect(vertBiasedHexMaze.grid.get(0).get(1).tileColor.getBlue()
                    >= vertBiasedHexMaze.grid.get(1).get(1).tileColor.getBlue()
                    && vertBiasedHexMaze.grid.get(0).get(1).tileColor.getBlue()
                    >= vertBiasedHexMaze.grid.get(0).get(1).tileColor.getBlue(),
            true);


    boolean testStartFromExitHeats = t.checkExpect(
            vertBiasedHexMaze.grid.get(0).get(0).tileColor.getRed()
                    <= vertBiasedHexMaze.grid.get(vertBiasedHexMaze.grid.size() - 1)
                    .get(vertBiasedHexMaze.grid.get(0).size() - 1).tileColor.getRed(),
            true);


    vertBiasedHexMaze.restart();


    boolean testTogglesFirst = t.checkExpect(vertBiasedHexMaze.showPath, true)
            && t.checkExpect(vertBiasedHexMaze.heatMode, false);


    vertBiasedHexMaze.toggleHeat();
    vertBiasedHexMaze.togglePath();


    boolean testTogglesAgain = t.checkExpect(vertBiasedHexMaze.showPath, false)
            && t.checkExpect(vertBiasedHexMaze.heatMode, true);

    unbiasedHexMaze.stickLeftTick();

    return testConstructor && testInConstruction && testConstructionAndBreakFirstWall
            && testWon && testWonAgain && testSolutionDisplayed
            && testRestart && testMove && testStartFromStartHeats
            && testStartFromExitHeats && testTogglesAgain && testTogglesFirst;
  }

  boolean testTileUtils(Tester t) {
    TileUtils ru = new RectUtils();
    TileUtils hu = new HexUtils();

    boolean testRectWidth = t.checkExpect(ru.calculateWidth(0, 9), 9)
            && t.checkExpect(ru.calculateWidth(3, 17), 17)
            && t.checkExpect(ru.calculateWidth(44, 26), 26);
    boolean testHexWidth = t.checkExpect(hu.calculateWidth(0, 7), 7)
            && t.checkExpect(hu.calculateWidth(3, 17), 20)
            && t.checkExpect(hu.calculateWidth(44, 26), 32)
            && t.checkException(
            new IllegalArgumentException("currRow (10) out of bounds for sideLength (5)"),
            hu, "calculateWidth", 10, 5);

    boolean testTileGen = t.checkExpect(ru.generateTile(), new RectTile())
            && t.checkExpect(hu.generateTile(), new HexTile())
            && t.checkExpect(ru.generateTile(Color.BLUE), new RectTile(Color.BLUE))
            && t.checkExpect(ru.generateTile(new Color(1, 2, 3)), new RectTile(new Color(1, 2, 3)))
            && t.checkExpect(hu.generateTile(Color.RED), new HexTile(Color.RED))
            && t.checkExpect(hu.generateTile(new Color(110, 220, 233)),
            new HexTile(new Color(110, 220, 233)));

    return testRectWidth && testHexWidth && testTileGen;
  }
  
  boolean testGame(Tester t) {
    Game g1 = new Game();
    boolean init = t.checkExpect(g1.checkField("paused", false), true)
        && t.checkExpect(g1.checkField("renderMode", "normal"), true)
        && t.checkExpect(g1.checkField("tickMode", "construction"), true)
        && t.checkExpect(g1.checkField("showConstruction", true), true)
        && t.checkExpect(g1.checkField("vertBias", false), true)
        && t.checkExpect(g1.checkField("horzBias", false), true);
    
    g1.onKeyEvent(" ");
    
    boolean paused = t.checkExpect(g1.checkField("paused", true), true)
        && t.checkExpect(g1.checkField("renderMode", "normal"), true)
        && t.checkExpect(g1.checkField("tickMode", "construction"), true)
        && t.checkExpect(g1.checkField("showConstruction", true), true)
        && t.checkExpect(g1.checkField("vertBias", false), true)
        && t.checkExpect(g1.checkField("horzBias", false), true);
    
    g1.onKeyEvent("k");
    g1.onKeyEvent("H");
    g1.onKeyEvent("c");
    g1.onKeyEvent("B");
    
    boolean keysWhilePausedConstructing = t.checkExpect(g1.checkField("paused", true), true)
        && t.checkExpect(g1.checkField("renderMode", "normal"), true)
        && t.checkExpect(g1.checkField("tickMode", "construction"), true)
        && t.checkExpect(g1.checkField("showConstruction", false), true)
        && t.checkExpect(g1.checkField("vertBias", true), true)
        && t.checkExpect(g1.checkField("horzBias", false), true);
    
    g1.onKeyEvent(" ");
    g1.onKeyEvent("k");
    g1.onKeyEvent("K");
    g1.onKeyEvent("H");
    g1.onKeyEvent("d");
    g1.onKeyEvent("B");
    

    boolean keysWhileUnpausedConstructing = t.checkExpect(g1.checkField("paused", false), true)
        && t.checkExpect(g1.checkField("renderMode", "normal"), true)
        && t.checkExpect(g1.checkField("tickMode", "construction"), true)
        && t.checkExpect(g1.checkField("showConstruction", false), true)
        && t.checkExpect(g1.checkField("vertBias", false), true)
        && t.checkExpect(g1.checkField("horzBias", true), true);
    
    g1.onTick();
    
    boolean finishedConstructing = t.checkExpect(g1.checkField("paused", false), true)
        && t.checkExpect(g1.checkField("renderMode", "normal"), true)
        && t.checkExpect(g1.checkField("showConstruction", false), true)
        && t.checkExpect(g1.checkField("vertBias", false), true)
        && t.checkExpect(g1.checkField("horzBias", true), true);
    
    if (g1.checkField("tileSize", 250)) {
      finishedConstructing = finishedConstructing
          && t.checkExpect(g1.checkField("tickMode", "won"), true);
    } else {
      finishedConstructing = finishedConstructing
          && t.checkExpect(g1.checkField("tickMode", "manual"), true);
    }
    
    g1.onKeyEvent("k");
    g1.onKeyEvent("H");
    g1.onKeyEvent("c");
    g1.onKeyEvent("B");
    
    boolean keysWhileUnpausedNotConstructing = t.checkExpect(g1.checkField("paused", false), true)
        && t.checkExpect(g1.checkField("renderMode", "exit heat map"), true)
        && t.checkExpect(g1.checkField("tickMode", "bfs"), true)
        && t.checkExpect(g1.checkField("showConstruction", true), true)
        && t.checkExpect(g1.checkField("vertBias", true), true)
        && t.checkExpect(g1.checkField("horzBias", true), true);
    
    g1.onKeyEvent("n");
    
    boolean checkNewMaze = t.checkExpect(g1.checkField("paused", false), true)
        && t.checkExpect(g1.checkField("renderMode", "normal"), true)
        && t.checkExpect(g1.checkField("tickMode", "construction"), true)
        && t.checkExpect(g1.checkField("showConstruction", true), true)
        && t.checkExpect(g1.checkField("vertBias", true), true)
        && t.checkExpect(g1.checkField("horzBias", true), true);
    
    g1.onTick();
    
    boolean checkConstructionTick = t.checkExpect(g1.checkField("paused", false), true)
        && t.checkExpect(g1.checkField("renderMode", "normal"), true)
        && t.checkExpect(g1.checkField("showConstruction", true), true)
        && t.checkExpect(g1.checkField("vertBias", true), true)
        && t.checkExpect(g1.checkField("horzBias", true), true);
    
    if (g1.checkField("tileSize", 250)) {
      finishedConstructing = finishedConstructing
          && t.checkExpect(g1.checkField("tickMode", "won"), true);
    } else {
      finishedConstructing = finishedConstructing
          && t.checkExpect(g1.checkField("tickMode", "construction"), true);
    }
    
    g1.onKeyEvent("c");
    g1.onTick();
    g1.onKeyEvent("D");
    g1.onKeyEvent("h");
    
    boolean checkGameTick = t.checkExpect(g1.checkField("paused", false), true)
        && t.checkExpect(g1.checkField("renderMode", "start heat map"), true)
        && t.checkExpect(g1.checkField("tickMode", "dfs"), true)
        && t.checkExpect(g1.checkField("showConstruction", false), true)
        && t.checkExpect(g1.checkField("vertBias", true), true)
        && t.checkExpect(g1.checkField("horzBias", true), true);
    
    Game g2 = new Game(2, 2);
    
    boolean g2Init = t.checkExpect(g2.checkField("paused", false), true)
        && t.checkExpect(g2.checkField("renderMode", "normal"), true)
        && t.checkExpect(g2.checkField("tickMode", "construction"), true)
        && t.checkExpect(g2.checkField("showConstruction", true), true)
        && t.checkExpect(g2.checkField("vertBias", false), true)
        && t.checkExpect(g2.checkField("horzBias", false), true);
    
    g2.onTick();
    g2.onTick();
    g2.onTick();
    
    boolean g2ConstructionFinished = t.checkExpect(g2.checkField("paused", false), true)
        && t.checkExpect(g2.checkField("renderMode", "normal"), true)
        && t.checkExpect(g2.checkField("tickMode", "manual"), true)
        && t.checkExpect(g2.checkField("showConstruction", true), true)
        && t.checkExpect(g2.checkField("vertBias", false), true)
        && t.checkExpect(g2.checkField("horzBias", false), true);
    
    g2.onKeyEvent("L");
    g2.onTick();
    g2.onTick();
    g2.onTick();
    g2.onTick();
    g2.onTick();
    
    boolean g2Won = t.checkExpect(g2.checkField("paused", false), true)
        && t.checkExpect(g2.checkField("renderMode", "normal"), true)
        && t.checkExpect(g2.checkField("tickMode", "won"), true)
        && t.checkExpect(g2.checkField("showConstruction", true), true)
        && t.checkExpect(g2.checkField("vertBias", false), true)
        && t.checkExpect(g2.checkField("horzBias", false), true);
    
    return init && paused && keysWhilePausedConstructing && keysWhileUnpausedConstructing
        && finishedConstructing && keysWhileUnpausedNotConstructing && checkNewMaze
        && checkConstructionTick && checkGameTick && g2Init && g2ConstructionFinished;
  }

}
