/*
 *    CSVResultListener.java
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

package weka.experiment;

import weka.core.Utils;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import weka.core.OptionHandler;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Option;

/**
 * CSVResultListener outputs the received results in csv format to
 * a Writer
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 1.5 $
 */
public class CSVResultListener implements ResultListener, OptionHandler {

  /** The ResultProducer sending us results */
  protected ResultProducer m_RP;

  /** The destination output file, null sends to System.out */
  protected File m_OutputFile = new File("-");

  /** The destination for results (typically connected to the output file) */
  protected transient PrintWriter m_Out = new PrintWriter(System.out, true);

  /**
   * Returns a string describing this result listener
   * @return a description of the result listener suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Takes results from a result producer and assembles them into "
      +"comma separated value form.";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options
   */
  public Enumeration listOptions() {

    Vector newVector = new Vector(1);

    newVector.addElement(new Option(
	     "\tThe filename where output will be stored. Use - for stdout.\n"
	      +"\t(default stdout)", 
	     "O", 1, 
	     "-O <file name>"));

    return newVector.elements();
  }

  /**
   * Parses a given list of options. Valid options are:<p>
   *
   * -O filename <br>
   * The filename where output will be stored. Use - for stdout.
   * (default stdout)
   *
   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    
    String fName = Utils.getOption('O', options);
    if (fName.length() != 0) {
      setOutputFile(new File(fName));
    } else {
      setOutputFile(new File("-"));
    }
  }

  /**
   * Gets the current settings of the Classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String [] getOptions() {

    String [] options = new String [2];
    int current = 0;

    options[current++] = "-O";
    options[current++] = getOutputFile().getName();
    while (current < options.length) {
      options[current++] = "";
    }
    return options;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String outputFileTipText() {
    return "File to save to. Use '-' to write to standard out.";
  }

  /**
   * Get the value of OutputFile.
   *
   * @return Value of OutputFile.
   */
  public File getOutputFile() {
    
    return m_OutputFile;
  }
  
  /**
   * Set the value of OutputFile.
   *
   * @param newOutputFile Value to assign to OutputFile.
   */
  public void setOutputFile(File newOutputFile) {
    
    m_OutputFile = newOutputFile;
  }
  
  /**
   * Prepare for the results to be received.
   *
   * @param rp the ResultProducer that will generate the results
   * @exception Exception if an error occurs during preprocessing.
   */
  public void preProcess(ResultProducer rp) throws Exception {

    m_RP = rp;
    if ((m_OutputFile == null) || (m_OutputFile.getName().equals("-"))) {
      m_Out = new PrintWriter(System.out, true);
    } else {
      m_Out = new PrintWriter(
	      new BufferedOutputStream(
	      new FileOutputStream(m_OutputFile)), true);
    }
    printResultNames(m_RP);
  }
  
  /**
   * Perform any postprocessing. When this method is called, it indicates
   * that no more results will be sent that need to be grouped together
   * in any way.
   *
   * @param rp the ResultProducer that generated the results
   * @exception Exception if an error occurs
   */
  public void postProcess(ResultProducer rp) throws Exception {

    if (!(m_OutputFile == null) && !(m_OutputFile.getName().equals("-"))) {
      m_Out.close();
    }
  }

  /**
   * Determines if there are any constraints (imposed by the
   * destination) on the result columns to be produced by
   * resultProducers. Null should be returned if there are NO
   * constraints, otherwise a list of column names should be
   * returned as an array of Strings.
   * @param rp the ResultProducer to which the constraints will apply
   * @return an array of column names to which resutltProducer's
   * results will be restricted.
   * @exception Exception if an error occurs.
   */
  public String [] determineColumnConstraints(ResultProducer rp) throws Exception {
    return null;
  }

  /**
   * Just prints out each result as it is received.
   *
   * @param rp the ResultProducer that generated the result
   * @param key The key for the results.
   * @param result The actual results.
   * @exception Exception if the result could not be accepted.
   */
  public void acceptResult(ResultProducer rp, Object[] key, Object[] result) 
    throws Exception {

    if (m_RP != rp) {
      throw new Error("Unrecognized ResultProducer sending results!!");
    }
    for (int i = 0; i < key.length; i++) {
      if (i != 0) {
	m_Out.print(',');
      }
      if (key[i] == null) {
	m_Out.print("?");
      } else {
	m_Out.print(Utils.quote(key[i].toString()));
      }
    }
    for (int i = 0; i < result.length; i++) {
      m_Out.print(',');
      if (result[i] == null) {
	m_Out.print("?");
      } else {
	m_Out.print(Utils.quote(result[i].toString()));
      }
    }
    m_Out.println("");
  }

  /**
   * Always says a result is required. If this is the first call,
   * prints out the header for the csv output.
   *
   * @param rp the ResultProducer wanting to generate the result
   * @param key The key for which a result may be needed.
   * @return true if the result should be calculated.
   * @exception Exception if it could not be determined if the result 
   * is needed.
   */
  public boolean isResultRequired(ResultProducer rp, Object[] key) 
    throws Exception {

    return true;
  }


  /**
   * Prints the names of each field out as the first row of the CSV output.
   *
   * @param rp the ResultProducer generating our results.
   * @exception Exception if the field names could not be determined.
   */
  private void printResultNames(ResultProducer rp) throws Exception {

    String [] key = rp.getKeyNames();
    for (int i = 0; i < key.length; i++) {
      if (i != 0) {
	m_Out.print(',');
      }
      if (key[i] == null) {
	m_Out.print("?");
      } else {
	m_Out.print(key[i].toString());
      }
    }
    String [] result = rp.getResultNames();
    for (int i = 0; i < result.length; i++) {
      m_Out.print(',');
      if (result[i] == null) {
	m_Out.print("?");
      } else {
	m_Out.print(result[i].toString());
      }
    }
    m_Out.println("");
  }
} // CSVResultListener



