// Sam Nosenzo
// 9/20/17


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchAgent {
    private Hashtable<String, State> explored;
    private ArrayList<State> fringe;
    private ArrayList<State> path;
    private int searchType;
    private String searchTypeName;
    private Problem problem;
    private String problemType;
    private State currentState;
    final private int BFS = 0;
    final private int UNICOST = 1;
    final private int GREEDY = 2;
    final private int IDDFS = 3;
    final private int ASTAR = 4;

    public SearchAgent() {
        explored = new Hashtable<>();
        fringe = new ArrayList<>();
        path = new ArrayList<>();
    }

    public void setProblem(String filename) throws IOException {
        File f = new File(filename);
        Scanner fileScan = new Scanner(f);
        problemType = fileScan.nextLine();
        if(problemType.equals("monitor")) {
            problem = new Monitor(fileScan);
        } else if(problemType.equals("aggregation")) {
            problem = new Aggregation(fileScan);
        }
    }

    public void setSearch(String type) {
        if(type.equals("bfs")) {
            searchType = BFS;
        } else if(type.equals("unicost")) {
            searchType = UNICOST;
        } else if(type.equals("greedy")) {
            searchType = GREEDY;
        } else if(type.equals("iddfs")) {
            searchType = IDDFS;
        } else if(type.equals("Astar")) {
            searchType = ASTAR;
        } else {
            System.out.println(type + " is not a valid search type -- please try again");
            System.exit(3);
        }
        searchTypeName = type;
    }

    public void search() {
        currentState = problem.initialState();
        if(searchType == BFS) {
            breadthFirst();
        } else if(searchType == UNICOST) {
            unicost();
        } else if(searchType == GREEDY) {
            searchType = GREEDY;
        } else if(searchType == IDDFS) {
            iddfs();
        } else if(searchType == ASTAR) {
            searchType = ASTAR;
        }
    }

    private boolean breadthFirst() {
        explored.put(currentState.value, currentState);
        path.add(currentState);
        LinkedList<State> fringe = new LinkedList<State>();
        fringe.addAll(currentState.children);
        while(fringe.size() > 0) {

            if(problem.isGoalState(currentState)) {
                System.out.println("Problem solved");
                return true;
            }

            currentState = fringe.poll();
            addFringeStates(fringe, currentState);
            explored.put(currentState.value, currentState);
        }
        return false;
    }

    private boolean unicost() {
        explored.put(currentState.value, currentState);
        path.add(currentState);
        PriorityQueue<State> fringe = new PriorityQueue<State>();
        fringe.addAll(currentState.children);
        while(fringe.size() > 0) {

            if(problem.isGoalState(currentState)) {
                System.out.println("Problem solved");
                return true;
            }

            currentState = fringe.poll();
            addFringeStates(fringe, currentState);
            explored.put(currentState.value, currentState);
        }
        return false;
    }

    // WIP
    private boolean iddfs() {
        int maxDepth = 1;
        int currentDepth = 1;
        explored.put(currentState.value, currentState);
        path.add(currentState);
        PriorityQueue<State> fringe = new PriorityQueue<State>();
        fringe.addAll(currentState.children);
        while(fringe.size() > 0) {

            if(problem.isGoalState(currentState)) {
                System.out.println("Problem solved");
                return true;
            }

            currentState = fringe.poll();
            addFringeStates(fringe, currentState);
            explored.put(currentState.value, currentState);
        }
        return false;
    }

    private void addFringeStates(Collection<State> fringe, State cState) {
        for(int i = 0; i < cState.children.size(); i++) {
            State s = cState.children.get(i);
            if(explored.get(s.value) == null) {
                fringe.add(s);
            }
        }
    }

    private boolean isGoalState(State s) {
        return true;
    }


    public class State {
        private String value;
        private int stateID;
        private double pathCost;
        private ArrayList<State> children;
        private State(String v, int id, ArrayList<State> childnodes) {

        }
    }

    private class Edge {

    }
    public interface Problem {
        ArrayList<State> expand(State currentState);
        boolean isGoalState(State state);
        State initialState();

    }


    private class Monitor implements Problem {
        private String type;
        private ArrayList<Sensor> sensors;
        private ArrayList<Target> targets;
        private Monitor(Scanner fileScan) throws IOException{
            sensors = new ArrayList<>();
            targets = new ArrayList<>();
            monitorInit(fileScan);
        }

        private void monitorInit(Scanner fileScan) throws IOException {

            String line = fileScan.nextLine();
            Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(line);
            while(m.find()) {
                String[] sensorInfo = m.group(1).split(",");
                Sensor s = new Sensor(
                        sensorInfo[0].replace('"', '\u0000'),
                        Integer.parseInt(sensorInfo[1]),
                        Integer.parseInt(sensorInfo[2]),
                        Integer.parseInt(sensorInfo[3]));
                sensors.add(s);
            }

            line = fileScan.nextLine();
            m = Pattern.compile("\\(([^)]+)\\)").matcher(line);
            while(m.find()) {
                String[] targetInfo = m.group(1).split(",");
                Target t = new Target(
                        targetInfo[0].replace('"', '\u0000'),
                        Integer.parseInt(targetInfo[1]),
                        Integer.parseInt(targetInfo[2]));

                targets.add(t);
            }
        }

        public State initialState() {
            StringBuilder stateValue = new StringBuilder("");
            for(int i = 0; i < sensors.size(); i++) {
                stateValue.append(i + " ");
                stateValue.append("\n");
            }
            return new State("", 3, new ArrayList<State>());
        }

        @Override
        public boolean isGoalState(State state) {
            return true;
        }

        @Override
        public ArrayList<State> expand(State state) {
            return new ArrayList<State>();
        }

        private double getDistance(Sensor s, Target t) {
            double aSquared = Math.pow(s.locX - t.locX, 2);
            double bSquared = Math.pow(s.locY - t.locY, 2);
            double distance = Math.sqrt(aSquared + bSquared);
            return distance;
        }

        private class Sensor {
            private String id;
            private int locX;
            private int locY;
            private int power;

            private Sensor(String name, int x, int y, int p) {
                id = name;
                locX = x;
                locY = y;
                power = p;
            }
        }

        private class Target {
            private String id;
            private int locX;
            private int locY;

            private Target(String name, int x, int y) {
                id = name;
                locX = x;
                locY = y;
            }
        }


    }




    private class Aggregation implements Problem{
        private String type;
        private HashMap<String, Node> nodes;
        private Aggregation(Scanner fileScan) throws IOException{
            nodes = new HashMap<>();

        }

        private void aggregationInit(Scanner fileScan) throws IOException {

            String line = fileScan.nextLine();
            Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(line);
            while(m.find()) {
                String[] nodeInfo = m.group(1).split(",");
                Node n = new Node(nodeInfo[0].replace('"', '\u0000'), Integer.parseInt(nodeInfo[1]), Integer.parseInt(nodeInfo[2]));
                nodes.put(n.id, n);
            }

            while(fileScan.hasNextLine()) {
                line = fileScan.nextLine();
                m = Pattern.compile("\\(([^)]+)\\)").matcher(line);
                while(m.find()) {
                    String[] connectInfo = m.group(1).split(",");
                    connectInfo[0] = connectInfo[0].replace('"', '\u0000');
                    connectInfo[1] = connectInfo[1].replace('"', '\u0000');
                    nodes.get(connectInfo[0]).addConnection(nodes.get(connectInfo[1]), Integer.parseInt(connectInfo[2]));
                    nodes.get(connectInfo[1]).addConnection(nodes.get(connectInfo[0]), Integer.parseInt(connectInfo[2]));

                }
            }
        }

        public State initialState() {
            StringBuilder stateValue = new StringBuilder("");

            return new State("", 3, new ArrayList<State>());
        }

        @Override
        public boolean isGoalState(State state) {
            return true;
        }

        @Override
        public ArrayList<State> expand(State currentState) {
            return new ArrayList<State>();
        }


        private class Node {
            private String id;
            private int locX;
            private int locY;
            private ArrayList<Connection> children;

            private Node(String name, int x, int y) {
                id = name;
                locX = x;
                locY = y;
                children = new ArrayList<>();
            }

            private void addConnection(Node n, int cost) {
                Connection c = new Connection(n, cost);
                children.add(c);
            }

            private class Connection {
                private Node end;
                private int cost;
                private Connection(Node e, int c) {
                    end = e;
                    cost = c;
                }
            }
        }
    }
}
