/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.dllearner.tools.ore.ui.wizard.panels;


import java.awt.Color;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dllearner.tools.ore.ui.UndoLabel;
import org.semanticweb.owl.model.OWLOntologyChange;
/**
 * JPanel where an ontology change and his undo function is listed.
 * @author Lorenz Buehmann
 *
 */
public class ChangePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -934113184795465461L;
	
	/**
	 * Constructor. 	
	 * @param label labelname
	 * @param changes ontology changes
	 * @param mL mouse listener
	 */
	public ChangePanel(String label, List<OWLOntologyChange> changes, MouseListener mL){
		super();
		add(new JLabel(label));
		add(new UndoLabel(changes, mL));
		setBackground(Color.WHITE);
		
	}

}
