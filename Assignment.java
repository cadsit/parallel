/*
 *    File: Assignment.java
 *    Author: Connor Adsit
 *    Date: 2014-12-02
 */

import edu.rit.pj2.Vbl;

/**
 *    Defines reduction functions across truth assignments for the Boolean Satisfiability
 *    problem
 *
 *    @author Connor Adsit
 *    @author Kevin Bradley
 *    @author Christian Heinrich
 */
public class Assignment implements edu.rit.pj2.Vbl, Comparable<Assignment> {
   private int[] assignment;
   private int[] clauses;
   int trueCount;
   int breakCount;
   int makeCount;

   /**
    *    Default constructor
    */
   public Assignment () {
      this.assignment = new int[0];
      this.clauses = new int[0];
      this.makeCount = 0;
      this.breakCount = Integer.MAX_VALUE;
      this.trueCount = 0;
   }

   /**
    *    Specialized Constructor
    *    @param   assign   a truth assignment
    *    @param   m        the makeCount of the truth assignment
    *    @param   b        the breakCount of the truth assignment
    *    @param   t        the number of clauses that evaluate to true
    */
   public Assignment (int[] assign, int[] clauses, int t, int b, int m) {
      this.assignment = assign.clone();
      this.clauses = clauses.clone();
      this.trueCount = t;
      this.breakCount = b;
      this.makeCount = m;
   }

   public int[] getAssign() {
      return this.assignment;
   }

   public int[] getClauses() {
      return this.clauses;
   }

   public int getTrueCount() {
      return this.trueCount;
   }

   public int getBreakCount() {
      return this.breakCount;
   }

   public int getMakeCount() {
      return this.makeCount;
   }

   /**
    *    Creates a clone of this object
    *    @return a deepcopy of this object
    */
   public Object clone () {
      return new Assignment (assignment, clauses, trueCount, breakCount, makeCount);
   }

   /**
    *    Performs a reduction with another variable. Sets this to the more optimal assignment
    *    @param   vbl   the variable to reduce with
    */
   public void reduce (Vbl vbl) {
      Assignment other = (Assignment) vbl;
      if (this.compareTo(other) < 0) {
         this.assignment = other.assignment;
         this.clauses = other.clauses;
         this.trueCount = other.trueCount;
         this.breakCount = other.breakCount;
         this.makeCount = other.makeCount;
      }
   }

   /**
    *    Sets this variable's fields to another variable
    *    @param   vbl   the Variable to be set to.
    */
   public void set (Vbl vbl) {
      Assignment other = (Assignment) vbl;
      this.assignment = other.assignment;
      this.clauses = other.clauses;
      this.makeCount = other.makeCount;
      this.breakCount = other.breakCount;
      this.trueCount = other.trueCount;
   }

   /**
    *    Compares this assignment to another assignment
    *    @param   other the other assignment to be compared with
    *    @return 1, if this assignment is better than the other assignment
    *            0, if they are equal
    *           -1, otherwise
    */
   public int compareTo (Assignment other) {
      int result = 1;
      boolean trueCounts = this.trueCount == other.trueCount;
      boolean breakCounts = this.breakCount == other.breakCount;
      boolean makeCounts = this.makeCount == other.makeCount;
      if (this.trueCount < other.trueCount) result = -1;
      else if (trueCounts && (this.breakCount > other.breakCount)) result = -1;
      else if (trueCounts && breakCounts && (this.makeCount < other.makeCount)) result = -1;
      else if (trueCounts && breakCounts && makeCounts) result = 0;
      return result;
   }
}
