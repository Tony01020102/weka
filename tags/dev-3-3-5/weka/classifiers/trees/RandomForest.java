/*
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

/*
 *    RandomForest.java
 *    Copyright (C) 2001 Richard Kirkby
 *
 */

package weka.classifiers.trees;

import weka.classifiers.*;
import weka.classifiers.meta.Bagging;
import weka.core.*;
import java.util.*;

/**
 * Class for constructing random forests.
 *
 * For more information see: <p>
 * Leo Breiman. Random Forests. Machine Learning 45 (1):5-32, October 2001. <p>
 *
 * Valid options are: <p>
 *
 * -I num <br>
 * Set the number of trees in the forest
 * (default 10) <p>
 *
 * -K num <br>
 * Set the number of features to consider.
 * If < 1 (the default) will use logM+1, where M is the number of inputs. <p>
 *
 * -S seed <br>
 * Random number seed (default 1). <p>
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 1.2 $
 */
public class RandomForest extends DistributionClassifier 
  implements OptionHandler, Randomizable, WeightedInstancesHandler {

  /** Number of trees in forest. */
  protected int m_numTrees = 10;

  /** Number of features to consider in random feature selection.
      If less than 1 will use int(logM+1) ) */
  protected int m_numFeatures = 0;

  /** The random seed. */
  protected int m_randomSeed = 1;  

  /** Final number of features that were considered in last build. */
  protected int m_KValue = 0;

  /** The bagger. */
  protected Bagging m_bagger = null;

  /**
   * Get the value of numTrees.
   *
   * @return Value of numTrees.
   */
  public int getNumTrees() {
    
    return m_numTrees;
  }
  
  /**
   * Set the value of numTrees.
   *
   * @param newNumTrees Value to assign to numTrees.
   */
  public void setNumTrees(int newNumTrees) {
    
    m_numTrees = newNumTrees;
  }

  /**
   * Get the number of features used in random selection.
   *
   * @return Value of numFeatures.
   */
  public int getNumFeatures() {
    
    return m_numFeatures;
  }
  
  /**
   * Set the number of features to use in random selection.
   *
   * @param newNumFeatures Value to assign to numFeatures.
   */
  public void setNumFeatures(int newNumFeatures) {
    
    m_numFeatures = newNumFeatures;
  }

  /**
   * Set the seed for random number generation.
   *
   * @param seed the seed 
   */
  public void setSeed(int seed) {

    m_randomSeed = seed;
  }
  
  /**
   * Gets the seed for the random number generations
   *
   * @return the seed for the random number generation
   */
  public int getSeed() {

    return m_randomSeed;
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options
   */
  public Enumeration listOptions() {
    
    Vector newVector = new Vector(3);

    newVector.
      addElement(new Option("\tNumber of trees to build.",
			    "I", 1, "-I <number of trees>"));
    newVector.
      addElement(new Option("\tNumber of features to consider (<1=int(logM+1)).",
			    "K", 1, "-K <number of features>"));
    newVector
      .addElement(new Option("\tSeed for random number generator.\n"
			     + "\t(default 1)",
			     "S", 1, "-S"));
    return newVector.elements();
  }

  /**
   * Gets the current settings of the forest.
   *
   * @return an array of strings suitable for passing to setOptions()
   */
  public String[] getOptions() {
    
    String [] options = new String [10];
    int current = 0;
    options[current++] = "-I"; 
    options[current++] = "" + getNumTrees();
    options[current++] = "-K"; 
    options[current++] = "" + getNumFeatures();
    options[current++] = "-S";
    options[current++] = "" + getSeed();
    while (current < options.length) {
      options[current++] = "";
    }
    return options;
  }

  /**
   * Parses a given list of options.
   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception{
    
    String numTreesString = Utils.getOption('I', options);
    if (numTreesString.length() != 0) {
      m_numTrees = Integer.parseInt(numTreesString);
    } else {
      m_numTrees = 10;
    }
    String numFeaturesString = Utils.getOption('K', options);
    if (numFeaturesString.length() != 0) {
      m_numFeatures = Integer.parseInt(numFeaturesString);
    } else {
      m_numFeatures = 0;
    }
    String seed = Utils.getOption('S', options);
    if (seed.length() != 0) {
      setSeed(Integer.parseInt(seed));
    } else {
      setSeed(1);
    }
    Utils.checkForRemainingOptions(options);
  }  

  /**
   * Builds a classifier for a set of instances.
   *
   * @param instances the instances to train the classifier with
   * @exception Exception if something goes wrong
   */
  public void buildClassifier(Instances data) throws Exception {

    m_bagger = new Bagging();
    RandomTree rTree = new RandomTree();

    // set up the random tree options
    m_KValue = m_numFeatures;
    if (m_KValue < 1) m_KValue = (int) Utils.log2(data.numAttributes())+1;
    rTree.setKValue(m_KValue);

    // set up the bagger and build the forest
    m_bagger.setClassifier(rTree);
    m_bagger.setSeed(m_randomSeed);
    m_bagger.setNumIterations(m_numTrees);
    m_bagger.buildClassifier(data);
  }

  /**
   * Returns the class probability distribution for an instance.
   *
   * @param instance the instance to be classified
   * @return the distribution the forest generates for the instance
   */
  public double[] distributionForInstance(Instance instance) throws Exception {

    return m_bagger.distributionForInstance(instance);
  }

  /**
   * Outputs a description of this classifier.
   *
   * @return a string containing a description of the classifier
   */
  public String toString() {

    if (m_bagger == null) return "Random forest not built yet";
    else return "Random forest of " + m_numTrees
	   + " trees, each constructed while considering "
	   + m_KValue + " random feature" + (m_KValue==1 ? "" : "s") + ".\n\n";
  }

  /**
   * Main method for this class.
   *
   * @param argv the options
   */
  public static void main(String[] argv) {

    try {
      System.out.println(Evaluation.evaluateModel(new RandomForest(), argv));
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
  }

}
