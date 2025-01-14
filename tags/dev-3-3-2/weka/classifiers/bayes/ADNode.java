/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * ADNode.java
 * Copyright (C) 2002 Remco Bouckaert
 * 
 */

package weka.classifiers.bayes;
import weka.core.*;
import java.util.Vector;

/**
 * The ADNode class implements the ADTree datastructure which increases
 * the speed with which sub-contingency tables can be constructed from
 * a data set in an Instances object. For details, see
 *
 * Cached Sufficient Statistics for Efficient Machine Learning with Large Datasets
 * Andrew Moore, and Mary Soon Lee
 * Journal of Artificial Intelligence Research 8 (1998) 67-91
 * *
 * @author Remco Bouckaert (rrb@xm.co.nz)
 * @version $Revision: 1.1 $
 */
public class ADNode {
        final static int MIN_RECORD_SIZE = 5;
	
	/** list of VaryNode children **/
	public VaryNode [] m_VaryNodes;
	/** list of Instance children (either m_Instances or m_VaryNodes is instantiated) **/
	public Instance [] m_Instances;

        /** count **/
	public int m_nCount;

        /** first node in VaryNode array **/
        public int m_nStartNode;

        /** Creates new ADNode */
        public ADNode() {
        }

	/** create sub tree
	 * @param iNode: index of the lowest node in the tree
	 * @param nRecords: set of records in instances to be considered
	 * @param instances: data set
         * @return VaryNode representing part of an ADTree
 	 **/
	public static VaryNode MakeVaryNode(int iNode, FastVector nRecords, Instances instances) {
		VaryNode _VaryNode = new VaryNode(iNode);
		int nValues = instances.attribute(iNode).numValues();
                

		// reserve memory and initialize
		FastVector [] nChildRecords = new FastVector[nValues];
		for (int iChild = 0; iChild < nValues; iChild++) {
			nChildRecords[iChild] = new FastVector();
		}
		// divide the records among children
		for (int iRecord = 0; iRecord < nRecords.size(); iRecord++) {
			int iInstance = ((Integer) nRecords.elementAt(iRecord)).intValue();
			nChildRecords[(int) instances.instance(iInstance).value(iNode)].addElement(new Integer(iInstance));
		}

		// find most common value
		int nCount = nChildRecords[0].size();
		int nMCV = 0; 
		for (int iChild = 1; iChild < nValues; iChild++) {
			if (nChildRecords[iChild].size() > nCount) {
				nCount = nChildRecords[iChild].size();
				nMCV = iChild;
			}
		}
                _VaryNode.m_nMCV = nMCV;

                // determine child nodes
                _VaryNode.m_ADNodes = new ADNode[nValues];
		for (int iChild = 0; iChild < nValues; iChild++) {
			if (iChild == nMCV || nChildRecords[iChild].size() == 0) {
				_VaryNode.m_ADNodes[iChild] = null;
			} else {
				_VaryNode.m_ADNodes[iChild] = MakeADTree(iNode + 1, nChildRecords[iChild], instances);
			}
		}
		return _VaryNode;
	} // MakeVaryNode

	/** create sub tree
	 * @param iNode: index of the lowest node in the tree
	 * @param nRecords: set of records in instances to be considered
	 * @param instances: data set
         * @return ADNode representing an ADTree
	 **/
	public static ADNode MakeADTree(int iNode, FastVector nRecords, Instances instances) {
		ADNode _ADNode = new ADNode();
                _ADNode.m_nCount = nRecords.size();
                _ADNode.m_nStartNode = iNode;
                if (nRecords.size() < MIN_RECORD_SIZE) {
                  _ADNode.m_Instances = new Instance[nRecords.size()];
                  for (int iInstance = 0; iInstance < nRecords.size(); iInstance++) {
                    _ADNode.m_Instances[iInstance] = instances.instance(((Integer) nRecords.elementAt(iInstance)).intValue());
                  }
                } else {
                  _ADNode.m_VaryNodes = new VaryNode[instances.numAttributes() - iNode];
                  for (int iNode2 = iNode; iNode2 < instances.numAttributes(); iNode2++) {
                          _ADNode.m_VaryNodes[iNode2 - iNode] = MakeVaryNode(iNode2, nRecords, instances);
                  }
                }
		return _ADNode;
	} // MakeADTree

	/** create AD tree from set of instances
	 * @param instances: data set
         * @return ADNode representing an ADTree
	 **/
	public static ADNode MakeADTree(Instances instances) {
          FastVector nRecords = new FastVector(instances.numInstances());
          for (int iRecord = 0; iRecord < instances.numInstances(); iRecord++) {
            nRecords.addElement(new Integer(iRecord));
          }
          return MakeADTree(0, nRecords, instances);
        } // MakeADTree
        
          /** get counts for specific instantiation of a set of nodes
           * @param nCounts - array for storing counts
           * @param nNodes - array of node indexes 
           * @param nOffsets - offset for nodes in nNodes in nCounts
           * @param iNode - index into nNode indicating current node
           * @param iOffset - Offset into nCounts due to nodes below iNode
           * @param bSubstract - indicate whether counts should be added or substracted
           */
        public void getCounts(
              int [] nCounts, 
              int [] nNodes, 
              int [] nOffsets, 
              int iNode, 
              int iOffset,
              boolean bSubstract
        ) {
          if (iNode >= nNodes.length) {
            if (bSubstract) {
              nCounts[iOffset] -= m_nCount;
            } else {
              nCounts[iOffset] += m_nCount;
            }
            return;
          } else {
            if (m_VaryNodes != null) {
              m_VaryNodes[nNodes[iNode] - m_nStartNode].getCounts(nCounts, nNodes, nOffsets, iNode, iOffset, this, bSubstract);
            } else {
              for (int iInstance = 0; iInstance < m_Instances.length; iInstance++) {
                int iOffset2 = iOffset;
                Instance instance = m_Instances[iInstance];
                for (int iNode2 = iNode; iNode2 < nNodes.length; iNode2++) {
                  iOffset2 = iOffset2 + nOffsets[iNode2] * (int) instance.value(nNodes[iNode2]);
                }
                nCounts[iOffset2]++;
              }
            }
          }
        } // getCounts


          /* print is used for debugging only and shows the ADTree in ASCII graphics
           */
        public void print() {
          String sTab = new String();for (int i = 0; i < m_nStartNode; i++) {sTab = sTab + "  ";}
          System.out.println(sTab + "Count = " + m_nCount);
          for (int iNode = 0; iNode < m_VaryNodes.length; iNode++) {
            System.out.println(sTab + "Node " + (iNode + m_nStartNode));
            m_VaryNodes[iNode].print(sTab);
          }
        }
} // class ADNode
