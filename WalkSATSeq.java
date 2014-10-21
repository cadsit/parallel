/*
 *    File: WalkSATSeq.java
 *    Authors: Connor Adsit
 *             Kevin Bradley
 *             Christian Heinrich
 *    Date: 2014-10-20
 */

import edu.rit.pj2.Task;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import edu.rit.util.Random;

/**
 * Performs a local stochastic search upon a given boolean equation
 * 
 * Usage: java pj2 WalkSATSeq N seed inputFile.cnf
 *        N - number of iterations to perform
 *        seed - long seed for PRNG
 *        inputFile.cnf - filepath for input, in CNF form
 *
 * @author Connor Adsit
 * @author Kevin Bradley
 * @author Christian Heinrich
 */
public class WalkSATSeq extends edu.rit.pj2.Task {

   boolean[] truth;
   int[][] clauses;
   long seed;
   long maxIter;
   BufferedReader bf;

   /**
    * Main Function
    * @param   args  contains the number of iterations to perform
    *                and the path to the input file
    */
   public void main (String[] args) {
      try {
         // read in input
         if (args.length != 3)
            usage();
         maxIter = Long.parseLong(args[0]);
         if (maxIter < 0) {
            throw new NumberFormatException();
         }
         seed = Long.parseLong(args[1]);
         bf = new BufferedReader(new FileReader (args[2]));
      } catch (NumberFormatException nfe) {
         usage();
      } catch (FileNotFoundException fnfe) {
         System.err.println("Error: File " + args[2] + " does not exist.");
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getMessage());
      } finally {
         try {
            bf.close();
         } catch (Exception e) {}
      }
   }

   private void usage() {
      System.err.println("Usage: java pj2 WalkSATSeq <N> <seed> <file>");
      System.err.println("N - long number of iterations");
      System.err.println("seed - long seed for PRNG");
      System.err.println("file - input file of CNF equation");
      System.exit(0);
   }
}
