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
 *    RemoteExperimentListener.java
 *    Copyright (C) 2000 Mark Hall
 *
 */


package weka.experiment;

/**
 * Interface for classes that want to listen for updates on RemoteExperiment
 * progress
 *
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @version $Revision: 1.3 $
 */
public interface RemoteExperimentListener {

  /**
   * Called when progress has been made in a remote experiment
   * @param e the event encapsulating what happened
   */
  public void remoteExperimentStatus(RemoteExperimentEvent e);

}
