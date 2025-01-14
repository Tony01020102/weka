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
 *    DensityBasedClusterer.java
 *    Copyright (C) 1999 Mark Hall
 *
 */

package weka.clusterers;

import weka.core.*;

/** 
 * Abstract clustering model that produces (for each test instance)
 * an estimate of the membership in each cluster 
 * (ie. a probability distribution).
 *
 * @author   Mark Hall (mhall@cs.waikato.ac.nz)
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version  $Revision: 1.1 $
 */
public abstract class DensityBasedClusterer extends Clusterer {

  // ===============
  // Public methods.
  // ===============

  /**
   * Returns the prior probability of each cluster.
   *
   * @return the prior probability for each cluster
   * @exception Exception if priors could not be 
   * returned successfully
   */
  public abstract double[] clusterPriors() 
    throws Exception;

  /**
   * Computes the log of the conditional density (per cluster) for a given instance.
   * 
   * @param instance the instance to compute the density for
   * @return the density.
   * @return an array containing the estimated densities
   * @exception Exception if the density could not be computed
   * successfully
   */
  public abstract double[] logDensityPerClusterForInstance(Instance instance) 
    throws Exception;

  /**
   * Computes the density for a given instance.
   * 
   * @param instance the instance to compute the density for
   * @return the density.
   * @exception Exception if the density could not be computed successfully
   */
  public double logDensityForInstance(Instance instance) throws Exception {

    double[] a = logJointDensitiesForInstance(instance);
    double max = a[Utils.maxIndex(a)];
    double sum = 0.0;

    for(int i = 0; i < a.length; i++) {
      sum += Math.exp(a[i] - max);
    }

    return max + Math.log(sum);
  }

  /**
   * Returns the cluster probability distribution for an instance. Will simply have a
   * probability of 1 for the chosen cluster and 0 for the others.
   *
   * @param instance the instance to be clustered
   * @return the probability distribution
   */  
  public double[] distributionForInstance(Instance instance) throws Exception {
    
    return Utils.logs2probs(logJointDensitiesForInstance(instance));
  }

  /** 
   * Returns the logs of the joint densities for a given instance.
   *
   * @param inst the instance 
   * @return the array of values
   * @exception Exception if values could not be computed
   */
  protected double[] logJointDensitiesForInstance(Instance inst)
    throws Exception {

    double[] weights = logDensityPerClusterForInstance(inst);
    double[] priors = clusterPriors();

    for (int i = 0; i < weights.length; i++) {
      if (priors[i] > 0) {
	weights[i] += Math.log(priors[i]);
      } else {
	throw new IllegalArgumentException("Cluster empty!");
      }
    }
    return weights;
  }
}
