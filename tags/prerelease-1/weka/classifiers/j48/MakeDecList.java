/*
 *    MakeDecList.java
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

import java.util.*;
import weka.core.*;
import weka.classifiers.*;

/**
 * Class for handling a decision list.
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version 1.0
 */

public class MakeDecList {

  /**
   * Vector storing the rules.
   */

  private Vector theRules;

  /**
   * The confidence for C45-type pruning.
   */

  private double CF = 0.25f;

  /**
   * Minimum number of objects
   */

  private int minNumObj;

  /**
   * The model selection method.
   */

  private ModelSelection toSelectModeL;

  /**
   * How many subsets of equal size?
   * One used for pruning, the rest for training
   */

  private int numSetS = 3;

  /**
   * Use reduced error pruning?
   */

  private boolean reducedErrorPruning = false;

  // ===============
  // Public methods.
  // ===============

  /**
   * Constructor for dec list pruned using pessimistic pruning.
   */

  public MakeDecList(ModelSelection toSelectLocModel, double cf,
		     int minNum){

    toSelectModeL = toSelectLocModel;
    CF = cf;
    reducedErrorPruning = false;
    minNumObj = minNum;
  }

  /**
   * Constructor for dec list pruned using pessimistic pruning.
   */

  public MakeDecList(ModelSelection toSelectLocModel, int num,
		     int minNum){

    toSelectModeL = toSelectLocModel;
    numSetS = num;
    reducedErrorPruning = true;
    minNumObj = minNum;
  }

  /**
   * Builds dec list.
   * @exception Exception if dec list can't be built successfully
   */

  public void buildClassifier(Instances data) throws Exception{
    
    ClassifierDecList currentRule;
    double currentWeight;
    Instances oldGrowData, newGrowData, oldPruneData,
      newPruneData;
    int numRules = 0;
    
    if (data.classAttribute().isNumeric())
      throw new Exception("Class is numeric!");
    if (data.checkForStringAttributes()) {
      throw new Exception("Can't handle string attributes!");
    }
    
    theRules = new Vector();
    data = new Instances(data);
    data.deleteWithMissingClass();
    if (data.numInstances() == 0)
      throw new Exception("No training instances/Only instances with missing class!");
    if (reducedErrorPruning) {
      data.stratify(numSetS);
      oldGrowData = data.trainCV(numSetS, numSetS - 1);
      oldPruneData = data.testCV(numSetS, numSetS - 1);
    } else {
      oldGrowData = data;
      oldPruneData = null;
    }

    while (Utils.gr(oldGrowData.numInstances(),0)){

      // Create rule

      if (reducedErrorPruning) {
	currentRule = new PruneableDecList(toSelectModeL,
					   minNumObj);
	((PruneableDecList)currentRule).buildRule(oldGrowData, 
						  oldPruneData);
      } else {
	currentRule = new C45PruneableDecList(toSelectModeL, CF,
					      minNumObj);
	((C45PruneableDecList)currentRule).buildRule(oldGrowData);
      }
      numRules++;

      // Remove instances from growing data

      newGrowData = new Instances(oldGrowData,
					   oldGrowData.numInstances());
      Enumeration enum = oldGrowData.enumerateInstances();
      while (enum.hasMoreElements()) {
	Instance instance = (Instance) enum.nextElement();
	currentWeight = currentRule.weight(instance);
	if (Utils.sm(currentWeight,1)) {
	  instance.setWeight(instance.weight()*(1-currentWeight));
	  newGrowData.add(instance);
	}
      }
      newGrowData.compactify();
      oldGrowData = newGrowData;
      
      // Remove instances from pruning data
      
      if (reducedErrorPruning) {
	newPruneData = new Instances(oldPruneData,
					     oldPruneData.numInstances());
	enum = oldPruneData.enumerateInstances();
	while (enum.hasMoreElements()) {
	  Instance instance = (Instance) enum.nextElement();
	  currentWeight = currentRule.weight(instance);
	  if (Utils.sm(currentWeight,1)) {
	    instance.setWeight(instance.weight()*(1-currentWeight));
	    newPruneData.add(instance);
	  }
	}
	newPruneData.compactify();
	oldPruneData = newPruneData;
      }
      theRules.addElement(currentRule);
    }
  }

  /**
   * Outputs the classifier into a string.
   */
  
  public String toString(){

    StringBuffer text = new StringBuffer();

    for (int i=0;i<theRules.size();i++)
      text.append((ClassifierDecList)theRules.elementAt(i)+"\n");
    text.append("Number of Rules  : \t"+theRules.size()+"\n");

    return text.toString();
  }

  /** 
   * Classifies an instance.
   * @exception Exception if instance can't be classified
   */

  public double classifyInstance(Instance instance) 
       throws Exception {

    double maxProb = -1;
    double [] sumProbs;
    int maxIndex = 0;

    sumProbs = distributionForInstance(instance);
    for (int j = 0; j < sumProbs.length; j++) {
      if (Utils.gr(sumProbs[j],maxProb)){
	maxIndex = j;
	maxProb = sumProbs[j];
      }
    }

    return (double)maxIndex;
  }

  /** 
   * Returns the class distribution for an instance.
   * @exception Exception if distribution can't be computed
   */

  public double[] distributionForInstance(Instance instance) 
       throws Exception {

    double [] currentProbs = null;
    double [] sumProbs;
    double currentWeight, weight = 1;
    int i,j;
	
    // Get probabilities.
 
    sumProbs = new double [instance.numClasses()];
    i = 0;
    while (Utils.gr(weight,0)){
      currentWeight = 
	((ClassifierDecList)theRules.elementAt(i)).weight(instance);
      if (Utils.gr(currentWeight,0)) {
	currentProbs = ((ClassifierDecList)theRules.elementAt(i)).
	  distributionForInstance(instance);
	for (j = 0; j < sumProbs.length; j++)
	  sumProbs[j] += weight*currentProbs[j];
	weight = weight*(1-currentWeight);
      }
      i++;
    }

    return sumProbs;
  }

  /**
   * Outputs the number of rules in the classifier.
   */

  public int numRules(){

    return theRules.size();
  }
}













