/*
 *    TimeSeriesDeltaFilter.java
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

package weka.filters;

import java.io.*;
import java.util.*;
import weka.core.*;

/** 
 * An instance filter that assumes instances form time-series data and
 * replaces attribute values in the current instance with the difference
 * between the current value and the equivalent attribute attribute value
 * of some previous (or future) instance. For instances where the time-shifted
 * value is unknown either the instance may be dropped, or missing values
 * used.<p>
 *
 * Valid filter-specific options are:<p>
 *
 * -R index1,index2-index4,...<br>
 * Specify list of columns to calculate new values for.
 * First and last are valid indexes.
 * (default none)<p>
 *
 * -V <br>
 * Invert matching sense (i.e. calculate for all non-specified columns)<p>
 *
 * -I num <br>
 * The number of instances forward to take value differences between.
 * A negative number indicates taking values from a past instance.
 * (default -1) <p>
 *
 * -M <br>
 * For instances at the beginning or end of the dataset where the delta
 * values are not known, use missing values (default is to remove those
 * instances). <p>
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 1.2 $
 */
public class TimeSeriesDeltaFilter extends TimeSeriesTranslateFilter {

  /**
   * Sets the format of the input instances.
   *
   * @param instanceInfo an Instances object containing the input instance
   * structure (any instances contained in the object are ignored - only the
   * structure is required).
   * @return true if the outputFormat may be collected immediately
   * @exception Exception if the format couldn't be set successfully
   */
  public boolean inputFormat(Instances instanceInfo) throws Exception {

    m_InputFormat = new Instances(instanceInfo, 0);
    m_NewBatch = true;
    resetHistory();
    m_SelectedCols.setUpper(m_InputFormat.numAttributes() - 1);
    // Create the output buffer
    Instances outputFormat = new Instances(instanceInfo, 0); 
    for(int i = 0; i < m_InputFormat.numAttributes(); i++) {
      if (m_SelectedCols.isInRange(i)) {
	if (outputFormat.attribute(i).isNumeric()) {
	  outputFormat.renameAttribute(i, outputFormat.attribute(i).name()
				       + " d"
				       + (m_InstanceRange < 0 ? '-' : '+')
				       + Math.abs(m_InstanceRange));
	} else {
	  throw new Exception("Time delta attributes must be numeric!");
	}
      }
    }
    setOutputFormat(outputFormat);
    return true;
  }
  

  /**
   * Creates a new instance the same as one instance (the "destination")
   * but with some attribute values copied from another instance
   * (the "source")
   *
   * @param source the source instance
   * @param dest the destination instance
   * @return the new merged instance
   * @exception Exception if a problem occurs during merging
   */
  protected Instance mergeInstances(Instance source, Instance dest)
    throws Exception {

    Instances outputFormat = outputFormatPeek();
    double[] vals = new double[outputFormat.numAttributes()];
    for(int i = 0; i < vals.length; i++) {
      if (m_SelectedCols.isInRange(i)) {
	if ((source != null)
	    && !source.isMissing(i)
	    && !dest.isMissing(i)) {
	  vals[i] = dest.value(i) - source.value(i);
	}
      } else {
	vals[i] = dest.value(i);
      }
    }
    if (dest instanceof SparseInstance) {
      return new SparseInstance(dest.weight(), vals);
    } else {
      return new Instance(dest.weight(), vals);
    }
  }
  
  /**
   * Main method for testing this class.
   *
   * @param argv should contain arguments to the filter: use -h for help
   */
  public static void main(String [] argv) {

    try {
      if (Utils.getFlag('b', argv)) {
 	Filter.batchFilterFile(new TimeSeriesDeltaFilter(), argv); 
      } else {
	Filter.filterFile(new TimeSeriesDeltaFilter(), argv);
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }
}








