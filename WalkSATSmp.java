/*
 *    File: WalkSATSmp.java
 *    Authors: Connor Adsit
 *             Kevin Bradley
 *             Christian Heinrich
 *    Date: 2014-12-03
 */

import edu.rit.pj2.LongLoop;
import java.lang.Math;
import edu.rit.pj2.Task;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import edu.rit.util.Random;

/**
 * Performs a local stochastic search upon a given boolean equation
 * 
 * Usage: java pj2 WalkSATSmp N nStep seed inputFile.cnf
 *        N - number of iterations to perform
 *        nStep - number of steps per iteration
 *        seed - long seed for PRNG
 *        inputFile.cnf - filepath for input, in CNF form
 * 
 * Input file format:
      first line: ignored
 *    second line: V C
 *       V - total number of variables
 *       C - total number of clauses
 *    next C lines:
 *       variable numbers delimited by a space, lines ending in 0
 *
 * @author Connor Adsit
 * @author Kevin Bradley
 * @author Christian Heinrich
 */
public class WalkSATSmp extends edu.rit.pj2.Task {
   int numVars;
   int numClauses;
   Assignment best;
   int[][] equation;
   long seed;
   long maxIter;
   long maxSteps;
   Scanner sc;

   // debug and probability variables
   //    (probability done by modular arithmatic of random numbers)
   private final int P_MOD = 5;
   private final int P_REM = 0;

   /**
    * Main Function:
    * @param   args  contains the number of iterations to perform
    *                and the path to the input file
    */
   public void main (String[] args) {
      try {
         // parse command line args
         if (args.length != 4)
            usage();
         maxIter = Long.parseLong(args[0]);
         maxSteps = Long.parseLong(args[1]);
         if (maxIter < 0 || maxSteps < 0) {
            throw new NumberFormatException();
         }
         seed = Long.parseLong(args[2]);

         // read in input and print out representation
         construct(args[3]);

         // initialize best of the best
         best = new Assignment ();

         // perform maxIter walks
         parallelFor (1,maxIter) .schedule (dynamic) .exec (new LongLoop() {
            Random prng;
            Assignment walkBest;
            Assignment stepBest;
            Assignment thrBest;

            public void start () {
               prng = new Random (seed + rank());
               thrBest = threadLocal (best);
               walkBest = new Assignment ();
            }

            public void run (long n) {
               // generate initial assignment
               int[] walkBestAssign = new int[(numVars + 31) / 32];
               for (int i = 0; i < walkBestAssign.length; ++i) 
                  walkBestAssign[i] = prng.nextInteger();
               int[] walkBestClauses = new int[(numClauses + 31) / 32];
               updateClauses (walkBestAssign, walkBestClauses);
               int walkBestTC = hammingWeightVector (walkBestClauses);
               int walkBestBC = Integer.MAX_VALUE; 
               int walkBestMC = 0;
               walkBest = new Assignment (walkBestAssign, walkBestClauses, walkBestTC, 
                                          walkBestBC, walkBestMC);

               // placeholder for the best of each step
               Assignment stepBest = (Assignment) walkBest.clone();
 
               // perform local search
               while (numClauses != walkBest.getTrueCount()) {
                  // perform step
                  for (int s = 0; s < maxSteps; ++s) {
                     int[] newAssign = flip (walkBest.getAssign(), walkBest.getClauses(), walkBest.getTrueCount(), prng);
                     int[] stepClauses = new int[(numClauses + 31)/ 32];
                     updateClauses(newAssign, stepClauses);
                     int stepTC = hammingWeightVector (stepClauses);
                     int stepBC = breakCount (walkBest.getAssign(), stepClauses);
                     int stepMC = makeCount (walkBest.getAssign(), stepClauses);
                     Assignment step = new Assignment (newAssign, stepClauses, stepTC, stepBC, stepMC);
                     stepBest.reduce(step);
                  } 

                  // reassign if we have a better assignment
                  if (walkBest.compareTo (stepBest) >= 0) break;
                  else walkBest.reduce (stepBest);
               }

               // if our best step is better than our current assignment,
               // propagate the change
               thrBest.reduce(walkBest);
            }
         });

         // print out results
         if (numClauses != best.getTrueCount()) System.out.println("No solution.");
         else System.out.println ("Solution found: ");
         System.out.println("\tTruth assignment: "); 
         for (int i = 0; i < numVars; ++i) {
            System.out.printf("\t\t%d -> %s\n", i + 1, lookup(i+1, best.getAssign()));
         }

      // handle any errors
      } catch (NumberFormatException nfe) {
         usage();
      } catch (FileNotFoundException fnfe) {
         System.err.println("Error: File " + args[3] + " does not exist.");
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getMessage());
      } finally {
         try {
            sc.close();
         } catch (Exception e) {}
      }
   }

   /**
    * Prints out a usage statement
    */
   private void usage() {
      System.err.println("Usage: java pj2 WalkSATSeq <N> <nStep> <seed> <file>");
      System.err.println("N - long number of iterations");
      System.err.println("nStep - long number of assignments to generate per iteration");
      System.err.println("seed - long seed for PRNG");
      System.err.println("file - input file of CNF equation");
      System.exit(0);
   }

   /**
    *    Creates a boolean satisfiability equation from a .dimacs file already in cnf
    *    @param   file  the path to the .dimacs file
    */
   private void construct(String file) throws FileNotFoundException {
      sc = new Scanner(new FileInputStream (file));
      sc.nextLine();
      String[] line = sc.nextLine().split(" ");
      numVars = Integer.parseInt(line[2]);
      numClauses = Integer.parseInt(line[3]);
      equation = new int[numClauses][];
      for (int i = 0; i < numClauses; ++i) {
         line = sc.nextLine().split(" ");
         int clauseLength = line.length - 1;
         equation[i] = new int[clauseLength];
         for (int j = 0; j < clauseLength; ++j) {
            equation[i][j] = Integer.parseInt(line[j]);
         }
      }
   }

   /**
    *    Looks up the value of a bit in a bit vector
    *
    *    @param   pos      the position of the bit
    *    @param   bvector  the bit vector to perfom the lookup on
    *    @return the value of the bit at position pos in bvector
    */
   private boolean lookup(int pos, int[] bvector) {
      int i = Math.abs(pos);
      int index = (i-1) / 32;
      int position = 32 - (i % 32);
      int assignment = bvector[index] & (1 << position);
      return !(assignment == 0);
   }

   /**
    *    Returns a truth assignment based on the given one
    *    with the bit at a position flipped
    *
    *    @param   assignment  the truth assignment to advance
    *    @param   clauses     the satisfiability of the clauses based on the assignment
    *    @param   satClauses  the number of clauses satisfied by the assignment
    *    @return the new truth assignment
    */
   private int[] flip (int[] assignment, int[] clauses, int satClauses, Random prng) {
      int[] result = assignment.clone ();
      // find a random unsatisfied clause, pick the c-thclause
      int c = prng.nextInt (numClauses - satClauses) + 1;
      int clause = 0;
      int n = 1;
      while (c > 0) {
         ++clause;
         if (!lookup (clause, clauses)) {
            --c;
         }
      }
      
      int[] clauseVars = equation[clause - 1];

      // pick a variable to flip
      if (prng.nextInteger() % P_MOD <= P_REM) {
         // flip a variable that breaks the least clauses
         int bc = Integer.MAX_VALUE;
         for (int v : clauseVars) {
            flipBit(v, result);
            int[] newClauses = new int[clauses.length];
            updateClauses(result, newClauses);
            if (bc > breakCount (clauses, newClauses)) n = v;
            flipBit(v, result);
         }
      } else {
         // flip a random var
         n = clauseVars[prng.nextInt(clauseVars.length)];
      }
      flipBit(n, result);
      return result;
   }

   /**
    *    Evaluates a boolean clause based upon a truth assignment
    *    @param   clause      the index of the clause in the equation
    *    @param   assignment  the truth assignment of all the variables
    *    @return true if the clause can be satisfied with the truth assignment
    */
   private boolean eval (int clause, int[] assignment) {
      boolean result = false;
      for (int v : equation[clause]) {
         if ((v < 0 && (!lookup (v, assignment))) ||
             (v > 0 && (lookup (v, assignment)))) {
               result = true;
               break;
         }
      }
      return result;
   }

   /**
    *    Flips the value truth assignment of a variable
    *    @param   var      the variable to flip
    *    @param   assign   the truth assignment to examine
    */
   private void flipBit(int index, int[] bitVector) {
      int ind = Math.abs(index);
      int i = (ind - 1) / 32;
      int position = 32 - (ind % 32);
      bitVector[i] ^= (1 << position);
   }

   /**
    *    Returns a pretty printed clause
    *    @param   clause   the index of a clause in the formula
    *    @return a readable string version of the clause
    */
   private String clauseString(int clause) {
      String accum = "";
      int[] vars = equation[clause];
      for (int c = 0; c < vars.length - 1; ++c) {
         accum += "" + vars[c] + " \\/ ";
      }
      accum += "" + vars[vars.length - 1];
      return accum;
   }

   /**
    *    Takes the current truth assignment and updates the truth values of all the clauses
    */
   private void updateClauses (int[] assign, int[] clauses) {
      // wipe out clauses
      for (int i = 0; i < clauses.length; ++i)
         clauses[i] = 0;
      // go through each clause and update its satisfiability
      for (int c = 0; c < numClauses; ++c) {
         if (eval (c, assign)) {
            flipBit (c + 1, clauses);
         }
      }
   }

   /**
    *    Calculates the number of 1's in a bitstring version of an integer 
    *
    *    @param   word  the integer to perform the hamming weight calculation
    *    @return the count of all the ones in the two's complement rep of the int
    */
   private int hammingWeight (int word) {
      int result = 0;
      while (word != 0) {
         result += word & 1;
         word  = word >>> 1;
      }
      return result;
   }

   /**
    *    Calculates the number of 1's in a bitvector 
    *
    *    @param   bvector  the bitvector to aggregate
    *    @return the count of all the ones in each word of the bitvector 
    */
   private int hammingWeightVector (int[] bvector) {
      int result = 0;
      for (int i = 0; i < bvector.length; ++i) {
         result += hammingWeight (bvector[i]);
         
      }
      return result;
   }

   /**
    *    Calculates the makecount of a new assignment
    *    Equivalent to everything in b that is not in a
    *
    *    @param   a  the former bitvector
    *    @param   b  the new bitvector
    *    @return the count of all clauses fixed by a new assignment
    */
   private int makeCount (int[] a, int[] b) {
      int result = 0;
      for (int i = 0; i < a.length; ++i) {
         result += hammingWeight (b[i] & (~a[i]));
      }
      return result;
   }

   /**
    *    Calculates the breakcount of a new array
    *    Equivalent to everything in a that is not in b
    *
    *    @param   a  the former bitvector
    *    @param   b  the new bitvector
    *    @return the count of all clauses broken by a new assignment
    */
   private int breakCount (int[] a, int[] b) {
      int result = 0;
      for (int i = 0; i < a.length; ++i) {
         result += hammingWeight (a[i] & (~b[i]));
      }
      return result;
   }
}
