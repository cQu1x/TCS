//WebSites that I used: https://translate.yandex.ru/
//                   https://tcs.innopolis.university/docs/lab_10/
import java.util.*;

public class Main {
    static final String DETERMIN = "[deterministic]";
    static final String NONDETERM = "[non-deterministic]";
    public static void main(String[] args) {
        //Below there are all initial validations
        Graph graph = new Graph();
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        String type =validateType(input);
        if (type==null){
            return;
        }
        input = sc.nextLine();
        String[] states = validateStates(input);
        if (states==null){
            return;
        }
        //Here I ensure that there are no repeating states that are accepted by the FSA
        ArrayList<String> setStates = new ArrayList<>();
        for (int i = 0; i < states.length; i++) {
            if (!setStates.contains(states[i])){
                setStates.add(states[i]);
            }
        }
        String[] clearStates = setStates.toArray(new String[setStates.size()]);
        for (String name : clearStates) {
            graph.addState(new State(name));
        }
        input = sc.nextLine();
        String[] alphabet = validateAlphabet(input);
        if (alphabet==null){
            return;
        }
        if (Arrays.asList(alphabet).contains("eps")&&type.equals("deterministic")){
            System.out.println("FSA is non-deterministic.");
            return;
        }
        //The check for initial state being a part of states
        input = sc.nextLine();
        String initial = validateInitial(input);
        if (initial==null){
            return;
        }
        if (!Arrays.asList(states).contains(initial)) {
            System.out.println("A state '" + initial + "' is not in the set of states.");
            return;
        }
        input = sc.nextLine();
        String[] accepting = validateAccepting(input);
        if (accepting==null){
            return;
        }
        //Check to ensure that accepting states are not repeating as well
        ArrayList<String> setAccepting = new ArrayList<>();
        for (int i = 0;i<accepting.length;i++){
            if (!setAccepting.contains(accepting[i])){
                setAccepting.add(accepting[i]);
            }
        }
        String[] clearAccepting = setAccepting.toArray(new String[setAccepting.size()]);
        ArrayList<String> stat = new ArrayList<>();
        for (String state : states) {
            stat.add(state);
        }
        //The check for all final states being a part of states
        for (String acceptingState:accepting) {
            if (!stat.contains(acceptingState)){
                System.out.println(("A state '" + acceptingState + "' is not in the set of states."));
                return;
            }
        }
        input = sc.nextLine();
        ArrayList<Transition> transitions = validateTransitions(input,alphabet,graph);
        if (transitions==null){
            return;
        }
        //Making sure that there are no repeating transitions
        Set<Transition> transSet = new HashSet<>();
        for (Transition transition:transitions) {
            for (Transition trans:transSet){
                if (trans.from.equals(transition.from) && trans.to.equals(transition.to) && trans.symbol.equals(transition.symbol)){
                    System.out.println("Input is malformed.");
                    return;
                }
            }
                transSet.add(transition);
        }
        Arrays.sort(clearAccepting,Comparator.naturalOrder());
        FSA fsa;
        if (type.equals("deterministic")){
            fsa = new FSA(Type.DETERMINISTIC,alphabet,clearStates,initial,clearAccepting,transitions);
        }
        else{
            fsa = new FSA(Type.NONDETERMINISTIC,alphabet,clearStates,initial,clearAccepting,transitions);
        }
        String symbCheck = fsa.symbolInAlph();
        if (symbCheck!=null){
            System.out.println("A transition symbol '"+symbCheck+ "' is not in the alphabet.");
            return;
        }
        if (fsa.epsInDeterministic()){
            System.out.println("FSA is non-deterministic.");
            return;
        }
        graph.DFS(graph.findSymbol(initial));
        if (graph.areDisjoint()){
            System.out.println("Some states are disjoint.");
            return;
        }
        //Initialisation of the first two-dimensional array for the Kleene algorithm
        //And invocation of the algorithm itself
        String[][] R = new String[fsa.states.length][fsa.states.length];
        String[][] finalR = fsa.RunKleene(R, -1,graph);
        Map<String,Integer> idx = new HashMap<>();
        for (int i = 0; i < clearStates.length; i++)
        { idx.put(clearStates[i], i);
        }
        //Compilation of the final answer
        StringBuilder sb = new StringBuilder();
        int i0 = idx.get(initial);
        for (String acc : clearAccepting) {
            int j = idx.get(acc);
            sb.append(finalR[i0][j]).append("|");
        }
        System.out.println(sb.deleteCharAt(sb.length()-1).toString());
    }

    /**
     * Function that validates the type of FSA that is provided from input
     * @param string input string
     * @return returns string for corresponding type of FSA if the type is valid and null otherwise
     */
    static String validateType(String string){
        String FSAtype;
        if (string.length()>=20){
            String type = string.substring(0,5);
            if (!type.equals("type=")){
                System.out.println("Input is malformed.");
                return null;
            }
            String checkdet = string.substring(5);
            if (checkdet.equals(DETERMIN)){
                FSAtype = "deterministic";
                return FSAtype;
            }
            else if (checkdet.equals(NONDETERM)){
                FSAtype = "non-deterministic";
                return FSAtype;
            }
            else{
                System.out.println("Input is malformed.");
                return null;
            }
        }
        System.out.println("Input is malformed.");
        return null;
    }

    /**
     * Function that validates the set of states, that is provided in input
     * @param string input string
     * @return returns null if the input does not follow the requirements and the array of states in String format
     * otherwise
     */
    static String[] validateStates(String string){
        String validStates = "^states=\\[\\w+(,\\w+)*\\]$";
        if (!string.matches(validStates)){
            System.out.println("Input is malformed.");
            return null;
        }
        String[] states = string.substring(8,string.length()-1).split(",");
        return states;
    }

    /**
     * Function that validates the alphabet, that is provided in input
     * @param string input string
     * @return returns null if the input does not follow the requirements and the array of alphabet symbols
     * in String format otherwise
     */
    static String[] validateAlphabet(String string){
        String validAlphabet = "^alphabet=\\[\\w+(,\\w+)*\\]$";
        if (!string.matches(validAlphabet)){
            System.out.println("Input is malformed.");
            return null;
        }
        String[] alphabet = string.substring(10,string.length()-1).split(",");
        return alphabet;
    }

    /**
     * Function that validates the initial state, that is provided in input
     * @param string input string
     * @return returns null if the input does not follow the requirements and the initial string otherwise
     */
    static String validateInitial(String string){
        String validInit = "^initial=\\[\\w+\\]$";
        String validFormat = "^initial=\\[(.*?)\\]$";
        if (!string.matches(validFormat)){
            System.out.println("Input is malformed.");
            return null;
        }
        else if (!string.matches(validInit)){
            System.out.println("Initial state is not defined.");
            return null;
        }
        String initial = string.substring(9,string.length()-1);
        return initial;
    }

    /**
     * Function that validates the set of accepting states, that are provided in input
     * @param string input string
     * @return returns null is the input does not follow the requirements and the array of accepting states in string
     * format otherwise
     */
    static String[] validateAccepting(String string){
        String validAccepting = "^accepting=\\[\\w+(,\\w+)*\\]$";
         if (string.equals("accepting=[]")){
            System.out.println("Set of accepting states is empty.");
            return null;
        }
        if (!string.matches(validAccepting)){
            System.out.println("Input is malformed.");
            return null;
        }
        String[] accepting = string.substring(11,string.length()-1).split(",");
        return accepting;
    }

    /**
     * Function that validates the transitions, that are provided the input, but also checks whether states and
     * transition symbols are accepted by FSA and adds the transition to the graph for future invocation of traversal
     * that will check if disjointed or not
     * @param string input string
     * @param alphabet alphabet that is accepted by FSA
     * @param graph graph that contains states and will be used to run the traversal
     * @return returns null if an error occurred and a list of Transitions otherwise
     */
    static ArrayList<Transition> validateTransitions(String string,String[] alphabet, Graph graph){
        String validTransitions = "^transitions=\\[(\\w+>\\w+>\\w+(,\\w+>\\w+>\\w+)*)\\]$";
        if (!string.matches(validTransitions)){
            System.out.println("Input is malformed.");
            return null;
        }
        String[] parts = string.substring(13, string.length()-1).split(",");
        ArrayList<Transition> transitions = new ArrayList<>();
        for (String transition : parts) {
            String[] transSplit = transition.split(">");
            if (!Arrays.asList(alphabet).contains(transSplit[1])){
                System.out.println("A transition symbol '" + transSplit[1] + "' is not in the alphabet.");
                return null;
            }
            State from = graph.findState(transSplit[0]);
            if (from == null) {
                System.out.println("A state '" + transSplit[0] + "' is not in the set of states.");
                return null;
            }
            State to = graph.findState(transSplit[2]);
            if (to == null) {
                System.out.println("A state '" + transSplit[2] + "' is not in the set of states.");
                return null;
            }
            Transition t = new Transition(from, transSplit[1], to);
            graph.addTransition(t);
            transitions.add(t);
        }
        return transitions;
    }

}
/**
 * Enum that was used to conveniently store the type of FSA
 */
enum Type{
    DETERMINISTIC,
    NONDETERMINISTIC,
}

/**
 * Class FSA that was created to implement and encapsulate the implementation of Kleene algorithm and also to check the
 * correctness of defining the FSA as "DETERMINISTIC"
 */
class FSA{
    Type type;
    String[] alphabet;
    String[] states;
    String initial;
    String[] accepting;
    ArrayList<Transition> transitions;
    public FSA(Type type, String[] alphabet, String[] states, String initial, String[] accepting,ArrayList<Transition> transitions){
        this.type = type;
        this.alphabet = alphabet;
        this.states = states;
        this.initial = initial;
        this.accepting = accepting;
        this.transitions = transitions;
        Arrays.sort(this.accepting);
    }
    String[][] RunKleene(String[][] R, int k,Graph graph) {
        String[][] Rcur = new String[states.length][states.length];
        if (k == -1) {
            for (int i = 0; i < states.length; i++) {
                for (int j = 0; j < states.length; j++) {
                    ArrayList<String> symbols = new ArrayList<>();
                    for (Transition t : transitions) {
                        if (graph.states.get(i).state.equals(t.from.state) && graph.states.get(j).state.equals(t.to.state)) {
                            symbols.add(t.symbol);
                        }
                    }
                    if (i == j) {
                        symbols.add("eps");
                    }
                    if (!symbols.isEmpty()) {
                        Rcur[i][j] = "(" + String.join("|",symbols) + ")";
                    }
                    else{
                        Rcur[i][j] = "({})";
                    }
                }
            }
            return RunKleene(Rcur, k+1,graph);
        }
        if (k < states.length) {
            for (int i = 0; i < states.length; i++) {
                for (int j = 0; j < states.length; j++) {
                    Rcur[i][j] = "(" + R[i][k] + R[k][k] + "*" + R[k][j] + "|" + R[i][j] + ")";
                }
            }
            return RunKleene(Rcur, k + 1,graph);
        }
        return R;
    }

    public boolean epsInDeterministic() {
        if (type == Type.DETERMINISTIC) {
            for (Transition t : transitions) {
                if (t.symbol.equals("eps")) {
                    return true;
                }
            }
            for (int i = 0; i < transitions.size(); i++) {
                for (int j = i + 1; j < transitions.size(); j++) {
                    Transition t1 = transitions.get(i);
                    Transition t2 = transitions.get(j);
                    if (t1.from.equals(t2.from)
                            && t1.symbol.equals(t2.symbol)
                            && !t1.to.equals(t2.to)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public String symbolInAlph(){
        for (Transition t : transitions){
            boolean flag = false;
            for (int i = 0; i < alphabet.length; i++){
                if (alphabet[i].equals(t.symbol)){
                    flag = true;
                    break;
                }
            }
            if (!flag){
                return t.symbol;
            }
        }
        return null;
    }
}

/**
 * Class Transition that was created to conveniently store the data about one transition. Also this class is used
 * as analogy of Edge in Graph
 */
class Transition{
    State from;
    String symbol;
    State to;
    public Transition(State from, String symbol, State to){
        this.from = from;
        this.symbol = symbol;
        this.to = to;
    }
}

/**
 * Class state that used as Vertex for the Graph
 */
class State{
    String state;
    boolean visited = false;
    ArrayList<Transition> adjacencyList;
    public State(String state){
        this.state = state;
        adjacencyList = new ArrayList<>();
    };
}

/**
 * Class Graph that contains states and transitions similarly to Vertices and Edges. This class used to check whether
 * the states of the FSA are disjoint or not. For this purpose I implemented DFS traversal and the method that
 * checks if all states were visited.
 */
class Graph{
    ArrayList<State> states;
    ArrayList<Transition> transitions;
    Graph(){
        states = new ArrayList<>();
        transitions = new ArrayList<>();
    }
    void addState(State s){
        states.add(s);
    }
    void addTransition(Transition transition){
        transitions.add(transition);
        transition.from.adjacencyList.add(transition);
    }
    public State findSymbol(String symbol){
        for (State s : states){
            if (s.state.equals(symbol)){
                return s;
            }
        }
        return null;
    }
    void DFS(State initial){
        Stack<State> stack = new Stack<>();
        stack.push(initial);
        while (!stack.isEmpty()){
            State state = stack.pop();
            state.visited = true;
            for (Transition transition : state.adjacencyList) {
                if (!transition.to.visited){
                    stack.push(transition.to);
                }
            }
        }
    }
    public State findState(String name) {
        for (State s : states) {
            if (s.state.equals(name)) return s;
        }
        return null;
    }
    boolean areDisjoint(){
        for(State s : states){
            if (!s.visited){
                return true;
            }
        }
        return false;
    }
}