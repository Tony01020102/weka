/*
 *    ThresholdCurve.java
 *    Copyright (C) 2000 Intelligenesis Corp.
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
package weka.classifiers.evaluation;

import weka.core.Utils;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.DistributionClassifier;

/**
 * Generates points illustrating prediction tradeoffs that can be obtained
 * by varying the threshold value between classes. For example, the typical 
 * threshold value of 0.5 means the predicted probability of "positive" must be
 * higher than 0.5 for the instance to be predicted as "positive". The 
 * resulting dataset can be used to visualize precision/recall tradeoff, or 
 * for ROC curve analysis (true positive rate vs false positive rate).
 *
 * @author Len Trigg (len@intelligenesis.net)
 * @version $Revision: 1.7 $
 */
public class ThresholdCurve {

  /** The name of the relation used in threshold curve datasets */
  public final static String RELATION_NAME = "ThresholdCurve";

  /**
   * Calculates the performance stats for the default class and return 
   * results as a set of Instances. The
   * structure of these Instances is as follows:<p> <ul> 
   * <li> <b>True Positives </b>
   * <li> <b>False Negatives</b>
   * <li> <b>False Positives</b>
   * <li> <b>True Negatives</b>
   * <li> <b>False Positive Rate</b>
   * <li> <b>True Positive Rate</b>
   * <li> <b>Precision</b>
   * <li> <b>Recall</b>  
   * <li> <b>Fallout</b>  
   * <li> <b>Threshold</b> contains the probability threshold that gives
   * rise to the previous performance values. 
   * </ul> <p>
   * For the definitions of these measures, see TwoClassStats <p>
   *
   * @see TwoClassStats
   * @param classIndex index of the class of interest.
   * @return datapoints as a set of instances, null if no predictions
   * have been made.
   */
  public Instances getCurve(FastVector predictions) {

    if (predictions.size() == 0) {
      return null;
    }
    return getCurve(predictions, 
                    ((NominalPrediction)predictions.elementAt(0))
                    .distribution().length - 1);
  }

  /**
   * Calculates the performance stats for the desired class and return 
   * results as a set of Instances.
   *
   * @param classIndex index of the class of interest.
   * @return datapoints as a set of instances.
   */
  public Instances getCurve(FastVector predictions, int classIndex) {

    if ((predictions.size() == 0) ||
        (((NominalPrediction)predictions.elementAt(0))
         .distribution().length <= classIndex)) {
      return null;
    }

    Instances insts = makeHeader();
    int totPos = 0, totNeg = 0;
    double [] probs = getProbabilities(predictions, classIndex);
    int [] sorted = Utils.sort(probs);

    // Get distribution of positive/negatives
    for (int i = 0; i < probs.length; i++) {
      NominalPrediction pred = (NominalPrediction)predictions.elementAt(i);
      if (pred.actual() == classIndex) {
        totPos += pred.weight();
      } else {
        totNeg += pred.weight();
      }
    }

    TwoClassStats tc = new TwoClassStats(totPos, totNeg, 0, 0);
    for (int i = 0; i < sorted.length; i++) {
      NominalPrediction pred = (NominalPrediction)predictions.elementAt(sorted[i]);
      if (pred.actual() == classIndex) {
        tc.setTruePositive(tc.getTruePositive() - pred.weight());
        tc.setFalseNegative(tc.getFalseNegative() + pred.weight());
      } else {
        tc.setFalsePositive(tc.getFalsePositive() - pred.weight());
        tc.setTrueNegative(tc.getTrueNegative() + pred.weight());
      }
      /*
      System.out.println(tc + " " + probs[sorted[i]] 
                         + " " + (pred.actual() == classIndex));
      */
      if ((i != (sorted.length - 1)) &&
          ((i == 0) ||  
          (probs[sorted[i]] != probs[sorted[i - 1]]))) {
        insts.add(makeInstance(tc, probs[sorted[i]]));
      }
    }
    return insts;
  }

  /**
   * Calculates the n point precision result, which is the precision averaged
   * over n evenly spaced (w.r.t recall) samples of the curve.
   *
   * @param tcurve a previously extracted threshold curve Instances.
   * @param n the number of points to average over.
   * @return the n-point precision.
   */
  public static double getNPointPrecision(Instances tcurve, int n) {

    if (!RELATION_NAME.equals(tcurve.relationName()) 
        || (tcurve.numInstances() == 0)) {
      return Double.NaN;
    }
    int recallInd = tcurve.attribute("Recall").index();
    int precisInd = tcurve.attribute("Precision").index();
    double [] recallVals = tcurve.attributeToDoubleArray(recallInd);
    int [] sorted = Utils.sort(recallVals);
    double isize = 1.0 / (n - 1);
    double psum = 0;
    for (int i = 0; i < n; i++) {
      int pos = binarySearch(sorted, recallVals, i * isize);
      double recall = recallVals[sorted[pos]];
      double precis = tcurve.instance(sorted[pos]).value(precisInd);
      /*
      System.err.println("Point " + (i + 1) + ": i=" + pos 
                         + " r=" + (i * isize)
                         + " p'=" + precis 
                         + " r'=" + recall);
      */
      // interpolate figures for non-endpoints
      while ((pos != 0) && (pos < sorted.length - 1)) {
        pos++;
        double recall2 = recallVals[sorted[pos]];
        if (recall2 != recall) {
          double precis2 = tcurve.instance(sorted[pos]).value(precisInd);
          double slope = (precis2 - precis) / (recall2 - recall);
          double offset = precis - recall * slope;
          precis = isize * i * slope + offset;
          /*
          System.err.println("Point2 " + (i + 1) + ": i=" + pos 
                             + " r=" + (i * isize)
                             + " p'=" + precis2 
                             + " r'=" + recall2
                             + " p''=" + precis);
          */
          break;
        }
      }
      psum += precis;
    }
    return psum / n;
  }


  /**
   * Gets the index of the instance with the closest threshold value to the
   * desired target
   *
   * @param tcurve a set of instances that have been generated by this class
   * @param threshold the target threshold
   * @return the index of the instance that has threshold closest to
   * the target, or -1 if this could not be found (i.e. no data, or
   * bad threshold target)
   */
  public static int getThresholdInstance(Instances tcurve, double threshold) {

    if (!RELATION_NAME.equals(tcurve.relationName()) 
        || (tcurve.numInstances() == 0)
        || (threshold < 0)
        || (threshold > 1.0)) {
      return -1;
    }
    if (tcurve.numInstances() == 1) {
      return 0;
    }
    double [] tvals = tcurve.attributeToDoubleArray(tcurve.numAttributes() - 1);
    int [] sorted = Utils.sort(tvals);
    return binarySearch(sorted, tvals, threshold);
  }


  private static int binarySearch(int [] index, double [] vals, double target) {
    
    int lo = 0, hi = index.length - 1;
    while (hi - lo > 1) {
      int mid = lo + (hi - lo) / 2;
      double midval = vals[index[mid]];
      if (target > midval) {
        lo = mid;
      } else if (target < midval) {
        hi = mid;
      } else {
        while ((mid > 0) && (vals[index[mid - 1]] == target)) {
          mid --;
        }
        return mid;
      }
    }
    return lo;
  }


  private double [] getProbabilities(FastVector predictions, int classIndex) {

    // sort by predicted probability of the desired class.
    double [] probs = new double [predictions.size()];
    for (int i = 0; i < probs.length; i++) {
      NominalPrediction pred = (NominalPrediction)predictions.elementAt(i);
      probs[i] = pred.distribution()[classIndex];
    }
    return probs;
  }

  private Instances makeHeader() {

    FastVector fv = new FastVector();
    fv.addElement(new Attribute("True Positives"));
    fv.addElement(new Attribute("False Negatives"));
    fv.addElement(new Attribute("False Positives"));
    fv.addElement(new Attribute("True Negatives"));
    fv.addElement(new Attribute("False Positive Rate"));
    fv.addElement(new Attribute("True Positive Rate"));
    fv.addElement(new Attribute("Precision"));
    fv.addElement(new Attribute("Recall"));
    fv.addElement(new Attribute("Fallout"));
    fv.addElement(new Attribute("FMeasure"));
    fv.addElement(new Attribute("Threshold"));      
    return new Instances(RELATION_NAME, fv, 100);
  }
  
  private Instance makeInstance(TwoClassStats tc, double prob) {

    int count = 0;
    double [] vals = new double[11];
    vals[count++] = tc.getTruePositive();
    vals[count++] = tc.getFalseNegative();
    vals[count++] = tc.getFalsePositive();
    vals[count++] = tc.getTrueNegative();
    vals[count++] = tc.getFalsePositiveRate();
    vals[count++] = tc.getTruePositiveRate();
    vals[count++] = tc.getPrecision();
    vals[count++] = tc.getRecall();
    vals[count++] = tc.getFallout();
    vals[count++] = tc.getFMeasure();
    vals[count++] = prob;
    return new Instance(1.0, vals);
  }
  
  /**
   * Tests the ThresholdCurve generation from the command line.
   * The classifier is currently hardcoded. Pipe in an arff file.
   *
   * @param args currently ignored
   */
  public static void main(String [] args) {

    try {
      
      Instances inst = new Instances(new java.io.InputStreamReader(System.in));
      if (false) {
        System.out.println(ThresholdCurve.getNPointPrecision(inst, 11));
      } else {
        inst.setClassIndex(inst.numAttributes() - 1);
        ThresholdCurve tc = new ThresholdCurve();
        EvaluationUtils eu = new EvaluationUtils();
        DistributionClassifier classifier = new weka.classifiers.SMO();
        FastVector predictions = new FastVector();
        for (int i = 0; i < 2; i++) { // Do two runs.
          eu.setSeed(i);
          predictions.appendElements(eu.getCVPredictions(classifier, inst, 10));
          //System.out.println("\n\n\n");
        }
        Instances result = tc.getCurve(predictions);
        System.out.println(result);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}



