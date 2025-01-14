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
 * Expression.java
 * Copyright (C) 2005 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.datagenerators.classifiers.regression;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;
import weka.datagenerators.RegressionGenerator;
import weka.datagenerators.DataGenerator;
import weka.filters.unsupervised.attribute.AddExpression;

import java.util.Random;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A regression generator that uses the 
 * <code>weka.filters.unsupervised.attribute.AddExpression</code> filter
 * to generate y out of a randomly generated x. E.g., for the mexican hat: <br/>
 * <pre>   sin(abs(a1)) / abs(a1)</pre>
 * It is a generalization of the Mexican Hat generator.<br/>
 * <p/>
 *
 * Valid options are: <p/>
 *
 * -d <br/>
 *  enables debugging information to be output on the console. <p/>
 *
 * -r string <br/>
 *  Name of the relation of the generated dataset. <p/>
 *
 * -o filename<br/>
 *  writes the generated dataset to the given file using ARFF-Format.
 *  (default = stdout). <p/>
 *
 * -n num <br/>
 *  Number of examples. <p/>
 * 
 * -S num <br/>
 *  the seed value for initializing the random number generator.
 *  <p/>
 *
 * -E expression <br/>
 *  the expression for generating y out of x. <p/>
 *
 * -A num <br/>
 *  the amplitude multiplier. <p/>
 *
 * -R num..num <br/>
 *  the range the x is randomly drawn from. <p/>
 *
 * -N num <br/>
 *  the rate of gaussian noise to add to the data (0 &lt;= num &lt; 1).
 *  <p/>
 *
 * -V num <br/>
 *  the variance of the gaussian noise.<p/>
 *
 * @author  FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1.1 $
 * @see     AddExpression
 * @see     MexicanHat
 */

public class Expression
  extends MexicanHat {

  /** the expression for computing y */
  protected String m_Expression;

  /** the filter for generating y out of x */
  protected AddExpression m_Filter;

  /** the input data structure for the filter */
  protected Instances m_RawData;
  
  /**
   * initializes the generator
   */
  public Expression() {
    super();

    setExpression(defaultExpression());
  }
  
  /**
   * Returns a string describing this data generator.
   *
   * @return a description of the data generator suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return 
        "A data generator for generating y according to a given expression "
        + "out of randomly generated x.\n"
        + "E.g., the mexican hat can be generated like this:\n"
        + "   sin(abs(a1)) / abs(a1)";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options
   */
  public Enumeration listOptions() {
    Vector result = enumToVector(super.listOptions());

    result.addElement(new Option(
              "\tThe expression to use for generating y out of x "
              + "\t(default " + defaultExpression() + ").",
              "E", 1, "-E <expression>"));

    return result.elements();
  }
  
  /**
   * Parses a list of options for this object. <p/>
   *
   * For list of valid options see class description.<p/>
   *
   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    String        tmpStr;
   
    super.setOptions(options);

    tmpStr = Utils.getOption('E', options);
    if (tmpStr.length() != 0)
      setExpression(tmpStr);
    else
      setExpression(defaultExpression());
  }

  /**
   * Gets the current settings of the datagenerator BIRCHCluster.
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
    
    result.add("-E"); 
    result.add("" + getExpression());
    
    return (String[]) result.toArray(new String[result.size()]);
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   *         displaying in the explorer/experimenter gui
   */
  public String amplitudeTipText() {
    return "The amplitude to multiply the y value with.";
  }

  /**
   * returns the default expression
   */
  protected String defaultExpression() {
    return "sin(abs(a1)) / abs(a1)";
  }

  /**
   * Gets the mathematical expression for generating y out of x
   *
   * @return the expression for computing y
   */
  public String getExpression() { 
    return m_Expression; 
  }
  
  /**
   * Sets the mathematical expression to generate y out of x.
   *
   * @param value the expression for computing y
   */
  public void setExpression(String value) {
    if (value.length() != 0)
      m_Expression = value;
    else
      throw new IllegalArgumentException(
          "An expression has to be provided!");
  }
  
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for
   *         displaying in the explorer/experimenter gui
   */
  public String expressionTipText() {
    return "The expression for generating y out of x.";
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
    FastVector      atts;

    // initialize input format
    atts = new FastVector();
    atts.addElement(new Attribute("x"));
    
    m_RawData = new Instances(getRelationNameToUse(), atts, 0);

    m_Filter = new AddExpression();
    m_Filter.setName("y");
    m_Filter.setExpression(getExpression());
    m_Filter.setInputFormat(m_RawData);

    return super.defineDataFormat();
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
    Random      rand;
    double      x;
    double      y;
    double[]    atts;
    Instance    inst;

    result = null;
    rand   = getRandom();

    if (m_DatasetFormat == null)
      throw new Exception("Dataset format not defined.");

    // random x
    x = rand.nextDouble();
    // fit into range
    x = x * (getMaxRange() - getMinRange()) + getMinRange();
    
    // generate y
    atts    = new double[1];
    atts[0] = x;
    inst    = new Instance(1.0, atts);
    m_Filter.input(inst);
    m_Filter.batchFinished();
    inst = m_Filter.output();
    
    // noise
    y = inst.value(1) + getAmplitude() 
            * m_NoiseRandom.nextGaussian() 
            * getNoiseRate() * getNoiseVariance();

    // generate attributes
    atts = new double[m_DatasetFormat.numAttributes()];
    
    atts[0] = x;
    atts[1] = y;
    result = new Instance(1.0, atts);

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
    return "";
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
   * Main method for testing this class.
   *
   * @param args should contain arguments for the data producer: 
   */
  public static void main(String[] args) {
    try {
      DataGenerator.makeData(new Expression(), args);
    } 
    catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
    }
  }
}

