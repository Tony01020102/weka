/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    CSVLoader.java
 *    Copyright (C) 2000-2012 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;

/**
 <!-- globalinfo-start -->
 * Reads a source that is in comma separated format (the default). One can also change the column separator from comma to tab or another character. Assumes that the first row in the file determines the number of and names of the attributes.
 * <p/>
 <!-- globalinfo-end -->
 * 
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -H
 *  No header row present in the data.</pre>
 * 
 * <pre> -N &lt;range&gt;
 *  The range of attributes to force type to be NOMINAL.
 *  'first' and 'last' are accepted as well.
 *  Examples: "first-last", "1,4,5-27,50-last"
 *  (default: -none-)</pre>
 * 
 * <pre> -S &lt;range&gt;
 *  The range of attribute to force type to be STRING.
 *  'first' and 'last' are accepted as well.
 *  Examples: "first-last", "1,4,5-27,50-last"
 *  (default: -none-)</pre>
 * 
 * <pre> -D &lt;range&gt;
 *  The range of attribute to force type to be DATE.
 *  'first' and 'last' are accepted as well.
 *  Examples: "first-last", "1,4,5-27,50-last"
 *  (default: -none-)</pre>
 * 
 * <pre> -format &lt;date format&gt;
 *  The date formatting string to use to parse date values.
 *  (default: "yyyy-MM-dd'T'HH:mm:ss")</pre>
 * 
 * <pre> -M &lt;str&gt;
 *  The string representing a missing value.
 *  (default: ?)</pre>
 * 
 * <pre> -F &lt;separator&gt;
 *  The field separator to be used.
 *  '\t' can be used as well.
 *  (default: ',')</pre>
 * 
 * <pre> -E &lt;enclosures&gt;
 *  The enclosure character(s) to use for strings.
 *  Specify as a comma separated list (e.g. ",' (default: '"')</pre>
 * 
 <!-- options-end -->
 * 
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @version $Revision$
 * @see Loader
 */
public class CSVLoader extends AbstractFileLoader implements BatchConverter,
    OptionHandler {

  /** for serialization. */
  static final long serialVersionUID = 5607529739745491340L;

  /** the file extension. */
  public static String FILE_EXTENSION = ".csv";

  /**
   * A list of hash tables for accumulating nominal values during parsing.
   */
  protected ArrayList<Hashtable<Object, Integer>> m_cumulativeStructure;

  /**
   * Holds instances accumulated so far.
   */
  protected ArrayList<ArrayList<Object>> m_cumulativeInstances;

  /** The reader for the data. */
  protected transient BufferedReader m_sourceReader;

  /** Tokenizer for the data. */
  protected transient StreamTokenizer m_st;

  /** The range of attributes to force to type nominal. */
  protected Range m_NominalAttributes = new Range();

  /** The range of attributes to force to type string. */
  protected Range m_StringAttributes = new Range();

  /** The range of attributes to force to type date */
  protected Range m_dateAttributes = new Range();

  /** The formatting string to use to parse dates */
  protected String m_dateFormat = "";

  /** The formatter to use on dates */
  protected SimpleDateFormat m_formatter;

  /** The placeholder for missing values. */
  protected String m_MissingValue = "?";

  /** the field separator. */
  protected String m_FieldSeparator = ",";

  /** whether the first row has been read. */
  protected boolean m_FirstCheck;

  /** whether the csv file contains a header row with att names */
  protected boolean m_noHeaderRow = false;

  /** enclosure character(s) to use for strings */
  protected String m_Enclosures = "\"";

  /**
   * holds the first row that we read when the data does not have a header row.
   * This row gets used to determine how many attributes the data has in
   * getStructure(), but we still need to process its values as the first
   * instance
   */
  protected ArrayList<Object> m_firstRow;

  /**
   * default constructor.
   */
  public CSVLoader() {
    // No instances retrieved yet
    setRetrieval(NONE);
  }

  /**
   * Get the file extension used for arff files.
   * 
   * @return the file extension
   */
  @Override
  public String getFileExtension() {
    return FILE_EXTENSION;
  }

  /**
   * Returns a description of the file type.
   * 
   * @return a short file description
   */
  @Override
  public String getFileDescription() {
    return "CSV data files";
  }

  /**
   * Gets all the file extensions used for this type of file.
   * 
   * @return the file extensions
   */
  @Override
  public String[] getFileExtensions() {
    return new String[] { getFileExtension() };
  }

  /**
   * Returns a string describing this attribute evaluator.
   * 
   * @return a description of the evaluator suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String globalInfo() {
    return "Reads a source that is in comma separated format (the default). "
        + "One can also change the column separator from comma to tab or "
        + "another character. "
        + "Assumes that the first row in the file determines the number of "
        + "and names of the attributes.";
  }

  /**
   * Returns an enumeration describing the available options.
   * 
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration listOptions() {
    Vector<Option> result = new Vector<Option>();

    result
        .add(new Option("\tNo header row present in the data.", "H", 0, "-H"));
    result.add(new Option(
        "\tThe range of attributes to force type to be NOMINAL.\n"
            + "\t'first' and 'last' are accepted as well.\n"
            + "\tExamples: \"first-last\", \"1,4,5-27,50-last\"\n"
            + "\t(default: -none-)", "N", 1, "-N <range>"));

    result.add(new Option(
        "\tThe range of attribute to force type to be STRING.\n"
            + "\t'first' and 'last' are accepted as well.\n"
            + "\tExamples: \"first-last\", \"1,4,5-27,50-last\"\n"
            + "\t(default: -none-)", "S", 1, "-S <range>"));

    result.add(new Option(
        "\tThe range of attribute to force type to be DATE.\n"
            + "\t'first' and 'last' are accepted as well.\n"
            + "\tExamples: \"first-last\", \"1,4,5-27,50-last\"\n"
            + "\t(default: -none-)", "D", 1, "-D <range>"));

    result.add(new Option(
        "\tThe date formatting string to use to parse date values.\n"
            + "\t(default: \"yyyy-MM-dd'T'HH:mm:ss\")", "format", 1,
        "-format <date format>"));

    result.add(new Option("\tThe string representing a missing value.\n"
        + "\t(default: ?)", "M", 1, "-M <str>"));

    result.addElement(new Option("\tThe field separator to be used.\n"
        + "\t'\\t' can be used as well.\n" + "\t(default: ',')", "F", 1,
        "-F <separator>"));

    result.addElement(new Option(
        "\tThe enclosure character(s) to use for strings.\n"
            + "\tSpecify as a comma separated list (e.g. \",'"
            + "\t(default: '\"')", "E", 1, "-E <enclosures>"));

    return result.elements();
  }

  /**
   * Parses a given list of options.
   * <p/>
   * 
   * <!-- options-start -->
   * * Valid options are: <p/>
   * * 
   * * <pre> -H
   * *  No header row present in the data.</pre>
   * * 
   * * <pre> -N &lt;range&gt;
   * *  The range of attributes to force type to be NOMINAL.
   * *  'first' and 'last' are accepted as well.
   * *  Examples: "first-last", "1,4,5-27,50-last"
   * *  (default: -none-)</pre>
   * * 
   * * <pre> -S &lt;range&gt;
   * *  The range of attribute to force type to be STRING.
   * *  'first' and 'last' are accepted as well.
   * *  Examples: "first-last", "1,4,5-27,50-last"
   * *  (default: -none-)</pre>
   * * 
   * * <pre> -D &lt;range&gt;
   * *  The range of attribute to force type to be DATE.
   * *  'first' and 'last' are accepted as well.
   * *  Examples: "first-last", "1,4,5-27,50-last"
   * *  (default: -none-)</pre>
   * * 
   * * <pre> -format &lt;date format&gt;
   * *  The date formatting string to use to parse date values.
   * *  (default: "yyyy-MM-dd'T'HH:mm:ss")</pre>
   * * 
   * * <pre> -M &lt;str&gt;
   * *  The string representing a missing value.
   * *  (default: ?)</pre>
   * * 
   * * <pre> -F &lt;separator&gt;
   * *  The field separator to be used.
   * *  '\t' can be used as well.
   * *  (default: ',')</pre>
   * * 
   * * <pre> -E &lt;enclosures&gt;
   * *  The enclosure character(s) to use for strings.
   * *  Specify as a comma separated list (e.g. ",' (default: '"')</pre>
   * * 
   * <!-- options-end -->
   * 
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  @Override
  public void setOptions(String[] options) throws Exception {
    String tmpStr;

    setNoHeaderRowPresent(Utils.getFlag('H', options));

    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0)
      setNominalAttributes(tmpStr);
    else
      setNominalAttributes("");

    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0)
      setStringAttributes(tmpStr);
    else
      setStringAttributes("");

    tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0)
      setMissingValue(tmpStr);
    else
      setMissingValue("?");

    tmpStr = Utils.getOption('F', options);
    if (tmpStr.length() != 0)
      setFieldSeparator(tmpStr);
    else
      setFieldSeparator(",");

    tmpStr = Utils.getOption('D', options);
    if (tmpStr.length() > 0) {
      setDateAttributes(tmpStr);
    }
    tmpStr = Utils.getOption("format", options);
    if (tmpStr.length() > 0) {
      setDateFormat(tmpStr);
    }
    tmpStr = Utils.getOption("E", options);
    if (tmpStr.length() > 0) {
      setEnclosureCharacters(tmpStr);
    }
  }

  /**
   * Gets the current settings of the Loader.
   * 
   * @return an array of strings suitable for passing to setOptions
   */
  @Override
  public String[] getOptions() {
    Vector<String> result;

    result = new Vector<String>();

    if (getNoHeaderRowPresent()) {
      result.add("-H");
    }

    if (getNominalAttributes().length() > 0) {
      result.add("-N");
      result.add(getNominalAttributes());
    }

    if (getStringAttributes().length() > 0) {
      result.add("-S");
      result.add(getStringAttributes());
    }

    if (getDateAttributes().length() > 0) {
      result.add("-D");
      result.add(getDateAttributes());
      result.add("-format");
      result.add(getDateFormat());
    }

    result.add("-M");
    result.add(getMissingValue());

    result.add("-F");
    result.add(getFieldSeparator());

    result.add("-E");
    result.add(getEnclosureCharacters());

    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String noHeaderRowPresentTipText() {
    return "First row of data does not contain attribute names";
  }

  /**
   * Set whether there is no header row in the data.
   * 
   * @param b true if there is no header row in the data
   */
  public void setNoHeaderRowPresent(boolean b) {
    m_noHeaderRow = b;
  }

  /**
   * Get whether there is no header row in the data.
   * 
   * @return true if there is no header row in the data
   */
  public boolean getNoHeaderRowPresent() {
    return m_noHeaderRow;
  }

  /**
   * Sets the attribute range to be forced to type nominal.
   * 
   * @param value the range
   */
  public void setNominalAttributes(String value) {
    m_NominalAttributes.setRanges(value);
  }

  /**
   * Returns the current attribute range to be forced to type nominal.
   * 
   * @return the range
   */
  public String getNominalAttributes() {
    return m_NominalAttributes.getRanges();
  }

  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String nominalAttributesTipText() {
    return "The range of attributes to force to be of type NOMINAL, example "
        + "ranges: 'first-last', '1,4,7-14,50-last'.";
  }

  /**
   * Sets the attribute range to be forced to type string.
   * 
   * @param value the range
   */
  public void setStringAttributes(String value) {
    m_StringAttributes.setRanges(value);
  }

  /**
   * Returns the current attribute range to be forced to type string.
   * 
   * @return the range
   */
  public String getStringAttributes() {
    return m_StringAttributes.getRanges();
  }

  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String stringAttributesTipText() {
    return "The range of attributes to force to be of type STRING, example "
        + "ranges: 'first-last', '1,4,7-14,50-last'.";
  }

  /**
   * Set the attribute range to be forced to type date.
   * 
   * @param value the range
   */
  public void setDateAttributes(String value) {
    m_dateAttributes.setRanges(value);
  }

  /**
   * Returns the current attribute range to be forced to type date.
   * 
   * @return the range.
   */
  public String getDateAttributes() {
    return m_dateAttributes.getRanges();
  }

  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String dateAttributesTipText() {
    return "The range of attributes to force to type STRING, example "
        + "ranges: 'first-last', '1,4,7-14, 50-last'.";
  }

  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String enclosureCharactersTipText() {
    return "The characters to use as enclosures for strings. E.g. \",'";
  }

  /**
   * Set the character(s) to use/recognize as string enclosures
   * 
   * @param enclosure the characters to use as string enclosures
   */
  public void setEnclosureCharacters(String enclosure) {
    m_Enclosures = enclosure;
  }

  /**
   * Get the character(s) to use/recognize as string enclosures
   * 
   * @return the characters to use as string enclosures
   */
  public String getEnclosureCharacters() {
    return m_Enclosures;
  }

  /**
   * Set the format to use for parsing date values.
   * 
   * @param value the format to use.
   */
  public void setDateFormat(String value) {
    m_dateFormat = value;
    m_formatter = null;
  }

  /**
   * Get the format to use for parsing date values.
   * 
   * @return the format to use for parsing date values.
   * 
   */
  public String getDateFormat() {
    return m_dateFormat;
  }

  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String dateFormatTipText() {
    return "The format to use for parsing date values.";
  }

  /**
   * Sets the placeholder for missing values.
   * 
   * @param value the placeholder
   */
  public void setMissingValue(String value) {
    m_MissingValue = value;
  }

  /**
   * Returns the current placeholder for missing values.
   * 
   * @return the placeholder
   */
  public String getMissingValue() {
    return m_MissingValue;
  }

  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String missingValueTipText() {
    return "The placeholder for missing values, default is '?'.";
  }

  /**
   * Sets the character used as column separator.
   * 
   * @param value the character to use
   */
  public void setFieldSeparator(String value) {
    m_FieldSeparator = Utils.unbackQuoteChars(value);
    if (m_FieldSeparator.length() != 1) {
      m_FieldSeparator = ",";
      System.err
          .println("Field separator can only be a single character (exception being '\t'), "
              + "defaulting back to '" + m_FieldSeparator + "'!");
    }
  }

  /**
   * Returns the character used as column separator.
   * 
   * @return the character to use
   */
  public String getFieldSeparator() {
    return Utils.backQuoteChars(m_FieldSeparator);
  }

  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String fieldSeparatorTipText() {
    return "The character to use as separator for the columns/fields (use '\\t' for TAB).";
  }

  /**
   * Resets the Loader object and sets the source of the data set to be the
   * supplied Stream object.
   * 
   * @param input the input stream
   * @exception IOException if an error occurs
   */
  @Override
  public void setSource(InputStream input) throws IOException {
    m_structure = null;
    m_sourceFile = null;
    m_File = null;
    m_FirstCheck = true;

    m_sourceReader = new BufferedReader(new InputStreamReader(input));
  }

  /**
   * Resets the Loader object and sets the source of the data set to be the
   * supplied File object.
   * 
   * @param file the source file.
   * @exception IOException if an error occurs
   */
  @Override
  public void setSource(File file) throws IOException {
    super.setSource(file);
  }

  /**
   * Determines and returns (if possible) the structure (internally the header)
   * of the data set as an empty set of instances.
   * 
   * @return the structure of the data set as an empty set of Instances
   * @exception IOException if an error occurs
   */
  @Override
  public Instances getStructure() throws IOException {
    if ((m_sourceFile == null) && (m_sourceReader == null)) {
      throw new IOException("No source has been specified");
    }

    if (m_structure == null) {
      try {
        m_st = new StreamTokenizer(m_sourceReader);
        initTokenizer(m_st);
        readStructure(m_st);
      } catch (FileNotFoundException ex) {
      }
    }

    return m_structure;
  }

  /**
   * reads the structure.
   * 
   * @param st the stream tokenizer to read from
   * @throws IOException if reading fails
   */
  private void readStructure(StreamTokenizer st) throws IOException {
    readHeader(st);
  }

  /**
   * Return the full data set. If the structure hasn't yet been determined by a
   * call to getStructure then method should do so before processing the rest of
   * the data set.
   * 
   * @return the structure of the data set as an empty set of Instances
   * @exception IOException if there is no source or parsing fails
   */
  @Override
  public Instances getDataSet() throws IOException {
    if ((m_sourceFile == null) && (m_sourceReader == null)) {
      throw new IOException("No source has been specified");
    }

    if (m_structure == null) {
      getStructure();
    }

    if (m_st == null) {
      m_st = new StreamTokenizer(m_sourceReader);
      initTokenizer(m_st);
    }

    m_st.ordinaryChar(m_FieldSeparator.charAt(0));

    m_cumulativeStructure = new ArrayList<Hashtable<Object, Integer>>(
        m_structure.numAttributes());
    for (int i = 0; i < m_structure.numAttributes(); i++) {
      m_cumulativeStructure.add(new Hashtable<Object, Integer>());
    }

    m_cumulativeInstances = new ArrayList<ArrayList<Object>>();
    if (m_noHeaderRow && m_firstRow != null) {
      // add the first row that was read in readHeader() in order
      // to determine how many attributes the data has
      m_cumulativeInstances.add(m_firstRow);
    }
    ArrayList<Object> current;
    while ((current = getInstance(m_st)) != null) {
      m_cumulativeInstances.add(current);
    }

    ArrayList<Attribute> atts = new ArrayList<Attribute>(
        m_structure.numAttributes());
    for (int i = 0; i < m_structure.numAttributes(); i++) {
      String attname = m_structure.attribute(i).name();
      Hashtable<Object, Integer> tempHash = m_cumulativeStructure.get(i);
      if (tempHash.size() == 0) {
        if (m_dateAttributes.isInRange(i)) {
          atts.add(new Attribute(attname, m_dateFormat));
        } else {
          atts.add(new Attribute(attname));
        }
      } else {
        if (m_StringAttributes.isInRange(i)) {
          atts.add(new Attribute(attname, (ArrayList<String>) null));
        } else {
          ArrayList<String> values = new ArrayList<String>(tempHash.size());
          // add dummy objects in order to make the ArrayList's size == capacity
          for (int z = 0; z < tempHash.size(); z++) {
            values.add("dummy");
          }
          Enumeration e = tempHash.keys();
          while (e.hasMoreElements()) {
            Object ob = e.nextElement();
            // if (ob instanceof Double) {
            int index = tempHash.get(ob).intValue();
            String s = ob.toString();
            if (s.startsWith("'") || s.startsWith("\""))
              s = s.substring(1, s.length() - 1);
            values.set(index, new String(s));
            // }
          }
          atts.add(new Attribute(attname, values));
        }
      }
    }

    // make the instances
    String relationName;
    if (m_sourceFile != null)
      relationName = (m_sourceFile.getName())
          .replaceAll("\\.[cC][sS][vV]$", "");
    else
      relationName = "stream";
    Instances dataSet = new Instances(relationName, atts,
        m_cumulativeInstances.size());

    for (int i = 0; i < m_cumulativeInstances.size(); i++) {
      current = m_cumulativeInstances.get(i);
      double[] vals = new double[dataSet.numAttributes()];
      for (int j = 0; j < current.size(); j++) {
        Object cval = current.get(j);
        if (cval instanceof String) {
          if (((String) cval).compareTo(m_MissingValue) == 0) {
            vals[j] = Utils.missingValue();
          } else {
            if (dataSet.attribute(j).isString()) {
              vals[j] = dataSet.attribute(j).addStringValue((String) cval);
            } else if (dataSet.attribute(j).isNominal()) {
              // find correct index
              Hashtable<Object, Integer> lookup = m_cumulativeStructure.get(j);
              int index = lookup.get(cval).intValue();
              vals[j] = index;
            } else {
              throw new IllegalStateException(
                  "Wrong attribute type at position " + (i + 1) + "!!!");
            }
          }
        } else if (dataSet.attribute(j).isNominal()) {
          // find correct index
          Hashtable<Object, Integer> lookup = m_cumulativeStructure.get(j);
          int index = lookup.get(cval).intValue();
          vals[j] = index;
        } else if (dataSet.attribute(j).isString()) {
          vals[j] = dataSet.attribute(j).addStringValue("" + cval);
        } else {
          vals[j] = ((Double) cval).doubleValue();
        }
      }
      dataSet.add(new DenseInstance(1.0, vals));
    }
    m_structure = new Instances(dataSet, 0);
    setRetrieval(BATCH);
    m_cumulativeStructure = null; // conserve memory

    // close the stream
    m_sourceReader.close();

    return dataSet;
  }

  /**
   * CSVLoader is unable to process a data set incrementally.
   * 
   * @param structure ignored
   * @return never returns without throwing an exception
   * @exception IOException always. CSVLoader is unable to process a data set
   *              incrementally.
   */
  @Override
  public Instance getNextInstance(Instances structure) throws IOException {
    throw new IOException("CSVLoader can't read data sets incrementally.");
  }

  private ArrayList<Object> getInstance(StreamTokenizer tokenizer)
      throws IOException {
    return getInstance(tokenizer, false);
  }

  /**
   * Attempts to parse a line of the data set.
   * 
   * @param tokenizer the tokenizer
   * @return a ArrayList containg String and Double objects representing the
   *         values of the instance.
   * @exception IOException if an error occurs
   * 
   *              <pre>
   * <jml>
   *    private_normal_behavior
   *      requires: tokenizer != null;
   *      ensures: \result  != null;
   *  also
   *    private_exceptional_behavior
   *      requires: tokenizer == null
   *                || (* unsucessful parse *);
   *      signals: (IOException);
   * </jml>
   * </pre>
   */
  private ArrayList<Object> getInstance(StreamTokenizer tokenizer,
      boolean readingFirstRow) throws IOException {

    ArrayList<Object> current = new ArrayList<Object>();

    // Check if end of file reached.
    ConverterUtils.getFirstToken(tokenizer);
    if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
      return null;
    }
    boolean first = true;
    boolean wasSep;

    while (tokenizer.ttype != StreamTokenizer.TT_EOL
        && tokenizer.ttype != StreamTokenizer.TT_EOF) {

      // Get next token
      if (!first) {
        ConverterUtils.getToken(tokenizer);
      }

      if (tokenizer.ttype == m_FieldSeparator.charAt(0)
          || tokenizer.ttype == StreamTokenizer.TT_EOL) {
        current.add(m_MissingValue);
        wasSep = true;
      } else {
        wasSep = false;
        if (tokenizer.sval.equals(m_MissingValue)) {
          current.add(new String(m_MissingValue));
        } else {
          // try to parse as a number
          try {
            double val = Double.valueOf(tokenizer.sval).doubleValue();
            current.add(new Double(val));
          } catch (NumberFormatException e) {
            // otherwise assume its an enumerated value
            current.add(new String(tokenizer.sval));
          }
        }
      }

      if (!wasSep) {
        ConverterUtils.getToken(tokenizer);
      }
      first = false;
    }

    if (!readingFirstRow) {
      // check number of values read
      if (current.size() != m_structure.numAttributes()) {
        ConverterUtils.errms(tokenizer, "wrong number of values. Read "
            + current.size() + ", expected " + m_structure.numAttributes());
      }

      // check for structure update
      try {
        checkStructure(current);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    return current;
  }

  /**
   * Checks the current instance against what is known about the structure of
   * the data set so far. If there is a nominal value for an attribute that was
   * believed to be numeric then all previously seen values for this attribute
   * are stored in a Hashtable.
   * 
   * @param current a <code>ArrayList</code> value
   * @exception Exception if an error occurs
   * 
   *              <pre>
   * <jml>
   *    private_normal_behavior
   *      requires: current != null;
   *  also
   *    private_exceptional_behavior
   *      requires: current == null
   *                || (* unrecognized object type in current *);
   *      signals: (Exception);
   * </jml>
   * </pre>
   */
  private void checkStructure(ArrayList<Object> current) throws Exception {
    if (current == null) {
      throw new Exception("current shouldn't be null in checkStructure");
    }

    // initialize ranges, if necessary
    if (m_FirstCheck) {
      m_NominalAttributes.setUpper(current.size() - 1);
      m_StringAttributes.setUpper(current.size() - 1);
      m_dateAttributes.setUpper(current.size() - 1);
      m_FirstCheck = false;
    }

    for (int i = 0; i < current.size(); i++) {
      Object ob = current.get(i);
      if ((ob instanceof String) || (m_NominalAttributes.isInRange(i))
          || (m_StringAttributes.isInRange(i)) || m_dateAttributes.isInRange(i)) {
        if (ob.toString().compareTo(m_MissingValue) == 0) {
          // do nothing
        } else {

          boolean notDate = true;
          if (m_dateAttributes.isInRange(i)) {
            // try to parse date string
            if (m_formatter == null) {
              m_formatter = new SimpleDateFormat(m_dateFormat);
            }

            try {
              long time = m_formatter.parse(ob.toString()).getTime();
              Double timeL = new Double(time);
              current.set(i, timeL);
              notDate = false;
            } catch (ParseException e) {
              notDate = true;
            }
          }

          if (notDate) {
            Hashtable<Object, Integer> tempHash = m_cumulativeStructure.get(i);
            if (!tempHash.containsKey(ob)) {
              // may have found a nominal value in what was previously thought
              // to
              // be a numeric variable.
              if (tempHash.size() == 0) {
                for (int j = 0; j < m_cumulativeInstances.size(); j++) {
                  ArrayList tempUpdate = m_cumulativeInstances.get(j);
                  Object tempO = tempUpdate.get(i);
                  if (tempO instanceof String) {
                    // must have been a missing value
                  } else {
                    if (!tempHash.containsKey(tempO)) {
                      tempHash.put(new Double(((Double) tempO).doubleValue()),
                          new Integer(tempHash.size()));
                    }
                  }
                }
              }
              int newIndex = tempHash.size();
              tempHash.put(ob, new Integer(newIndex));
            }
          }
        }
      } else if (ob instanceof Double) {
        Hashtable<Object, Integer> tempHash = m_cumulativeStructure.get(i);
        if (tempHash.size() != 0) {
          if (!tempHash.containsKey(ob)) {
            int newIndex = tempHash.size();
            tempHash.put(new Double(((Double) ob).doubleValue()), new Integer(
                newIndex));
          }
        }
      } else {
        throw new Exception("Wrong object type in checkStructure!");
      }
    }
  }

  /**
   * Assumes the first line of the file contains the attribute names. Assumes
   * all attributes are Strung (Reading the full data set with getDataSet will
   * establish the true structure).
   * 
   * @param tokenizer a <code>StreamTokenizer</code> value
   * @exception IOException if an error occurs
   * 
   *              <pre>
   * <jml>
   *    private_normal_behavior
   *      requires: tokenizer != null;
   *      modifiable: m_structure;
   *      ensures: m_structure != null;
   *  also
   *    private_exceptional_behavior
   *      requires: tokenizer == null
   *                || (* unsucessful parse *);
   *      signals: (IOException);
   * </jml>
   * </pre>
   */
  private void readHeader(StreamTokenizer tokenizer) throws IOException {

    ArrayList<Attribute> attribNames = new ArrayList<Attribute>();
    m_firstRow = null;
    if (m_noHeaderRow) {
      tokenizer.ordinaryChar(m_FieldSeparator.charAt(0));
      ArrayList<Object> firstRow = getInstance(tokenizer, true);
      for (int i = 0; i < firstRow.size(); i++) {
        attribNames.add(new Attribute("att" + (i + 1),
            (java.util.List<String>) null));
      }
      m_firstRow = firstRow;
    } else {
      ConverterUtils.getFirstToken(tokenizer);
      if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
        ConverterUtils.errms(tokenizer, "premature end of file");
      }

      while (tokenizer.ttype != StreamTokenizer.TT_EOL) {
        attribNames.add(new Attribute(tokenizer.sval,
            (java.util.List<String>) null));
        ConverterUtils.getToken(tokenizer);
      }
    }
    String relationName;
    if (m_sourceFile != null)
      relationName = (m_sourceFile.getName())
          .replaceAll("\\.[cC][sS][vV]$", "");
    else
      relationName = "stream";
    m_structure = new Instances(relationName, attribNames, 0);
  }

  /**
   * Initializes the stream tokenizer.
   * 
   * @param tokenizer the tokenizer to initialize
   */
  private void initTokenizer(StreamTokenizer tokenizer) {
    tokenizer.resetSyntax();
    tokenizer.whitespaceChars(0, (' ' - 1));
    tokenizer.wordChars(' ', '\u00FF');
    tokenizer.whitespaceChars(m_FieldSeparator.charAt(0),
        m_FieldSeparator.charAt(0));
    tokenizer.commentChar('%');

    String[] parts = m_Enclosures.split(",");
    for (String e : parts) {
      if (e.length() > 1 || e.length() == 0) {
        throw new IllegalArgumentException(
            "Enclosures can only be single characters");
      }
      tokenizer.quoteChar(e.charAt(0));
    }

    tokenizer.eolIsSignificant(true);
  }

  /**
   * Resets the Loader ready to read a new data set or the same data set again.
   * 
   * @throws IOException if something goes wrong
   */
  @Override
  public void reset() throws IOException {
    m_structure = null;
    m_st = null;
    setRetrieval(NONE);

    if (m_File != null) {
      setFile(new File(m_File));
    }
  }

  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision$");
  }

  /**
   * Main method.
   * 
   * @param args should contain the name of an input file.
   */
  public static void main(String[] args) {
    runFileLoader(new CSVLoader(), args);
  }
}

