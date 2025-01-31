/*
 *    PoissonEstimator.java
 *    Copyright (C) 1999 Len Trigg
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package weka.estimators;

import java.util.*;
import weka.core.*;

/** 
 * Simple probability estimator that places a single Poisson distribution
 * over the observed values.
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version 1.0
 */

public class PoissonEstimator implements Estimator {

  // =================
  // Private variables
  // =================

  /**
   * The number of values seen
   */
  private double m_NumValues;

  /**
   * The sum of the values seen
   */
  private double m_SumOfValues;

  /** 
   * The average number of times
   * an event occurs in an interval.
   */
  private double m_Lambda;

  // ===============
  // Private methods
  // ===============

  private double logFac(double x) {

    double result = 0;

    for (double i = 2; i <= x; i++)
      result += Math.log(i);
    return result;
  }

  /**
   * Returns value for Poisson distribution
   * @param x the argument to the kernel function
   * @return the value for a Poisson kernel
   */

  private double Poisson(double x) 
  {
    return Math.exp(-m_Lambda + (x * Math.log(m_Lambda)) - logFac(x));
  }
  
  // ===============
  // Public methods.
  // ===============
  
  /**
   * Constructor
   * @param the number of possible symbols
   */
  public PoissonEstimator()
  {
    m_NumValues = 0;
    m_SumOfValues = 0;
    m_Lambda = 0;
  }

  /**
   * Add a new data value to the current estimator.
   * @param data the new data value 
   * @param weight the weight assigned to the data value 
   */
  public void addValue(double data, double weight)
  {
    m_NumValues += weight;
    m_SumOfValues += data*weight;
    if (m_NumValues != 0){
      m_Lambda = m_SumOfValues / m_NumValues;
    }
  }

  /**
   * Get a probability estimate for a value
   * @param data the value to estimate the probability of
   * @return the estimated probability of the supplied value
   */
  public double getProbability(double data)
  {
    return Poisson(data);
  }

  /**
   * Display a representation of this estimator
   */
  public String toString()
  {
    return "Poisson Lambda = " + Utils.doubleToString(m_Lambda,4,2) + "\n";
  }

  /**
   * Main method for testing this class.
   * @param argv should contain a sequence of numeric values
   */

  public static void main(String [] argv)
  {
    try
    {
      if (argv.length == 0)
      {
	System.out.println("Please specify a set of instances.");
	return;
      }
      PoissonEstimator newEst = new PoissonEstimator();
      for(int i = 0; i < argv.length; i++)
      {
	double current = Double.valueOf(argv[i]).doubleValue();
	System.out.println(newEst);
	System.out.println("Prediction for " + current 
			   + " = " + newEst.getProbability(current));
	newEst.addValue(current, 1);
      }

    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
    }
  }
}








