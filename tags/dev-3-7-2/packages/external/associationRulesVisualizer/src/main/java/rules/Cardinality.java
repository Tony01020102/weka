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
 *    Cardinality.java
 *    Copyright (C) 2003 DESS IAGL of Lille
 *
 */

package rules;

import java.util.LinkedList;

/**
 * @author beleg
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Cardinality {

	private LinkedList attributes;
	private float confidence;
	private float frequency;

	public Cardinality(
		LinkedList attributes,
		float confidence,
		float frequency) {
		this.attributes = attributes;
		this.confidence = confidence;
		this.frequency = frequency;
	}

	public float getFrequency() {
		return frequency;
	}

	public float getConfidence() {
		return confidence;
	}

	public LinkedList getAttributes() {
		return attributes;
	}

}
