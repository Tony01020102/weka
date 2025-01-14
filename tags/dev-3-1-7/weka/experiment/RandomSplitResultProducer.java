/*
 *    RandomSplitResultProducer.java
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

import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Option;
import weka.core.Utils;

import java.util.Enumeration;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Random;
import java.util.Vector;
import java.io.File;

/**
 * Generates a single train/test split and calls the appropriate
 * SplitEvaluator to generate some results.
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 1.5 $
 */

public class RandomSplitResultProducer 
  implements ResultProducer, OptionHandler {
  
  /** The dataset of interest */
  protected Instances m_Instances;

  /** The ResultListener to send results to */
  protected ResultListener m_ResultListener = new CSVResultListener();

  /** The percentage of instances to use for training */
  protected int m_TrainPercent = 66;

  /** The SplitEvaluator used to generate results */
  protected SplitEvaluator m_SplitEvaluator = new ClassifierSplitEvaluator();

  /** The names of any additional measures to look for in SplitEvaluators */
  protected String [] m_AdditionalMeasures = null;

  /** Save raw output of split evaluators --- for debugging purposes */
  protected boolean m_debugOutput = false;

  /** The output zipper to use for saving raw splitEvaluator output */
  protected OutputZipper m_ZipDest = null;

  /** The destination output file/directory for raw output */
  protected File m_OutputFile = new File(
			        new File(System.getProperty("user.dir")), 
				"splitEvalutorOut.zip");

  /* The name of the key field containing the dataset name */
  public static String DATASET_FIELD_NAME = "Dataset";

  /* The name of the key field containing the run number */
  public static String RUN_FIELD_NAME = "Run";

  /* The name of the result field containing the timestamp */
  public static String TIMESTAMP_FIELD_NAME = "Date_time";

  /**
   * Returns a string describing this result producer
   * @return a description of the result producer suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Performs a random train and test using a supplied "
      +"evaluator.";
  }

  /**
   * Sets the dataset that results will be obtained for.
   *
   * @param instances a value of type 'Instances'.
   */
  public void setInstances(Instances instances) {
    
    m_Instances = instances;
  }

  /**
   * Set a list of method names for additional measures to look for
   * in SplitEvaluators.
   * @param additionalMeasures an array of measure names, null if none
   */
  public void setAdditionalMeasures(String [] additionalMeasures) {
    m_AdditionalMeasures = additionalMeasures;

    if (m_SplitEvaluator != null) {
      System.err.println("RandomSplitResultProducer: setting additional "
			 +"measures for "
			 +"split evaluator");
      m_SplitEvaluator.setAdditionalMeasures(m_AdditionalMeasures);
    }
  }

  /**
   * Sets the object to send results of each run to.
   *
   * @param listener a value of type 'ResultListener'
   */
  public void setResultListener(ResultListener listener) {

    m_ResultListener = listener;
  }

  /**
   * Gets a Double representing the current date and time.
   * eg: 1:46pm on 20/5/1999 -> 19990520.1346
   *
   * @return a value of type Double
   */
  public static Double getTimestamp() {

    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    double timestamp = now.get(Calendar.YEAR) * 10000
      + (now.get(Calendar.MONTH) + 1) * 100
      + now.get(Calendar.DAY_OF_MONTH)
      + now.get(Calendar.HOUR_OF_DAY) / 100.0
      + now.get(Calendar.MINUTE) / 10000.0;
    return new Double(timestamp);
  }

  /**
   * Prepare to generate results.
   *
   * @exception Exception if an error occurs during preprocessing.
   */
  public void preProcess() throws Exception {

    if (m_SplitEvaluator == null) {
      throw new Exception("No SplitEvalutor set");
    }
    if (m_ResultListener == null) {
      throw new Exception("No ResultListener set");
    }
    m_ResultListener.preProcess(this);
  }
  
  /**
   * Perform any postprocessing. When this method is called, it indicates
   * that no more requests to generate results for the current experiment
   * will be sent.
   *
   * @exception Exception if an error occurs
   */
  public void postProcess() throws Exception {

    m_ResultListener.postProcess(this);
    if (m_debugOutput) {
      if (m_ZipDest != null) {
	m_ZipDest.finished();
	m_ZipDest = null;
      }
    }
  }

  /**
   * Gets the keys for a specified run number. Different run
   * numbers correspond to different randomizations of the data. Keys
   * produced should be sent to the current ResultListener
   *
   * @param run the run number to get keys for.
   * @exception Exception if a problem occurs while getting the keys
   */
  public void doRunKeys(int run) throws Exception {
    if (m_Instances == null) {
      throw new Exception("No Instances set");
    }
    // Add in some fields to the key like run number, dataset name
    Object [] seKey = m_SplitEvaluator.getKey();
    Object [] key = new Object [seKey.length + 2];
    key[0] = m_Instances.relationName();
    key[1] = "" + run;
    System.arraycopy(seKey, 0, key, 2, seKey.length);
    if (m_ResultListener.isResultRequired(this, key)) {
      try {
	m_ResultListener.acceptResult(this, key, null);
      } catch (Exception ex) {
	// Save the train and test datasets for debugging purposes?
	throw ex;
      }
    }
  }

  /**
   * Gets the results for a specified run number. Different run
   * numbers correspond to different randomizations of the data. Results
   * produced should be sent to the current ResultListener
   *
   * @param run the run number to get results for.
   * @exception Exception if a problem occurs while getting the results
   */
  public void doRun(int run) throws Exception {

    if (getRawOutput()) {
      if (m_ZipDest == null) {
	m_ZipDest = new OutputZipper(m_OutputFile);
      }
    }

    if (m_Instances == null) {
      throw new Exception("No Instances set");
    }
    // Add in some fields to the key like run number, dataset name
    Object [] seKey = m_SplitEvaluator.getKey();
    Object [] key = new Object [seKey.length + 2];
    key[0] = m_Instances.relationName();
    key[1] = "" + run;
    System.arraycopy(seKey, 0, key, 2, seKey.length);
    if (m_ResultListener.isResultRequired(this, key)) {
      // Randomize on a copy of the original dataset
      Instances runInstances = new Instances(m_Instances);
      runInstances.randomize(new Random(run));

      int trainSize = runInstances.numInstances() * m_TrainPercent / 100;
      int testSize = runInstances.numInstances() - trainSize;
      Instances train = new Instances(runInstances, 0, trainSize);
      Instances test = new Instances(runInstances, trainSize, testSize);
      try {
	Object [] seResults = m_SplitEvaluator.getResult(train, test);
	Object [] results = new Object [seResults.length + 1];
	results[0] = getTimestamp();
	System.arraycopy(seResults, 0, results, 1,
			 seResults.length);
	if (m_debugOutput) {
	  String resultName = (""+run+"."+runInstances.relationName()
			       +"."
			       +m_SplitEvaluator.toString()).replace(' ','_');
	  m_ZipDest.zipit(m_SplitEvaluator.getRawResultOutput(), resultName);
	}
	m_ResultListener.acceptResult(this, key, results);
      } catch (Exception ex) {
	// Save the train and test datasets for debugging purposes?
	throw ex;
      }
    }
  }

  /**
   * Gets the names of each of the columns produced for a single run.
   * This method should really be static.
   *
   * @return an array containing the name of each column
   */
  public String [] getKeyNames() {

    String [] keyNames = m_SplitEvaluator.getKeyNames();
    // Add in the names of our extra key fields
    String [] newKeyNames = new String [keyNames.length + 2];
    newKeyNames[0] = DATASET_FIELD_NAME;
    newKeyNames[1] = RUN_FIELD_NAME;
    System.arraycopy(keyNames, 0, newKeyNames, 2, keyNames.length);
    return newKeyNames;
  }

  /**
   * Gets the data types of each of the columns produced for a single run.
   * This method should really be static.
   *
   * @return an array containing objects of the type of each column. The 
   * objects should be Strings, or Doubles.
   */
  public Object [] getKeyTypes() {

    Object [] keyTypes = m_SplitEvaluator.getKeyTypes();
    // Add in the types of our extra fields
    Object [] newKeyTypes = new String [keyTypes.length + 2];
    newKeyTypes[0] = new String();
    newKeyTypes[1] = new String();
    System.arraycopy(keyTypes, 0, newKeyTypes, 2, keyTypes.length);
    return newKeyTypes;
  }

  /**
   * Gets the names of each of the columns produced for a single run.
   * This method should really be static.
   *
   * @return an array containing the name of each column
   */
  public String [] getResultNames() {

    String [] resultNames = m_SplitEvaluator.getResultNames();
    // Add in the names of our extra Result fields
    String [] newResultNames = new String [resultNames.length + 1];
    newResultNames[0] = TIMESTAMP_FIELD_NAME;
    System.arraycopy(resultNames, 0, newResultNames, 1, resultNames.length);
    return newResultNames;
  }

  /**
   * Gets the data types of each of the columns produced for a single run.
   * This method should really be static.
   *
   * @return an array containing objects of the type of each column. The 
   * objects should be Strings, or Doubles.
   */
  public Object [] getResultTypes() {

    Object [] resultTypes = m_SplitEvaluator.getResultTypes();
    // Add in the types of our extra Result fields
    Object [] newResultTypes = new Object [resultTypes.length + 1];
    newResultTypes[0] = new Double(0);
    System.arraycopy(resultTypes, 0, newResultTypes, 1, resultTypes.length);
    return newResultTypes;
  }

  /**
   * Gets a description of the internal settings of the result
   * producer, sufficient for distinguishing a ResultProducer
   * instance from another with different settings (ignoring
   * those settings set through this interface). For example,
   * a cross-validation ResultProducer may have a setting for the
   * number of folds. For a given state, the results produced should
   * be compatible. Typically if a ResultProducer is an OptionHandler,
   * this string will represent the command line arguments required
   * to set the ResultProducer to that state.
   *
   * @return the description of the ResultProducer state, or null
   * if no state is defined
   */
  public String getCompatibilityState() {

    String result = "-P " + m_TrainPercent + " " ;
    if (m_SplitEvaluator == null) {
      result += "<null SplitEvaluator>";
    } else {
      result += "-W " + m_SplitEvaluator.getClass().getName();
    }
    return result + " --";
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String outputFileTipText() {
    return "Set the destination for saving raw output. If the rawOutput "
      +"option is selected, then output from the splitEvaluator for "
      +"individual train-test splits is saved. If the destination is a "
      +"directory, "
      +"then each output is saved to an individual gzip file; if the "
      +"destination is a file, then each output is saved as an entry "
      +"in a zip file.";
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
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String rawOutputTipText() {
    return "Save raw output (useful for debugging). If set, then output is "
      +"sent to the destination specified by outputFile";
  }

  /**
   * Get if raw split evaluator output is to be saved
   * @return true if raw split evalutor output is to be saved
   */
  public boolean getRawOutput() {
    return m_debugOutput;
  }
  
  /**
   * Set to true if raw split evaluator output is to be saved
   * @param d true if output is to be saved
   */
  public void setRawOutput(boolean d) {
    m_debugOutput = d;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String trainPercentTipText() {
    return "Set the percentage of data to use for training.";
  }

  /**
   * Get the value of TrainPercent.
   *
   * @return Value of TrainPercent.
   */
  public int getTrainPercent() {
    
    return m_TrainPercent;
  }
  
  /**
   * Set the value of TrainPercent.
   *
   * @param newTrainPercent Value to assign to TrainPercent.
   */
  public void setTrainPercent(int newTrainPercent) {
    
    m_TrainPercent = newTrainPercent;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String splitEvaluatorTipText() {
    return "The evaluator to apply to the cross validation folds. "
      +"This may be a classifier, regression scheme etc.";
  }

  /**
   * Get the SplitEvaluator.
   *
   * @return the SplitEvaluator.
   */
  public SplitEvaluator getSplitEvaluator() {
    
    return m_SplitEvaluator;
  }
  
  /**
   * Set the SplitEvaluator.
   *
   * @param newSplitEvaluator new SplitEvaluator to use.
   */
  public void setSplitEvaluator(SplitEvaluator newSplitEvaluator) {
    
    m_SplitEvaluator = newSplitEvaluator;
    m_SplitEvaluator.setAdditionalMeasures(m_AdditionalMeasures);
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options
   */
  public Enumeration listOptions() {

    Vector newVector = new Vector(4);

    newVector.addElement(new Option(
	     "\tThe percentage of instances to use for training.\n"
	      +"\t(default 66)", 
	     "P", 1, 
	     "-P <percent>"));

    newVector.addElement(new Option(
	     "Save raw split evaluator output.",
	     "D",0,"-D"));

    newVector.addElement(new Option(
	     "\tThe filename where raw output will be stored.\n"
	     +"\tIf a directory name is specified then then individual\n"
	     +"\toutputs will be gzipped, otherwise all output will be\n"
	     +"\tzipped to the named file. Use in conjuction with -D."
	     +"\t(default splitEvalutorOut.zip)", 
	     "O", 1, 
	     "-O <file/directory name/path>"));

    newVector.addElement(new Option(
	     "\tThe full class name of a SplitEvaluator.\n"
	      +"\teg: weka.experiment.ClassifierSplitEvaluator", 
	     "W", 1, 
	     "-W <class name>"));

    if ((m_SplitEvaluator != null) &&
	(m_SplitEvaluator instanceof OptionHandler)) {
      newVector.addElement(new Option(
	     "",
	     "", 0, "\nOptions specific to split evaluator "
	     + m_SplitEvaluator.getClass().getName() + ":"));
      Enumeration enum = ((OptionHandler)m_SplitEvaluator).listOptions();
      while (enum.hasMoreElements()) {
	newVector.addElement(enum.nextElement());
      }
    }
    return newVector.elements();
  }

  /**
   * Parses a given list of options. Valid options are:<p>
   *
   * -P num <br>
   * The percent of instances used for training. <p>
   *
   * -D <br>
   * Specify that raw split evaluator output is to be saved. <p>
   *
   * -O file/directory name <br>
   * Specify the file or directory to which raw split evaluator output
   * is to be saved. If a directory is specified, then each output string
   * is saved as an individual gzip file. If a file is specified, then
   * each output string is saved as an entry in a zip file. <p>
   *
   * -W classname <br>
   * Specify the full class name of the split evaluator. <p>
   *
   * All option after -- will be passed to the split evaluator.
   *
   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    
    setRawOutput(Utils.getFlag('D', options));

    String fName = Utils.getOption('O', options);
    if (fName.length() != 0) {
      setOutputFile(new File(fName));
    }

    String trainPct = Utils.getOption('P', options);
    if (trainPct.length() != 0) {
      setTrainPercent(Integer.parseInt(trainPct));
    } else {
      setTrainPercent(66);
    }

    String seName = Utils.getOption('W', options);
    if (seName.length() == 0) {
      throw new Exception("A SplitEvaluator must be specified with"
			  + " the -W option.");
    }
    // Do it first without options, so if an exception is thrown during
    // the option setting, listOptions will contain options for the actual
    // SE.
    setSplitEvaluator((SplitEvaluator)Utils.forName(
		      SplitEvaluator.class,
		      seName,
		      null));
    if (getSplitEvaluator() instanceof OptionHandler) {
      ((OptionHandler) getSplitEvaluator())
	.setOptions(Utils.partitionOptions(options));
    }
  }

  /**
   * Gets the current settings of the result producer.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String [] getOptions() {

    String [] seOptions = new String [0];
    if ((m_SplitEvaluator != null) && 
	(m_SplitEvaluator instanceof OptionHandler)) {
      seOptions = ((OptionHandler)m_SplitEvaluator).getOptions();
    }
    
    String [] options = new String [seOptions.length + 8];
    int current = 0;

    options[current++] = "-P"; options[current++] = "" + getTrainPercent();

        if (getRawOutput()) {
      options[current++] = "-D";
    }

    options[current++] = "-O"; 
    options[current++] = getOutputFile().getName();

    if (getSplitEvaluator() != null) {
      options[current++] = "-W";
      options[current++] = getSplitEvaluator().getClass().getName();
    }
    options[current++] = "--";

    System.arraycopy(seOptions, 0, options, current, 
		     seOptions.length);
    current += seOptions.length;
    while (current < options.length) {
      options[current++] = "";
    }
    return options;
  }

  /**
   * Gets a text descrption of the result producer.
   *
   * @return a text description of the result producer.
   */
  public String toString() {

    String result = "RandomSplitResultProducer: ";
    result += getCompatibilityState();
    if (m_Instances == null) {
      result += ": <null Instances>";
    } else {
      result += ": " + m_Instances.relationName();
    }
    return result;
  }

} // RandomSplitResultProducer




