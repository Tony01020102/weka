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
 *    SufficientStatistics.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */
package weka.classifiers.mi.miti;

import java.util.HashMap;

import weka.core.Instance;

/**
 * Interface to be implemented by classes that maintain sufficient statistics.
 *
 * @author Luke Bjerring
 * @version $Revision$
 */
public interface SufficientStatistics {

  /**
   * The number of positive cases on the left side.
   */
  public double positiveCountLeft();

  /**
   * The number of positive cases on the right side.
   */
  public double positiveCountRight();

  /**
   * The total number of cases on the left.
   */
  public double totalCountLeft();

  /**
   * The total number of cases on the right.
   */
  public double totalCountRight();

  /**
   * Method used to update the sufficient statistics by shifting an instance
   * from one side to the other.
   */
  public void updateStats(Instance i, HashMap<Instance, Bag> instanceBags);
}
