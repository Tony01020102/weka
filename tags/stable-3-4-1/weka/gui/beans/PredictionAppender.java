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
 *    PredictionAppender.java
 *    Copyright (C) 2003 Mark Hall
 *
 */

package weka.gui.beans;

import java.io.Serializable;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.*;
import java.beans.EventSetDescriptor;

import weka.core.Instances;
import weka.core.Instance;

/**
 * Bean that can can accept batch or incremental classifier events
 * and produce dataset or instance events which contain instances with
 * predictions appended.
 *
 * @author <a href="mailto:mhall@cs.waikato.ac.nz">Mark Hall</a>
 * @version $Revision: 1.3 $
 */
public class PredictionAppender extends JPanel
  implements DataSource, Visible, BeanCommon,
	     EventConstraints, BatchClassifierListener,
	     IncrementalClassifierListener, Serializable {

  /**
   * Objects listenening for dataset events
   */
  protected Vector m_dataSourceListeners = new Vector();

  /**
   * Objects listening for instances events
   */
  protected Vector m_instanceListeners = new Vector();

  /**
   * Non null if this object is a target for any events.
   */
  protected Object m_listenee = null;

  protected BeanVisual m_visual = 
    new BeanVisual("PredictionAppender", 
		   BeanVisual.ICON_PATH+"PredictionAppender.gif",
		   BeanVisual.ICON_PATH+"PredictionAppender_animated.gif");

  /**
   * Append classifier's predicted probabilities (if the class is discrete
   * and the classifier is a distribution classifier)
   */
  protected boolean m_appendProbabilities;

  protected weka.gui.Logger m_logger;

  /**
   * Global description of this bean
   *
   * @return a <code>String</code> value
   */
  public String globalInfo() {
    return "Accepts batch or incremental classifier events and "
      +"produces a new data set with classifier predictions appended.";
  }

  /**
   * Creates a new <code>PredictionAppender</code> instance.
   */
  public PredictionAppender() {
    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER);
  }

  /**
   * Return a tip text suitable for displaying in a GUI
   *
   * @return a <code>String</code> value
   */
  public String appendPredictedProbabilitiesTipText() {
    return "append probabilities rather than labels for discrete class "
      +"predictions";
  }

  /**
   * Return true if predicted probabilities are to be appended rather
   * than class value
   *
   * @return a <code>boolean</code> value
   */
  public boolean getAppendPredictedProbabilities() {
    return m_appendProbabilities;
  }

  /**
   * Set whether to append predicted probabilities rather than
   * class value (for discrete class data sets)
   *
   * @param ap a <code>boolean</code> value
   */
  public void setAppendPredictedProbabilities(boolean ap) {
    m_appendProbabilities = ap;
  }

  /**
   * Add a datasource listener
   *
   * @param dsl a <code>DataSourceListener</code> value
   */
  public synchronized void addDataSourceListener(DataSourceListener dsl) {
    m_dataSourceListeners.addElement(dsl);
  }
  
  /**
   * Remove a datasource listener
   *
   * @param dsl a <code>DataSourceListener</code> value
   */
  public synchronized void removeDataSourceListener(DataSourceListener dsl) {
    m_dataSourceListeners.remove(dsl);
  }

  /**
   * Add an instance listener
   *
   * @param dsl a <code>InstanceListener</code> value
   */
  public synchronized void addInstanceListener(InstanceListener dsl) {
    m_instanceListeners.addElement(dsl);
  }
  
  /**
   * Remove an instance listener
   *
   * @param dsl a <code>InstanceListener</code> value
   */
  public synchronized void removeInstanceListener(InstanceListener dsl) {
    m_instanceListeners.remove(dsl);
  }

  /**
   * Set the visual for this data source
   *
   * @param newVisual a <code>BeanVisual</code> value
   */
  public void setVisual(BeanVisual newVisual) {
    m_visual = newVisual;
  }

  /**
   * Get the visual being used by this data source.
   *
   */
  public BeanVisual getVisual() {
    return m_visual;
  }

  /**
   * Use the default images for a data source
   *
   */
  public void useDefaultVisual() {
    m_visual.loadIcons(BeanVisual.ICON_PATH+"PredictionAppender.gif",
		       BeanVisual.ICON_PATH+"PredictionAppender_animated.gif");
  }

  protected Instances m_incrementalStructure;
  protected InstanceEvent m_instanceEvent;
  protected double [] m_instanceVals;

  /**
   * Accept and process an incremental classifier event
   *
   * @param e an <code>IncrementalClassifierEvent</code> value
   */
  public void acceptClassifier(IncrementalClassifierEvent e) {
    weka.classifiers.Classifier classifier = e.getClassifier();
    Instance currentI = e.getCurrentInstance();
    int status = e.getStatus();
    int oldNumAtts = currentI.dataset().numAttributes();

    if (status == IncrementalClassifierEvent.NEW_BATCH) {
      m_instanceEvent = new InstanceEvent(this, null, 0);
      // create new header structure
      Instances oldStructure = new Instances(currentI.dataset(), 0);
      String relationNameModifier = oldStructure.relationName()
	+"_with predictions";
       if (!m_appendProbabilities 
	   || oldStructure.classAttribute().isNumeric()) {
	 try {
	   m_incrementalStructure = makeDataSetClass(oldStructure, classifier,
						     relationNameModifier);
	   m_instanceVals = new double [m_incrementalStructure.numAttributes()];
	 } catch (Exception ex) {
	   ex.printStackTrace();
	   return;
	 }
       } else if (m_appendProbabilities) {
	 try {
	   m_incrementalStructure = 
	     makeDataSetProbabilities(oldStructure, classifier,
				      relationNameModifier);
	   m_instanceVals = new double [m_incrementalStructure.numAttributes()];
	 } catch (Exception ex) {
	   ex.printStackTrace();
	   return;
	 }
       }
    }

    Instance newInst;
    try {
      // process the actual instance
      for (int i = 0; i < oldNumAtts; i++) {
	m_instanceVals[i] = currentI.value(i);
      }
      if (!m_appendProbabilities 
	  || currentI.dataset().classAttribute().isNumeric()) {
	double predClass = 
	  classifier.classifyInstance(currentI);
	m_instanceVals[m_instanceVals.length - 1] = predClass;
      } else if (m_appendProbabilities) {
	double [] preds = classifier.distributionForInstance(currentI);
	for (int i = oldNumAtts; i < m_instanceVals.length; i++) {
	  m_instanceVals[i] = preds[i-oldNumAtts];
	}      
      }      
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    } finally {
      newInst = new Instance(currentI.weight(), m_instanceVals);
      newInst.setDataset(m_incrementalStructure);
      m_instanceEvent.setInstance(newInst);
      m_instanceEvent.setStatus(status);
      // notify listeners
      notifyInstanceAvailable(m_instanceEvent);
    }

    if (status == IncrementalClassifierEvent.BATCH_FINISHED) {
      // clean up
      m_incrementalStructure = null;
      m_instanceVals = null;
      m_instanceEvent = null;
    }
  }

  /**
   * Accept and process a batch classifier event
   *
   * @param e a <code>BatchClassifierEvent</code> value
   */
  public void acceptClassifier(BatchClassifierEvent e) {
    if (m_dataSourceListeners.size() > 0) {
      Instances testSet = e.getTestSet();
      weka.classifiers.Classifier classifier = e.getClassifier();
      String relationNameModifier = "_set_"+e.getSetNumber()+"_of_"
	+e.getMaxSetNumber();
      
      if (!m_appendProbabilities || testSet.classAttribute().isNumeric()) {
	try {
	  Instances newInstances = makeDataSetClass(testSet, classifier,
						    relationNameModifier);
	  // fill in predicted values
	  for (int i = 0; i < testSet.numInstances(); i++) {
	    double predClass = 
	      classifier.classifyInstance(testSet.instance(i));
	    newInstances.instance(i).setValue(newInstances.numAttributes()-1,
					      predClass);
	  }
	  // notify listeners
	  notifyDataSetAvailable(new DataSetEvent(this, newInstances));
	  return;
	} catch (Exception ex) {
	  ex.printStackTrace();
	}
      }
      if (m_appendProbabilities) {
	try {
	  Instances newInstances = 
	    makeDataSetProbabilities(testSet,
				     classifier,relationNameModifier);
	  // fill in predicted probabilities
	  for (int i = 0; i < testSet.numInstances(); i++) {
	    double [] preds = classifier.
	      distributionForInstance(testSet.instance(i));
	    for (int j = 0; j < testSet.classAttribute().numValues(); j++) {
	      newInstances.instance(i).setValue(testSet.numAttributes()+j,
						preds[j]);
	    }
	  }
	  // notify listeners
	  notifyDataSetAvailable(new DataSetEvent(this, newInstances));
	} catch (Exception ex) {
	  ex.printStackTrace();
	}
      }
    }
  }

  private Instances 
    makeDataSetProbabilities(Instances format,
			     weka.classifiers.Classifier classifier,
			     String relationNameModifier) 
  throws Exception {
    int numOrigAtts = format.numAttributes();
    Instances newInstances = new Instances(format);
    for (int i = 0; i < format.classAttribute().numValues(); i++) {
      weka.filters.unsupervised.attribute.Add addF = new
	weka.filters.unsupervised.attribute.Add();
      addF.setAttributeIndex("last");
      addF.setAttributeName("prob_"+format.classAttribute().value(i));
      addF.setInputFormat(newInstances);
      newInstances = weka.filters.Filter.useFilter(newInstances, addF);
    }
    newInstances.setRelationName(format.relationName()+relationNameModifier);
    return newInstances;
  }

  private Instances makeDataSetClass(Instances format,
				     weka.classifiers.Classifier classifier,
				     String relationNameModifier) 
  throws Exception {
    
    weka.filters.unsupervised.attribute.Add addF = new
      weka.filters.unsupervised.attribute.Add();
    addF.setAttributeIndex("last");
    String classifierName = classifier.getClass().getName();
    classifierName = classifierName.
      substring(classifierName.lastIndexOf('.')+1, classifierName.length());
    addF.setAttributeName("class_predicted_by: "+classifierName);
    if (format.classAttribute().isNominal()) {
      String classLabels = "";
      Enumeration enum = format.classAttribute().enumerateValues();
      classLabels += (String)enum.nextElement();
      while (enum.hasMoreElements()) {
	classLabels += ","+(String)enum.nextElement();
      }
      addF.setNominalLabels(classLabels);
    }
    addF.setInputFormat(format);


    Instances newInstances = 
      weka.filters.Filter.useFilter(format, addF);
    newInstances.setRelationName(format.relationName()+relationNameModifier);
    return newInstances;
  }

  /**
   * Notify all instance listeners that an instance is available
   *
   * @param e an <code>InstanceEvent</code> value
   */
  protected void notifyInstanceAvailable(InstanceEvent e) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_instanceListeners.clone();
    }
    
    if (l.size() > 0) {
      for(int i = 0; i < l.size(); i++) {
	((InstanceListener)l.elementAt(i)).acceptInstance(e);
      }
    }
  }

  /**
   * Notify all Data source listeners that a data set is available
   *
   * @param e a <code>DataSetEvent</code> value
   */
  protected void notifyDataSetAvailable(DataSetEvent e) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_dataSourceListeners.clone();
    }
    
    if (l.size() > 0) {
      for(int i = 0; i < l.size(); i++) {
	((DataSourceListener)l.elementAt(i)).acceptDataSet(e);
      }
    }
  }

  /**
   * Set a logger
   *
   * @param logger a <code>weka.gui.Logger</code> value
   */
  public void setLog(weka.gui.Logger logger) {
    m_logger = logger;
  }

  public void stop() {
    // cant really do anything meaningful here
  }

  /**
   * Returns true if, at this time, 
   * the object will accept a connection according to the supplied
   * event name
   *
   * @param eventName the event
   * @return true if the object will accept a connection
   */
  public boolean connectionAllowed(String eventName) {
    return (m_listenee == null);
  }

  /**
   * Notify this object that it has been registered as a listener with
   * a source with respect to the supplied event name
   *
   * @param eventName
   * @param source the source with which this object has been registered as
   * a listener
   */
  public synchronized void connectionNotification(String eventName,
						  Object source) {
    if (connectionAllowed(eventName)) {
      m_listenee = source;
    }
  }

  /**
   * Notify this object that it has been deregistered as a listener with
   * a source with respect to the supplied event name
   *
   * @param eventName the event name
   * @param source the source with which this object has been registered as
   * a listener
   */
  public synchronized void disconnectionNotification(String eventName,
						     Object source) {
    if (m_listenee == source) {
      m_listenee = null;
    }
  }

  /**
   * Returns true, if at the current time, the named event could
   * be generated. Assumes that supplied event names are names of
   * events that could be generated by this bean.
   *
   * @param eventName the name of the event in question
   * @return true if the named event could be generated at this point in
   * time
   */
  public boolean eventGeneratable(String eventName) {
    if (m_listenee == null) {
      return false;
    }

    if (m_listenee instanceof EventConstraints) {
      if (eventName.equals("instance")) {
	if (!((EventConstraints)m_listenee).
	    eventGeneratable("incrementalClassifier")) {
	  return false;
	}
      }
      if (eventName.equals("dataSet")) {
	if (!((EventConstraints)m_listenee).
	    eventGeneratable("batchClassifier")) {
	  return false;
	}
      }
    }
    return true;
  }
}
