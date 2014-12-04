/*
 *    File: AssignmentVbl.java
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
public class AssignmentVbl implements edu.rit.pj2.Vbl {
   private int[] assignment;
   int makeCount;
   int breakCount;
   int trueCount;

   /**
    *    Default constructor
    */
   public AssignmentVbl () {
      this.assignment = null;
      this.makeCount = 0;
      this.breakCount = 0;
      this.trueCount = 0;
   }

   /**
    *    Specialized Constructor
    *    @param   assign   a truth assignment
    *    @param   m        the makeCount of the truth assignment
    *    @param   b        the breakCount of the truth assignment
    *    @param   t        the number of clauses that evaluate to true
    */
   public AssignmentVbl (int[] assign, int m, int b, int t) {
      this.assignment = assign.clone();
      this.makeCount = m;
      this.breakCount = b;
      this.trueCount = t;
   }

   /**
    *    Creates a clone of this object
    *    @return a deepcopy of this object
    */
   public Object clone () {
      return new AssignmentVbl (assignment, makeCount, breakCount, trueCount);
   }

   /**
    *    Performs a reduction with another variable. Sets this to the more optimal assignment
    *    @param   vbl   the variable to reduce with
    */
   public void reduce (Vbl vbl) {
      AssignmentVbl other = (AssignmentVbl) vbl;
      if (!this.isBetterThan (other)) {
         this.assignment = other.assignment;
         this.makeCount = other.makeCount;
         this.breakCount = other.breakCount;
         this.trueCount = other.trueCount;
      }
   }

   /**
    *    Sets this variable's fields to another variable
    *    @param   vbl   the Variable to be set to.
    */
   public void set (Vbl vbl) {
      AssignmentVbl other = (AssignmentVbl) vbl;
      this.assignment = other.assignment;
      this.makeCount = other.makeCount;
      this.breakCount = other.breakCount;
      this.trueCount = other.trueCount;
   }

   /**
    *    Compares this assignment to another assignment
    *    @param   other the other assignment to be compared with
    *    @return true, if this assignment is better than the other assignment
    */
   public boolean isBetterThan (AssignmentVbl other) {
      boolean result = true;
      if (other.trueCount > this.trueCount) result = false;
      else if (other.breakCount < this.breakCount) result = false;
      else if (other.makeCount > this.makeCount) result = false;
      return result;
   }
}
