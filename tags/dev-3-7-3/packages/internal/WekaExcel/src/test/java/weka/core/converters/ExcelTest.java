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
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */

package weka.core.converters;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests ExcelLoader/ExcelSaver. Run from the command line with:<p/>
 * java weka.core.converters.ExcelTest
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExcelTest
  extends AbstractFileConverterTest {

  /**
   * Constructs the <code>ExcelTest</code>.
   *
   * @param name the name of the test class
   */
  public ExcelTest(String name) {
    super(name);
  }

  /**
   * returns the loader used in the tests.
   *
   * @return the configured loader
   */
  public AbstractLoader getLoader() {
    return new ExcelLoader();
  }

  /**
   * returns the saver used in the tests.
   *
   * @return the configured saver
   */
  public AbstractSaver getSaver() {
    return new ExcelSaver();
  }

  /**
   * Called by JUnit before each test method. This implementation creates
   * the default loader/saver to test and generates a test set of Instances.
   *
   * @throws Exception if an error occurs reading the example instances.
   */
  protected void setUp() throws Exception {
    super.setUp();

    m_CompareValuesAsString = true;
  }

  /**
   * returns a test suite.
   *
   * @return the test suite
   */
  public static Test suite() {
    return new TestSuite(ExcelTest.class);
  }

  /**
   * for running the test from commandline.
   *
   * @param args the commandline arguments - ignored
   */
  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }
}

