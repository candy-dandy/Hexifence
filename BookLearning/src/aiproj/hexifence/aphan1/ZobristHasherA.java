/* 
 * COMP30024 - Project B
 * Andy Phan (aphan1) and Martin Cheong (cheongm)
 * 
 */

package aiproj.hexifence.aphan1;

import java.util.Random;

import aiproj.hexifence.Piece;

/** Zobrist hasher to be used for our learner.
 */
public class ZobristHasherA implements Hasher {
	private static final int SEED = 12345; 
									// We can randomise the seed if needed.
	
	private long[] elementValueTable;
	
	public ZobristHasherA(int dimension) {
		// Total number of edges for a game of dimension N.
		int totalEdges = 3*dimension * (3*dimension - 1);
		
		elementValueTable = new long[totalEdges];
								// Indexed left-to-right and top-to-bottom.
		
		Random rand = new Random(SEED);
		
		for (int i = 0; i < totalEdges; i++) {
			// Longer bit-strings => less hash collisions.
			elementValueTable[i] = rand.nextLong();
		}
	}
	
	public long generateHashKey(int[][] edges) {
		long hashKey = 0;
		int edgeIndex = 0;
		
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges[i].length; j++) {
				// if the location (i, j) is the centre of some
				// cell, and not a valid edge
				if (i % 2 == 1 && j % 2 == 1) {
					continue;
				}

				if (edges[i][j] != Piece.EMPTY) {
					hashKey ^= elementValueTable[edgeIndex];
				}

				edgeIndex++;
			}
		}
		
		return hashKey;
	}
	
}