/* 
 * COMP30024 - Project B
 * Andy Phan (aphan1) and Martin Cheong (cheongm)
 * 
 */

package aiproj.hexifence.aphan1;

import java.util.List;
import java.util.Map.Entry;

import aiproj.hexifence.Piece;

/** Learning class, responsible for finding suitable weights,
 * given the features.
 * 
 * GradientLearn.java is responsible for giving samples.
 */
public class LearningTest {
	/** Number of samples to use in our gradient descent learning */
	private static final int NUM_SAMPLES = 100000;
	/** Parameter for learning rate (how fast weights change) */
	private static final double LEARNING_RATE = 0.1;
	
	// weights for our features (some are dummy weights, for testing
	// purposes
	private static double W_CELL = 1, W_CHAIN = 1, W_SCORE = 1,
			W_SCORE_ENEMY = 1, W_DUMMY = 1;
	
	// our player color
	private static final int MY_COLOR = Piece.RED;
	
	public static void main(String[] args) {
		// do tests in dimensions 2 and 3
		System.out.println("First test");
		GradientLearn utilityCalc = new GradientLearn(2, NUM_SAMPLES);
		utilityCalc.minimax_value(new Board(2, MY_COLOR, Piece.BLUE));
		doTest(utilityCalc);

		System.out.println("Second test");
		utilityCalc = new GradientLearn(3, NUM_SAMPLES);
		utilityCalc.minimax_value(new Board(3, MY_COLOR, Piece.BLUE));
		doTest(utilityCalc);

	}
	
	private static void doTest(GradientLearn utilityCalc) {
		for (Entry<Board, Integer> entry : utilityCalc.samples.entrySet()) {
			Board board = entry.getKey();
			int scoreMargin = entry.getValue();
			ChainFinder chain_finder = new ChainFinder(board);

			List<Chain> chains = chain_finder.chains;
			
			double test_eval = getEval(board, chain_finder);
			double difference = test_eval - scoreMargin;
			
			// Update weights			
			W_CHAIN = W_CHAIN - 
					LEARNING_RATE* difference * f_getChain(board, chains);
			W_SCORE = W_SCORE - 
					LEARNING_RATE* difference *f_getScore(board);
			W_SCORE_ENEMY = W_SCORE_ENEMY - 
					LEARNING_RATE* difference * f_getEnemyScore(board);
			W_DUMMY = W_DUMMY - 
				LEARNING_RATE* difference * f_getDummy(board, chain_finder);
			
			if (Double.isNaN(test_eval)) {
				System.out.println("ERROR INFINITY");
				System.exit(0);
			}
			
			// print information about new weights and eval function
			System.out.println("Cell feature weight: " + W_CELL);
			System.out.println("Chain feature weight: " + W_CHAIN);
			System.out.println("Score enemy weight: " + W_SCORE_ENEMY);
			System.out.println("Score weight: " + W_SCORE);
			System.out.println("Dummy weight: " + W_DUMMY);
			
			test_eval = getEval(board, chain_finder);
			difference = test_eval - scoreMargin;
			
			System.out.println("New test eval: " + test_eval + " " + 
			scoreMargin + "   " + board.getNumEdgesLeft());
		}
	}

	public static double getEval(Board board, ChainFinder chain_finder) {

		return W_CHAIN * f_getChain(board, chain_finder.chains) +
			   W_SCORE * f_getScore(board) +
			   W_SCORE_ENEMY * f_getEnemyScore(board) +
			   W_DUMMY * f_getDummy(board, chain_finder);
		
	}
	
	/** Feature which gets the score difference, for a state.
	 */
	public static int f_getScore(Board board) {
		return board.getScoreDiff();
	}
	
	/** Feature which finds the longest chain.
	 */
	public static int f_getChain(Board board, List<Chain> chains) {
		// Get the length of the longest chain.
		int chainFeature = 0;
		
		if (chains.size() > 0) {
			chainFeature = chains.get(0).cells.size();
			
			for (Chain chain : chains) {
				if (chain.cells.size() > chainFeature) {
					chainFeature = chain.cells.size();
				}
			}
		}

		return chainFeature;
	}
	
	/** Dummy feature, if we needed it.
	 */
	public static double f_getDummy(Board board, ChainFinder chain_finder) {
		return 0;
	}
	
	/** Peculiar function (for testing), which was to account for the
	 * fact that the person who started second (and not first) has an
	 * 'easier time' winning the game.
	 */
	public static double f_getEnemyScore(Board board) {
		// number of edges is always odd, so if the first player
		// takes an edge, then the second turn will have an
		// even number of unoccupied edges
		if (board.getNumEdgesLeft() % 2 == 0 && 
				board.getCurrTurn() == board.getMyColor())
			return 1;
		
		return -1;
	}
}
