import java.util.Date;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Date;

/** 
 * @author Catalina Ionescu, Aditi Joshi
 *
 * A class for our own OthelloPlayer
 * This player uses minimax algorithm with minimax algorithm with alpha beta pruning 
 * and some additional modifications to choose the best move for the MaxPlayer.
 */

public class ACOthelloPlayer extends OthelloPlayer implements MiniMax{

    private int depthLimit = -1;
    private static int staticEvaluations = 0;
    private static int totalSuccessors = 0;
    private static int exploredSuccessors = 0;
    private static int totalParents = 0;
    
    // To keep track of the visited nodes.
    private HashMap<GameState, Integer> visited = new HashMap<GameState, Integer>();

    /**
     * Constructor 1
     * 
     * @param name the name of the player
     */
    public ACOthelloPlayer (String name) {
        super(name);
    }

    /**
     * Constructor 2
     * 
     * @param name the name of the player
     * @param depthLimit maximum depth that can be explored
     */
    public ACOthelloPlayer (String name, int depthLimit) {
        super(name);
        this.depthLimit = depthLimit;
    }

    /**
     * This method uses iterative deepening to find the best move for MaxPlayer
     * 
     * @param currentState current state of the game
     * @param deadline maximum amount of time the operation can take
     * @return return the best move for MaxPlayer
     */
    @Override
    public Square getMove(GameState currentState, Date deadline) {
        AbstractSet<GameState> successors = currentState.getSuccessors(true);

        GameState optimalState = null;

        int evaluation = Integer.MAX_VALUE;
        int depth = 0;
        while (depthLimit == -1 || depth < depthLimit) {

            Long startTime = System.currentTimeMillis();

            GameState cur = iterativeDeepening(currentState, depth, startTime, deadline);
            int tempEvaluation = staticEvaluator(cur);

            if (tempEvaluation < evaluation) {
                optimalState = cur;
                evaluation = tempEvaluation;
            }

            if (System.currentTimeMillis() - startTime >= deadline.getTime()) break;

            depth++;
        }

        if (optimalState == null) return null;

        return optimalState.getPreviousMove();

    }

    /**
     * Check if the state is terminal, or if the depth limit has been exceeded
     * 
     * @param state the state to be evaluated
     * @param depth current depth of the state
     * @return true if it terminal state, else return false;
     */
    private boolean isTerminalState(GameState state, int depth, long startTime,  Date deadline) {

        if (depthLimit != -1 && depth >= depthLimit) return true;
        if (state.getScore(state.getCurrentPlayer()) + state.getScore(state.getOpponent(state.getCurrentPlayer())) >= 64) return true;
        if(state.getStatus() != GameState.GameStatus.PLAYING) return true;
        if (deadline != null &&  System.currentTimeMillis() - startTime >= deadline.getTime()) return true;

        return false;

    }

    /**
     * 
     * 
     * @param currentState the current state of the game
     * @param depth current depth of the state
     * @param startTime 
     * @param deadline maximum amount of time the operation can take
     * @return 
     */
    public GameState iterativeDeepening(GameState currentState, int depth, long startTime, Date deadline) {
        AbstractSet<GameState> successors = currentState.getSuccessors(true);

        GameState optimalState = null;

        int evaluation = Integer.MAX_VALUE;

        for (GameState state : successors) {
//            int curEval = NegaScout(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depth, startTime, deadline);
            int curEval = minValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depth, startTime, deadline);
            if (curEval < evaluation) {
                evaluation = curEval;
                optimalState = state;
            }

            if (System.currentTimeMillis() - startTime >= deadline.getTime()) break;
        }

        if (optimalState == null) return null;

        return optimalState;
    }

    /**
     * 
     * @param state the state to be evaluated
     * @param depth current depth of the state
     * @param alpha value of the best alternative for max
     * @param beta value of the best alternative for min
     * @param startTime the starting time of the algorithm
     * @param deadline maximum amount of time the operation can take
     * @return
     */
    public int NegaScout (GameState state, int depth, int alpha, int beta, long startTime, Date deadline) {

        if ( depth == 0 || isTerminalState(state, depth, startTime, deadline)) {
            return staticEvaluator(state);
        }
        int score = Integer.MIN_VALUE;
        int n = beta;
        AbstractSet<GameState>successors = state.getSuccessors(true);
        totalSuccessors += successors.size();
        totalParents++;
        for (GameState succ : successors) {
            if (succ == null) continue;
            exploredSuccessors++;
            int cur = -NegaScout(succ, depth - 1, -n, -alpha, startTime, deadline);
            if (cur > score) {
                if (n == beta || depth <= 2) {
                    score = cur;
                } else {
                    score = -NegaScout(succ, depth - 1, -beta, -cur, startTime, deadline);
                }
            }

            if (score > alpha) {
                alpha = score;
            }
            if (alpha >= beta) return alpha;
            n = alpha + 1;
        }

        return score;
    }

    /**
     * It maximizes the value of the evaluation function.
     * 
     * @param state the state to be evaluated
     * @param a value of the best alternative for max
     * @param b value of the best alternative for min
     * @param depth current depth of the state
     * @param startTime the starting time of the algorithm
     * @param deadline maximum amount of time the operation can take
     * @return the maximum value of the evaluation function
     */
    public int maxValue(GameState state, int a, int b, int depth, long startTime, Date deadline ) {
        if (isTerminalState(state, depth, startTime, deadline)) {
            return staticEvaluator(state);
        }

        int v = Integer.MIN_VALUE;
        AbstractSet<GameState> successors = state.getSuccessors(true);
        totalSuccessors += successors.size();
        totalParents++;
        depth++;

        for (GameState s : successors) {
            if ( s == null) continue;
            v = Math.max(v, (minValue(s,a, b, depth, startTime, deadline)));
            if (System.currentTimeMillis() - startTime >= deadline.getTime()) break;
            exploredSuccessors++;
            if (v >= b) return v;

            a = Math.max(v, a);
        }

        return v;

    }

    /**
     * It minimizes the value of the evaluation function.
     * 
     * @param state the state to be evaluated
     * @param a value of the best alternative for max
     * @param b value of the best alternative for min
     * @param depth current depth of the state
     * @param startTime the starting time of the algorithm
     * @param deadline maximum amount of time the operation can take
     * @return the minimum value of the evaluation function
     */
    public int minValue(GameState state, int a, int b, int depth, long startTime, Date deadline) {
        if (isTerminalState(state, depth, startTime, deadline)) {
            return staticEvaluator(state);
        }

        int v = Integer.MAX_VALUE;
        AbstractSet<GameState> successors = state.getSuccessors(true);

        totalSuccessors+= successors.size();
        totalParents++;
        depth++;
        for (GameState s : successors) {
            if (s == null) continue;
            v = Math.min(v, (maxValue(s, a, b, depth, startTime, deadline)));
            if (System.currentTimeMillis() - startTime >= deadline.getTime()) break;
            exploredSuccessors++;
            if (v <= a) return v;
            b = Math.min(v, b);
        }
        return v;

    }

    /**
     * Compute the value of the simple static evaluation function
     * 
     * @state the state to be evaluated
     * @return the value of the simple static evaluation function
     */
    @Override
    public int staticEvaluator(GameState state) {
        if (state == null) return 0;
        
        // keeping track of the visited nodes
        if (visited.containsKey(state)) {
            return visited.get(state); 
        }
        int staticEvaluation = state.getValidMoves().size();
        staticEvaluations++;
        visited.put(state, staticEvaluation);
        
        return staticEvaluation;

    }

    /**
     * Get the number of nodes generated
     * 
     * @return the number of nodes generated.
     */
    @Override
    public int getNodesGenerated() {
        return exploredSuccessors;
    }

    /**
     * Get the number of static evaluations
     * 
     * @return the number of static evaluations performed.
     */
    @Override
    public int getStaticEvaluations() {
        return staticEvaluations;
    }

    /**
     * Get the average branching factor of the nodes that
     * were expanded during the search.
     * 
     * @return the average branching factor.
     */
    @Override
    public double getAveBranchingFactor() {
        return (double)totalSuccessors/(double)totalParents;
    }
    
    /**
     * Get the effective branching factor of the nodes that
     * were expanded during the search.
     * 
     * @return the effective branching factor.
     */
    @Override
    public double getEffectiveBranchingFactor() {
        return (double)exploredSuccessors/(double)totalParents;
    }
}
