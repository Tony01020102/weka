/*
 * SorterComboBoxModel.java
 *
 * Created on 13 f�vrier 2003, 12:28
 */

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
 *    SorterComboBoxModel.java
 *    Copyright (C) 2003 DESS IAGL of Lille
 *
 */
package sortedListPanel;




/**
 *
 * @author  beleg
 */
public class SorterComboBoxModel extends javax.swing.DefaultComboBoxModel {
    
    /** Creates a new instance of SortedListModel */
    public SorterComboBoxModel(Object[] objects) {
	super(objects);
    }
    
    /**Set the contents of the ListModel and notifies its listeners.
     *@param objects the objects to put in the list
     */
    public void setElements(Object[] objects) {
	this.removeAllElements();
	for (int i = 0; i < objects.length; i++)
	    this.addElement(objects[i]);
    }

}
