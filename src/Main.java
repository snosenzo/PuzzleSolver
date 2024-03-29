
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {
        String filename = args[0];
        String searchType = args[1];

        SearchAgent a = new SearchAgent();
        a.setProblem(filename);
        a.setSearch(searchType);
        a.search();

    }

    public interface Problem {
        ArrayList<SearchAgent.Edge> expand(SearchAgent.State currentState);
        boolean isGoalState(SearchAgent.State state);
        SearchAgent.State initialState();
        String getReadableValue(int[] config);
        double heuristic(SearchAgent.State s);
    }

    public static class SearchAgent {

        private Hashtable<Integer, State> explored;
        private ArrayList<State> path;
        private int searchType;
        private String searchTypeName;
        private Problem problem;
        private String problemType;
        private State currentState;
        PrintWriter writer;

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
            } else if(problemType.equals("Aggregation")) {
                problem = new Aggregation(fileScan);
            } else if(problemType.equals("pancakes")) {
                problem = new Pancakes(fileScan);
            } else {
                System.out.println("Problem type not found");
            }
            System.out.println(problemType);
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

            try {
                writer = new PrintWriter(problemType + "-" + searchTypeName + "-out.txt");
            } catch (FileNotFoundException e) {
                System.out.println(e.toString());
                System.exit(23423);
            }
        }

        public void search() {
            currentState = problem.initialState();

            if(searchType == BFS) {
                if(!breadthFirst()) {
                    writer.println("Solution not found.");
                    writer.println("Nodes explored: " + path.size());
                };
            } else if(searchType == UNICOST) {
                if(!unicost()) {
                    writer.println("Solution not found.");
                    writer.println("Nodes explored: " + path.size());
                }
            } else if(searchType == GREEDY) {
                if(!unicost()) {
                    writer.println("Solution not found.");
                    writer.println("Nodes explored: " + path.size());
                }
            } else if(searchType == IDDFS) {
                if(!iddfs()) {
                    writer.println("Solution not found.");
                    writer.println("Nodes explored: " + path.size());
                };
            } else if(searchType == ASTAR) {
                if(!unicost()) {
                    writer.println("Solution not found.");
                    writer.println("Nodes explored: " + path.size());
                }
            }
            writer.close();

        }

        private boolean breadthFirst() {
            explored.put(currentState.hashValue, currentState);
            path.add(currentState);
            int maxFringeSize = 0;
            currentState.addChildren();
            LinkedList<Edge> fringe = new LinkedList<>();
            fringe.addAll(currentState.children);
            while(fringe.size() > 0) {

                writer.println(currentState.toString());
                if(problem.isGoalState(currentState)) {
                    printResults(fringe.size() + path.size(), maxFringeSize, path.size(), currentState.cost);
                    return true;
                }

                currentState = fringe.poll().end;
                currentState.addChildren();
                addFringeStates(fringe, currentState);
                explored.put(currentState.hashValue, currentState);
                path.add(currentState);
                maxFringeSize = Math.max(maxFringeSize, fringe.size());
            }
            return false;
        }

        private boolean unicost() {
            explored.put(currentState.hashValue, currentState);
            path.add(currentState);
            int maxFringeSize = 0;
            currentState.addChildren();
            PriorityQueue<Edge> fringe = new PriorityQueue<Edge>(currentState.children.get(0));
            fringe.addAll(currentState.children);
            while(fringe.size() > 0) {
                writer.println(currentState.toString());

                if(searchType == GREEDY || searchType == ASTAR) {
//                    writer.println("heuristic value: " + currentState.heuristicCost);
                }
                if(problem.isGoalState(currentState)) {
                    printResults(fringe.size() + path.size(), maxFringeSize, path.size(), currentState.cost);
                    return true;
                }

                currentState = fringe.poll().end;
                currentState.addChildren();
                addFringeStates(fringe, currentState);
                path.add(currentState);
                explored.put(currentState.hashValue, currentState);
                maxFringeSize = Math.max(maxFringeSize, fringe.size());

            }
            return false;
        }

        private boolean iddfs() {
            int maxDepth = 1;
            int maxFringeSize = 0;
//        int currentDepth = 0;
            currentState.addChildren();
            explored.put(currentState.hashValue, currentState);
            path.add(currentState);
            LinkedList<Edge> fringe = new LinkedList<>();
            fringe.addAll(currentState.children);
            while(maxDepth < Integer.MAX_VALUE) {
                writer.println("Max depth: " + maxDepth);
                writer.println(currentState.toString());
                while (fringe.size() > 0) {


                    currentState = fringe.remove(fringe.size() - 1).end;
                    path.add(currentState);
                    explored.put(currentState.hashValue, currentState);
                    writer.println(currentState.toString());
                    if (problem.isGoalState(currentState)) {
                        printResults(fringe.size() + path.size(), maxFringeSize, path.size(), currentState.cost);
                        return true;
                    }
                    currentState.addChildren();
                    if( currentState.depth < maxDepth) {
                        addFringeStates(fringe, currentState);
                    }
                    maxFringeSize = Math.max(maxFringeSize, fringe.size());
                }
                currentState = path.get(0);
                path.clear();
                path.add(currentState);
                explored.clear();
                explored.put(currentState.hashValue, currentState);
                fringe.clear();
                fringe.addAll(currentState.children);
                maxDepth++;
            }

            writer.println("Reached maximum depth: ");
            return false;
        }

        private void printResults(int nodesCreated, int maxFrontier, int exploredNum, double finalCost){
            writer.println("Problem solved");
            writer.println("Time: " + nodesCreated);
            writer.println("Space -- " + "Max Frontier: " + maxFrontier + " | Visited: "  + exploredNum);
            writer.println("Final path cost: " + finalCost);
        }

        private void addFringeStates(Collection<Edge> fringe, State cState) {
            for(int i = 0; i < cState.children.size(); i++) {
                Edge e = cState.children.get(i);
                if(explored.get(e.end.hashValue) == null) {
                    fringe.add(e);
                }
            }
        }

        public class State {
            private int[] value;
            private int hashValue;
            private double heuristicCost;
            private ArrayList<Edge> children;
            private double cost;
            private int depth;


            private State(int[] conf, double c) {
                value = conf.clone();
                hashValue = Arrays.hashCode(value);
                cost = c;
            }
            private State(int[] conf, int hashCode, double c) {
                value = conf.clone();
                hashValue = hashCode;
                cost = c;
            }

            private void addChildren() {

                children = problem.expand(this);
                if(searchType == GREEDY || searchType == ASTAR) {
                    for(Edge c: children) {
                        c.end.setHeuristic();
                    }
                }
            }

            public String toString(){
                return problem.getReadableValue(value);
            }

            private void setDepth(int d) {
                depth = d;
            }

            private void setHeuristic(){
                heuristicCost = problem.heuristic(this);
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
                    if(problemType.equals("monitor")) {
                        if (e1.cost > e2.cost) {
                            return -1;
                        } else if (e1.cost < e2.cost) {
                            return 1;
                        }
                    } else {
                        if (e1.cost < e2.cost) {
                            return -1;
                        } else if (e1.cost > e2.cost) {
                            return 1;
                        }
                    }
                } else if (searchType == GREEDY) {
                    if(problemType.equals("monitor")) {
                        if (e1.end.heuristicCost > e2.end.heuristicCost) {
                            return -1;
                        } else if (e1.end.heuristicCost < e2.end.heuristicCost) {
                            return 1;
                        }
                    } else {
                        if (e1.end.heuristicCost < e2.end.heuristicCost) {
                            return -1;
                        } else if (e1.end.heuristicCost > e2.end.heuristicCost) {
                            return 1;
                        }
                    }
                } else if (searchType == ASTAR) {
                    if(problemType.equals("monitor")) {
                        if (e1.end.heuristicCost+e1.end.cost > e2.end.heuristicCost+e2.end.cost) {
                            return -1;
                        } else if (e1.end.heuristicCost+e1.end.cost < e2.end.heuristicCost+e2.end.cost) {
                            return 1;
                        }
                    } else {
                        if (e1.end.heuristicCost+e1.end.cost < e2.end.heuristicCost+e1.end.cost) {
                            return -1;
                        } else if (e1.end.heuristicCost+e1.end.cost > e2.end.heuristicCost+e2.end.cost) {
                            return 1;
                        }
                    }
                }
                return 0;
            }
        }


        private class Monitor implements Problem {
            private String type;
            private ArrayList<Sensor> sensors;
            private ArrayList<Target> targets;
            private Hashtable<Integer, State> createdStates;
            private Monitor(Scanner fileScan) throws IOException{
                sensors = new ArrayList<>();
                targets = new ArrayList<>();
                createdStates = new Hashtable<>();
                monitorInit(fileScan);
            }

            private void monitorInit(Scanner fileScan) throws IOException {

                String line = fileScan.nextLine();
                // This finds everything with parentheses
                Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(line);
                while(m.find()) {
                    String[] sensorInfo = m.group(1).split(",");
                    Sensor s = new Sensor(
                            sensorInfo[0].replace("\"", ""),
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
                            targetInfo[0].replace("\"", ""),
                            Integer.parseInt(targetInfo[1]),
                            Integer.parseInt(targetInfo[2]));

                    targets.add(t);
                }
            }

            public State initialState() {
                int[] sensorTargets = new int[sensors.size()];
                for(int i = 0; i < sensors.size(); i++) {
                    sensorTargets[i] = 0; // all point to first sensor
                }
                State init = new State(sensorTargets, totalCost(sensorTargets));
                createdStates.put(init.hashValue, init);
                init.setDepth(0);
                return init;
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
                    for(int j = childConfig[i] + 1; j%targets.size() != state.value[i]; j++) {
                        // This prevents one extra comparison each time (doing modular
                        childConfig[i] = j%targets.size();
//                    System.out.println(getReadableValue(childConfig));
                        int hash = Arrays.hashCode(childConfig);
                        State s = createdStates.get(hash);
                        if(s == null){ // If the state has been created
                            s = new State(childConfig, hash, totalCost(childConfig));
                            s.setDepth(state.depth + 1);
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
//            System.out.println(s.locX + ", " +  t.locX);
//            System.out.println(s.locY + ", " +  t.locY);
//            System.out.println(distance);
                return distance;
            }

            private double totalCost(int[] config) {
                double total = Double.MAX_VALUE;
                double[] sensorCost = new double[targets.size()];
                Arrays.fill(sensorCost, 0);
                for(int i = 0; i < config.length; i++){
                    Target t = targets.get(config[i]);
                    Sensor s = sensors.get(i);
                    double d = getDistance(s, t);
                    double time = s.power/d;
                    sensorCost[config[i]]+=time;
                    // only pay attention to minimum target up-time
                    total = Math.min(sensorCost[config[i]], total);
                }
//            System.out.println("Total cost for state: " + total);
                return total;
            }

            // Goal is to maximize the heuristic so that it can be incorporated with the cost function
            public double heuristic(State state) {
                boolean[] targetMonitored = new boolean[targets.size()];
                Arrays.fill(targetMonitored, false);
                int enumerator = 0;
                for(int i = 0; i < state.value.length; i++) {
                    targetMonitored[state.value[i]] = true;
                }
                for(int i = 0; i < targetMonitored.length; i++) {
                    if(targetMonitored[i]) enumerator++;
                }
                return enumerator;
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




        private class Aggregation implements Problem {
            private String type;
            private ArrayList<Node> nodes;
            private Hashtable<String, Node> nodeTable;

            private Aggregation(Scanner fileScan) throws IOException{
                nodes = new ArrayList<>();
                nodeTable = new Hashtable<>();
                type = "aggregation";
                aggregationInit(fileScan);

            }

            private void aggregationInit(Scanner fileScan) throws IOException {

                String line = fileScan.nextLine();
                Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(line);
                int nodesArrayIndex = 0;
                while(m.find()) {
                    String[] nodeInfo = m.group(1).split(",");
                    Node n = new Node(nodeInfo[0].replace("\"", ""), nodesArrayIndex, Integer.parseInt(nodeInfo[1]), Integer.parseInt(nodeInfo[2]));
                    nodes.add(n);
                    nodesArrayIndex++;
                    nodeTable.put(n.name, n);
                }

                while(fileScan.hasNextLine()) {
                    line = fileScan.nextLine();
                    m = Pattern.compile("\\(([^)]+)\\)").matcher(line);
                    while(m.find()) {
                        String[] connectInfo = m.group(1).split(",");
                        connectInfo[0] = connectInfo[0].replace("\"", "");
                        connectInfo[1] = connectInfo[1].replace("\"", "");
                        nodeTable.get(connectInfo[0]).addConnection(nodeTable.get(connectInfo[1]), Integer.parseInt(connectInfo[2]));
                        nodeTable.get(connectInfo[1]).addConnection(nodeTable.get(connectInfo[0]), Integer.parseInt(connectInfo[2]));
                    }
                }
            }

            // TODO FIX
            public String getReadableValue(int[] config) {
                StringBuilder s = new StringBuilder("");
                for(int i = 0; i < config.length; i++) {
                    if(config[i] == -1 ){
                        break;
                    }
                    s.append(nodes.get(config[i]).name);
                    if(i < config.length - 1) s.append(",");
                }
                return s.toString();
            }

            public State initialState() {
                int[] config = new int[nodes.size()];
                Arrays.fill(config, -1);
                State s = new State(config, 0);
                s.setDepth(0);
                s.addChildren();
                return s;
            }

            @Override
            public boolean isGoalState(State state) {
                int[] config = state.value;
                boolean[] visitAllNodes = new boolean[nodes.size()];
                Arrays.fill(visitAllNodes, false);
                for(int i = 0; i < config.length; i++) {
                    if(config[i] == -1 ) {
                        return false;
                    }
                    if(visitAllNodes[config[i]]) {
                        return false;
                    }
                    visitAllNodes[config[i]] = true;
                }
                return true;
            }

            @Override
            public ArrayList<Edge> expand(State currentState) {
                ArrayList<Edge> children = new ArrayList<Edge>();
                if(currentState.value[0] == -1) {
                    int[] config = currentState.value.clone();
                    for(int i = 0; i < nodes.size(); i++) {
                        config[0] = i;
                        State s = new State(config, 0);
                        s.setDepth(currentState.depth + 1);
                        children.add(new Edge(s, 0));
                    }
                } else {
                    int[] config = currentState.value.clone();
                    int pathIndex = -1;

                    for (int i = 0; i < currentState.value.length; i++) {
                        if (config[i] == -1) {
                            pathIndex = i;
                            break;
                        }
                    }

                    if(pathIndex == -1) {
                        return children;
                    }

                    Node branchNode = nodes.get(config[pathIndex-1]);
                    for(int i = 0; i < branchNode.connections.size(); i++) {
                        Connection c = branchNode.connections.get(i);
                        if(config[pathIndex - 1] != c.end.id) {
                            config[pathIndex] = c.end.id;
                            State s = new State(config, currentState.cost + c.cost);
                            s.setDepth(currentState.depth + 1);
                            Edge e = new Edge(s, c.cost);
                            children.add(e);
                        }
                    }
                }
                return children;
            }

            private double calculateTotalCost(int[] config) {
                double pathCost = 0;
                // If there is no first node -- then the path cost will be infinite because no edges have been used yet
                if(config[0] < 0 || config[1] < 0) {
                    return Integer.MAX_VALUE;
                } else {
                    // Find edges
                    Node n1 = nodes.get(config[0]);
                    for(int pathIndex = 0; pathIndex < config.length-1; pathIndex++) {
                        if(config[pathIndex] > 0) {
                            n1 = nodes.get(config[pathIndex]);
                            for (int i = 0; i < n1.connections.size(); i++) {
                                Connection c = n1.connections.get(i);
                                if (c.end.id == config[pathIndex]) {
                                    pathCost += c.cost;
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
                return pathCost;
            }

            // returns how many nodes still need to be visited
            // goal is to minimize
            public double heuristic(State state) {
                int[] config = state.value;
                boolean[] visitAllNodes = new boolean[nodes.size()];
                Arrays.fill(visitAllNodes, false);
                for(int i = 0; i < config.length; i++) {
                    if(config[i] == -1 ) {
                        break;
                    }
                    visitAllNodes[config[i]] = true;
                }
                double enumerator = config.length;

                for(int i = 0; i < config.length; i++) {
                    if(visitAllNodes[i]) {
                        enumerator--;
                    }
                }

                return enumerator;
            }

            public class Node {
                private String name;
                private int id;
                private int locX;
                private int locY;
                private ArrayList<Connection> connections;

                private Node(String name, int ID, int x, int y) {
                    this.name = name;
                    id = ID;
                    locX = x;
                    locY = y;
                    connections = new ArrayList<>();
                }

                private void addConnection(Node n, int cost) {
                    Connection c = new Connection(n, cost);
                    connections.add(c);
                }
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

        private class Pancakes implements Problem {

            int numPancakes;
            int[] initialConfig;
            Hashtable<Integer, State> createdStates;
            String type;

            private Pancakes(Scanner fileScan) {
                type = "aggregation";
                createdStates = new Hashtable<>();
                pancakesInit(fileScan);
            }

            private void pancakesInit(Scanner fileScan) {
                String pStackLine = fileScan.nextLine();
                Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(pStackLine);
                while(m.find()) {
                    String[] pStack = m.group(1).split(",");
                    numPancakes = pStack.length;
                    initialConfig = new int[numPancakes];
                    for (int i = 0; i < numPancakes; i++) {
                        initialConfig[i] = Integer.parseInt(pStack[i]);
//                        System.out.println(initialConfig[i]);
                    }
                }
            }

            public boolean isGoalState(State s) {
                for(int i = 0; i < s.value.length; i++ ){
                    if(s.value[i] != i+1) {
                        return false;
                    }
                }
                return true;
            }

            public String getReadableValue(int[] config) {
                StringBuilder s = new StringBuilder("");
                for(int i = 0; i < config.length; i++) {
                    s.append(config[i] + "");
                    if(i < config.length - 1) s.append(",");
                }
                return s.toString();
            }

            public State initialState() {
                State s = new State(initialConfig, 0);
                createdStates.put(Arrays.hashCode(initialConfig), s);
                s.setDepth(0);
                return s;
            }

            public ArrayList<Edge> expand(State currentState) {

                ArrayList<Edge> children =  new ArrayList<Edge>();
                for(int i = 1; i <= currentState.value.length; i++) {
                    int[] childConfig = flipPancakes(currentState.value, i);
                    int hashCode =Arrays.hashCode(childConfig);
                    State child = createdStates.get(hashCode);
                    if(child == null) {
                        child = new State(childConfig, hashCode, currentState.cost + 1.0);
                        createdStates.put(hashCode, child);
                        child.setDepth(currentState.depth + 1);
                        Edge e = new Edge(child, child.cost - currentState.cost);
                        children.add(e);
                    }

                }

                return children;
            }

            // loc is the index after the last pancake being flipped (i.e. the pancake @ loc does not get flipped)
            private int[] flipPancakes(int[] config, int loc) {
                int[] flipped = config.clone();
                for(int i = 0; i < loc; i++) {
                    flipped[i] = -1*config[loc - i - 1];
                }
                return flipped;
            }

            // heuristic should prefer the minimum value
            public double heuristic(State s) {
                int[] config = s.value;
                int enumerator = 0; // # of values out of place
                for(int i = config.length-1; i >= 0; i--) {
                    // if a value is out of place
                    if(config[i] != i + 1) {
                        return i+1;
                    }
                }
//                // This heuristic allows for the preference of a negative reverse order
//                // This would eventually allow for the higher heuristic of the correct order and orientation
//                for(int i = config.length-1; i >= 0; i--) {
//                    if(config[config.length-1-i] != -1*(i+1)) {
//                        enumerator+=1;
//                    }
//                }

                return 0;
            }

        }
    }

}


