/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    GraphEdge.java
 *    Copyright (C) 2003 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.graphvisualizer;

/**
 * This class represents an edge in the graph
 *
 * @author Ashraf M. Kibriya (amk14@cs.waikato.ac.nz)
 * @version $Revision$ - 23 Apr 2003 - Initial version (Ashraf M. Kibriya)
 */
public class GraphEdge extends Object {
  
  /** The index of source node in Nodes vector */
  protected int src;
  /** The index of target node in Nodes vector */
  protected int dest;
  /** The type of Edge */
  protected int type;
  /** Label of source node */
  protected String srcLbl;
  /** Label of target node */
  protected String destLbl;
  
  public GraphEdge(int s, int d, int t) {
    src=s; dest=d; type=t;
    srcLbl = null; destLbl = null;
  }
  
  public GraphEdge(int s, int d, int t, String sLbl, String dLbl) {
    src=s; dest=d; type=t;
    srcLbl = sLbl; destLbl = dLbl;
  }
  
  public String toString() {
    return ("("+src+","+dest+","+type+")");
  }
  
  public boolean equals(Object e) {
    if( e instanceof GraphEdge &&
    ((GraphEdge)e).src==this.src &&
    ((GraphEdge)e).dest==this.dest &&
    ((GraphEdge)e).type==this.type)
      return true;
    else
      return false;
  }
  
} // GraphEdge
