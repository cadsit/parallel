/*
 *    File: WalkSATSeq.java
 *    Authors: Connor Adsit
 *             Kevin Bradley
 *             Christian Heinrich
 *    Date: 2014-10-20
 */

import edu.rit.pj2.Task;
import java.io.FileReader;
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


   /**
    * Main Function
    * @param   args  contains the number of iterations to perform
    *                and the path to the input file
    */
   public void main (String[] args) {
      try {
         // read in input
         
         BufferedReader bf = new BufferedReader(new FileReader (args[2]));
      } catch (Exception e) {
      }
      
   }
}
