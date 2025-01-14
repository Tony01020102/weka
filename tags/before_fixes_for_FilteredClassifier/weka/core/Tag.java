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
 *    Tag.java
 *    Copyright (C) 1999 Len Trigg
 *
 */

package weka.core;

/**
 * A <code>Tag</code> simply associates a numeric ID with a String description.
 *
 * @author <a href="mailto:len@reeltwo.com">Len Trigg</a>
 * @version $Revision: 1.7 $
 */
public class Tag {

  /** The ID */
  protected int m_ID;

  /** The unique string for this tag, doesn't have to be numeric */
  protected String m_IDStr;
  
  /** The descriptive text */
  protected String m_Readable;
  
  /**
   * Creates a new <code>Tag</code> instance.
   *
   * @param ident the ID for the new Tag.
   * @param readable the description for the new Tag.
   */
  public Tag(int ident, String readable) {
    this(ident, "", readable);
  }
  
  /**
   * Creates a new <code>Tag</code> instance.
   *
   * @param ident the ID for the new Tag.
   * @param identStr the ID string for the new Tag (case-insensitive).
   * @param readable the description for the new Tag.
   */
  public Tag(int ident, String identStr, String readable) {
    m_ID = ident;
    if (identStr.length() == 0)
      m_IDStr = "" + ident;
    else
      m_IDStr = identStr.toUpperCase();
    m_Readable = readable;
  }

  /**
   * Gets the numeric ID of the Tag.
   *
   * @return the ID of the Tag.
   */
  public int getID() {
    return m_ID;
  }

  /**
   * Gets the string ID of the Tag.
   *
   * @return the string ID of the Tag.
   */
  public String getIDStr() {
    return m_IDStr;
  }

  /**
   * Gets the string description of the Tag.
   *
   * @return the description of the Tag.
   */
  public String getReadable() {
    return m_Readable;
  }
  
  /**
   * returns the IDStr
   * 
   * @return the IDStr
   */
  public String toString() {
    return m_IDStr;
  }
  
  /**
   * returns a list that can be used in the listOption methods to list all
   * the available ID strings, e.g.: &lt;0|1|2&gt; or &lt;what|ever&gt;
   * 
   * @return a list of all ID strings
   */
  public static String toOptionList(Tag[] tags) {
    String	result;
    int		i;
    
    result = "<";
    for (i = 0; i < tags.length; i++) {
      if (i > 0)
	result += "|";
      result += tags[i];
    }
    result += ">";
    
    return result;
  }
}
