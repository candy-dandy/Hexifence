package aiproj.hexifence.MartinAndy;

import java.util.ArrayList;
import java.util.List;

import aiproj.hexifence.Move;

public class GradientDescentLearn {
	private TranspositionTable table;
	
	private long num_explored = 0;
	
	private int s_low_level = 9000;
	private int s_best_value = 0;
	
	private int dim;
	
	public GradientDescentLearn(int dim) {
		this.dim = dim;
		
		this.table = new TranspositionTable(dim);
	}
	
	public int getInitMinimax() {
		int[] result = minimax_value(new Board(dim, Main.playerStart), 0);
		
		return result[0];
	}
	
	
	public int[] minimax_value(Board state, int level) {
		num_explored++;
		int[] ret = new int[2]; // index 0 = minimax value
								// index 1 = capture value
		
		if (state.isFinished()) {		// terminal state
			ret[0] = state.getScoreDiff();
			ret[1] = 0;
			
			return ret;
		}

		// detect symmetry
		Board sym = state.isRotateSymmetric(table);
			
		if (sym != null) {
			// TODO: FIX THIS
			int possible_capt = table.getEntry(sym);
			
			ret[0] = state.getScoreDiff() + 2*possible_capt - state.getNumUncaptured();
			ret[1] = possible_capt;
			
			return ret;
		}

		// go through each child of this state
		int[] minimax = null;
		int capt_score = 0;

		int curr_ch_score;
		int[] child_minimax;
		// System.out.println("GOT FIRST MINIMAX CHILD");

		// search through each child state with minimax
		Board child = state.deepCopy(true);
		List<Board> sym_childs = new ArrayList<Board>();
		
		for (int r = 0; r < child.getEdges().length; r++) {
			for (int c = 0; c < child.getEdges()[r].length; c++) {
				// create a move
				Move m = new Move();
				m.Row = r;
				m.Col = c + Math.max(0, r - (2*dim - 1));
				m.P = child.getCurrTurn();
				
				// if the edge has been occupied (or is cell centre)
				// then move onto the next possible move
				if (!child.occupyEdge(m)) {
					continue;
				}
				

				// if no cells were captured, then give turn to
				// the other player
				if (state.getNumUncaptured() == child.getNumUncaptured()) {
					child.switchTurns();
				}

				// if the child created is not symmetric to a
				// previous child that was made before
				if (!sym_childs.contains(child)) {
					sym_childs.addAll(child.getSymmetricBoards());

					// TODO: present checks
					curr_ch_score = child.getMyScore() - state.getMyScore();
					child_minimax = minimax_value(child, level + 1);

					if (state.getCurrTurn() == Main.myColor) {
						if (minimax == null ||
							child_minimax[0] > minimax[0]) {
							
							minimax = child_minimax;
							capt_score = curr_ch_score;
						}
					} else {
						if (minimax == null ||
							child_minimax[0] < minimax[0]) {
							
							minimax = child_minimax;
							capt_score = curr_ch_score;
						}
					}
					

				}

				// create another child
				child = state.deepCopy(true);
			}
		}

		/*
		 * If current turn is self:
		 * 		minimax[1] has the maximum optimal number of cells that
		 * 		can be captured by self, STARTING from the child state
		 * 		with this minimax value.
		 * 
		 * 		capt_score counts the number of cells captured by self,
		 *		due to a move BEFORE reaching to the child state.
		 * 
		 *  	Thus the total number of cells captured FROM this state
		 *  	is minimax[1] + capt_score.
		 *  
		 *  	If the child state was a terminal state (ie. this state
		 *  	has only one open edge left), then the child state returns
		 *  	minimax[1] = 0.
		 *
		 */
		minimax[1] += capt_score;

		// System.out.println("\nSTATE END: " + state.toString() + "\t\t" + state.getScore() + "\t\t" + minimax_value + "\t\t" + state.getCurrTurn());
		table.storeEntry(state, minimax[1]);
		
		if (level < s_low_level || (level == s_low_level && s_best_value < minimax[0])) {
			s_low_level = level;
			s_best_value = minimax[0];
			
			System.out.println(table.getSize() + "\t\t" + num_explored +
					"\t\t" + s_low_level + "  " + s_best_value + "  " + minimax[1]);
		}
		


		return minimax;
	}
}