// Sam Nosenzo
// 9/20/17


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchAgent {
    private Hashtable<Integer, State> explored;
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
        explored.put(currentState.hashValue, currentState);
        path.add(currentState);
        currentState.addChildren();
        LinkedList<Edge> fringe = new LinkedList<Edge>();
        fringe.addAll(currentState.children);
        while(fringe.size() > 0) {

            if(problem.isGoalState(currentState)) {
                System.out.println("Problem solved");
                return true;
            }

            currentState = fringe.poll().end;
            currentState.addChildren();
            addFringeStates(fringe, currentState);
            explored.put(currentState.hashValue, currentState);
            path.add(currentState);
        }
        return false;
    }

    private boolean unicost() {
        explored.put(currentState.hashValue, currentState);
        path.add(currentState);
        currentState.addChildren();
        PriorityQueue<Edge> fringe = new PriorityQueue<Edge>();
        fringe.addAll(currentState.children);
        while(fringe.size() > 0) {

            if(problem.isGoalState(currentState)) {
                System.out.println("Problem solved");
                return true;
            }

            currentState = fringe.poll().end;
            addFringeStates(fringe, currentState);
            explored.put(currentState.hashValue, currentState);
            path.add(currentState);
        }
        return false;
    }

    private boolean iddfs() {
        int maxDepth = 1;
        int currentDepth = 0;
        currentState.addChildren();
        explored.put(currentState.hashValue, currentState);
        path.add(currentState);
        LinkedList<Edge> fringe = new LinkedList<Edge>();
        fringe.addAll(currentState.children);
        while(maxDepth < Integer.MAX_VALUE) {
            while (fringe.size() > 0 || currentDepth > maxDepth) {

                if (problem.isGoalState(currentState)) {
                    System.out.println("Problem solved");
                    return true;
                }
                currentState = fringe.remove(fringe.size() - 1).end;
                addFringeStates(fringe, currentState);
                explored.put(currentState.hashValue, currentState);
                currentDepth++;
            }
            maxDepth++;
        }

        System.out.println("Reached maximum depth: ");
        return false;
    }

    private void addFringeStates(Collection<Edge> fringe, State cState) {
        for(int i = 0; i < cState.children.size(); i++) {
            Edge e = cState.children.get(i);
            if(explored.get(e.end.hashValue) == null) {
                fringe.add(e);
            }
        }
    }

    private boolean isGoalState(State s) {
        return true;
    }


    public class State {
        private int[] value;
        private int hashValue;
        private ArrayList<Edge> children;
        private double cost;

        private State(int[] conf, double c) {
            value = conf.clone();
            hashValue = Arrays.hashCode(value);
            cost = c;
        }
        private State(int[] conf, int hashCode, double c) {
            value = conf.clone();
            hashValue = hashCode;
        }

        private void addChildren() {
            children = problem.expand(this);
        }

    }
    private class Edge implements Comparator<Edge> {
        private State end;
        private double cost;
        private Edge(State e, double c) {
            end = e;
            cost = c;
        }

        @Override
        public int compare(Edge e1, Edge e2) {
            if(searchType == UNICOST) {
                if(e1.end.cost > e2.end.cost) {
                    return -1;
                } else if(e1.end.cost < e2.end.cost) {
                    return 1;
                }
            } else if (searchType == GREEDY) {
                if(e1.end.cost > e2.end.cost) {
                    return -1;
                } else if(e1.end.cost < e2.end.cost) {
                    return 1;
                }
            }
            return 0;
        }
    }


    public interface Problem {
        ArrayList<Edge> expand(State currentState);
        boolean isGoalState(State state);
        State initialState();

    }


    private class Monitor implements Problem {
        private String type;
        private ArrayList<Sensor> sensors;
        private ArrayList<Target> targets;
        private Hashtable<Integer, State> createdStates;
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
            int[] sensorTargets = new int[sensors.size()];
            StringBuilder stateValue = new StringBuilder("");
            for(int i = 0; i < sensors.size(); i++) {
                sensorTargets[i] = 0; // all point to first sensor
            }

            State init = new State(sensorTargets, totalCost(sensorTargets));
            createdStates.put(init.hashValue, init);
            return init;
        }

        private String getStateValue(int[] sensorTargets) {
            StringBuilder s = new StringBuilder("");
            for(int i = 0; i < sensorTargets.length; i++) {
                s.append(sensorTargets[i]);
                if(i < sensorTargets.length - 1) s.append(",");
            }
            return s.toString();
        }

        public String getReadableValue(int[] sensorTargets) {
            StringBuilder s = new StringBuilder("");
            for(int i = 0; i < sensorTargets.length; i++) {
                s.append(targets.get(sensorTargets[i]).id);
                if(i < sensorTargets.length - 1) s.append(",");
            }
            return s.toString();
        }


        @Override
        public boolean isGoalState(State state) {
            boolean[] targetMonitored = new boolean[targets.size()];
            for(int i = 0; i < state.value.length; i++) {
                targetMonitored[state.value[i]] = true;
            }
            for(int i = 0; i < targetMonitored.length; i++) {
                if(!targetMonitored[i]) return false;
            }
            return true;
        }

        @Override
        public ArrayList<Edge> expand(State state) {
            ArrayList<Edge> children = new ArrayList<>();
            for(int i = 0; i < state.value.length; i++) {
                int[] childConfig = state.value.clone();
                for(int j = childConfig[i] + 1; j%targets.size() != childConfig[i]; j++) {
                    // This prevents one extra comparison each time (doing modular
                    childConfig[i] = j;
                    int hash = Arrays.hashCode(childConfig);
                    State s = createdStates.get(hash);
                    if(s == null){ // If the state has been created
                        s = new State(childConfig, hash, totalCost(childConfig));
                        createdStates.put(hash, s);
                    }
                    Edge e = new Edge(s, s.cost - state.cost);
                    children.add(e);
                }
            }
            return children;
        }

        private double getDistance(Sensor s, Target t) {
            double aSquared = Math.pow(s.locX - t.locX, 2);
            double bSquared = Math.pow(s.locY - t.locY, 2);
            double distance = Math.sqrt(aSquared + bSquared);
            return distance;
        }

        private double totalCost(int[] config) {
            double total = Double.MAX_VALUE;
            for(int i = 0; i < config.length; i++){
                Target t = targets.get(config[i]);
                Sensor s = sensors.get(i);
                double d = getDistance(s, t);
                double time = s.power/d;
                // only pay attention to minimum target up-time
                total = Math.max(time, total);
            }
            return total;
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
            return new State(new int[2], 3, 1);
        }

        @Override
        public boolean isGoalState(State state) {
            return true;
        }

        @Override
        public ArrayList<Edge> expand(State currentState) {
            return new ArrayList<Edge>();
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
