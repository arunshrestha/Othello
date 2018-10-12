import java.util.Date;
import java.util.AbstractSet;

public class ACOthelloPlayer extends OthelloPlayer implements MiniMax{

    private int depthLimit = 4;
    private static int staticEvaluations = 0;
    private static int totalSuccessors = 0;
    private static int exploredSuccessors = 0;
    private static int totalParents = 0;

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
        GameState.Player currentPlayer = currentState.getCurrentPlayer();

        int evaluation = Integer.MAX_VALUE;

        for (GameState state : successors) {
            int curEval = minValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);

            if (curEval < evaluation) {
                evaluation = curEval;
                optimalState = state;
            }
        }

        if (optimalState == null) return null;

        return optimalState.getPreviousMove();

    }

    private boolean isTerminalState(GameState state, int depth) {

        if (depthLimit != -1 && depth >= depthLimit) return true;

        if(state.getStatus() != GameState.GameStatus.PLAYING) return true;

        return false;

    }

    public int maxValue(GameState state, int a, int b, int depth) {
        if (isTerminalState(state, depth)) {
            return staticEvaluator(state);
        }

        int v = Integer.MIN_VALUE;
        AbstractSet<GameState> successors = state.getSuccessors(true);
        totalSuccessors += successors.size();
        totalParents++;
        depth++;

        for (GameState s : successors) {
            if ( s == null) continue;

            exploredSuccessors++;
            v = Math.max(v, (minValue(s,a, b, depth)));

            if (v >= b) return v;

            a = Math.max(v, a);
        }

        return v;

    }

    public int minValue(GameState state, int a, int b, int depth) {
        if (isTerminalState(state, depth)) {
            return staticEvaluator(state);
        }

        int v = Integer.MAX_VALUE;
        AbstractSet<GameState> successors = state.getSuccessors(true);
        totalSuccessors+= successors.size();
        totalParents++;
        depth++;
        for (GameState s : successors) {
            if (s == null) continue;
            exploredSuccessors++;
            v = Math.min(v, (maxValue(s, a, b, depth)));
            if (v <= a) return v;
            b = Math.min(v, b);
        }
        return v;

    }

    @Override
    public int staticEvaluator(GameState state) {
        staticEvaluations++;
        return state.getScore(state.getCurrentPlayer());
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
