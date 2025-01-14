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
 *    MatrixPanel.java
 *    Copyright (C) 2002 Ashraf M. Kibriya
 *
 */


package weka.gui.visualize;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.lang.ref.SoftReference;
import java.util.Random;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ComponentAdapter;

import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JSplitPane;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import weka.gui.*;
import weka.gui.visualize.*;
import weka.core.*;

/** 
 * This panel displays a plot matrix of the user selected attributes
 * of a given data set. 
 * 
 * The datapoints are coloured using a discrete colouring set if the 
 * user has selected a nominal attribute for colouring. If the user
 * has selected a numeric attribute then the datapoints are coloured
 * using a colour spectrum ranging from blue to red (low values to
 * high). Datapoints missing a class value are displayed in black.
 * 
 * @author Ashraf M. Kibriya (amk14@cs.waikato.ac.nz)
 * @version $Revision: 1.3 $
 */


public class MatrixPanel extends JPanel{

  /** The that panel contains the actual matrix */
  private final Plot m_plotsPanel;

  /** The panel that displays the legend of the colouring attribute */
  protected final ClassPanel m_cp = new ClassPanel();

  /** The panel that contains all the buttons and tools, i.e. resize, jitter bars and sub-sampling buttons etc
      on the bottom of the panel */
  protected JPanel optionsPanel;

  /** Split pane for splitting the matrix and the buttons and bars */
  protected JSplitPane jp;
  /** The button that updates the display to reflect the changes made by the user. 
      E.g. changed attribute set for the matrix    */
  protected JButton m_updateBt = new JButton("Update");

  /** The button to display a window to select attributes */
  protected JButton m_selAttrib = new JButton("Select Attributes");

  /** The dataset for which this panel will display the plot matrix for  */
  protected Instances m_data=null;

  /** The list for selecting the attributes to display the plot matrix */
  protected JList m_attribList = new JList();

  /** The scroll pane to scrolling the matrix */
  protected final JScrollPane m_js = new JScrollPane();

  /** The combo box to allow user to select the colouring attribute */
  protected JComboBox m_classAttrib = new JComboBox();

  /** The slider to adjust the size of the cells in the matrix  */  
  protected JSlider m_cellSize = new JSlider(50, 500, 100);

  /** The slider to add jitter to the plots */  
  protected JSlider m_jitter = new JSlider(0, 20, 0); 

  /** For adding random jitter */
  private Random rnd = new Random();
    
  /** Array containing precalculated jitter values */
  private int jitterVals[][];
 
  /** The text area for percentage to resample data */
  protected JTextField m_resamplePercent = new JTextField(5);

  /** The label for resample percentage */
  protected JButton m_resampleBt =  new JButton("SubSample % :");

  /** Random seed for random subsample */
  protected JTextField m_rseed = new JTextField(5);
 
  /** For selecting same class distribution in the subsample as in the input */
  protected JRadioButton origDist = new JRadioButton("Class distribution as in input data");

  /** For selecting uniform class distribution in the subsample */
  protected JRadioButton unifDist = new JRadioButton("Uniform class distribution");

  /** Button group for subsampling radio buttons */
  private ButtonGroup distGroup = new ButtonGroup();

  /** Displays the current size beside the slider bar for cell size */
  private final JLabel m_sizeLb = new JLabel("Size: [100]");

  /** This array contains the indices of the attributes currently selected  */
  private int [] m_selectedAttribs;

  /** This contains the index of the currently selected colouring attribute  */
  private int m_classIndex;

  /** This is a local array cache for all the instance values for faster rendering */
  private int [][] m_points;

  /** This is an array cache for the colour of each of the instances depending on the 
      colouring attribute. If the colouring attribute is nominal then it contains the 
      index of the colour in our colour list. Otherwise, for numeric colouring attribute,
      it contains the precalculated red component for each instance's colour */
  private int [] m_pointColors;

  /** Contains true for each value that is  missing, for each instance */ 
  private boolean [][] m_missing;

  /** This array contains: <br>
      m_type[0][i] = [type of attribute, nominal, string or numeric]<br>
      m_type[1][i] = [number of discrete values of nominal or string attribute <br>
      or same as m_type[0][i] for numeric attribute] */
  private int [][] m_type;

  /** Stores the maximum size for Size label to keep it's size constant */
  private Dimension m_sizeD;

  /** Contains discrete colours for colouring for nominal attributes */
  private FastVector m_colorList = new FastVector();

  /** default colour list */
  private static final Color [] m_defaultColors = {Color.blue,
		 				   Color.red,
						   Color.cyan,
						   new Color(75, 123, 130),
						   Color.pink,
						   Color.green,
						   Color.orange,
						   new Color(255, 0, 255),
						   new Color(255, 0, 0),
						   new Color(0, 255, 0),
						   Color.black};

  /** Constructor
      @param ins The instances object for the matrix
  */
  public MatrixPanel() {
    m_rseed.setText("1");
    origDist.setSelected(true);
    distGroup.add(origDist);
    distGroup.add(unifDist); 

    /** Setting up GUI **/
    m_selAttrib.addActionListener( new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  final JDialog jd = new JDialog((JFrame) MatrixPanel.this.getTopLevelAncestor(), 
					 "Attribute Selection Panel",
					 true);

	  JPanel jp = new JPanel();
	  JScrollPane js = new JScrollPane(m_attribList);
	  JButton okBt = new JButton("OK");
	  JButton cancelBt = new JButton("Cancel");
	  final int [] savedSelection = m_attribList.getSelectedIndices();
					
	  okBt.addActionListener( new ActionListener() {	
	      public void actionPerformed(ActionEvent e) {
		jd.dispose(); }
	    } );

	  cancelBt.addActionListener( new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
		m_attribList.setSelectedIndices(savedSelection);
		jd.dispose();}
	    });
	  jd.addWindowListener( new WindowAdapter() {
	      public void windowClosing(WindowEvent e) {
		m_attribList.setSelectedIndices(savedSelection);
		jd.dispose();}
	    });
	  jp.add(okBt);
	  jp.add(cancelBt);

	  jd.getContentPane().add(js, BorderLayout.CENTER); 
	  jd.getContentPane().add(jp, BorderLayout.SOUTH);

	  if(js.getPreferredSize().width < 200)
	    jd.setSize( 250, 250 );
	  else
	    jd.setSize( (int) js.getPreferredSize().width+10, 250);
					
	  jd.setLocation( m_selAttrib.getLocationOnScreen().x,
			  m_selAttrib.getLocationOnScreen().y-jd.getHeight() );
	  jd.show();
	}
      });
      
    m_updateBt.addActionListener( new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  m_selectedAttribs = m_attribList.getSelectedIndices();
	  initInternalFields();
					
	  Plot a = m_plotsPanel;
	  java.awt.FontMetrics fm = a.getFontMetrics(a.getFont());
	  a.setCellSize( m_cellSize.getValue() );					
	  Dimension d = new Dimension((m_selectedAttribs.length)*(a.cellSize+a.extpad)+100, 
				      (m_selectedAttribs.length)*(a.cellSize+a.extpad)
				      +2*fm.getHeight()+a.extpad);
	  //System.out.println("Size: "+a.cellSize+" Extpad: "+
	  //		   a.extpad+" selected: "+
	  //		   m_selectedAttribs.length+' '+d); 
	  a.setPreferredSize(d);
	  a.setSize( a.getPreferredSize() );
	  a.setJitter( m_jitter.getValue() );
					
	  m_js.revalidate();
	  m_cp.setColours(m_colorList);
	  m_cp.setCindex(m_classIndex);
					
	  repaint();
	}
      });
    m_updateBt.setPreferredSize( m_selAttrib.getPreferredSize() );
      
    m_cellSize.addChangeListener( new ChangeListener() {
	public void stateChanged(ChangeEvent ce) {
	  m_sizeLb.setText("Size: ["+m_cellSize.getValue()+"]");
	  m_sizeLb.setPreferredSize( m_sizeD );
	  m_jitter.setMaximum( m_cellSize.getValue()/5 ); //20% of cell Size
	}
      });
 
    m_resampleBt.addActionListener( new ActionListener() { 
	public void actionPerformed(ActionEvent e) {
	  JLabel rseedLb = new JLabel("Random Seed: ");
	  JTextField rseedTxt = m_rseed;
	  JLabel percentLb = new JLabel("Subsample as");
	  JLabel percent2Lb = new JLabel("% of input: ");
	  final JTextField percentTxt = new JTextField(5);
	  percentTxt.setText( m_resamplePercent.getText() );
	  JButton doneBt = new JButton("Done");

	  final JDialog jd = new JDialog((JFrame) MatrixPanel.this.getTopLevelAncestor(), 
					 "Attribute Selection Panel",
					 true) {
	      public void dispose() { 
		m_resamplePercent.setText(percentTxt.getText());
		super.dispose();
	      } 
	    };
	  jd.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
			       
	  doneBt.addActionListener( new ActionListener(){ 
	      public void actionPerformed(ActionEvent ae) {
		jd.dispose();  
	      }
	    });
	  GridBagLayout gbl = new GridBagLayout();
	  GridBagConstraints gbc = new GridBagConstraints();
	  JPanel p1 = new JPanel( gbl );		
	  gbc.anchor = gbc.WEST; gbc.fill = gbc.HORIZONTAL;
	  gbc.insets = new Insets(0,2,2,2);
	  gbc.gridwidth = gbc.RELATIVE;
	  p1.add(rseedLb, gbc); gbc.weightx = 0;
	  gbc.gridwidth = gbc.REMAINDER; gbc.weightx=1;
	  p1.add(rseedTxt, gbc);
	  gbc.insets = new Insets(8,2,0,2); gbc.weightx=0;
	  p1.add(percentLb, gbc);
	  gbc.insets = new Insets(0,2,2,2); gbc.gridwidth = gbc.RELATIVE;
	  p1.add(percent2Lb, gbc);
	  gbc.gridwidth = gbc.REMAINDER; gbc.weightx=1;
	  p1.add(percentTxt, gbc);
	  gbc.insets = new Insets(8,2,2,2);

	  if(m_data.attribute(m_classAttrib.getSelectedIndex()).isNominal()) {
	    JPanel p2 = new JPanel( gbl );
	    p2.add(origDist, gbc);
	    p2.add(unifDist, gbc);
	    p2.setBorder( BorderFactory.createTitledBorder("Class Distribution") );
	    p1.add(p2, gbc);
	  }
				
	  JPanel p3 = new JPanel( gbl );
	  gbc.fill = gbc.HORIZONTAL; gbc.gridwidth = gbc.REMAINDER;
	  gbc.weightx = 1;  gbc.weighty = 0;
	  p3.add(p1, gbc);
	  gbc.insets = new Insets(8,4,8,4);
	  p3.add(doneBt, gbc);
					   
	  jd.getContentPane().setLayout( new BorderLayout() );
	  jd.getContentPane().add(p3, BorderLayout.NORTH);
	  jd.pack();
	  jd.setLocation( m_resampleBt.getLocationOnScreen().x,
			  m_resampleBt.getLocationOnScreen().y-jd.getHeight() );
	  jd.show();
	}
      });

    optionsPanel = new JPanel( new GridBagLayout() ); //all the rest of the panels are in here.
    final JPanel p2 = new JPanel( new BorderLayout() );  //this has class colour panel
    final JPanel p3 = new JPanel( new GridBagLayout() ); //this has update and select buttons
    final JPanel p4 = new JPanel( new GridBagLayout() ); //this has the slider bars and combobox
    GridBagConstraints gbc = new GridBagConstraints();
     
    m_sizeD = m_sizeLb.getPreferredSize();
    m_resampleBt.setPreferredSize( m_selAttrib.getPreferredSize() );

    gbc.fill = gbc.HORIZONTAL;
    gbc.anchor = gbc.NORTHWEST;
    gbc.insets = new Insets(2,2,2,2);
    p4.add(m_sizeLb, gbc);
    gbc.weightx=1; gbc.gridwidth = gbc.REMAINDER;
    p4.add(m_cellSize, gbc);
    gbc.weightx=0; gbc.gridwidth = gbc.RELATIVE;
    p4.add( new JLabel("Jitter: "), gbc);
    gbc.weightx=1; gbc.gridwidth = gbc.REMAINDER;
    p4.add(m_jitter, gbc);
    p4.add(m_classAttrib, gbc);
      
    gbc.gridwidth = gbc.REMAINDER;
    gbc.weightx=1;
    gbc.fill = gbc.NONE;
    p3.add(m_updateBt, gbc);
    p3.add(m_selAttrib, gbc);
    gbc.gridwidth = gbc.RELATIVE;
    gbc.weightx = 0;
    gbc.fill = gbc.VERTICAL;
    gbc.anchor = gbc.WEST;
    p3.add(m_resampleBt, gbc);
    gbc.gridwidth = gbc.REMAINDER;
    p3.add(m_resamplePercent, gbc);
    
    p2.setBorder(BorderFactory.createTitledBorder("Class Colour"));
    p2.add(m_cp, BorderLayout.SOUTH);

    gbc.insets = new Insets(8,5,2,5);
    gbc.anchor = gbc.NORTHWEST; gbc.fill = gbc.HORIZONTAL; gbc.weightx=1;
    gbc.gridwidth = gbc.RELATIVE;
    optionsPanel.add(p4, gbc);
    gbc.gridwidth = gbc.REMAINDER;
    optionsPanel.add(p3, gbc);
    optionsPanel.add(p2, gbc);

    this.addComponentListener( new ComponentAdapter() {
	public void componentResized(ComponentEvent cv) {
	  m_js.setMinimumSize( new Dimension(MatrixPanel.this.getWidth(),
					     MatrixPanel.this.getHeight()
					     -optionsPanel.getPreferredSize().height-10));
	  jp.setDividerLocation( MatrixPanel.this.getHeight()-optionsPanel.getPreferredSize().height-10 );
	}
      });

    optionsPanel.setMinimumSize( new Dimension(0,0) );
    jp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_js, optionsPanel);
    jp.setOneTouchExpandable(true);
    jp.setResizeWeight(1);
    this.setLayout( new BorderLayout() );
    this.add(jp, BorderLayout.CENTER);

    /** Setting up the initial color list **/
    for(int i=0; i<m_defaultColors.length-1; i++)
      m_colorList.addElement(m_defaultColors[i]);
      
    /** Initializing internal fields and components **/
    m_plotsPanel = new Plot();
    m_plotsPanel.setLayout(null);
    m_plotsPanel.setFont( new java.awt.Font("Dialog", java.awt.Font.BOLD, 11) );
    m_js.getHorizontalScrollBar().setUnitIncrement( 10 );
    m_js.getVerticalScrollBar().setUnitIncrement( 10 ); 
    m_js.setViewportView( m_plotsPanel );
    m_cp.setInstances(m_data);
    m_cp.setBorder(BorderFactory.createEmptyBorder(15,10,10,10));
    m_cp.addRepaintNotify(m_plotsPanel);
    //m_updateBt.doClick(); //not until setting up the instances
  }



  /** Initializes internal data fields, i.e. data values, type, missing and color cache arrays 
   */
  public void initInternalFields() {
    Instances inst = m_data;
    m_classIndex = m_classAttrib.getSelectedIndex();
    double minC=0, maxC=0;

    m_data.setClassIndex(m_classIndex);
    /** Resampling  **/
    if(Double.parseDouble(m_resamplePercent.getText())<100) {
      try {
	if( m_data.attribute(m_classIndex).isNominal() ) {
	  weka.filters.supervised.instance.Resample r = new weka.filters.supervised.instance.Resample();
		  
	  r.setRandomSeed( Integer.parseInt(m_rseed.getText()) );
	  r.setSampleSizePercent( Double.parseDouble(m_resamplePercent.getText()) );
		  
	  if(origDist.isSelected())
	    r.setBiasToUniformClass(0);
	  else
	    r.setBiasToUniformClass(1);
	  r.setInputFormat(m_data);
	  inst = weka.filters.Filter.useFilter(m_data, r);
	}
	else {
	  weka.filters.unsupervised.instance.Resample r = new weka.filters.unsupervised.instance.Resample();
		  
	  r.setRandomSeed( Integer.parseInt(m_rseed.getText()) );
	  r.setSampleSizePercent( Double.parseDouble(m_resamplePercent.getText()) );
	  r.setInputFormat(m_data);
	  inst = weka.filters.Filter.useFilter(m_data, r);
	}
      }
      catch(Exception ex) { System.out.println("Error occurred while sampling"); ex.printStackTrace();  }
    }
    
    m_points = new int[inst.numInstances()][inst.numAttributes()];
    m_pointColors = new int[inst.numInstances()];
    m_missing = new boolean[inst.numInstances()][inst.numAttributes()];
    m_type = new int[2][inst.numAttributes()];
    jitterVals = new int[inst.numInstances()][2];
      
    /** Setting up the color list for non-numeric attribute as well as jittervals**/
    if(!(inst.attribute(m_classIndex).isNumeric())) {
	  
      for(int i=m_colorList.size(); i<inst.attribute(m_classIndex).numValues()+1; i++) {
	Color pc = m_defaultColors[i % 10];
	int ija =  i / 10;
	ija *= 2; 
	for (int j=0;j<ija;j++) {
	  pc = pc.darker();     
	}
	m_colorList.addElement(pc);
      }
	  
      for(int i=0; i<inst.numInstances(); i++) {
	//set to black for missing class value which is last colour is default list
	if(inst.instance(i).isMissing(m_classIndex))
	  m_pointColors[i] =  m_defaultColors.length-1;
	else
	  m_pointColors[i] = (int) inst.instance(i).value(m_classIndex);

	jitterVals[i][0] = rnd.nextInt(m_jitter.getValue()+1)
	  - m_jitter.getValue()/2;
	jitterVals[i][1] = rnd.nextInt(m_jitter.getValue()+1)
	  - m_jitter.getValue()/2;
	      
      }
    }
    /** Setting up color variations for numeric attribute as well as jittervals **/
    else {
      for(int i=0; i<inst.numInstances(); i++) {
	if(!(inst.instance(i).isMissing(m_classIndex))) {
	  minC = maxC = inst.instance(i).value(m_classIndex);
	  break;
	}
      }
	  
      for(int i=1; i<inst.numInstances(); i++) {
	if(!(inst.instance(i).isMissing(m_classIndex))) {
	  if(minC > inst.instance(i).value(m_classIndex))
	    minC = inst.instance(i).value(m_classIndex);
	  if(maxC < inst.instance(i).value(m_classIndex))
	    maxC = inst.instance(i).value(m_classIndex);
	}
      }
	  
      for(int i=0; i<inst.numInstances(); i++) {
	double r = (inst.instance(i).value(m_classIndex) - minC) / (maxC - minC);
	r = (r * 240) + 15;
	m_pointColors[i] = (int)r;

	jitterVals[i][0] = rnd.nextInt(m_jitter.getValue()+1)
	  - m_jitter.getValue()/2;
	jitterVals[i][1] = rnd.nextInt(m_jitter.getValue()+1)
	  - m_jitter.getValue()/2;
      }
    }

    /** Creating local cache of the data values **/
    double min[]=new double[inst.numAttributes()], max=0;
    double ratio[] = new double[inst.numAttributes()];
    double cellSize = m_cellSize.getValue(), temp1=0, temp2=0;

    for(int j=0; j<inst.numAttributes(); j++) {
      int i;
      for(i=0; i<inst.numInstances(); i++) {
	min[j] = max = 0;
	if(!(inst.instance(i).isMissing(j))) {
	  min[j] = max = inst.instance(i).value(j);
	  break;
	}
      }
      for( i=i; i<inst.numInstances(); i++ ) {
	if(!(inst.instance(i).isMissing(j))) {
	  if(inst.instance(i).value(j) < min[j])
	    min[j] = inst.instance(i).value(j);
	  if(inst.instance(i).value(j) > max)
	    max = inst.instance(i).value(j);
	}
      }
      ratio[j] =  cellSize / (max - min[j]);
    }

    for(int j=0; j<inst.numAttributes(); j++) {
      if(inst.attribute(j).isNominal() || inst.attribute(j).isString()) {
	m_type[0][j] = 1;  m_type[1][j] = inst.attribute(j).numValues();

	temp1 = cellSize/(double)m_type[1][j];
	temp2 = temp1/2;
	for(int i=0; i<inst.numInstances(); i++) {
	  m_points[i][j] = (int) Math.round(temp2+temp1*inst.instance(i).value(j));
	  if(inst.instance(i).isMissing(j))
	    m_missing[i][j] = true;    //represents missing value
	}
      }
      else {
	m_type[0][j] = m_type[1][j] = 0;
	for(int i=0; i<inst.numInstances(); i++) {
	  m_points[i][j] = (int) Math.round((inst.instance(i).value(j)
					     -min[j])*ratio[j]);	
	  if(inst.instance(i).isMissing(j))
	    m_missing[i][j] = true;    //represents missing value
	}
      }
    }
    m_cp.setColours(m_colorList);
  }

  /** Sets up the UI's attributes lists 
   */  
  public void setupAttribLists() {
    String [] tempAttribNames = new String[m_data.numAttributes()];
    String type;

    m_classAttrib.removeAllItems();
    for(int i=0; i<tempAttribNames.length; i++) {
      switch (m_data.attribute(i).type()) {
      case Attribute.NOMINAL:
	type = " (Nom)";
	break;
      case Attribute.NUMERIC:
	type = " (Num)";
	break;
      case Attribute.STRING:
	type = " (Str)";
	break;
      default:
	type = " (???)";
      }
      tempAttribNames[i] = new String("Colour: "+m_data.attribute(i).name()+" "+type);
      m_classAttrib.addItem(tempAttribNames[i]);
    }
    m_classAttrib.setSelectedIndex( tempAttribNames.length-1 );
    m_attribList.setListData(tempAttribNames);
    m_attribList.setSelectionInterval(0, tempAttribNames.length-1);
  }

  /** Calculates the percentage to resample 
   */
  public void setPercent() {
    if(m_data.numInstances() > 700) {
      String percnt = Double.toString(500D/m_data.numInstances()*100);
	  
      if( percnt.indexOf('.')+3 < percnt.length() ) {
	m_resamplePercent.setText(percnt.substring(0, percnt.indexOf('.')-1)+
				  percnt.substring(percnt.indexOf('.'), percnt.indexOf('.')+3) );
      }
      else
	m_resamplePercent.setText(percnt);
    }
    else
      m_resamplePercent.setText("100");
  }


  /** This method changes the Instances object of this class to a new one. It also does all the necessary
      initializations for displaying the panel. This must be called before trying to display the panel.
      @param newInst The new set of Instances
  */
  public void setInstances(Instances newInst) {
    m_data = newInst;
    setPercent();
    setupAttribLists();
    m_rseed.setText("1");
    origDist.setSelected(true);
    initInternalFields();
    m_cp.setInstances(m_data);
    m_cp.setCindex(m_classIndex);
    m_updateBt.doClick();
  }


  /**
     Main method for testing this class
  */
  public static void main(String [] args)  {
    final JFrame jf = new JFrame("Weka Knowledge Explorer: MatrixPanel");
    final JButton setBt = new JButton("Set Instances");
    Instances data = null;
    try {
      if(args.length==1)
	data = new Instances( new BufferedReader( new FileReader(args[0])) ); 
      else {
	System.out.println("Usage: MatrixPanel <arff file>"); 
	System.exit(-1);
      }
    } catch(IOException ex) { ex.printStackTrace(); System.exit(-1); }
     
    final MatrixPanel mp = new MatrixPanel();
    mp.setInstances(data);
    setBt.addActionListener( new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  JFileChooser chooser = new JFileChooser(new java.io.File(System.getProperty("user.dir")));
	  ExtensionFileFilter myfilter = new ExtensionFileFilter("arff", "Arff data files");
	  chooser.setFileFilter(myfilter);
	  int returnVal = chooser.showOpenDialog(jf);
		  
	  if(returnVal == JFileChooser.APPROVE_OPTION)
	    {
	      try{
		System.out.println("You chose to open this file: " +chooser.getSelectedFile().getName());
		Instances in = new Instances ( new FileReader(chooser.getSelectedFile().getAbsolutePath()) );
		mp.setInstances(in);
	      }
	      catch(Exception ex) { ex.printStackTrace(); }
	    }
	}
      });
    //System.out.println("Loaded: "+args[0]+"\nRelation: "+data.relationName()+"\nAttributes: "+data.numAttributes());
    //System.out.println("The attributes are: ");
    //for(int i=0; i<data.numAttributes(); i++)
    //  System.out.println(data.attribute(i).name());

    //RepaintManager.currentManager(jf.getRootPane()).setDoubleBufferingEnabled(false);
    jf.getContentPane().setLayout( new BorderLayout() );
    jf.getContentPane().add(mp, BorderLayout.CENTER);
    jf.getContentPane().add(setBt, BorderLayout.SOUTH);
    jf.getContentPane().setFont( new java.awt.Font( "SansSerif", java.awt.Font.PLAIN, 11) );
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.setSize(800, 600);
    jf.show();
    jf.repaint();
  }




  /**
     Internal class responsible for displaying the actual matrix
     Requires the internal data fields of the parent class to be properly initialized
     before being created
  */
  private class Plot extends JPanel implements MouseMotionListener, MouseListener {

    int extpad=3, intpad=4, cellSize=100, cellRange=100, lastx=0, lasty=0, jitter=0;
    Color fontColor = new Color(98, 101, 156);
    java.awt.Rectangle r;
    java.awt.FontMetrics fm = this.getFontMetrics(this.getFont());
    int lastxpos, lastypos;

    /** Constructor 
     */
    public Plot() {
      super();
      this.setToolTipText("blah");
      this.addMouseMotionListener( this );
      this.addMouseListener( this );
      initialize();
    }


    /** Initializes the internal fields */
    public void initialize() {
      lastxpos = lastypos = 0;	  
      cellRange = cellSize; cellSize = cellRange + 2*intpad;
    }      

    public void mouseMoved(MouseEvent e) {
      Graphics g = this.getGraphics();
      int xpos=100+extpad, ypos=extpad+2*fm.getHeight();

      for(int j=m_selectedAttribs.length-1; j>=0; j--) {
	for(int i=0; i<m_selectedAttribs.length; i++) {
	  if(e.getX()>=xpos && e.getX()<=xpos+cellSize+extpad)
	    if(e.getY()>=ypos && e.getY()<=ypos+cellSize+extpad) {
	      if(xpos!=lastxpos || ypos!=lastypos) {
		g.setColor( Color.red );
		g.drawRect(xpos-1, ypos-1, cellSize+1, cellSize+1);
		if(lastxpos!=0 && lastypos!=0) {
		  g.setColor( this.getBackground().darker() );
		  g.drawRect(lastxpos-1, lastypos-1, cellSize+1, cellSize+1); }
		lastxpos = xpos; lastypos = ypos;
	      }
	      return;
	    }
	  xpos+=cellSize+extpad;
	}
	xpos=100+extpad;
	ypos+=cellSize+extpad;
      }
      if(lastxpos!=0 && lastypos!=0) {
	g.setColor( this.getBackground().darker() );
	g.drawRect(lastxpos-1, lastypos-1, cellSize+1, cellSize+1); }
      lastxpos=lastypos=0;
    }

    public void mouseDragged(MouseEvent e){ }

    public void mouseClicked(MouseEvent e) {
      int i=0, j=0, found=0;
	  
      int xpos=100+extpad, ypos=extpad+2*fm.getHeight();
      for(j=m_selectedAttribs.length-1; j>=0; j--) {
	for(i=0; i<m_selectedAttribs.length; i++) {
	  if(e.getX()>=xpos && e.getX()<=xpos+cellSize+extpad)
	    if(e.getY()>=ypos && e.getY()<=ypos+cellSize+extpad) {
	      found=1; break;
	    }
	  xpos+=cellSize+extpad;
	}
	if(found==1)
	  break;
	xpos=100+extpad;
	ypos+=cellSize+extpad;
      }
      if(found==0)
	return;

      JFrame jf = new JFrame("Weka Knowledge Explorer: Visualizing "+m_data.relationName() );
      VisualizePanel vp = new VisualizePanel();
      try {
	PlotData2D pd = new PlotData2D(m_data);
	pd.setPlotName("Master Plot");
	vp.setMasterPlot(pd);
	//System.out.println("x: "+i+" y: "+j);
	vp.setXIndex(i);
	vp.setYIndex(j);
	vp.m_ColourCombo.setSelectedIndex( m_classIndex );
      }
      catch(Exception ex) { ex.printStackTrace(); }
      jf.getContentPane().add(vp);
      jf.setSize(800,600);
      jf.show();
    } 

    public void mouseEntered(MouseEvent e){ }
    public void mouseExited(MouseEvent e){ }
    public void mousePressed(MouseEvent e){ }
    public void mouseReleased(MouseEvent e){ }

    /** sets the new jitter value for the plots
     */
    public void setJitter(int newjitter) {
      jitter = newjitter;
    }
      
    /** sets the new size for the plots
     */
    public void setCellSize(int newCellSize) {
      cellSize = newCellSize;
      initialize();
    }

    /** Returns the X and Y attributes of the plot the mouse is currently
	on
    */
    public String getToolTipText(MouseEvent event) {
      int xpos=100+extpad, ypos=extpad+2*fm.getHeight();
	  
      for(int j=m_selectedAttribs.length-1; j>=0; j--) {
	for(int i=0; i<m_selectedAttribs.length; i++) {
	  if(event.getX()>=xpos && event.getX()<=xpos+cellSize+extpad)
	    if(event.getY()>=ypos && event.getY()<=ypos+cellSize+extpad)
	      return("X: "+m_data.attribute(m_selectedAttribs[i]).name()+
		     " Y: "+m_data.attribute(m_selectedAttribs[j]).name()+
		     " (click to enlarge)");
	  xpos+=cellSize+extpad;
	}
	xpos=100+extpad;
	ypos+=cellSize+extpad;
      }
      return ("Matrix Panel");
    }

    /**  Paints a single Plot at xpos, ypos. and xattrib and yattrib on X and
	 Y axes
    */
    public void paintGraph(Graphics g, int xattrib, int yattrib, int xpos, int ypos) {
      int x, y;
      g.setColor( this.getBackground().darker() );
      g.drawRect(xpos-1, ypos-1, cellSize+1, cellSize+1);
      g.setColor(Color.white);
      g.fillRect(xpos, ypos, cellSize, cellSize);
      for(int i=0; i<m_points.length; i++) {

	if( !(m_missing[i][yattrib] || m_missing[i][xattrib]) ) {      

	  if(m_type[0][m_classIndex]==0)
	    if(m_missing[i][m_classIndex])
	      g.setColor(m_defaultColors[m_defaultColors.length-1]);
	    else
	      g.setColor( new Color(m_pointColors[i],150,(255-m_pointColors[i])));
	  else 
	    g.setColor((Color)m_colorList.elementAt(m_pointColors[i]));

	  if(m_points[i][xattrib]+jitterVals[i][0]<0 || m_points[i][xattrib]+jitterVals[i][0]>cellRange)
	    if(cellRange-m_points[i][yattrib]+jitterVals[i][1]<0 || cellRange-m_points[i][yattrib]+jitterVals[i][1]>cellRange) {
	      //both x and y out of range don't add jitter
	      x=intpad+m_points[i][xattrib];
	      y=intpad+(cellRange - m_points[i][yattrib]);
	    }
	    else {
	      //only x out of range
	      x=intpad+m_points[i][xattrib];
	      y=intpad+(cellRange - m_points[i][yattrib])+jitterVals[i][1];
	    }
	  else if(cellRange-m_points[i][yattrib]+jitterVals[i][1]<0 || cellRange-m_points[i][yattrib]+jitterVals[i][1]>cellRange) {
	    //only y out of range
	    x=intpad+m_points[i][xattrib]+jitterVals[i][0];
	    y=intpad+(cellRange - m_points[i][yattrib]);
	  }
	  else {
	    //none out of range
	    x=intpad+m_points[i][xattrib]+jitterVals[i][0];
	    y=intpad+(cellRange - m_points[i][yattrib])+jitterVals[i][1];
	  }
	  g.drawLine(x+xpos, y+ypos, x+xpos, y+ypos);
	}
      }
      g.setColor( fontColor );
    }


    /**
       Paints the matrix of plots in the current visible region
    */
    public void paintME(Graphics g) {
      r = g.getClipBounds();

      g.setColor( this.getBackground() );
      g.fillRect(r.x, r.y, r.width, r.height);
      g.setColor( fontColor );

      int xpos = 0, ypos = 0, attribWidth=0;
	  
      xpos = extpad;
      ypos=extpad+fm.getHeight();
	  
      if(r.y < (ypos+cellSize+extpad)) {
	g.drawString("Plot Matrix", xpos, ypos);
	xpos += 100;
	for(int i=0; i<m_selectedAttribs.length; i++) {
	  if( xpos+cellSize < r.x)
	    { xpos += cellSize+extpad; continue; }
	  else if(xpos > r.x+r.width)
	    { break; }
	  else {
	    attribWidth = fm.stringWidth(m_data.attribute(m_selectedAttribs[i]).name());
	    g.drawString(m_data.attribute(m_selectedAttribs[i]).name(), 
			 (attribWidth<cellSize) ? (xpos + (cellSize/2 - attribWidth/2)):xpos, 
			 ypos);
	  }
	  xpos += cellSize+extpad;
	}
      }
      xpos = extpad; ypos += fm.getHeight();
	  
      for(int j=m_selectedAttribs.length-1; j>=0; j--) {
	if( ypos+cellSize < r.y )
	  { ypos += cellSize+extpad;  continue; }
	else if( ypos > r.y+r.height )
	  break;
	else {
	  if(r.x < (xpos+cellSize+extpad))
	    {g.drawString(m_data.attribute(m_selectedAttribs[j]).name(), xpos+extpad, ypos+cellSize/2); }
	  xpos += 100;
	  for(int i=0; i<m_selectedAttribs.length; i++) {
	    if( xpos+cellSize < r.x) {
	      xpos += cellSize+extpad; continue; }
	    else if(xpos > r.x+r.width)
	      break;
	    else
	      paintGraph(g, m_selectedAttribs[i], m_selectedAttribs[j], xpos, ypos);
	    xpos += cellSize+extpad;
	  }
	}
	xpos = extpad;
	ypos += cellSize+extpad;
      }
    }
      
    /** paints this JPanel (PlotsPanel)
     */
    public void paintComponent(Graphics g) {
      paintME(g);
    }
  }
}
