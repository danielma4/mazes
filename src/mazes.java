import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//buggy quicksort implementation i dont feel like debugging it rn lol
/*
class ArrayListUtils {
  void quickSortEdges(ArrayList<Edge> edges) {
    ArrayList<Edge> temp = new ArrayList<>();
    quickSortHelp(edges, temp, 0, edges.size());
  }

  void quickSortHelp(ArrayList<Edge> src, ArrayList<Edge> scratch, int low, int high) {
    if (low >= high) {
      return;
    }
    Edge pivot = src.get(low);
    //middle index
    int mid = partition(src, scratch, pivot, low, high);
    quickSortHelp(src, scratch, low, mid);
    quickSortHelp(src, scratch, mid + 1, high);
  }

  int partition(ArrayList<Edge> src, ArrayList<Edge> scratch, Edge pivot,
                int low, int high) {
    int currLow = low;
    int currHigh = high - 1;
    for (int index = low + 1; index < high; index += 1) {
      if (src.get(index).weight < pivot.weight) {
        scratch.set(currLow, src.get(index));
        currLow += 1;
      } else {
        scratch.set(currHigh, src.get(index));
        currHigh -= 1;
      }
    }
    scratch.set(currLow, pivot);
    for (int index = low; index < high; index += 1) {
      src.set(index, scratch.get(index));
    }
    return currLow;
  }
}

 */

class EdgeComparator implements Comparator<Edge> {
  public int compare(Edge e1, Edge e2) {
    return Integer.compare(e1.weight, e2.weight);
  }
}

class Tile {
  private final Tile left;
  private final Tile right;
  private final Tile up;
  private final Tile down;

  Tile(Tile down, Tile left, Tile right, Tile up) {
    this.down = down;
    this.left = left;
    this.right = right;
    this.up = up;
  }

  Tile() {
    this.down = this;
    this.left = this;
    this.right = this;
    this.up = this;
  }
}

class Edge {
  //protected
  protected final Tile t1;
  protected final Tile t2; //Tiles which are connected by the edge
  protected final int weight;

  Edge(Tile t1, Tile t2, int weight) {
    this.t1 = t1;
    this.t2 = t2;
    this.weight = weight;
  }

  Edge(Tile t1, Tile t2) {
    this.t1 = t1;
    this.t2 = t2;
    this.weight = (int) (Math.random() * 100);
  }
}

class Grid {
  //make private
  protected final ArrayList<ArrayList<Tile>> grid;
  //protected final ArrayList<Edge> forest;
  protected final HashMap<Tile, ArrayList<Tile>> adjacencyList;

  Grid(ArrayList<ArrayList<Tile>> grid) {
    this.grid = grid;
    //this.forest = this.Kruskal();
    this.adjacencyList = this.getMSTNodes();
  }

  ArrayList<Edge> getEdges() {
    ArrayList<Edge> edges = new ArrayList<>();
    for (int row = 0; row < this.grid.size(); row += 1) {
      for (int col = 0; col < this.grid.get(0).size(); col += 1) {
        if (col != this.grid.get(0).size() - 1) {
          edges.add(new Edge(this.grid.get(row).get(col),
                  this.grid.get(row).get(col + 1)));
        }
        if (row != this.grid.size() - 1) {
          edges.add(new Edge(this.grid.get(row).get(col),
                  this.grid.get(row + 1).get(col)));
        }
      }
    }
    return edges;
  }

  ArrayList<Edge> Kruskal() {
    ArrayList<Edge> edgesInTree = new ArrayList<>();
    ArrayList<Edge> worklist = this.getEdges();
    //util.quickSortEdges(worklist);
    Collections.sort(worklist, new EdgeComparator());
    HashMap<Tile, Tile> representatives = new HashMap<>();

    //initialize with each "node" mapping to itself
    for (int row = 0; row < this.grid.size(); row += 1) {
      for (int col = 0; col < this.grid.get(0).size(); col += 1) {
        representatives.put(this.grid.get(row).get(col),
                this.grid.get(row).get(col));
      }
    }

    while (!worklist.isEmpty()) {
      Edge currEdge = worklist.remove(0);
      if (!findRep(currEdge.t1, representatives).equals(
              findRep(currEdge.t2, representatives))) {
        //not connected, add to forest and union reps
        edgesInTree.add(currEdge);
        Union(findRep(currEdge.t1, representatives),
                findRep(currEdge.t2, representatives),
                representatives);
      }
      //else they are already connected, discard
    }
    return edgesInTree;
  }

  Tile findRep(Tile t, HashMap<Tile, Tile> reps) {
    Tile rep = reps.get(t);
    if (rep.equals(t)) {
      return rep;
    } else {
      return findRep(rep, reps);
    }
  }

  void Union(Tile t1, Tile t2, HashMap<Tile, Tile> reps) {
    Tile r1 = findRep(t1, reps);
    Tile r2 = findRep(t2, reps);
    reps.put(r1, r2);
  }

  HashMap<Tile, ArrayList<Tile>> getMSTNodes() {
    HashMap<Tile, ArrayList<Tile>> adjList = new HashMap<>();
    ArrayList<Edge> edgesInMST = this.Kruskal();
    //initializing all arrayLists
    for (int row = 0; row < this.grid.size(); row += 1) {
      for (int col = 0; col < this.grid.get(0).size(); col += 1) {
        adjList.put(this.grid.get(row).get(col), new ArrayList<>());
      }
    }

    for (Edge e : edgesInMST) {
      adjList.get(e.t1).add(e.t2);
      adjList.get(e.t2).add(e.t1);
    }
    return adjList;
  }
}

class ExamplesMazes {
  Edge e1 = new Edge(new Tile(), new Tile(), 5);
  Edge e2 = new Edge(new Tile(), new Tile(), 2);
  Edge e3 = new Edge(new Tile(), new Tile(), 15);
  Edge e4 = new Edge(new Tile(), new Tile(), 27);
  Edge e5 = new Edge(new Tile(), new Tile(), 1);
  Edge e6 = new Edge(new Tile(), new Tile(), 15);

  ArrayList<ArrayList<Tile>> tiles = new ArrayList<>(Arrays.asList(
          new ArrayList<>(Arrays.asList(new Tile(), new Tile(), new Tile())),
          new ArrayList<>(Arrays.asList(new Tile(), new Tile(), new Tile())),
          new ArrayList<>(Arrays.asList(new Tile(), new Tile(), new Tile()))));

  Grid g1 = new Grid(this.tiles);

  ArrayList<Edge> edges = new ArrayList<>(Arrays.asList(
          this.e1, this.e2, this.e3,
          this.e4, this.e5, this.e6));


  boolean testSortEdges(Tester t) {
    Collections.sort(this.edges, new EdgeComparator());
    return t.checkExpect(edges, new ArrayList<>(Arrays.asList(
            this.e5, this.e2, this.e1, this.e3, this.e6, this.e4)));
  }

  boolean testTileEquality(Tester t) {
    Tile t1 = new Tile();
    Tile t2 = new Tile();
    return t.checkExpect(t1.equals(t2), false);
  }

  /*
  boolean testGetEdges(Tester t) {
    return t.checkExpect(g1.forest.size(), 8);
  }

   */

  boolean testAdjacencyList(Tester t) {
    return t.checkExpect(g1.adjacencyList.get(g1.grid.get(1).get(1)).size(),
            2);
  }
}

/*
Thoughts:
tiles have no identifying characteristics, so i can't really tell if this is making a min spanning tree lol
 */
