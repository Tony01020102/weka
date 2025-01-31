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
 *    AbstractLoader.java
 *    Copyright (C) 2002 Richard Kirkby
 *
 */

package weka.core.converters;

import weka.core.Instances;
import weka.core.Instance;
import java.io.*;

/**
 * Abstract class gives default implementation of setSource 
 * methods. All other methods must be overridden.
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 1.4 $
 */
public abstract class AbstractLoader implements Loader {
  
  /** The retrieval modes */
  protected static final int NONE = 0;
  protected static final int BATCH = 1;
  protected static final int INCREMENTAL = 2;

  /** The current retrieval mode */
  protected int m_retrieval;

  /**
   * Sets the retrieval mode.
   *
   * @param mode the retrieval mode
   */
  protected void setRetrieval(int mode) {

    m_retrieval = mode;
  }

  /**
   * Gets the retrieval mode.
   *
   * @return the retrieval mode
   */
  protected int getRetrieval() {

    return m_retrieval;
  }

  /**
   * Default implementation throws an IOException.
   *
   * @param file the File
   * @exception IOException always
   */
  public void setSource(File file) throws IOException {

    throw new IOException("Setting File as source not supported");
  }
  
  /**
   * Default implementation throws an IOException.
   *
   * @param input the input stream
   * @exception IOException always
   */
  public void setSource(InputStream input) throws IOException {

    throw new IOException("Setting InputStream as source not supported");
  }
  
  /*
   * To be overridden.
   */
  public abstract Instances getStructure() throws IOException;

  /*
   * To be overridden.
   */
  public abstract Instances getDataSet() throws IOException;

  /*
   * To be overridden.
   */
  public abstract Instance getNextInstance() throws IOException;
}
