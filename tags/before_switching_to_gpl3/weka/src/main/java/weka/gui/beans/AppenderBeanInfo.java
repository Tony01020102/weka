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
 *    AppenderBeanInfo.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * Bean info class for the appender bean
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class AppenderBeanInfo extends SimpleBeanInfo {
  
  /**
   * Returns the event set descriptors
   *
   * @return an <code>EventSetDescriptor[]</code> value
   */
  public EventSetDescriptor [] getEventSetDescriptors() {
    try {
      EventSetDescriptor [] esds = 
      { new EventSetDescriptor(DataSource.class, 
                               "dataSet", 
                               DataSourceListener.class, 
                               "acceptDataSet"),
        new EventSetDescriptor(DataSource.class, 
                               "instance", 
                               InstanceListener.class, 
                               "acceptInstance"),
        new EventSetDescriptor(TrainingSetProducer.class, 
                               "trainingSet", 
                               TrainingSetListener.class, 
                               "acceptTrainingSet"),
        new EventSetDescriptor(TestSetProducer.class, 
                               "testSet", 
                               TestSetListener.class, 
                               "acceptTestSet")  };
      return esds;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
}
