/*
 *    DNConditionalEstimator.java
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
 * Conditional probability estimator for a discrete domain conditional upon
 * a numeric domain.
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version 1.0
 */

public class DNConditionalEstimator implements ConditionalEstimator {

  // =================
  // Private variables
  // =================

  /**
   * Hold the sub-estimators
   */
  private NormalEstimator [] m_Estimators;

  /**
   * Hold the weights for each of the sub-estimators
   */
  private DiscreteEstimator m_Weights;

  // ===============
  // Public methods.
  // ===============
  
  /**
   * Constructor
   *
   * @param numSymbols the number of symbols 
   * @param precision the  precision to which numeric values are given. For
   * example, if the precision is stated to be 0.1, the values in the
   * interval (0.25,0.35] are all treated as 0.3. 
   */
  public DNConditionalEstimator(int numSymbols, double precision) {

    m_Estimators = new NormalEstimator [numSymbols];
    for(int i = 0; i < numSymbols; i++) {
	m_Estimators[i] = new NormalEstimator(precision);
    }
    m_Weights = new DiscreteEstimator(numSymbols, true);
  }

  /**
   * Add a new data value to the current estimator.
   *
   * @param data the new data value 
   * @param given the new value that data is conditional upon 
   * @param weight the weight assigned to the data value 
   */
  public void addValue(double data, double given, double weight) {

    m_Estimators[(int)data].addValue(given, weight);
    m_Weights.addValue((int)data, weight);
  }

  /**
   * Get a probability estimator for a value
   *
   * @param data the value to estimate the probability of
   * @param given the new value that data is conditional upon 
   * @return the estimator for the supplied value given the condition
   */
  public Estimator getEstimator(double given) {

    Estimator result = new DiscreteEstimator(m_Estimators.length,false);
    for(int i = 0; i < m_Estimators.length; i++) {
      result.addValue(i,m_Weights.getProbability(i)
		      *m_Estimators[i].getProbability(given));
    }
    return result;
  }

  /**
   * Get a probability estimate for a value
   *
   * @param data the value to estimate the probability of
   * @param given the new value that data is conditional upon 
   * @return the estimated probability of the supplied value
   */
  public double getProbability(double data, double given) {

    return getEstimator(given).getProbability(data);
  }

  /**
   * Display a representation of this estimator
   */
  public String toString() {

    String result = "DN Conditional Estimator. " 
      + m_Estimators.length + " sub-estimators:\n";
    for(int i = 0; i < m_Estimators.length; i++) {
      result += "Sub-estimator " + i + ": " + m_Estimators[i];
    }
    result += "Weights of each estimator given by " + m_Weights;
    return result;
  }
}








