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

class Tile implements ITile{
  
  private Color tileColor;
  
  private boolean upWall;
  private boolean downWall;
  private boolean rightWall;
  private boolean leftWall;
  
  private Tile up;
  private Tile down;
  private Tile right;
  private Tile left;
  
  Tile(boolean upWall, boolean downWall, boolean rightWall, boolean leftWall,
      Tile up, Tile down, Tile right, Tile left, Color tileColor) {
    this.tileColor = tileColor;
    this.upWall = upWall;
    this.downWall = downWall;
    this.rightWall = rightWall;
    this.leftWall = leftWall;
    this.up = up;
    this.down = down;
    this.right = right;
    this.left = up;
  }
  
  Tile(boolean upWall, boolean downWall, boolean rightWall, boolean leftWall, Color tileColor) {
    this.tileColor = tileColor;
    this.upWall = upWall;
    this.downWall = downWall;
    this.rightWall = rightWall;
    this.leftWall = leftWall;
    this.up = null;
    this.down = null;
    this.right = null;
    this.left = null;
  }
  
  Tile(Color tileColor) {
    this(true, true, true, true, tileColor);
  }
  
  Tile() {
    this(true, true, true, true, ITile.TILE_COLOR);
  }
  
  void setUp(Tile up) {
    this.up = up;
  }
  
  void setDown(Tile down) {
    this.down = down;
  }
  
  void setRight(Tile right) {
    this.right = right;
  }
  
  void setLeft(Tile left) {
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
  
  String getDirection(Tile neighbor) {
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
  
  void breakTile(Tile that) {
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
  
  Tile findRep(HashMap<Tile, Tile> reps) {
    Tile rep = reps.get(this);
    if (rep.equals(this)) {
      return rep;
    } else {
      return rep.findRep(reps);
    }
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
        throw new IllegalArgumentException("Not a direction: " + direction);
    }
  }
  
  void moveTo() {
    this.tileColor = new Color(61, 118, 204);
  }
  
  void moveFrom() { 
    this.tileColor = new Color(145, 184, 242);
  }
}

class Edge {
  
  private final Tile topLeft;
  private final Tile botRight; 
  private final int weight;

  Edge(Tile topLeft, Tile botRight, int weight) {
    this.topLeft = topLeft;
    this.botRight = botRight;
    this.weight = weight;
  }

  Edge(Tile topLeft, Tile botRight) {
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
  
  boolean sameReps(HashMap<Tile, Tile> reps) {
    return this.topLeft.findRep(reps).equals(this.botRight.findRep(reps));
  }
  
  void unionReps(HashMap<Tile, Tile> reps) {
    reps.put(this.topLeft.findRep(reps), this.botRight.findRep(reps));
  }
}

class WeightComparator implements Comparator<Edge> {
  public int compare(Edge e1, Edge e2) {
    return e1.compareWeight(e2);
  }
}

class Maze {
  private final int width;
  private final int height;
  private final int tileSize;
  private final ArrayList<ArrayList<Tile>> grid;
  private final ArrayList<Tile> shortestPath;
  private int xPos;
  private int yPos;
  
  Maze(int width, int height, int tileSize) {
    if (width > 100 || width < 0) {
      throw new IllegalArgumentException("Width must be between 0 and 100");
    }
    if (height > 60 || height < 0) {
      throw new IllegalArgumentException("Height must be between 0 and 60");
    }
    this.width = width;
    this.height = height;
    this.tileSize = tileSize;
    this.grid = this.buildTiles();
    this.breakTreeWalls();
    this.xPos = 0;
    this.yPos = 0;
    this.grid.get(0).get(0).moveTo();
    this.shortestPath = new ArrayList<Tile>();
    this.shortestPath.add(grid.get(0).get(0));
  }
  
  Maze(ArrayList<ArrayList<Tile>> grid) {
    if (grid.size() > 60 || grid.size() < 0) {
      throw new IllegalArgumentException("Height must be between 0 and 60");
    }
    if (grid.get(0).size() > 100 || grid.get(0).size() < 0) {
      throw new IllegalArgumentException("Width must be between 0 and 100");
    }
    this.width = grid.get(0).size();
    this.height = grid.size();
    this.tileSize = Math.min(1500 / this.width, 800 / this.height);
    this.grid = grid;
    this.breakTreeWalls();
    this.xPos = 0;
    this.yPos = 0;
    this.grid.get(0).get(0).moveTo();
    this.shortestPath = new ArrayList<Tile>();
    this.shortestPath.add(grid.get(0).get(0));
  }
  
  ArrayList<ArrayList<Tile>> buildTiles() {
    ArrayList<ArrayList<Tile>> tiles = new ArrayList<ArrayList<Tile>>();
    for (int row = 0; row < this.height; row++) {
      ArrayList<Tile> acc = new ArrayList<Tile>();
      for (int col = 0; col < this.width; col++) {
        Tile tile;
        if (row == 0 && col == 0) {
          tile = new Tile(new Color(31, 128, 70));
        } else if (row == this.height - 1 && col == this.width - 1) {
          tile = new Tile(new Color(106, 34, 128));
        } else {
          tile = new Tile();
        }
        acc.add(tile);
      }
      tiles.add(acc);
    }
    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {
        Tile t = tiles.get(row).get(col);
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
    
    HashMap<Tile, Tile> representatives = new HashMap<Tile, Tile>();
    for (ArrayList<Tile> row : grid) {
      for (Tile t : row) {
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
    for (ArrayList<Tile> row : this.grid) {
      WorldImage rowImg = new EmptyImage();
      for (Tile t : row) {
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
      Tile oldTile = this.grid.get(this.yPos).get(this.xPos);
      this.xPos += dx;
      this.yPos += dy;
      Tile newTile = this.grid.get(this.yPos).get(this.xPos);
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
    return this.xPos == this.width - 1 && this.yPos == this.height - 1;
  }
  
  void showShortestPath() {
    for (Tile t : this.shortestPath) {
      t.moveTo();
    }
  }
}

class Game extends World {
  
  private final Maze maze;
  private int width;
  private int height;
  private int tileSize;
  
  Game(int width, int height) {
    this.tileSize = Math.min(1400 / width, 700 / height);
    this.maze = new Maze(width, height, this.tileSize);
    this.width = width * this.tileSize;
    this.height = height * this.tileSize;
  }
  
  public WorldScene makeScene() {
    WorldImage mazeImage = this.maze.render();
    WorldScene scene = new WorldScene(this.width + this.tileSize, this.height + this.tileSize);
    scene.placeImageXY(mazeImage, (this.width + this.tileSize) / 2, (height + this.tileSize) / 2);
    return scene;
  }
  
  public void onKeyEvent(String key) {
    this.maze.move(key);
  }
  
  public boolean shouldWorldEnd() {
    return this.maze.won();
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
  Game m = new Game(5, 5);
  
  void testStuff(Tester t) {
    m.bigBang(1500, 800, 0.1);
  }
  

}
