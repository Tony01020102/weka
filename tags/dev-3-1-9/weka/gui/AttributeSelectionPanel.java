/*
 *    AttributeSelectionPanel.java
 *    Copyright (C) 1999 Len Trigg
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

package weka.gui;

import weka.core.Instances;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.BorderFactory;

/**
 * Creates a panel that displays the attributes contained in a set of
 * instances, letting the user toggle whether each attribute is selected
 * or not (eg: so that unselected attributes can be removed before
 * classification).
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 1.3 $
 */
public class AttributeSelectionPanel extends JPanel {

  /**
   * A table model that looks at the names of attributes and maintains
   * a list of attributes that have been "selected".
   */
  class AttributeTableModel extends AbstractTableModel {

    /** The instances who's attribute structure we are reporting */
    protected Instances m_Instances;
    
    /** The flag for whether the instance will be included */
    protected boolean [] m_Selected;

    
    /**
     * Creates the tablemodel with the given set of instances.
     *
     * @param instances the initial set of Instances
     */
    public AttributeTableModel(Instances instances) {

      setInstances(instances);
    }

    /**
     * Sets the tablemodel to look at a new set of instances.
     *
     * @param instances the new set of Instances.
     */
    public void setInstances(Instances instances) {

      m_Instances = instances;
      m_Selected = new boolean [m_Instances.numAttributes()];
      includeAll();
    }
    
    /**
     * Gets the number of attributes.
     *
     * @return the number of attributes.
     */
    public int getRowCount() {
      
      return m_Selected.length;
    }
    
    /**
     * Gets the number of columns: 3
     *
     * @return 3
     */
    public int getColumnCount() {
      
      return 3;
    }
    
    /**
     * Gets a table cell
     *
     * @param row the row index
     * @param column the column index
     * @return the value at row, column
     */
    public Object getValueAt(int row, int column) {
      
      switch (column) {
      case 0:
	return new Integer(row + 1);
      case 1:
	return new Boolean(m_Selected[row]);
      case 2:
	return m_Instances.attribute(row).name();
      default:
	return null;
      }
    }
    
    /**
     * Gets the name for a column.
     *
     * @param column the column index.
     * @return the name of the column.
     */
    public String getColumnName(int column) {
      
      switch (column) {
      case 0:
	return new String("No.");
      case 1:
	return new String("");
      case 2:
	return new String("Name");
      default:
	return null;
      }
    }
    
    /**
     * Sets the value at a cell.
     *
     * @param value the new value.
     * @param row the row index.
     * @param col the column index.
     */
    public void setValueAt(Object value, int row, int col) {
      
      if (col == 1) {
	m_Selected[row] = ((Boolean) value).booleanValue(); 
      }
    }
    
    /**
     * Gets the class of elements in a column.
     *
     * @param col the column index.
     * @return the class of elements in the column.
     */
    public Class getColumnClass(int col) {
      return getValueAt(0, col).getClass();
    }

    /**
     * Returns true if the column is the "selected" column.
     *
     * @param row ignored
     * @param col the column index.
     * @return true if col == 1.
     */
    public boolean isCellEditable(int row, int col) {

      if (col == 1) { 
	return true;
      }
      return false;
    }
    
    /**
     * Gets an array containing the indices of all selected attributes.
     *
     * @return the array of selected indices.
     */
    public int [] getSelectedAttributes() {
      
      int [] r1 = new int[getRowCount()];
      int selCount = 0;
      for (int i = 0; i < getRowCount(); i++) {
	if (m_Selected[i]) {
	  r1[selCount++] = i;
	}
      }
      int [] result = new int[selCount];
      System.arraycopy(r1, 0, result, 0, selCount);
      return result;
    }
    
    /**
     * Sets the state of all attributes to selected.
     */
    public void includeAll() {
      
      for (int i = 0; i < m_Selected.length; i++) {
	m_Selected[i] = true;
      }
      fireTableRowsUpdated(0, m_Selected.length);
    }
    
    /**
     * Deselects all attributes.
     */
    public void removeAll() {
      
      for (int i = 0; i < m_Selected.length; i++) {
	m_Selected[i] = false;
      }
      fireTableRowsUpdated(0, m_Selected.length);
    }

    /**
     * Inverts the selected status of each attribute.
     */
    public void invert() {

      for (int i = 0; i < m_Selected.length; i++) {
	m_Selected[i] = !m_Selected[i];
      }
      fireTableRowsUpdated(0, m_Selected.length);
    }
  }

  /** Press to select all attributes */  
  protected JButton m_IncludeAll = new JButton("All");

  /** Press to deselect all attributes */
  protected JButton m_RemoveAll = new JButton("None");

  /** Press to invert the current selection */
  protected JButton m_Invert = new JButton("Invert");

  /** The table displaying attribute names and selection status */
  protected JTable m_Table = new JTable();

  /** The table model containingn attribute names and selection status */
  protected AttributeTableModel m_Model;
  
  /**
   * Creates the attribute selection panel with no initial instances.
   */
  public AttributeSelectionPanel() {

    m_IncludeAll.setToolTipText("Selects all attributes");
    m_IncludeAll.setEnabled(false);
    m_IncludeAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	m_Model.includeAll();
      }
    });
    m_RemoveAll.setToolTipText("Unselects all attributes");
    m_RemoveAll.setEnabled(false);
    m_RemoveAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	m_Model.removeAll();
      }
    });
    m_Invert.setToolTipText("Inverts the current attribute selection");
    m_Invert.setEnabled(false);
    m_Invert.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	m_Model.invert();
      }
    });
    m_Table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    m_Table.setColumnSelectionAllowed(false); 
    m_Table.setPreferredScrollableViewportSize(new Dimension(250, 150));

    // Set up the layout
    JPanel p1 = new JPanel();
    p1.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    p1.setLayout(new GridLayout(1, 3, 5, 5));
    p1.add(m_IncludeAll);
    p1.add(m_RemoveAll);
    p1.add(m_Invert);

    setLayout(new BorderLayout());
    add(p1, BorderLayout.NORTH);
    add(new JScrollPane(m_Table), BorderLayout.CENTER);
  }

  /**
   * Sets the instances who's attribute names will be displayed.
   *
   * @param newInstances the new set of instances
   */
  public void setInstances(Instances newInstances) {

    if (m_Model == null) {
      m_Model = new AttributeTableModel(newInstances);
      m_Table.setModel(m_Model);
      TableColumnModel tcm = m_Table.getColumnModel();
      tcm.getColumn(0).setMaxWidth(60);
      tcm.getColumn(1).setMaxWidth(tcm.getColumn(1).getMinWidth());
      tcm.getColumn(2).setMinWidth(100);
    } else {
      m_Model.setInstances(newInstances);
      m_Table.clearSelection();
    }
    m_IncludeAll.setEnabled(true);
    m_RemoveAll.setEnabled(true);
    m_Invert.setEnabled(true);
    m_Table.sizeColumnsToFit(2);
    m_Table.revalidate();
    m_Table.repaint();
  }

  /**
   * Gets an array containing the indices of all selected attributes.
   *
   * @return the array of selected indices.
   */
  public int [] getSelectedAttributes() {
    
    return m_Model.getSelectedAttributes();
  }
  
  /**
   * Gets the selection model used by the table.
   *
   * @return a value of type 'ListSelectionModel'
   */
  public ListSelectionModel getSelectionModel() {

    return m_Table.getSelectionModel();
  }
  
  /**
   * Tests the attribute selection panel from the command line.
   *
   * @param args must contain the name of an arff file to load.
   */
  public static void main(String[] args) {

    try {
      if (args.length == 0) {
	throw new Exception("supply the name of an arff file");
      }
      Instances i = new Instances(new java.io.BufferedReader(
				  new java.io.FileReader(args[0])));
      AttributeSelectionPanel asp = new AttributeSelectionPanel();
      final javax.swing.JFrame jf =
	new javax.swing.JFrame("Attribute Selection Panel");
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(asp, BorderLayout.CENTER);
      jf.addWindowListener(new java.awt.event.WindowAdapter() {
	public void windowClosing(java.awt.event.WindowEvent e) {
	  jf.dispose();
	  System.exit(0);
	}
      });
      jf.pack();
      jf.setVisible(true);
      asp.setInstances(i);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  
} // AttributeSelectionPanel
