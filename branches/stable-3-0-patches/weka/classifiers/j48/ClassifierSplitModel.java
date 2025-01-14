/*
 *    ClassifierSplitModel.java
 *    Copyright (C) 1999 Eibe Frank
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
package weka.classifiers.j48;

import java.io.*;

import java.util.*;
import weka.core.*;

/** 
 * Abstract class for classification models that can be used 
 * recursively to split the data.
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 1.2.2.1 $
 */
public abstract class ClassifierSplitModel implements Cloneable, Serializable {

  /** Distribution of class values. */  
  protected Distribution m_distribution;  

  /** Number of created subsets. */
  protected int m_numSubsets;         

  /**
   * Allows to clone a model (shallow copy).
   */
  public Object clone() {

    Object clone = null;
    
    try {
      clone = super.clone();
    } catch (CloneNotSupportedException e) {
    } 
    return clone;
  }

  /**
   * Builds the classifier split model for the given set of instances.
   *
   * @exception Exception if something goes wrong
   */
  abstract public void buildClassifier(Instances instances) throws Exception;
  
  /**
   * Checks if generated model is valid.
   */
  public final boolean checkModel() {
    
    if (m_numSubsets > 0)
      return true;
    else
      return false;
  }
  
  /**
   * Classifies a given instance.
   *
   * @exception Exception if something goes wrong
   */
  public final double classifyInstance(Instance instance)
       throws Exception {
    
    int theSubset;
    
    theSubset = whichSubset(instance);
    if (theSubset > -1)
      return (double)m_distribution.maxClass(theSubset);
    else
      return (double)m_distribution.maxClass();
  }

  /**
   * Gets class probability for instance.
   *
   * @exception Exception if something goes wrong
   */
  public double classProb(int classIndex,Instance instance) 
       throws Exception {
    
    int theSubset = whichSubset(instance);

    if (theSubset > -1)
      return m_distribution.prob(classIndex,theSubset);
    else
      return m_distribution.prob(classIndex);
  }

  /**
   * Returns coding costs of model. Returns 0 if not overwritten.
   */
  public double codingCost() {

    return 0;
  }

  /**
   * Returns the distribution of class values induced by the model.
   */
  public final Distribution distribution() {

    return m_distribution;
  }

  /**
   * Prints left side of condition satisfied by instances.
   *
   * @param data the data.
   */
  abstract public String leftSide(Instances data);

  /**
   * Prints left side of condition satisfied by instances in subset index.
   */
  abstract public String rightSide(int index,Instances data);

  /**
   * Prints label for subset index of instances (eg class).
   *
   * @exception Exception if something goes wrong
   */
  public final String dumpLabel(int index,Instances data) throws Exception {

    StringBuffer text;

    text = new StringBuffer();
    text.append(((Instances)data).classAttribute().
		value(m_distribution.maxClass(index)));
    text.append(" ("+Utils.roundDouble(m_distribution.perBag(index),2));
    if (Utils.gr(m_distribution.numIncorrect(index),0))
      text.append("/"+Utils.roundDouble(m_distribution.numIncorrect(index),2));
    text.append(")");

    return text.toString();
  }
  
  /**
   * Prints the split model.
   *
   * @exception Exception if something goes wrong
   */
  public final String dumpModel(Instances data) throws Exception {

    StringBuffer text;
    int i;

    text = new StringBuffer();
    for (i=0;i<m_numSubsets;i++) {
      text.append(leftSide(data)+rightSide(i,data)+": ");
      text.append(dumpLabel(i,data)+"\n");
    }
    return text.toString();
  }
 
  /**
   * Returns the number of created subsets for the split.
   */
  public final int numSubsets() {

    return m_numSubsets;
  }
  
  /**
   * Sets distribution associated with model.
   */
  public void resetDistribution(Instances data) throws Exception {

    m_distribution = new Distribution(data, this);
  }

  /**
   * Splits the given set of instances into subsets.
   *
   * @exception Exception if something goes wrong
   */
  public final Instances [] split(Instances data) 
       throws Exception { 

    Instances [] instances = new Instances [m_numSubsets];
    double [] weights;
    double newWeight;
    Instance instance;
    int subset, i, j;

    for (j=0;j<m_numSubsets;j++)
      instances[j] = new Instances((Instances)data,
					    data.numInstances());
    for (i = 0; i < data.numInstances(); i++) {
      instance = ((Instances) data).instance(i);
      weights = weights(instance);
      subset = whichSubset(instance);
      if (subset > -1)
	instances[subset].add(instance);
      else
	for (j = 0; j < m_numSubsets; j++)
	  if (Utils.gr(weights[j],0)) {
	    newWeight = weights[j]*instance.weight();
	    instances[j].add(instance);
	    instances[j].lastInstance().setWeight(newWeight);
	  }
    }
    for (j = 0; j < m_numSubsets; j++)
      instances[j].compactify();
    
    return instances;
  }

  /**
   * Returns weights if instance is assigned to more than one subset.
   * Returns null if instance is only assigned to one subset.
   */
  abstract public double [] weights(Instance instance);
  
  /**
   * Returns index of subset instance is assigned to.
   * Returns -1 if instance is assigned to more than one subset.
   *
   * @exception Exception if something goes wrong
   */
  abstract public int whichSubset(Instance instance) throws Exception;
}





