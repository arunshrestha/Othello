import java.util.Date;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Date;

public class ACOthelloPlayer extends OthelloPlayer implements MiniMax{

    private int depthLimit = -1;
    private static int staticEvaluations = 0;
    private static int totalSuccessors = 0;
    private static int exploredSuccessors = 0;
    private static int totalParents = 0;
    private HashMap<GameState, Integer> visited = new HashMap<GameState, Integer>();

    public ACOthelloPlayer (String name) {
        super(name);
    }

    public ACOthelloPlayer (String name, int depthLimit) {
        super(name);
        this.depthLimit = depthLimit;
    }

    //the get move should implement minimaxAlgorithm
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

    private boolean isTerminalState(GameState state, int depth, long startTime,  Date deadline) {

        if (depthLimit != -1 && depth >= depthLimit) return true;
        if (state.getScore(state.getCurrentPlayer()) + state.getScore(state.getOpponent(state.getCurrentPlayer())) >= 64) return true;
        if(state.getStatus() != GameState.GameStatus.PLAYING) return true;
        if (deadline != null &&  System.currentTimeMillis() - startTime >= deadline.getTime()) return true;

        return false;

    }

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

    @Override
    public int staticEvaluator(GameState state) {
        if (state == null) return 0;
        if (visited.containsKey(state)) {
            return visited.get(state); //Memo
        }
        int staticEvaluation = state.getValidMoves().size();
//        int staticEvaluation = state.getScore(state.getCurrentPlayer());
        staticEvaluations++;
        visited.put(state, staticEvaluation);

//        return staticEvaluation;
        return staticEvaluation;

    }

    @Override
    public int getNodesGenerated() {
        return exploredSuccessors;
    }

    @Override
    public int getStaticEvaluations() {
        return staticEvaluations;
    }

    @Override
    public double getAveBranchingFactor() {
        return (double)totalSuccessors/(double)totalParents;
    }
    @Override
    public double getEffectiveBranchingFactor() {
        return (double)exploredSuccessors/(double)totalParents;
    }
}
