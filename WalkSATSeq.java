/*
 *    File: WalkSATSeq.java
 *    Authors: Connor Adsit
 *             Kevin Bradley
 *             Christian Heinrich
 *    Date: 2014-12-03
 */

import java.lang.Math;
import edu.rit.pj2.Task;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import edu.rit.util.Random;

/**
 * Performs a local stochastic search upon a given boolean equation
 * 
 * Usage: java pj2 WalkSATSeq N nStep seed inputFile.cnf
 *        N - number of iterations to perform
 *        nStep - number of steps per iteration
 *        seed - long seed for PRNG
 *        inputFile.cnf - filepath for input, in CNF form
 * 
 * Input file format:
 *    first line: V C
 *       V - total number of variables
 *       C - total number of clauses
 *    next C lines:
 *       variable numbers delimited by a space, lines ending in 0
 *
 * @author Connor Adsit
 * @author Kevin Bradley
 * @author Christian Heinrich
 */
public class WalkSATSeq extends edu.rit.pj2.Task {
   int numVars;
   int numClauses;
   int[] bestAssign;
   int bestMC;
   int bestBC;
   int bestTrueClauses;
   int[][] equation;
   long seed;
   long maxIter;
   long maxSteps;
   boolean[] clauses;
   Scanner sc;
   Random prng;

   // debug and probability variables
   //    (probability done by modular arithmatic of random numbers)
   private final boolean DEBUG = false;
   private final int P_MOD = 2;
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
         prng = new Random(seed);

         // read in input and print out representation
         construct(args[3]);
         System.out.println("Number of Variables: " + numVars);
         System.out.println("Number of Clauses: " + numClauses);
         System.out.println("Formula is:");
         for (int c = 0; c < numClauses; ++c) System.out.println("\t" + clauseString(c)); 
         System.out.println();

         // generate initial assignment
         for (int i = 0; i < bestAssign.length; ++i) 
            bestAssign[i] = prng.nextInteger();

         for (int c = 0; c < numClauses; ++c) 
            if (eval (c, bestAssign)) ++bestTrueClauses;
         int bestMC = 0; 
         int bestBC = Integer.MAX_VALUE;
         updateClauses(); 

         // perform local search
         for (long l = 0L; l < maxIter; ++l) {
            if (DEBUG) {
               System.out.println("Iteration #" + (l+1) + "\nAssignment is: ");
               for (int i = 0; i < bestAssign.length; ++i) System.out.print (Integer.toBinaryString (bestAssign[i]));
               System.out.println();

               System.out.println ("Number of broken clauses is: " + (numClauses - bestTrueClauses));
               for (int c = 0; c < numClauses; ++c) {
                  System.out.println("Clause " + c + " (" + clauseString (c) + ") is " + clauses[c]);
               }
            }

            // stop when we've found a solution
            if (bestTrueClauses == numClauses)
               break;

            int[] stepBest = bestAssign;
            int stepBestClauses = bestTrueClauses;
            int stepBestMC = 0; 
            int stepBestBC = Integer.MAX_VALUE; 

            // perform step
            for (int n = 0; n < maxSteps; ++n) {
               int[] newAssign = flip();
               int satClauses = 0; 
               int makeCount = 0; 
               int breakCount = 0;
               for (int c = 0; c < numClauses; ++c) {
                  if (eval (c, newAssign)) ++satClauses;
                  if (eval (c, bestAssign) && (!eval (c, newAssign))) ++breakCount;
                  if ((!eval (c, bestAssign)) && eval (c, newAssign)) ++ makeCount;
               } 

               // reassign if we have a better assignment
               boolean isBetter = false;
               if (satClauses > stepBestClauses) isBetter = true;
               else if (satClauses == stepBestClauses && 
                        breakCount < stepBestBC) isBetter = true;
               else if (satClauses == stepBestClauses &&
                        breakCount == stepBestBC &&
                        makeCount > stepBestMC) isBetter = true;
               if (isBetter) {
                  stepBest = newAssign;
                  stepBestClauses = satClauses;
                  stepBestMC = makeCount;
                  stepBestBC = breakCount;
               }
            }

            // if our best step is better than our current assignment,
            // propagate the change
            boolean isBetter = false;
            if (stepBestClauses > bestTrueClauses) isBetter = true;
            else if (stepBestClauses == bestTrueClauses && 
                     stepBestBC < bestBC) isBetter = true;
            else if (stepBestClauses == bestTrueClauses &&
                     stepBestBC == bestBC &&
                     stepBestMC > bestMC) isBetter = true;
            if (isBetter) {
               bestAssign = stepBest;
               bestTrueClauses = stepBestClauses;
               bestMC = stepBestMC;
               bestBC = stepBestBC;
               updateClauses();
            }
         }

         // print out results
         if (numClauses == bestTrueClauses) {
            System.out.println("Truth assignment:");

            for (int i = 0; i < numVars; ++i) {
               System.out.printf("\t%d -> %s\n", i + 1, lookup(i, bestAssign));
            }
         } else {
            System.out.println("No solution.");
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
      bestAssign = new int[(numVars + 31) / 32];
      clauses = new boolean[numClauses];
      bestTrueClauses = 0;
   }

   /**
    * @param index the variable to lookup
    * @return the truth assignment for that variable
    */
   private boolean lookup(int index, int[] assign) {
      int i = (index-1) / 32;
      int assignment = assign[i] & (1 << 32 - (index % 32));
      return !(assignment == 0);
   }

   /**
    * Returns a truth assignment based on the current one
    * with the bit at position n flipped
    *
    * @return the new truth assignment
    */
   private int[] flip() {
      int[] result = bestAssign.clone();
      // find a random unsatisfied clause, pick the c-thclause
      int c = prng.nextInt(numClauses - bestTrueClauses) + 1;
      
      int clause = -1;
      int n = 0;
      do {
         ++clause;
         if (!clauses[clause]) {
            --c;
         }
      } while (c > 0);
      
      int[] clauseVars = equation[clause];

      // pick a variable to flip
      if (prng.nextInteger() % P_MOD <= P_REM) {
         // flip a variable that breaks the least clauses
         int bc = Integer.MAX_VALUE;
         n = 1;
         for (int v : clauseVars) {
            flipAssign(v, result);
            int varbc = 0;
            if (eval (c, bestAssign) && (!eval (c, result))) ++varbc;
            if (varbc < bc) n = v;
            flipAssign(v, result);
         }
      } else {
         // flip a random var
         n = clauseVars[prng.nextInt(clauseVars.length)];
      }
      flipAssign(n, result);
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
         if ((v < 0 && (!lookup (-v, assignment))) ||
             (v > 0 && lookup (v, assignment))) {
            result = true;
            break;
         } 
      }
      return result;
   }

   /**
    *
    */
   private void flipAssign(int var, int[] assign) {
      var = Math.abs(var);
      int index = (var-1) / 32;
      int position = 32 - (var % 32);
      assign[index] ^= (1 << position);
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
   private void updateClauses () {
      for (int c = 0; c < numClauses; ++c) {
         clauses[c] = eval(c, bestAssign);
      }
   }
}
