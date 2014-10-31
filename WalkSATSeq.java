/*
 *    File: WalkSATSeq.java
 *    Authors: Connor Adsit
 *             Kevin Bradley
 *             Christian Heinrich
 *    Date: 2014-10-20
 */

import edu.rit.pj2.Task;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import edu.rit.util.Random;

/**
 * Performs a local stochastic search upon a given boolean equation
 * 
 * Usage: java pj2 WalkSATSeq N seed inputFile.cnf
 *        N - number of iterations to perform
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
   int[] truth;
   int[][] equation;
   boolean[] clauses;
   long seed;
   long maxIter;
   Scanner sc;
   Random prng;
   boolean satisfied;

   /**
    * Main Function:
    * @param   args  contains the number of iterations to perform
    *                and the path to the input file
    */
   public void main (String[] args) {
      try {
         // parse command line args
         
         if (args.length != 3)
            usage();
         maxIter = Long.parseLong(args[0]);
         if (maxIter < 0) {
            throw new NumberFormatException();
         }
         seed = Long.parseLong(args[1]);
         sc = new Scanner(new FileInputStream (args[2]));

         // read in input
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
         truth = new int[(numVars + 1) / 2];
         clauses = new boolean[numClauses];
         satisfied = false;
         prng = new Random(seed);

         // generate initial assignment
         for (int i = 0; i < truth.length; ++i) {
            truth[i] = prng.nextInteger();
         }
         update();

         // perform local search
         for (long l = 0L; l < maxIter; ++l) {
            if (satisfied)
               break;
            
         }
      } catch (NumberFormatException nfe) {
         usage();
      } catch (FileNotFoundException fnfe) {
         System.err.println("Error: File " + args[2] + " does not exist.");
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
      System.err.println("Usage: java pj2 WalkSATSeq <N> <seed> <file>");
      System.err.println("N - long number of iterations");
      System.err.println("seed - long seed for PRNG");
      System.err.println("file - input file of CNF equation");
      System.exit(0);
   }

   private void update() {
      boolean allTrue = true;
      for (int i = 0; i < numClauses; ++i) {
         boolean marker = true;
         for (int x : equation[i]) {
            if (x < 0) {
               marker &= !lookup(-(x + 1));
            } else {
               marker &= lookup (x - 1);
            }
         }
         clauses[i] = marker;
         if (!marker)
            allTrue = false;
      }
      satisfied = allTrue;
   }

   private boolean lookup(int index) {
      int i = index / 32;
      int assignment = truth[i] & (1 << (index % 32));
      return assignment == 1;
   }

}
