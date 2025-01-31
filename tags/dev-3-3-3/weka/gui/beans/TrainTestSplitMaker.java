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
 *    TrainTestSplitMaker.java
 *    Copyright (C) 2002 Mark Hall
 *
 */

package weka.gui.beans;

import weka.core.Instances;

import java.util.Random;
import java.util.Enumeration;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.awt.*;

/**
 * Bean that accepts data sets, training sets, test sets and produces
 * both a training and test set by randomly spliting the data
 *
 * @author <a href="mailto:mhall@cs.waikato.ac.nz">Mark Hall</a>
 * @version 1.0
 */
public class TrainTestSplitMaker
  extends AbstractTrainAndTestSetProducer
  implements DataSourceListener, TrainingSetListener, TestSetListener,
	     UserRequestAcceptor, EventConstraints, Serializable {

  private int m_trainPercentage = 66;
  private int m_randomSeed = 42;
  
  private Thread m_splitThread = null;

  public TrainTestSplitMaker() {
    super();
    m_visual.setText("TrainTestSplitMaker");
  }

  /**
   * Set the percentage of data to be in the training portion of the split
   *
   * @param newTrainPercent an <code>int</code> value
   */
  public void setTrainPercent(int newTrainPercent) {
    m_trainPercentage = newTrainPercent;
  }

  /**
   * Get the percentage of the data that will be in the training portion of
   * the split
   *
   * @return an <code>int</code> value
   */
  public int getTrainPercent() {
    return m_trainPercentage;
  }

  /**
   * Set the random seed
   *
   * @param newSeed an <code>int</code> value
   */
  public void setSeed(int newSeed) {
    m_randomSeed = newSeed;
  }

  /**
   * Get the value of the random seed
   *
   * @return an <code>int</code> value
   */
  public int getSeed() {
    return m_randomSeed;
  }

  /**
   * Accept a training set
   *
   * @param e a <code>TrainingSetEvent</code> value
   */
  public void acceptTrainingSet(TrainingSetEvent e) {
    Instances trainingSet = e.getTrainingSet();
    DataSetEvent dse = new DataSetEvent(this, trainingSet);
    acceptDataSet(dse);
  }

  /**
   * Accept a test set
   *
   * @param e a <code>TestSetEvent</code> value
   */
  public void acceptTestSet(TestSetEvent e) {
    Instances testSet = e.getTestSet();
    DataSetEvent dse = new DataSetEvent(this, testSet);
    acceptDataSet(dse);
  }

  /**
   * Accept a data set
   *
   * @param e a <code>DataSetEvent</code> value
   */
  public void acceptDataSet(DataSetEvent e) {
    if (m_splitThread == null) {
      final Instances dataSet = new Instances(e.getDataSet());
      m_splitThread = new Thread() {
	  public void run() {
	    try {
	      dataSet.randomize(new Random(m_randomSeed));
	      int trainSize = dataSet.numInstances() * m_trainPercentage / 100;
	      int testSize = dataSet.numInstances() - trainSize;
      
	      Instances train = new Instances(dataSet, 0, trainSize);
	      Instances test = new Instances(dataSet, trainSize, testSize);
      
	      TrainingSetEvent tse =
		new TrainingSetEvent(TrainTestSplitMaker.this, train);
	      tse.m_setNumber = 1; tse.m_maxSetNumber = 1;
	      if (m_splitThread != null) {
		notifyTrainingSetProduced(tse);
	      }
    
	      // inform all test set listeners
	      TestSetEvent teste = 
		new TestSetEvent(TrainTestSplitMaker.this, test);
	      teste.m_setNumber = 1; teste.m_maxSetNumber = 1;
	      if (m_splitThread != null) {
		notifyTestSetProduced(teste);
	      } else {
		if (m_logger != null) {
		  m_logger.logMessage("Split has been canceled!");
		  m_logger.statusMessage("OK");
		}
	      }
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    } finally {
	      if (isInterrupted()) {
		System.err.println("Split maker interrupted");
	      }
	      block(false);
	    }
	  }
	};
      m_splitThread.setPriority(Thread.MIN_PRIORITY);
      m_splitThread.start();

      //      if (m_splitThread.isAlive()) {
      block(true);
      //      }
      m_splitThread = null;
    }
  }

  /**
   * Notify test set listeners that a test set is available
   *
   * @param tse a <code>TestSetEvent</code> value
   */
  protected void notifyTestSetProduced(TestSetEvent tse) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_testListeners.clone();
    }
    if (l.size() > 0) {
      for(int i = 0; i < l.size(); i++) {
	System.err.println("Notifying test listeners "
			   +"(cross validation fold maker)");
	((TestSetListener)l.elementAt(i)).acceptTestSet(tse);
      }
    }
  }

  /**
   * Notify training set listeners that a training set is available
   *
   * @param tse a <code>TrainingSetEvent</code> value
   */
  protected void notifyTrainingSetProduced(TrainingSetEvent tse) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_trainingListeners.clone();
    }
    if (l.size() > 0) {
      for(int i = 0; i < l.size(); i++) {
	System.err.println("Notifying training listeners "
			   +"(cross validation fold maker)");
	((TrainingSetListener)l.elementAt(i)).acceptTrainingSet(tse);
      }
    }
  }

  /**
   * Function used to stop code that calls acceptDataSet. This is 
   * needed as cross validation is performed inside a separate
   * thread of execution.
   *
   * @param tf a <code>boolean</code> value
   */
  private synchronized void block(boolean tf) {
    if (tf) {
      try {
	// make sure that the thread is still alive before blocking
	if (m_splitThread.isAlive()) {
	  wait();
	}
      } catch (InterruptedException ex) {
      }
    } else {
      notifyAll();
    }
  }

  /**
   * Stop processing
   */
  public void stop() {
    // tell the listenee (upstream bean) to stop
    if (m_listenee instanceof BeanCommon) {
      System.err.println("Listener is BeanCommon");
      ((BeanCommon)m_listenee).stop();
    }

    // stop the split thread
    if (m_splitThread != null) {
      //      m_buildThread.interrupt();
      Thread temp = m_splitThread;
      //      m_buildThread.stop();
      m_splitThread = null;
      temp.interrupt();
    }
  }

  /**
   * Get list of user requests
   *
   * @return an <code>Enumeration</code> value
   */
  public Enumeration enumerateRequests() {
    Vector newVector = new Vector(0);
    if (m_splitThread != null) {
      newVector.addElement("Stop");
    }
    return newVector.elements();
  }

  /**
   * Perform the named request
   *
   * @param request a <code>String</code> value
   * @exception IllegalArgumentException if an error occurs
   */
  public void performRequest(String request) throws IllegalArgumentException {
    if (request.compareTo("Stop") == 0) {
      stop();
    } else {
      throw new IllegalArgumentException(request
			 + " not supported (TrainTestSplitMaker)");
    }
  }

  /**
   * Returns true, if at the current time, the named event could
   * be generated. Assumes that the supplied event name is
   * an event that could be generated by this bean
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
      if (((EventConstraints)m_listenee).eventGeneratable("dataSet") ||
	  ((EventConstraints)m_listenee).eventGeneratable("trainingSet") ||
	  ((EventConstraints)m_listenee).eventGeneratable("testSet")) {
	return true;
      } else {
	return false;
      }
    }
    return true;
  }
}
