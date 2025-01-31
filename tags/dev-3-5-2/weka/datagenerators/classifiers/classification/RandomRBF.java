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
 * RandomRBF.java
 * Copyright (C) 2005 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.datagenerators.classifiers.classification;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;
import weka.datagenerators.DataGenerator;
import weka.datagenerators.ClassificationGenerator;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * RandomRBF data is generated by first creating a random set of centers for
 * each class. Each center is randomly assigned a weight, a central point per
 * attribute, and a standard deviation. To generate new instances, a center is
 * chosen at random taking the weights of each center into consideration.
 * Attribute values are randomly generated and offset from the center, where
 * the overall vector has been scaled so that its length equals a value sampled
 * randomly from the Gaussian distribution of the center. The particular center
 * chosen determines the class of the instance. RandomRBF data contains only
 * numeric attributes as it is non-trivial to include nominal values. <p/>
 *
 * Valid options are: <p/>
 *
 * -d <br/>
 *  enables debugging information to be output on the console. <p/>
 *
 * -r string <br/>
 *  Name of the relation of the generated dataset. <p/>
 *
 * -a num <br/>
 *  Number of attributes. <p/>
 *
 * -o filename<br/>
 *  writes the generated dataset to the given file using ARFF-Format.
 *  (default = stdout). <p/>
 *
 * -c num <br/>
 *  Number of classes - NOT used in this generator. <p/>
 *
 * -n num <br/>
 *  Number of examples. <p/>
 * 
 * -S num <br/>
 *  the seed value for initializing the random number generator.
 *  <p/>
 *
 * -C num <br/>
 *  the number of centroids to use. <p/>
 *
 * @author Richard Kirkby (rkirkby at cs dot waikato dot ac dot nz)
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1.1 $
 */

public class RandomRBF
  extends ClassificationGenerator {

  /** Number of attribute the dataset should have */
  protected int m_NumAttributes;

  /** Number of Classes the dataset should have */
  protected int m_NumClasses;

  /** the number of centroids to use for generation */
  protected int m_NumCentroids;
  
  /** the centroids */
  protected double[][] m_centroids;
  
  /** the classes of the centroids */
  protected int[] m_centroidClasses;
  
  /** the weights of the centroids */
  protected double[] m_centroidWeights;
  
  /** the stddevs of the centroids */
  protected double[] m_centroidStdDevs;

  /**
   * initializes the generator with default values
   */
  public RandomRBF() {
    super();

    setNumAttributes(defaultNumAttributes());
    setNumClasses(defaultNumClasses());
    setNumCentroids(defaultNumCentroids());
  }

  /**
   * Returns a string describing this data generator.
   *
   * @return a description of the data generator suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return 
        "RandomRBF data is generated by first creating a random set of "
      + "centers for each class. Each center is randomly assigned a weight, "
      + "a central point per attribute, and a standard deviation. To "
      + "generate new instances, a center is chosen at random taking the "
      + "weights of each center into consideration. Attribute values are "
      + "randomly generated and offset from the center, where the overall "
      + "vector has been scaled so that its length equals a value sampled "
      + "randomly from the Gaussian distribution of the center. The "
      + "particular center chosen determines the class of the instance.\n "
      + "RandomRBF data contains only numeric attributes as it is "
      + "non-trivial to include nominal values.";
  }

 /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options
   */
  public Enumeration listOptions() {
    Vector result = enumToVector(super.listOptions());

    result.addElement(new Option(
          "\tThe number of attributes (default " 
          + defaultNumAttributes() + ").",
          "a", 1, "-a <num>"));

    result.addElement(new Option(
        "\tThe number of classes (default " + defaultNumClasses() + ")",
        "c", 1, "-c <num>"));

    result.add(new Option(
              "\tThe number of centroids to use. (default " 
              + defaultNumCentroids() + ")",
              "C", 1, "-C <num>"));

    return result.elements();
  }

  /**
   * Parses a list of options for this object. <p/>
   *
   * For list of valid options see class description.<p/>
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    String        tmpStr;

    super.setOptions(options);

    tmpStr = Utils.getOption('a', options);
    if (tmpStr.length() != 0)
      setNumAttributes(Integer.parseInt(tmpStr));
    else
      setNumAttributes(defaultNumAttributes());

    tmpStr = Utils.getOption('c', options);
    if (tmpStr.length() != 0)
      setNumClasses(Integer.parseInt(tmpStr));
    else
      setNumClasses(defaultNumClasses());
    
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0)
      setNumCentroids(Integer.parseInt(tmpStr));
    else
      setNumCentroids(defaultNumCentroids());
  }

  /**
   * Gets the current settings of the datagenerator.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String[] getOptions() {
    Vector        result;
    String[]      options;
    int           i;
    
    result  = new Vector();
    options = super.getOptions();
    for (i = 0; i < options.length; i++)
      result.add(options[i]);
    
    result.add("-a");
    result.add("" + getNumAttributes());

    result.add("-c");
    result.add("" + getNumClasses());

    result.add("-C");
    result.add("" + getNumCentroids());
    
    return (String[]) result.toArray(new String[result.size()]);
  }

  /**
   * returns the default number of attributes
   */
  protected int defaultNumAttributes() {
    return 10;
  }

  /**
   * Sets the number of attributes the dataset should have.
   * @param numAttributes the new number of attributes
   */
  public void setNumAttributes(int numAttributes) {
    m_NumAttributes = numAttributes;
  }

  /**
   * Gets the number of attributes that should be produced.
   * @return the number of attributes that should be produced
   */
  public int getNumAttributes() { 
    return m_NumAttributes; 
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   *         displaying in the explorer/experimenter gui
   */
  public String numAttributesTipText() {
    return "The number of attributes the generated data will contain.";
  }

  /**
   * returns the default number of classes
   */
  protected int defaultNumClasses() {
    return 2;
  }

  /**
   * Sets the number of classes the dataset should have.
   * @param numClasses the new number of classes
   */
  public void setNumClasses(int numClasses) { 
    m_NumClasses = numClasses; 
  }

  /**
   * Gets the number of classes the dataset should have.
   * @return the number of classes the dataset should have
   */
  public int getNumClasses() { 
    return m_NumClasses; 
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   *         displaying in the explorer/experimenter gui
   */
  public String numClassesTipText() {
    return "The number of classes to generate.";
  }

  /**
   * returns the default number of centroids
   */
  protected int defaultNumCentroids() {
    return 50;
  }
  
  /**
   * Gets the number of centroids.
   *
   * @return the number of centroids.
   */
  public int getNumCentroids() { 
    return m_NumCentroids; 
  }
  
  /**
   * Sets the number of centroids to use.
   *
   * @param value the number of centroids to use.
   */
  public void setNumCentroids(int value) { 
    if (value > 0)
      m_NumCentroids = value; 
    else
      System.out.println("At least 1 centroid is necessary (provided: " 
          + value + ")!");
  }  
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   *         displaying in the explorer/experimenter gui
   */
  public String numCentroidsTipText() {
    return "The number of centroids to use.";
  }

  /**
   * Return if single mode is set for the given data generator
   * mode depends on option setting and or generator type.
   * 
   * @return single mode flag
   * @throws Exception if mode is not set yet
   */
  public boolean getSingleModeFlag() throws Exception {
    return true;
  }

  /**
   * returns a random index based on the given proportions
   *
   * @param proportionArray     the proportions
   * @param random              the random number generator to use
   */
  protected int chooseRandomIndexBasedOnProportions(
      double[] proportionArray, Random random) {

    double      probSum;
    double      val;
    int         index;
    double      sum;

    probSum = Utils.sum(proportionArray);
    val     = random.nextDouble() * probSum;
    index   = 0;
    sum     = 0.0;
    
    while ((sum <= val) && (index < proportionArray.length))
      sum += proportionArray[index++];
    
    return index - 1;
  }

  /**
   * Initializes the format for the dataset produced. 
   * Must be called before the generateExample or generateExamples
   * methods are used.
   * Re-initializes the random number generator with the given seed.
   *
   * @return the format for the dataset 
   * @throws Exception if the generating of the format failed
   * @see  #getSeed()
   */
  public Instances defineDataFormat() throws Exception {
    int             i;
    int             j;
    FastVector      atts;
    FastVector      clsValues;
    Random          rand;

    m_Random = new Random(getSeed());
    rand     = getRandom();

    // number of examples is the same as given per option
    setNumExamplesAct(getNumExamples());

    // initialize centroids
    m_centroids       = new double[getNumCentroids()][getNumAttributes()];
    m_centroidClasses = new int[getNumCentroids()];
    m_centroidWeights = new double[getNumCentroids()];
    m_centroidStdDevs = new double[getNumCentroids()];

    for (i = 0; i < getNumCentroids(); i++) {
      for (j = 0; j < getNumAttributes(); j++)
        m_centroids[i][j] = rand.nextDouble();
      m_centroidClasses[i] = rand.nextInt(getNumClasses());
      m_centroidWeights[i] = rand.nextDouble();
      m_centroidStdDevs[i] = rand.nextDouble();
    }

    // initialize dataset format
    atts = new FastVector();
    for (i = 0; i < getNumAttributes(); i++)
      atts.addElement(new Attribute("a" + i));

    clsValues = new FastVector();
    for (i = 0; i < getNumClasses(); i++)
      clsValues.addElement("c" + i);
    atts.addElement(new Attribute("class", clsValues));
    
    m_DatasetFormat = new Instances(getRelationNameToUse(), atts, 0);
    
    return m_DatasetFormat;
  }

  /**
   * Generates one example of the dataset. 
   *
   * @return the generated example
   * @throws Exception if the format of the dataset is not yet defined
   * @throws Exception if the generator only works with generateExamples
   * which means in non single mode
   */
  public Instance generateExample() throws Exception {
    Instance    result;
    int         centroid;
    double[]    atts;
    double      magnitude;
    double      desiredMag;
    double      scale;
    int         i;
    double      label;
    Random      rand;

    result = null;
    rand   = getRandom();

    if (m_DatasetFormat == null)
      throw new Exception("Dataset format not defined.");

    // generate class label based on class probs
    centroid = chooseRandomIndexBasedOnProportions(m_centroidWeights, rand);
    label    = m_centroidClasses[centroid];

    // generate attributes
    atts = new double[getNumAttributes() + 1];
    for (i = 0; i < getNumAttributes(); i++)
      atts[i] = (rand.nextDouble() * 2.0) - 1.0;
    atts[atts.length - 1] = label;
    
    magnitude = 0.0;
    for (i = 0; i < getNumAttributes(); i++)
      magnitude += atts[i] * atts[i];
    
    magnitude  = Math.sqrt(magnitude);
    desiredMag = rand.nextGaussian() * m_centroidStdDevs[centroid];
    scale      = desiredMag / magnitude;
    for (i = 0; i < getNumAttributes(); i++) {
      atts[i] *= scale;
      atts[i] += m_centroids[centroid][i];
      result   = new Instance(1.0, atts);
    }

    // dataset reference
    result.setDataset(m_DatasetFormat);
    
    return result;
  }

  /**
   * Generates all examples of the dataset. Re-initializes the random number
   * generator with the given seed, before generating instances.
   *
   * @return the generated dataset
   * @throws Exception if the format of the dataset is not yet defined
   * @throws Exception if the generator only works with generateExample,
   * which means in single mode
   * @see   #getSeed()
   */
  public Instances generateExamples() throws Exception {
    Instances       result;
    int             i;

    result   = new Instances(m_DatasetFormat, 0);
    m_Random = new Random(getSeed());

    for (i = 0; i < getNumExamplesAct(); i++)
      result.add(generateExample());
    
    return result;
  }

  /**
   * Generates a comment string that documentates the data generator.
   * By default this string is added at the beginning of the produced output
   * as ARFF file type, next after the options.
   * 
   * @return string contains info about the generated rules
   * @throws Exception if the generating of the documentation fails
   */
  public String generateStart () {
    StringBuffer        result;
    int                 i;

    result = new StringBuffer();

    result.append("%\n");
    result.append("% centroids:\n");
    for (i = 0; i < getNumCentroids(); i++)
      result.append(
          "% " + i + ".: " + Utils.arrayToString(m_centroids[i]) + "\n");
    result.append("%\n");
    result.append(
        "% centroidClasses: " + Utils.arrayToString(m_centroidClasses) + "\n");
    result.append("%\n");
    result.append(
        "% centroidWeights: " + Utils.arrayToString(m_centroidWeights) + "\n");
    result.append("%\n");
    result.append(
        "% centroidStdDevs: " + Utils.arrayToString(m_centroidStdDevs) + "\n");
    result.append("%\n");
    
    return result.toString();
  }

  /**
   * Generates a comment string that documentats the data generator.
   * By default this string is added at the end of theproduces output
   * as ARFF file type.
   * 
   * @return string contains info about the generated rules
   * @throws Exception if the generating of the documentaion fails
   */
  public String generateFinished() throws Exception {
    return "";
  }

  /**
   * Main method for executing this class.
   *
   * @param args should contain arguments for the data producer: 
   */
  public static void main(String[] args) {
    try {
      DataGenerator.makeData(new RandomRBF(), args);
    } 
    catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
    }
  }
}
