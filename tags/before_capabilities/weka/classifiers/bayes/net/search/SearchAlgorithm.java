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
 * SearchAlgorithm.java
 * Copyright (C) 2003 Remco Bouckaert
 * 
 */
package weka.classifiers.bayes.net.search;

import java.io.Serializable;
import java.util.Vector;
import java.util.Enumeration;

import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Instances;
import weka.core.OptionHandler;

/**
 * This is the base class for all search algorithms for learning Bayes networks.
 * It contains some common code, used by other network structure search algorithms,
 * and should not be used by itself.
 * 
 * @author Remco Bouckaert
 * @version $Revision: 1.5 $
 */
public class SearchAlgorithm implements OptionHandler, Serializable {
    /**
     * Holds upper bound on number of parents
     */
    protected int m_nMaxNrOfParents = 1;

    /**
     * determines whether initial structure is an empty graph or a Naive Bayes network
     */
    protected boolean m_bInitAsNaiveBayes = true;

    /**
     * Determines whether after structure is found a MarkovBlanketClassifier correction should be applied
     * If this is true, m_bInitAsNaiveBayes is overridden and interpreted as false.
     */
    protected boolean m_bMarkovBlanketClassifier = false;

    /** c'tor **/
    public SearchAlgorithm() {
    } // SearchAlgorithm

    /**
     * AddArcMakesSense checks whether adding the arc from iAttributeTail to iAttributeHead
     * does not already exists and does not introduce a cycle
     * 
     * @param iAttributeHead index of the attribute that becomes head of the arrow
     * @param iAttributeTail index of the attribute that becomes tail of the arrow
     * @return true if adding arc is allowed, otherwise false
     */
    protected boolean addArcMakesSense(
        BayesNet bayesNet,
        Instances instances,
        int iAttributeHead,
        int iAttributeTail) {
        if (iAttributeHead == iAttributeTail) {
            return false;
        }

        // sanity check: arc should not be in parent set already
        if (isArc(bayesNet, iAttributeHead, iAttributeTail)) {
               return false;
        }

        // sanity check: arc should not introduce a cycle
        int nNodes = instances.numAttributes();
        boolean[] bDone = new boolean[nNodes];

        for (int iNode = 0; iNode < nNodes; iNode++) {
            bDone[iNode] = false;
        }

        // check for cycles
        bayesNet.getParentSet(iAttributeHead).addParent(iAttributeTail, instances);

        for (int iNode = 0; iNode < nNodes; iNode++) {

            // find a node for which all parents are 'done'
            boolean bFound = false;

            for (int iNode2 = 0; !bFound && iNode2 < nNodes; iNode2++) {
                if (!bDone[iNode2]) {
                    boolean bHasNoParents = true;

                    for (int iParent = 0; iParent < bayesNet.getParentSet(iNode2).getNrOfParents(); iParent++) {
                        if (!bDone[bayesNet.getParentSet(iNode2).getParent(iParent)]) {
                            bHasNoParents = false;
                        }
                    }

                    if (bHasNoParents) {
                        bDone[iNode2] = true;
                        bFound = true;
                    }
                }
            }

            if (!bFound) {
                bayesNet.getParentSet(iAttributeHead).deleteLastParent(instances);

                return false;
            }
        }

        bayesNet.getParentSet(iAttributeHead).deleteLastParent(instances);

        return true;
    } // AddArcMakesCycle

    /**
     * reverseArcMakesSense checks whether the arc from iAttributeTail to
     * iAttributeHead exists and reversing does not introduce a cycle
     * 
     * @param index of the attribute that is head of the arrow
     * @param index of the attribute that is tail of the arrow
     */
    protected boolean reverseArcMakesSense(
        BayesNet bayesNet,
        Instances instances,
        int iAttributeHead,
        int iAttributeTail) {
        if (iAttributeHead == iAttributeTail) {
            return false;
        }

        // sanity check: arc should be in parent set already
        if (!isArc(bayesNet, iAttributeHead, iAttributeTail)) {
            return false;
        }

        // sanity check: arc should not introduce a cycle
        int nNodes = instances.numAttributes();
        boolean[] bDone = new boolean[nNodes];

        for (int iNode = 0; iNode < nNodes; iNode++) {
            bDone[iNode] = false;
        }

        // check for cycles
		bayesNet.getParentSet(iAttributeTail).addParent(iAttributeHead, instances);

        for (int iNode = 0; iNode < nNodes; iNode++) {

            // find a node for which all parents are 'done'
            boolean bFound = false;

            for (int iNode2 = 0; !bFound && iNode2 < nNodes; iNode2++) {
                if (!bDone[iNode2]) {
                	ParentSet parentSet = bayesNet.getParentSet(iNode2);
                    boolean bHasNoParents = true;
                    for (int iParent = 0; iParent < parentSet.getNrOfParents(); iParent++) {
                        if (!bDone[parentSet.getParent(iParent)]) {
                        	
                            // this one has a parent which is not 'done' UNLESS it is the arc to be reversed
                            if (!(iNode2 == iAttributeHead && parentSet.getParent(iParent) == iAttributeTail)) {
                                bHasNoParents = false;
                            }
                        }
                    }

                    if (bHasNoParents) {
                        bDone[iNode2] = true;
                        bFound = true;
                    }
                }
            }

            if (!bFound) {
                bayesNet.getParentSet(iAttributeTail).deleteLastParent(instances);
                return false;
            }
        }

        bayesNet.getParentSet(iAttributeTail).deleteLastParent(instances);
        return true;
    } // ReverseArcMakesCycle

    /**
     * IsArc checks whether the arc from iAttributeTail to iAttributeHead already exists
     * 
     * @param index of the attribute that becomes head of the arrow
     * @param index of the attribute that becomes tail of the arrow
     */
    protected boolean isArc(BayesNet bayesNet, int iAttributeHead, int iAttributeTail) {
        for (int iParent = 0; iParent < bayesNet.getParentSet(iAttributeHead).getNrOfParents(); iParent++) {
            if (bayesNet.getParentSet(iAttributeHead).getParent(iParent) == iAttributeTail) {
                return true;
            }
        }

        return false;
    } // IsArc

    /**
     * Returns an enumeration describing the available options.
     *
     * @return an enumeration of all the available options.
     */
    public Enumeration listOptions() {
        return new Vector(0).elements();
    } // listOption

    /**
     * Parses a given list of options. Valid options are:<p>
     * @param options the list of options as an array of strings
     * @exception Exception if an option is not supported
     */
    public void setOptions(String[] options) throws Exception {
    } // setOptions

    /**
     * Gets the current settings of the Classifier.
     *
     * @return an array of strings suitable for passing to setOptions
     */
    public String[] getOptions() {
        return new String[0];
    } // getOptions

    public String toString() {
        return "SearchAlgorithm\n";
    } // toString

    /**
     * buildStructure determines the network structure/graph of the network.
     * The default behavior is creating a network where all nodes have the first
     * node as its parent (i.e., a BayesNet that behaves like a naive Bayes classifier).
     * This method can be overridden by derived classes to restrict the class
     * of network structures that are acceptable.
     */
    public void buildStructure(BayesNet bayesNet, Instances instances) throws Exception {
        if (m_bInitAsNaiveBayes) {
            int iClass = instances.classIndex();
            // initialize parent sets to have arrow from classifier node to
            // each of the other nodes
            for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
                if (iAttribute != iClass) {
                    bayesNet.getParentSet(iAttribute).addParent(iClass, instances);
                }
            }
        }
        search(bayesNet, instances);
        if (m_bMarkovBlanketClassifier) {
            doMarkovBlanketCorrection(bayesNet, instances);
        }
    } // buildStructure 

    protected void search(BayesNet bayesNet, Instances instances) throws Exception {
        // placeholder with implementation in derived classes
    } // search

    /* for each node in the network make sure it is in the
     * Markov blanket of the classifier node, and if not,
     * add arrows so that it is. If the node is an ancestor
     * of the classifier node, add arrow pointing to the classifier
     * node, otherwise, add arrow pointing to attribute node.
     */
    protected void doMarkovBlanketCorrection(BayesNet bayesNet, Instances instances) {
        // Add class node as parent if it is not in the Markov Boundary
        int iClass = instances.classIndex();
        ParentSet ancestors = new ParentSet();
        int nOldSize = 0;
        ancestors.addParent(iClass, instances);
        while (nOldSize != ancestors.getNrOfParents()) {
            nOldSize = ancestors.getNrOfParents();
            for (int iNode = 0; iNode < nOldSize; iNode++) {
                int iCurrent = ancestors.getParent(iNode);
                ParentSet p = bayesNet.getParentSet(iCurrent);
                for (int iParent = 0; iParent < p.getNrOfParents(); iParent++) {
                    if (!ancestors.contains(p.getParent(iParent))) {
                        ancestors.addParent(p.getParent(iParent), instances);
                    }
                }
            }
        }

        for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
            boolean bIsInMarkovBoundary = (iAttribute == iClass);
            bIsInMarkovBoundary =
                bayesNet.getParentSet(iAttribute).contains(iClass)
                    || bayesNet.getParentSet(iClass).contains(iAttribute);
            for (int iAttribute2 = 0; !bIsInMarkovBoundary && iAttribute2 < instances.numAttributes(); iAttribute2++) {
                bIsInMarkovBoundary =
                    bayesNet.getParentSet(iAttribute2).contains(iAttribute)
                        && bayesNet.getParentSet(iAttribute2).contains(iClass);
            }
            if (!bIsInMarkovBoundary) {
                if (ancestors.contains(iAttribute) && bayesNet.getParentSet(iClass).getCardinalityOfParents() < 1024) {
                    bayesNet.getParentSet(iClass).addParent(iAttribute, instances);
                } else {
                    bayesNet.getParentSet(iAttribute).addParent(iClass, instances);
                }
            }
        }
    } // doMarkovBlanketCorrection

    protected void setMarkovBlanketClassifier(boolean bMarkovBlanketClassifier) {
        m_bMarkovBlanketClassifier = bMarkovBlanketClassifier;
    }

    protected boolean getMarkovBlanketClassifier() {
        return m_bMarkovBlanketClassifier;
    }

    /**
     * @return a string to describe the MaxNrOfParentsoption.
     */
    public String maxNrOfParentsTipText() {
        return "Set the maximum number of parents a node in the Bayes net can have."
            + " When initialized as Naive Bayes, setting this parameter to 1 results in"
            + " a Naive Bayes classifier. When set to 2, a Tree Augmented Bayes Network (TAN)"
            + " is learned, and when set >2, a Bayes Net Augmented Bayes Network (BAN)"
            + " is learned. By setting it to a value much larger than the number of nodes"
            + " in the network (the default of 100000 pretty much guarantees this), no"
            + " restriction on the number of parents is enforced";
    } // maxNrOfParentsTipText

    /**
     * @return a string to describe the InitAsNaiveBayes option.
     */
    public String initAsNaiveBayesTipText() {
        return "When set to true (default), the initial network used for structure learning"
            + " is a Naive Bayes Network, that is, a network with an arrow from the classifier"
            + " node to each other node. When set to false, an empty network is used as initial"
            + " network structure";
    } // initAsNaiveBayesTipText

    /**
     * @return a string to describe the MarkovBlanketClassifier option.
     */
    protected String markovBlanketClassifierTipText() {
        return "When set to true (default is false), after a network structure is learned"
            + " a Markov Blanket correction is applied to the network structure. This ensures"
            + " that all nodes in the network are part of the Markov blanket of the classifier"
            + " node.";
    } // markovBlanketClassifierTipText
} // class SearchAlgorithm
