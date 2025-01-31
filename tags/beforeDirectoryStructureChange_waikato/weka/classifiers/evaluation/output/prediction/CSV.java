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
 * CSV.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package weka.classifiers.evaluation.output.prediction;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Option;
import weka.core.Utils;

import java.util.Enumeration;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Outputs the predictions as CSV.
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -p &lt;range&gt;
 *  The range of attributes to print in addition to the classification.
 *  (default: none)</pre>
 * 
 * <pre> -distribution
 *  Whether to turn on the output of the class distribution.
 *  Only for nominal class attributes.
 *  (default: off)</pre>
 * 
 * <pre> -use-tab
 *  Whether to use TAB as separator instead of comma.
 *  (default: comma)</pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class CSV
  extends AbstractOutput {
  
  /** for serialization. */
  private static final long serialVersionUID = 3401604538169573720L;

  /** the delimiter. */
  protected String m_Delimiter = ",";
  
  /**
   * Returns a string describing the output generator.
   * 
   * @return 		a description suitable for
   * 			displaying in the GUI
   */
  public String globalInfo() {
    return "Outputs the predictions as CSV.";
  }
  
  /**
   * Returns a short display text, to be used in comboboxes.
   * 
   * @return 		a short display text
   */
  public String getDisplay() {
    return "CSV";
  }

  /**
   * Returns an enumeration of all the available options..
   *
   * @return 		an enumeration of all available options.
   */
  public Enumeration listOptions() {
    Vector	result;
    Enumeration	enm;
    
    result = new Vector();
    
    enm = super.listOptions();
    while (enm.hasMoreElements())
      result.add(enm.nextElement());
    
    result.addElement(new Option(
        "\tWhether to use TAB as separator instead of comma.\n"
	+ "\t(default: comma)",
        "use-tab", 0, "-use-tab"));
    
    return result.elements();
  }

  /**
   * Sets the OptionHandler's options using the given list. All options
   * will be set (or reset) during this call (i.e. incremental setting
   * of options is not possible).
   *
   * @param options 	the list of options as an array of strings
   * @throws Exception 	if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    setUseTab(Utils.getFlag("use-tab", options));
    super.setOptions(options);
  }

  /**
   * Gets the current option settings for the OptionHandler.
   *
   * @return the list of current option settings as an array of strings
   */
  public String[] getOptions() {
    Vector<String>	result;
    String[]		options;
    int			i;
    
    result = new Vector<String>();
    
    options = super.getOptions();
    for (i = 0; i < options.length; i++)
      result.add(options[i]);
    
    if (getUseTab())
      result.add("-use-tab");
    
    return result.toArray(new String[result.size()]);
  }
  
  /**
   * Sets whether to use tab instead of comma as separator.
   * 
   * @param value	true if tab is to be used
   */
  public void setUseTab(boolean value) {
    if (value)
      m_Delimiter = "\t";
    else
      m_Delimiter = ",";
  }
  
  /**
   * Returns whether tab is used as separator.
   * 
   * @return		true if tab is used instead of comma
   */
  public boolean getUseTab() {
    return m_Delimiter.equals("\t");
  }

  /**
   * Returns the tip text for this property.
   * 
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI
   */
  public String useTabTipText() {
    return "Whether to use TAB instead of COMMA as column separator.";
  }

  /**
   * Performs the actual printing of the header.
   */
  protected void doPrintHeader() {
    if (m_Header.classAttribute().isNominal()) {
      if (m_OutputDistribution) {
	m_Buffer.append("inst#" + m_Delimiter + "actual" + m_Delimiter + "predicted" + m_Delimiter + "error" + m_Delimiter + "distribution");
	for (int i = 1; i < m_Header.classAttribute().numValues(); i++)
	  m_Buffer.append(m_Delimiter);
      }
      else {
	m_Buffer.append("inst#" + m_Delimiter + "actual" + m_Delimiter + "predicted error" + m_Delimiter + "prediction");
      }
    }
    else {
      m_Buffer.append("inst#" + m_Delimiter + "actual" + m_Delimiter + "predicted" + m_Delimiter + "error");
    }
    
    if (m_Attributes != null) {
      m_Buffer.append(m_Delimiter);
      boolean first = true;
      for (int i = 0; i < m_Header.numAttributes(); i++) {
        if (i == m_Header.classIndex())
          continue;

        if (m_Attributes.isInRange(i)) {
          if (!first)
            m_Buffer.append(m_Delimiter);
          m_Buffer.append(m_Header.attribute(i).name());
          first = false;
        }
      }
    }
    
    m_Buffer.append("\n");
  }

  /**
   * Builds a string listing the attribute values in a specified range of indices,
   * separated by commas and enclosed in brackets.
   *
   * @param instance 	the instance to print the values from
   * @return 		a string listing values of the attributes in the range
   */
  protected String attributeValuesString(Instance instance) {
    StringBuffer text = new StringBuffer();
    if (m_Attributes != null) {
      m_Attributes.setUpper(instance.numAttributes() - 1);
      boolean first = true;
      for (int i=0; i<instance.numAttributes(); i++)
	if (m_Attributes.isInRange(i) && i != instance.classIndex()) {
	  if (!first)
	    text.append(m_Delimiter);
	  text.append(instance.toString(i));
	  first = false;
	}
    }
    return text.toString();
  }

  /**
   * Store the prediction made by the classifier as a string.
   * 
   * @param classifier	the classifier to use
   * @param inst	the instance to generate text from
   * @param index	the index in the dataset
   * @throws Exception	if something goes wrong
   */
  protected void doPrintClassification(Classifier classifier, Instance inst, int index) throws Exception {
    int prec = 3;

    Instance withMissing = (Instance)inst.copy();
    withMissing.setDataset(inst.dataset());
    double predValue = ((Classifier)classifier).classifyInstance(withMissing);

    // index
    m_Buffer.append("" + (index+1));

    if (inst.dataset().classAttribute().isNumeric()) {
      // actual
      if (inst.classIsMissing())
	m_Buffer.append(m_Delimiter + "?");
      else
	m_Buffer.append(m_Delimiter + Utils.doubleToString(inst.classValue(), prec));
      // predicted
      if (Instance.isMissingValue(predValue))
	m_Buffer.append(m_Delimiter + "?");
      else
	m_Buffer.append(m_Delimiter + Utils.doubleToString(predValue, prec));
      // error
      if (Instance.isMissingValue(predValue) || inst.classIsMissing())
	m_Buffer.append(m_Delimiter + "?");
      else
	m_Buffer.append(m_Delimiter + Utils.doubleToString(predValue - inst.classValue(), prec));
    } else {
      // actual
      m_Buffer.append(m_Delimiter + ((int) inst.classValue()+1) + ":" + inst.toString(inst.classIndex()));
      // predicted
      if (Instance.isMissingValue(predValue))
	m_Buffer.append(m_Delimiter + "?");
      else
	m_Buffer.append(m_Delimiter + ((int) predValue+1) + ":" + inst.dataset().classAttribute().value((int)predValue));
      // error?
      if ((int) predValue+1 != (int) inst.classValue()+1)
	m_Buffer.append(m_Delimiter + "+");
      else
	m_Buffer.append(m_Delimiter + "");
      // prediction/distribution
      if (m_OutputDistribution) {
	if (Instance.isMissingValue(predValue)) {
	  m_Buffer.append(m_Delimiter + "?");
	}
	else {
	  m_Buffer.append(m_Delimiter);
	  double[] dist = classifier.distributionForInstance(withMissing);
	  for (int n = 0; n < dist.length; n++) {
	    if (n > 0)
	      m_Buffer.append(m_Delimiter);
	    if (n == (int) predValue)
	      m_Buffer.append("*");
            m_Buffer.append(Utils.doubleToString(dist[n], prec));
	  }
	}
      }
      else {
	if (Instance.isMissingValue(predValue))
	  m_Buffer.append(m_Delimiter + "?");
	else
	  m_Buffer.append(m_Delimiter + Utils.doubleToString(classifier.distributionForInstance(withMissing) [(int)predValue], prec));
      }
    }

    // attributes
    m_Buffer.append(m_Delimiter + attributeValuesString(withMissing) + "\n");
  }
  
  /**
   * Does nothing.
   */
  protected void doPrintFooter() {
  }
}
