package MasterCut.gui;

import MasterCut.domain.Controller;
import MasterCut.domain.SaveManager;
import MasterCut.domain.dto.DimensionsDTO;
import MasterCut.domain.dto.PanelDTO;
import MasterCut.domain.dto.ToolDTO;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.utils.*;
import MasterCut.domain.utils.enumPackage.Unit;
import MasterCut.gui.dialog.AddToolDialog;
import MasterCut.gui.dialog.FileChooserDialog;
import static MasterCut.gui.dialog.FileChooserDialog.*;
import MasterCut.gui.panel.InfoPanel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class MainWindow extends javax.swing.JFrame implements MessageHandler
{

	public Controller controller;

	public MainWindow()
	{
		Message.getInstance().subscribe(this);
		controller = new Controller();
		initComponents();

		infoPanel = new InfoPanel(drawingPanel);
		infoPanel.setPreferredSize(new java.awt.Dimension(250, 320));
		infoPanel.setVisible(true);
		tabInfos.add(infoPanel);

		setCutDepthThroughAll();

		initToolsList(); // remove
		updatePanelDepth();
		updatePanelPanel();
		initToolsInfo();
	}

	private void setActiveTool()
	{
		controller.setActiveTool(topButtonToolsList.getItemAt(topButtonToolsList.getSelectedIndex()));
		toolList.setSelectedItem(controller.getActiveToolDTO());
	}

	private void setCutDepthThroughAll()
	{
		try
		{
			String offsetText = TextFieldOffset.getText();
			double offset = Double.parseDouble(offsetText);
			Unit usedUnit = controller.getPanelDTO().usedUnit;

			if (usedUnit == Unit.IMPERIAL)
				offset = UnitConverter.convertToMetric(offset);

			controller.setCutDepth(controller.getPanelDTO().thickness + offset);

		} catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void updatePanelInfo(Object object)
	{
		infoPanel.showInfo(object);
	}

	private void setCutDepthCustom(double cutDepth)
	{
		controller.setCutDepth(cutDepth);
	}

	private void updatePanelDepth()
	{
		PanelDTO panelDTO = controller.getPanelDTO();

		if (panelDTO == null)
			LabelUnit.setText("Unit : Nan");
		else if (panelDTO.usedUnit == Unit.METRIC)
			LabelUnit.setText("Unit : mm");
		else
			LabelUnit.setText("Unit : in");
	}

	private void updatePanelPanel()
	{
		PanelDTO panelDTO = controller.getPanelDTO();
		DimensionsDTO dimensionsDTO = panelDTO.dimensions;
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		DecimalFormat df = new DecimalFormat("#.##", symbols);

		if (panelDTO.usedUnit == Unit.IMPERIAL)
		{
			TextFieldPanelHeight.setText(df.format(UnitConverter.convertToMetric(dimensionsDTO.height)));
			TextFieldPanelWidth.setText(df.format(UnitConverter.convertToMetric(dimensionsDTO.width)));
			TextFieldPanelThickness.setText(df.format(UnitConverter.convertToMetric(panelDTO.thickness)));
		} else
		{
			TextFieldPanelHeight.setText(df.format(dimensionsDTO.height));
			TextFieldPanelWidth.setText(df.format(dimensionsDTO.width));
			TextFieldPanelThickness.setText(df.format(panelDTO.thickness));
		}

		if (panelDTO.usedUnit == Unit.METRIC)
		{
			RadioButtonPanelMetric.setSelected(true);
		} else
		{
			RadioButtonPanelImperial.setSelected(true);
		}
	}

	public void updateInfoCursor(Point2D.Double point) {
		PanelDTO panelDTO = controller.getPanelDTO();
		Object hoverClickable = controller.getDTOOnHover(point);

		if (panelDTO == null) {
			labelXposCursor.setText("X: -");
			labelYposCursor.setText("Y: -");
			return;
		}

		Unit usedUnit = panelDTO.usedUnit;
		double panelHeight = panelDTO.dimensions.getHeight();
		double adjustedY = panelHeight - point.y;
		String unitSymbol = usedUnit == Unit.IMPERIAL ? "in" : "mm";

		double xDisplay = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(point.x) : point.x;
		double yDisplay = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(adjustedY) : adjustedY;

		labelXposCursor.setText(String.format("X: %.2f %s", xDisplay, unitSymbol));
		labelYposCursor.setText(String.format("Y: %.2f %s", yDisplay, unitSymbol));

		if (hoverClickable != null) {
			if (hoverClickable instanceof BaseCutDTO cut) {
				labelSize.setVisible(true);
				DimensionsDTO dimensions = controller.getDimensionsOfCut(cut);

				// Conversion des dimensions si nécessaire
				double width = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(dimensions.width) : dimensions.width;
				double height = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(dimensions.height) : dimensions.height;

				// Mise à jour du label avec les dimensions formatées
				labelSizeData.setText(String.format("%.2f x %.2f %s", width, height, unitSymbol));
				labelSizeData.setVisible(true);
			}
		} else {
			labelSize.setVisible(false);
			labelSizeData.setVisible(false);
		}
	}

	private void initToolsInfo()
	{
		java.util.List<ToolDTO> toolsDTO = controller.getToolsDTO();

		for (ToolDTO toolDTO : toolsDTO)
		{
			toolList.addItem(toolDTO);
			topButtonToolsList.addItem(toolDTO);
		}
	}

	private void initToolsList()
	{
		java.util.List<ToolDTO> toolsDTO = controller.getToolsDTO();

		if (!toolsDTO.isEmpty())
		{
			ToolDTO tool = toolsDTO.getFirst();
			showToolInfo(tool);
		}
	}

	private void addToolToList(ToolDTO newTool)
	{
		toolList.addItem(newTool);
		topButtonToolsList.addItem(newTool);
	}

	private void removeToolToList(ToolDTO tool)
	{
		toolList.removeItem(tool);
		topButtonToolsList.removeItem(tool);
	}

	private void showToolInfo(ToolDTO tool)
	{
		if (tool != null)
		{
			String unit = tool.usedUnit.toString();

			if (tool.usedUnit == Unit.IMPERIAL)
				labelDiameter.setText(String.format("%.2f %s", UnitConverter.convertToImperial(tool.diameter), unit));
			else
				labelDiameter.setText(String.format("%.2f %s", tool.diameter, unit));

			labelRPM.setText(String.valueOf(tool.rpm));

			if (tool.usedUnit == Unit.IMPERIAL)
				labelFeedrate.setText(String.format("%.2f %s", UnitConverter.convertToImperial(tool.feedrate), unit));
			else
				labelFeedrate.setText(String.format("%.2f %s", tool.feedrate, unit));

		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        depthGroup = new javax.swing.ButtonGroup();
        unitGroup = new javax.swing.ButtonGroup();
        unitGroupTool = new javax.swing.ButtonGroup();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        panelMain = new javax.swing.JPanel();
        panelTopButtons = new javax.swing.JPanel(new FlowLayout(FlowLayout.LEFT));
        topButtonToolsList = new javax.swing.JComboBox<>();
        topButtonRedo = new javax.swing.JButton();
        topButtonUndo = new javax.swing.JButton();
        InputPanelGrid = new javax.swing.JTextField();
        ToggleButtonSnapToGrid = new javax.swing.JToggleButton();
        ToggleButtonShowGrid = new javax.swing.JToggleButton();
        labelInfo = new javax.swing.JLabel();
        labelInfoData = new javax.swing.JLabel();
        panelLeft = new javax.swing.JPanel();
        pannelTabs = new javax.swing.JTabbedPane();
        tabTools = new javax.swing.JPanel();
        toolList = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        topButtonAddTool = new javax.swing.JButton();
        topButtonEditTool = new javax.swing.JButton();
        topButtonDeleteTool = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        labelDiameter = new javax.swing.JLabel();
        labelRPM = new javax.swing.JLabel();
        labelFeedrate = new javax.swing.JLabel();
        tabPanel = new javax.swing.JPanel();
        labelPanelLength = new javax.swing.JLabel();
        labelPanelWidth = new javax.swing.JLabel();
        labelPanelUnit = new javax.swing.JLabel();
        TextFieldPanelWidth = new javax.swing.JTextField();
        RadioButtonPanelMetric = new javax.swing.JRadioButton();
        RadioButtonPanelImperial = new javax.swing.JRadioButton();
        labelPanelLength1 = new javax.swing.JLabel();
        TextFieldPanelThickness = new javax.swing.JTextField();
        TextFieldPanelHeight = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        tabButtonAddPanel = new javax.swing.JButton();
        tabButtonEditPanel = new javax.swing.JButton();
        tabButtonDeletePanel = new javax.swing.JButton();
        tabDepth = new javax.swing.JPanel();
        RadioButtonThroughAll = new javax.swing.JRadioButton();
        RadioButtonCustom = new javax.swing.JRadioButton();
        LabelOffset = new javax.swing.JLabel();
        TextFieldDepth = new javax.swing.JTextField();
        TextFieldOffset = new javax.swing.JTextField();
        LabelUnit = new javax.swing.JLabel();
        ButtonSaveDepth = new javax.swing.JButton();
        tabBorderCut = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        pannelTabs2 = new javax.swing.JTabbedPane();
        tabInfos = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        PanelInfoCursor = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        labelYposCursor = new javax.swing.JLabel();
        labelXposCursor = new javax.swing.JLabel();
        labelSize = new javax.swing.JLabel();
        labelSizeData = new javax.swing.JLabel();
        PanelGrid = new javax.swing.JPanel();
        drawingPanel = new MasterCut.gui.panel.DrawingPanel(this);
        topMenuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        menuImport = new javax.swing.JMenu();
        itemMenuImportPannel = new javax.swing.JMenuItem();
        itemMenuImportTools = new javax.swing.JMenuItem();
        menuSave = new javax.swing.JMenu();
        itemMenuSavePannel = new javax.swing.JMenuItem();
        itemMenuSaveTools = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        itemMenuQuit = new javax.swing.JMenuItem();

        jMenu1.setText("jMenu1");

        jMenu2.setText("jMenu2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelMain.setLayout(new java.awt.BorderLayout());

        panelTopButtons.setMaximumSize(new java.awt.Dimension(35767, 32767));
        panelTopButtons.setPreferredSize(new java.awt.Dimension(400, 35));

        topButtonToolsList.setToolTipText("");
        topButtonToolsList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topButtonToolsListActionPerformed(evt);
            }
        });
        panelTopButtons.add(topButtonToolsList);

        topButtonRedo.setText("Redo");
        topButtonRedo.setFocusable(false);
        topButtonRedo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topButtonRedoActionPerformed(evt);
            }
        });
        panelTopButtons.add(topButtonRedo);

        topButtonUndo.setText("Undo");
        topButtonUndo.setFocusable(false);
        topButtonUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topButtonUndoActionPerformed(evt);
            }
        });
        panelTopButtons.add(topButtonUndo);

        InputPanelGrid.setText("50");
        InputPanelGrid.setMaximumSize(new java.awt.Dimension(64, 22));
        InputPanelGrid.setPreferredSize(new java.awt.Dimension(100, 22));
        InputPanelGrid.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                InputPanelGridKeyReleased(evt);
            }
        });
        panelTopButtons.add(InputPanelGrid);

        ToggleButtonSnapToGrid.setText("Snap to Grid");
        ToggleButtonSnapToGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ToggleButtonSnapToGridActionPerformed(evt);
            }
        });
        panelTopButtons.add(ToggleButtonSnapToGrid);

        ToggleButtonShowGrid.setSelected(true);
        ToggleButtonShowGrid.setText("Show Grid");
        ToggleButtonShowGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ToggleButtonShowGridActionPerformed(evt);
            }
        });
        panelTopButtons.add(ToggleButtonShowGrid);

        labelInfo.setText("Info: ");
        panelTopButtons.add(labelInfo);
        panelTopButtons.add(labelInfoData);

        panelMain.add(panelTopButtons, java.awt.BorderLayout.NORTH);

        panelLeft.setPreferredSize(new java.awt.Dimension(250, 800));

        pannelTabs.setToolTipText("");
        pannelTabs.setName(""); // NOI18N
        pannelTabs.setPreferredSize(new java.awt.Dimension(250, 200));

        toolList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolListActionPerformed(evt);
            }
        });

        topButtonAddTool.setText("New");
        topButtonAddTool.setFocusable(false);
        topButtonAddTool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topButtonAddToolActionPerformed(evt);
            }
        });
        jPanel2.add(topButtonAddTool);

        topButtonEditTool.setText("Edit");
        topButtonEditTool.setFocusable(false);
        topButtonEditTool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topButtonEditToolActionPerformed(evt);
            }
        });
        jPanel2.add(topButtonEditTool);

        topButtonDeleteTool.setText("Delete");
        topButtonDeleteTool.setEnabled(false);
        topButtonDeleteTool.setFocusable(false);
        topButtonDeleteTool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topButtonDeleteToolActionPerformed(evt);
            }
        });
        jPanel2.add(topButtonDeleteTool);

        jLabel2.setText("Feedrate :");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel3.setText("Tool name :");

        jLabel4.setText("Diameter :");
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel5.setText("RPM :");
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        labelDiameter.setText("10");

        labelRPM.setText("1000");

        labelFeedrate.setText("1000");

        javax.swing.GroupLayout tabToolsLayout = new javax.swing.GroupLayout(tabTools);
        tabTools.setLayout(tabToolsLayout);
        tabToolsLayout.setHorizontalGroup(
            tabToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabToolsLayout.createSequentialGroup()
                .addGroup(tabToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(tabToolsLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(tabToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(tabToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(toolList, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelDiameter)
                            .addComponent(labelRPM)
                            .addComponent(labelFeedrate))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        tabToolsLayout.setVerticalGroup(
            tabToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabToolsLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(tabToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(toolList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(tabToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(labelDiameter))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(labelRPM))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(labelFeedrate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pannelTabs.addTab("Tools", tabTools);

        tabPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labelPanelLength.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelPanelLength.setText("Height:");
        labelPanelLength.setToolTipText("");
        labelPanelLength.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        tabPanel.add(labelPanelLength, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, -1, -1));

        labelPanelWidth.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelPanelWidth.setText("Width :");
        labelPanelWidth.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        tabPanel.add(labelPanelWidth, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, -1));

        labelPanelUnit.setText("Unit:");
        labelPanelUnit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tabPanel.add(labelPanelUnit, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 90, -1, -1));

        TextFieldPanelWidth.setMaximumSize(new java.awt.Dimension(64, 22));
        TextFieldPanelWidth.setPreferredSize(new java.awt.Dimension(100, 22));
        tabPanel.add(TextFieldPanelWidth, new org.netbeans.lib.awtextra.AbsoluteConstraints(76, 34, 90, -1));

        unitGroup.add(RadioButtonPanelMetric);
        RadioButtonPanelMetric.setText("Metric - mm");
        RadioButtonPanelMetric.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadioButtonPanelMetricActionPerformed(evt);
            }
        });
        tabPanel.add(RadioButtonPanelMetric, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 112, -1, -1));

        unitGroup.add(RadioButtonPanelImperial);
        RadioButtonPanelImperial.setSelected(true);
        RadioButtonPanelImperial.setText("Imperial - in");
        RadioButtonPanelImperial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadioButtonPanelImperialActionPerformed(evt);
            }
        });
        tabPanel.add(RadioButtonPanelImperial, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 112, -1, -1));

        labelPanelLength1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelPanelLength1.setText("Thickness:");
        labelPanelLength1.setToolTipText("");
        labelPanelLength1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        tabPanel.add(labelPanelLength1, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 65, -1, -1));

        TextFieldPanelThickness.setMaximumSize(new java.awt.Dimension(64, 22));
        TextFieldPanelThickness.setPreferredSize(new java.awt.Dimension(100, 22));
        tabPanel.add(TextFieldPanelThickness, new org.netbeans.lib.awtextra.AbsoluteConstraints(76, 62, 90, -1));

        TextFieldPanelHeight.setMaximumSize(new java.awt.Dimension(64, 22));
        TextFieldPanelHeight.setPreferredSize(new java.awt.Dimension(100, 22));
        tabPanel.add(TextFieldPanelHeight, new org.netbeans.lib.awtextra.AbsoluteConstraints(76, 6, 90, -1));

        tabButtonAddPanel.setText("New");
        tabButtonAddPanel.setEnabled(false);
        tabButtonAddPanel.setFocusable(false);
        tabButtonAddPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabButtonAddPanelActionPerformed(evt);
            }
        });
        jPanel1.add(tabButtonAddPanel);

        tabButtonEditPanel.setText("Save");
        tabButtonEditPanel.setFocusable(false);
        tabButtonEditPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabButtonEditPanelActionPerformed(evt);
            }
        });
        jPanel1.add(tabButtonEditPanel);

        tabButtonDeletePanel.setText("Delete");
        tabButtonDeletePanel.setFocusable(false);
        tabButtonDeletePanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabButtonDeletePanelActionPerformed(evt);
            }
        });
        jPanel1.add(tabButtonDeletePanel);

        tabPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 137, 250, -1));

        pannelTabs.addTab("Panel", tabPanel);

        tabDepth.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        depthGroup.add(RadioButtonThroughAll);
        RadioButtonThroughAll.setSelected(true);
        RadioButtonThroughAll.setText("Through all (with offset)");
        RadioButtonThroughAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadioButtonThroughAllActionPerformed(evt);
            }
        });
        tabDepth.add(RadioButtonThroughAll, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 18, -1, -1));

        depthGroup.add(RadioButtonCustom);
        RadioButtonCustom.setText("Custom :");
        RadioButtonCustom.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        RadioButtonCustom.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        RadioButtonCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadioButtonCustomActionPerformed(evt);
            }
        });
        tabDepth.add(RadioButtonCustom, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 46, -1, -1));

        LabelOffset.setText("Offset :");
        LabelOffset.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        tabDepth.add(LabelOffset, new org.netbeans.lib.awtextra.AbsoluteConstraints(37, 75, -1, -1));
        LabelOffset.getAccessibleContext().setAccessibleDescription("");

        TextFieldDepth.setMaximumSize(new java.awt.Dimension(64, 22));
        TextFieldDepth.setPreferredSize(new java.awt.Dimension(100, 22));
        tabDepth.add(TextFieldDepth, new org.netbeans.lib.awtextra.AbsoluteConstraints(88, 45, 110, -1));

        TextFieldOffset.setText("2");
        TextFieldOffset.setMaximumSize(new java.awt.Dimension(64, 22));
        TextFieldOffset.setPreferredSize(new java.awt.Dimension(100, 22));
        tabDepth.add(TextFieldOffset, new org.netbeans.lib.awtextra.AbsoluteConstraints(88, 73, 110, -1));

        LabelUnit.setText("Unit are in: ");
        tabDepth.add(LabelUnit, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, -1, -1));

        ButtonSaveDepth.setText("Save");
        ButtonSaveDepth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonSaveDepthActionPerformed(evt);
            }
        });
        tabDepth.add(ButtonSaveDepth, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 130, -1, -1));

        pannelTabs.addTab("Depth", tabDepth);

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Distance from panel : ");

        jButton1.setText("Save");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tabBorderCutLayout = new javax.swing.GroupLayout(tabBorderCut);
        tabBorderCut.setLayout(tabBorderCutLayout);
        tabBorderCutLayout.setHorizontalGroup(
            tabBorderCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabBorderCutLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabBorderCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1)
                    .addGroup(tabBorderCutLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(53, Short.MAX_VALUE))
        );
        tabBorderCutLayout.setVerticalGroup(
            tabBorderCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabBorderCutLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(tabBorderCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap(69, Short.MAX_VALUE))
        );

        pannelTabs.addTab("Border Cut", tabBorderCut);

        panelLeft.add(pannelTabs);

        pannelTabs2.setPreferredSize(new java.awt.Dimension(250, 350));
        pannelTabs2.addTab("Infos", tabInfos);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(250, 350));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Tools");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Tool 1");
        javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Cut 1");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Cut 3");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Cut 7");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Tool 2");
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Cut 2");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Cut 4");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Tool 3");
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Cut 5");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Cut 6");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree1.setMaximumSize(new java.awt.Dimension(3000, 3000));
        jTree1.setName(""); // NOI18N
        jTree1.setPreferredSize(new java.awt.Dimension(230, 250));
        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTree1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);
        jTree1.getAccessibleContext().setAccessibleName("");

        pannelTabs2.addTab("Objects", jScrollPane1);

        panelLeft.add(pannelTabs2);

        PanelInfoCursor.setPreferredSize(new java.awt.Dimension(250, 125));
        PanelInfoCursor.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Cursor Information");
        jLabel8.setPreferredSize(new java.awt.Dimension(250, 16));
        PanelInfoCursor.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, -1, -1));

        labelYposCursor.setText("Y : 0");
        PanelInfoCursor.add(labelYposCursor, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        labelXposCursor.setText("X : 0");
        PanelInfoCursor.add(labelXposCursor, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, -1));

        labelSize.setText("Dimensions of the resulting panel:");
        PanelInfoCursor.add(labelSize, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        labelSizeData.setText("2x2");
        PanelInfoCursor.add(labelSizeData, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, -1, -1));

        panelLeft.add(PanelInfoCursor);

        PanelGrid.setPreferredSize(new java.awt.Dimension(250, 100));
        panelLeft.add(PanelGrid);

        panelMain.add(panelLeft, java.awt.BorderLayout.WEST);

        drawingPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                drawingPanelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout drawingPanelLayout = new javax.swing.GroupLayout(drawingPanel);
        drawingPanel.setLayout(drawingPanelLayout);
        drawingPanelLayout.setHorizontalGroup(
            drawingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1094, Short.MAX_VALUE)
        );
        drawingPanelLayout.setVerticalGroup(
            drawingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 721, Short.MAX_VALUE)
        );

        panelMain.add(drawingPanel, java.awt.BorderLayout.CENTER);

        menuFile.setText("File");
        menuFile.add(jSeparator2);

        menuImport.setText("Import");

        itemMenuImportPannel.setText("Pannel");
        itemMenuImportPannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuImportPannelActionPerformed(evt);
            }
        });
        menuImport.add(itemMenuImportPannel);

        itemMenuImportTools.setText("Tools configuration");
        itemMenuImportTools.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuImportToolsActionPerformed(evt);
            }
        });
        menuImport.add(itemMenuImportTools);

        menuFile.add(menuImport);

        menuSave.setText("Save");

        itemMenuSavePannel.setText("Panel");
        itemMenuSavePannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuSavePannelActionPerformed(evt);
            }
        });
        menuSave.add(itemMenuSavePannel);

        itemMenuSaveTools.setText("Tools configuration");
        itemMenuSaveTools.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuSaveToolsActionPerformed(evt);
            }
        });
        menuSave.add(itemMenuSaveTools);

        menuFile.add(menuSave);

        jMenuItem1.setText("Export");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        menuFile.add(jMenuItem1);
        menuFile.add(jSeparator1);

        itemMenuQuit.setText("Quit");
        itemMenuQuit.setActionCommand("menuQuit");
        itemMenuQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuQuitActionPerformed(evt);
            }
        });
        menuFile.add(itemMenuQuit);

        topMenuBar.add(menuFile);

        setJMenuBar(topMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, 756, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void itemMenuQuitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_itemMenuQuitActionPerformed
	{//GEN-HEADEREND:event_itemMenuQuitActionPerformed
		System.exit(0);
	}//GEN-LAST:event_itemMenuQuitActionPerformed

	private void topButtonRedoActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_topButtonRedoActionPerformed
		controller.redo();
		drawingPanel.repaint();
		repaint();
                updateToolComboBox();

	}//GEN-LAST:event_topButtonRedoActionPerformed

	private void topButtonUndoActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_topButtonUndoActionPerformed
		controller.undo();
		updateToolComboBox();
		initToolsTree();
		drawingPanel.repaint();
		updatePanelInfo(controller.getItemSelectedDTO());
	}//GEN-LAST:event_topButtonUndoActionPerformed

	private void RadioButtonThroughAllActionPerformed(java.awt.event.ActionEvent evt)
	{
	}

	private void RadioButtonCustomActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_RadioButtonCustomActionPerformed
	{//GEN-HEADEREND:event_RadioButtonCustomActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_RadioButtonCustomActionPerformed

	private void drawingPanelMouseExited(java.awt.event.MouseEvent evt)
	{//GEN-FIRST:event_drawingPanelMouseExited
		PanelDTO panelDTO = controller.getPanelDTO();

		if (panelDTO != null)
		{
			Unit usedUnit = panelDTO.usedUnit;
			String unitSuffix = (usedUnit == Unit.IMPERIAL) ? " in" : " mm";
			labelXposCursor.setText("X:  --- " + unitSuffix);
			labelYposCursor.setText("Y:  --- " + unitSuffix);
		} else
		{
			labelXposCursor.setText("X: -");
			labelYposCursor.setText("Y: -");
		}

	}//GEN-LAST:event_drawingPanelMouseExited

	private void setGridSize(JTextField textField)
	{
		try
		{
			String input = textField.getText().trim();
			Unit defaultUnit = RadioButtonPanelMetric.isSelected() ? Unit.METRIC : Unit.IMPERIAL;
			TextParser.Result result = TextParser.parseInputWithUnits(input, defaultUnit); // Ajout de `defaultUnit`
			double valueInMm = result.getValueInMm();
			Unit detectedUnit = result.getUnit();

			textField.setBackground(Color.WHITE);
			controller.setUsedUnit(detectedUnit);
			controller.setGridSize((int) Math.round(valueInMm));
			String unitSuffix = (detectedUnit == Unit.IMPERIAL) ? " in" : " mm";
			labelXposCursor.setText("X:  --- " + unitSuffix);
			labelYposCursor.setText("Y:  --- " + unitSuffix);
			drawingPanel.repaint();
		} catch (NumberFormatException ex)
		{
			textField.setBackground(Color.PINK);
		}
	}

	private void InputPanelGridKeyReleased(java.awt.event.KeyEvent evt)
	{//GEN-FIRST:event_InputPanelGridKeyReleased
		setGridSize(InputPanelGrid);
	}//GEN-LAST:event_InputPanelGridKeyReleased

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_jButton1ActionPerformed
		PanelDTO panel = controller.getPanelDTO();
		Double distanceFromPanel;
            Point2D.Double topLeftCorner = panel.borders.get(1).start.point;
            Point2D.Double bottomLeftCorner = panel.borders.get(0).start.point;
            Point2D.Double bottomRightCorner = panel.borders.get(0).end.point;
		try
		{
                    distanceFromPanel = Double.valueOf(jTextField1.getText());

		} catch (NumberFormatException e)
		{
                    System.out.println("Enter a distance");
			return;
		}
            if (distanceFromPanel <= 0)		{
                    System.out.println("Enter a valid distance");
			return;
		}
            if (distanceFromPanel >= (panel.borders.get(0).end.point.x) / 2.0d)		{
                    System.out.println("Distance is too big");
			return;
            }
            Double height = topLeftCorner.getY() - bottomLeftCorner.getY() - (2 * distanceFromPanel);
            double length = bottomRightCorner.getX() - bottomLeftCorner.getX() - (2 * distanceFromPanel);
		controller.addCut(new DimensionsDTO(length, height));
		drawingPanel.repaint();
	}//GEN-LAST:event_jButton1ActionPerformed

	private void itemMenuImportPannelActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_itemMenuImportPannelActionPerformed
		JFileChooser j = new JFileChooser();
		int valide = FileChooserDialog.ShowOpenFileChooser(this, j);
		if (valide == JFileChooser.APPROVE_OPTION)
		{
			String path = GetPath(j);
			
			try
			{
                            controller.LoadPanel(path);
			} catch (IOException e)
			{
				System.out.println(e);
			}
                        updatePanelInfo(controller.getPanelDTO());
                        drawingPanel.repaint();
                        updateToolComboBox();

			/*





			 */
			// ALEXIS CHECK SI LES CUT.TOOL PEUVENT ETRE PASSER DIRECTEMENT EN ARGUMENT OU SINON ON VA DEVOIR FAIRE UN CONSTRUCTEUR PAR COPIE DE DTO
			/*
			*
			*
			*
			*
			* */
 /*PanelDTO paneldto = controller.getPanelDTO();
			var mapdto = paneldto.cutsMap;
			var map = panel.cutsMap;
			var list = controller.getToolsDTO();
			java.util.List<ToolDTO> list2 = new ArrayList<>();
			for (Map.Entry<UUID, BaseCutDTO> entry : map.entrySet())
			{
				for (RegularCutDTO cut : entry.getValue().getRegularCuts())
				{
					if ((!(list.contains(cut.tool))) && (!(list2.contains(cut.tool))))
					{
						list2.add((cut.tool));
					}
				}
			}
			int total = list2.size() + controller.getToolsSize();
			if (total > 12)
			{
				System.out.println("You need to remove at least " + (total - 12) + " tools to import this panel");
			} else
			{
				for (Map.Entry<UUID, BaseCutDTO> entry : mapdto.entrySet())
				{
					controller.removeCut(entry.getValue());
				}
				for (ToolDTO tool : list2)
				{
					controller.addTool(tool);
					addToolToList(tool);
				}
				for (Map.Entry<UUID, BaseCutDTO> entry : map.entrySet())
				{
					for (RegularCutDTO cut : entry.getValue().getRegularCuts())
					{
						controller.addCut(cut);
					}
				}
				drawingPanel.repaint();
			}*/
            }
	}//GEN-LAST:event_itemMenuImportPannelActionPerformed

	private void itemMenuImportToolsActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_itemMenuImportToolsActionPerformed
		JFileChooser j = new JFileChooser();
		int valide = FileChooserDialog.ShowOpenFileChooser(this, j);
		if (valide == JFileChooser.APPROVE_OPTION)
		{
			String path = GetPath(j);
			SaveManager file = new SaveManager();
			java.util.List<ToolDTO> list = new ArrayList<>();
			try
			{
				list = file.LoadTools(path);
			} catch (IOException e)
			{
				System.out.println(e);
			}
			java.util.List<ToolDTO> list2 = controller.getToolsDTO();
			for (int i = 0; i < list2.size(); i++)
			{
				controller.removeTool(list2.get(i));
				removeToolToList(list2.get(i));
			}
			for (ToolDTO tool : list)
			{
				controller.addTool(tool);
				addToolToList(tool);
			}

		}
	}//GEN-LAST:event_itemMenuImportToolsActionPerformed

	private void itemMenuSavePannelActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_itemMenuSavePannelActionPerformed
		JFileChooser j = new JFileChooser();
		int valide = FileChooserDialog.ShowSaveFileChooser(this, j);
		if (valide == JFileChooser.APPROVE_OPTION)
		{
                    String path = GetPath(j);
			try
			{
                            controller.SavePanel(path);
			} catch (IOException e)
			{
				System.out.println(e);
			}
		}
	}//GEN-LAST:event_itemMenuSavePannelActionPerformed

	private void itemMenuSaveToolsActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_itemMenuSaveToolsActionPerformed
		JFileChooser j = new JFileChooser();
		int valide = FileChooserDialog.ShowSaveFileChooser(this, j);
		if (valide == JFileChooser.APPROVE_OPTION)
		{
			String path = GetPath(j);
			SaveManager save = new SaveManager();
			try
			{
				save.SaveTools(controller.getToolsDTO(), path);
			} catch (IOException e)
			{
				System.out.println(e);
			}
		}

	}//GEN-LAST:event_itemMenuSaveToolsActionPerformed

	private void jTree1MouseClicked(java.awt.event.MouseEvent evt)
	{//GEN-FIRST:event_jTree1MouseClicked
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();

		if (selectedNode != null)
		{
			String nodeText = selectedNode.toString();
			System.out.println("Texte du nœud sélectionné : " + nodeText);

			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
			if (parentNode != null)
			{
				String parentText = parentNode.toString();
				System.out.println("Texte du nœud parent : " + parentText);
				controller.SetSelectionCut(nodeText, parentText, true);
				drawingPanel.repaint();
				// exit remet couleur par defaut et switch dans le info pannel
			}
		}
	}//GEN-LAST:event_jTree1MouseClicked

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        JFileChooser j = new JFileChooser();
		int valide = FileChooserDialog.ShowSaveFileChooser(this, j);
		if (valide == JFileChooser.APPROVE_OPTION)
		{
                    String path = GetPath(j);
			try
			{
                            controller.generateGCode(path);
			} catch (IOException e)
			{
				System.out.println(e);
			}
		}
    }//GEN-LAST:event_jMenuItem1ActionPerformed

	private void ButtonSaveDepthActionPerformed(java.awt.event.ActionEvent evt)
	{
		if (RadioButtonThroughAll.isSelected())
		{
			try
			{
				String offsetText = TextFieldOffset.getText();
				double offset = Double.parseDouble(offsetText);
				Unit usedUnit = controller.getPanelDTO().usedUnit;

				if (usedUnit == Unit.IMPERIAL)
					offset = UnitConverter.convertToMetric(offset);

				setCutDepthThroughAll();

			} catch (NumberFormatException e)
			{
				JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else
		{
			try
			{
				String depthText = TextFieldDepth.getText();
				double depth = Double.parseDouble(depthText);
				Unit usedUnit = controller.getPanelDTO().usedUnit;

				if (usedUnit == Unit.IMPERIAL)
					depth = UnitConverter.convertToMetric(depth);

				setCutDepthCustom(depth);

			} catch (NumberFormatException e)
			{
				JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	private void topButtonToolsListActionPerformed(java.awt.event.ActionEvent evt)
	{
		setActiveTool();
	}

	private void topButtonAddCutActionPerformed(java.awt.event.ActionEvent evt)
	{
		// TODO add your handling code here:
	}

	private void topButtonAddToolActionPerformed(java.awt.event.ActionEvent evt)
	{
		AddToolDialog addToolDialog = new AddToolDialog(this, new ToolDTO());
		addToolDialog.setVisible(true);

		ToolDTO newTool = addToolDialog.getTool();
		if (newTool != null)
		{
			controller.addTool(newTool);
			addToolToList(newTool);
			toolList.setSelectedItem(newTool);
			topButtonDeleteTool.setEnabled(true);
			if (controller.getToolsSize() >= 12)
				topButtonAddTool.setEnabled(false);
		}
	}

	private void RadioButtonPanelImperialActionPerformed(java.awt.event.ActionEvent evt)
	{
		PanelDTO panelDTO = controller.getPanelDTO();
		if (panelDTO != null)
		{
			DimensionsDTO dimensions = panelDTO.dimensions;
			System.out.println("Unité changée en IMPERIAL dans PanelDTO"); // Debug
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
			DecimalFormat df = new DecimalFormat("#.##", symbols);
			TextFieldPanelHeight.setText(String.valueOf(df.format(UnitConverter.convertToImperial(dimensions.height))));
			TextFieldPanelWidth.setText(String.valueOf(df.format(UnitConverter.convertToImperial(dimensions.width))));
			TextFieldPanelThickness.setText(String.valueOf(df.format(UnitConverter.convertToImperial(panelDTO.thickness))));
			InputPanelGrid.setText(String.valueOf(df.format(UnitConverter.convertToImperial(Double.parseDouble(InputPanelGrid.getText())))));
			labelXposCursor.setText("X:  --- in");
			labelYposCursor.setText("Y:  --- in");
		} else
		{
			System.out.println("PanelDTO est null");
		}
	}

	private void tabButtonAddPanelActionPerformed(java.awt.event.ActionEvent evt)
	{
		String widthText = TextFieldPanelWidth.getText();
		String heightText = TextFieldPanelHeight.getText();
		String thicknessText = TextFieldPanelThickness.getText();
                
                Unit defaultUnit = RadioButtonPanelMetric.isSelected() ? Unit.METRIC : Unit.IMPERIAL;
                TextParser.Result resultwidth  = TextParser.parseInputWithUnits(widthText, defaultUnit); 
                TextParser.Result resultheight = TextParser.parseInputWithUnits(heightText, defaultUnit); 
                TextParser.Result resultthickness = TextParser.parseInputWithUnits(thicknessText, defaultUnit); 

		Unit usedUnit = RadioButtonPanelMetric.isSelected() ? Unit.METRIC : Unit.IMPERIAL;
                double width = -1;
                double height = -1;
                double thickness = -1;
		try
		{     
			width = resultwidth .getValueInMm();
			height = resultheight.getValueInMm();
			thickness = resultthickness.getValueInMm();
		} catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this, "Please enter valid values.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
                if (width > -1 && height > -1 && thickness > -1) { 
		controller.createPanel(new DimensionsDTO(width, height), thickness, usedUnit);
		tabButtonAddPanel.setEnabled(false);
		tabButtonDeletePanel.setEnabled(true);
		tabButtonEditPanel.setEnabled(true);
		drawingPanel.repaint();}
	}

	private void tabButtonDeletePanelActionPerformed(java.awt.event.ActionEvent evt)
	{
		if (controller.doesPanelExist())
		{
			controller.removePanel();
			tabButtonAddPanel.setEnabled(true);
			tabButtonDeletePanel.setEnabled(false);
			tabButtonEditPanel.setEnabled(false);
			this.repaint();
		} else
		{
			JOptionPane.showMessageDialog(this, "No panel to remove.", "Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void tabButtonEditPanelActionPerformed(java.awt.event.ActionEvent evt)
	{
		String widthText = TextFieldPanelWidth.getText();
		String heightText = TextFieldPanelHeight.getText();
		String thicknessText = TextFieldPanelThickness.getText();

                Unit defaultUnit = RadioButtonPanelMetric.isSelected() ? Unit.METRIC : Unit.IMPERIAL;
                TextParser.Result resultwidth  = TextParser.parseInputWithUnits(widthText, defaultUnit); // Ajout de `defaultUnit`
                TextParser.Result resultheight = TextParser.parseInputWithUnits(heightText, defaultUnit); // Ajout de `defaultUnit`
                TextParser.Result resultthickness = TextParser.parseInputWithUnits(thicknessText, defaultUnit); // Ajout de `defaultUnit`

		Unit usedUnit = RadioButtonPanelMetric.isSelected() ? Unit.METRIC : Unit.IMPERIAL;
                double width = -1;
                double height = -1;
                double thickness = -1;
		try
		{     
			width = resultwidth .getValueInMm();
			height = resultheight.getValueInMm();
			thickness = resultthickness.getValueInMm();
		} catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this, "Please enter valid values.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
                if (width > -1 && height > -1 && thickness > -1) { 

		if (RadioButtonPanelMetric.isSelected())
		{
			usedUnit = Unit.METRIC;
		} else
		{
			usedUnit = Unit.IMPERIAL;
		}

		controller.updatePanel(new DimensionsDTO(width, height), thickness, usedUnit);
		updatePanelDepth();
		drawingPanel.repaint();}
	}

	private void topButtonEditToolActionPerformed(java.awt.event.ActionEvent evt)
	{      
		if (toolList.getSelectedIndex() != -1)
		{
			ToolDTO toolDTO = toolList.getItemAt(toolList.getSelectedIndex());

			AddToolDialog addToolDialog = new AddToolDialog(this, toolDTO);
			addToolDialog.setVisible(true);

			ToolDTO updatedTool = addToolDialog.getTool();

			if (updatedTool == null)
			{
				return;
			}
			if (controller.updateTool(updatedTool))
			{
				toolList.updateUI();
				topButtonToolsList.updateUI();
                                var map = controller.getPanelDTO().cutsMap;
                                    for (Map.Entry<UUID, BaseCutDTO> entry : map.entrySet())
                                    {   
//                                        if (entry.getValue().name.equals("RectangularCut") && entry.getValue().tool.uuid.equals(updatedTool.uuid))
//                                        {
//                                            controller.removeCut(entry.getValue());
//                                            RectangularCutDTO cut = controller.updateRectangularCutFromTool(updatedTool, entry.getValue());
//                                            controller.addCut(cut);
//                                        }
//
//                                        if (entry.getValue().name.equals("LCut") && entry.getValue().tool.uuid.equals(updatedTool.uuid))
//                                        {
//                                            controller.updateLcutFromTool(updatedTool, entry.getValue());
//                                        }
                                }
                                drawingPanel.repaint();

			} else
			{
				JOptionPane.showMessageDialog(this, "Tool not found.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else
		{
			JOptionPane.showMessageDialog(this, "Please select a tool to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void toolListActionPerformed(java.awt.event.ActionEvent evt)
	{
		ToolDTO selectedTool = (ToolDTO) toolList.getSelectedItem();
		showToolInfo(selectedTool);
		topButtonToolsList.setSelectedItem(selectedTool);
	}


	private void RadioButtonPanelMetricActionPerformed(java.awt.event.ActionEvent evt)
	{
		PanelDTO panelDTO = controller.getPanelDTO();
		DimensionsDTO dimensions = panelDTO.dimensions;
		DecimalFormat df = new DecimalFormat("#.##");

		TextFieldPanelHeight.setText(df.format(dimensions.height));
		TextFieldPanelWidth.setText(df.format(dimensions.width));
		TextFieldPanelThickness.setText(df.format(panelDTO.thickness));
		InputPanelGrid.setText(String.valueOf((int) Math.round(UnitConverter.convertToMetric(Double.parseDouble(InputPanelGrid.getText())))));
		labelXposCursor.setText("X:  --- mm");
		labelYposCursor.setText("Y:  --- mm");
	}

	private void ToggleButtonShowGridActionPerformed(java.awt.event.ActionEvent evt)
	{
		boolean showGrid = ToggleButtonShowGrid.isSelected();
		controller.showGrid(showGrid);
		drawingPanel.repaint();

		ToggleButtonSnapToGrid.setEnabled(showGrid);


		if (!showGrid)
		{
			ToggleButtonSnapToGrid.setSelected(false);
			controller.setGridMagnetic(false);
		}
	}

	private void ToggleButtonSnapToGridActionPerformed(java.awt.event.ActionEvent evt)
	{
		boolean snapToGridEnabled = ToggleButtonSnapToGrid.isSelected();
		controller.setGridMagnetic(snapToGridEnabled); // Active/désactive la grille magnétique
		System.out.println("Grille magnétique " + (snapToGridEnabled ? "activée" : "désactivée"));
	}

	private void topButtonDeleteToolActionPerformed(java.awt.event.ActionEvent evt)
	{
		if (toolList.getSelectedIndex() != -1)
		{
			ToolDTO tool = toolList.getItemAt(toolList.getSelectedIndex());
			if (controller.removeTool(tool))
			{
				removeToolToList(tool);
				if (controller.getToolsSize() <= 1)
					topButtonDeleteTool.setEnabled(false);
				if (controller.getToolsSize() < 12)
					topButtonAddTool.setEnabled(true);
			} else
			{
				JOptionPane.showMessageDialog(this, "Tool not found.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else
		{
			JOptionPane.showMessageDialog(this, "Please select a tool to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
		}
		repaint();

	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ButtonSaveDepth;
    private javax.swing.JTextField InputPanelGrid;
    private javax.swing.JLabel LabelOffset;
    private javax.swing.JLabel LabelUnit;
    private javax.swing.JPanel PanelGrid;
    private javax.swing.JPanel PanelInfoCursor;
    private javax.swing.JRadioButton RadioButtonCustom;
    private javax.swing.JRadioButton RadioButtonPanelImperial;
    private javax.swing.JRadioButton RadioButtonPanelMetric;
    private javax.swing.JRadioButton RadioButtonThroughAll;
    private javax.swing.JTextField TextFieldDepth;
    private javax.swing.JTextField TextFieldOffset;
    private javax.swing.JTextField TextFieldPanelHeight;
    private javax.swing.JTextField TextFieldPanelThickness;
    private javax.swing.JTextField TextFieldPanelWidth;
    private javax.swing.JToggleButton ToggleButtonShowGrid;
    private javax.swing.JToggleButton ToggleButtonSnapToGrid;
    private javax.swing.ButtonGroup depthGroup;
    private MasterCut.gui.panel.DrawingPanel drawingPanel;
    private javax.swing.JMenuItem itemMenuImportPannel;
    private javax.swing.JMenuItem itemMenuImportTools;
    private javax.swing.JMenuItem itemMenuQuit;
    private javax.swing.JMenuItem itemMenuSavePannel;
    private javax.swing.JMenuItem itemMenuSaveTools;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTree jTree1;
    private javax.swing.JLabel labelDiameter;
    private javax.swing.JLabel labelFeedrate;
    private javax.swing.JLabel labelInfo;
    private javax.swing.JLabel labelInfoData;
    private javax.swing.JLabel labelPanelLength;
    private javax.swing.JLabel labelPanelLength1;
    private javax.swing.JLabel labelPanelUnit;
    private javax.swing.JLabel labelPanelWidth;
    private javax.swing.JLabel labelRPM;
    private javax.swing.JLabel labelSize;
    private javax.swing.JLabel labelSizeData;
    private javax.swing.JLabel labelXposCursor;
    private javax.swing.JLabel labelYposCursor;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuImport;
    private javax.swing.JMenu menuSave;
    private javax.swing.JPanel panelLeft;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelTopButtons;
    private javax.swing.JTabbedPane pannelTabs;
    private javax.swing.JTabbedPane pannelTabs2;
    private javax.swing.JPanel tabBorderCut;
    private javax.swing.JButton tabButtonAddPanel;
    private javax.swing.JButton tabButtonDeletePanel;
    private javax.swing.JButton tabButtonEditPanel;
    private javax.swing.JPanel tabDepth;
    private javax.swing.JPanel tabInfos;
    private javax.swing.JPanel tabPanel;
    private javax.swing.JPanel tabTools;
    private javax.swing.JComboBox<ToolDTO> toolList;
    private javax.swing.JButton topButtonAddTool;
    private javax.swing.JButton topButtonDeleteTool;
    private javax.swing.JButton topButtonEditTool;
    private javax.swing.JButton topButtonRedo;
    private javax.swing.JComboBox<ToolDTO> topButtonToolsList;
    private javax.swing.JButton topButtonUndo;
    private javax.swing.JMenuBar topMenuBar;
    private javax.swing.ButtonGroup unitGroup;
    private javax.swing.ButtonGroup unitGroupTool;
    // End of variables declaration//GEN-END:variables

	private InfoPanel infoPanel;

	public boolean isSnapToGridEnabled()
	{
		return controller.isGridMagnetic();
	}

	public void toggleMagneticGrid(boolean enable)
	{
		controller.setGridMagnetic(enable);
	}

	public void setGridSize(int size)
	{
		controller.setGridSize(size);
	}

        public void initToolsTree() {
            DefaultMutableTreeNode root = controller.getToolsTree();
            jTree1.setModel(new javax.swing.tree.DefaultTreeModel(root));
        }
    private void updateToolComboBox() {
        ActionListener[] listeners = topButtonToolsList.getActionListeners();
        for (ActionListener listener : listeners) {
            topButtonToolsList.removeActionListener(listener);
        }

		toolList.removeAllItems();
		topButtonToolsList.removeAllItems();

		java.util.List<ToolDTO> toolsDTO = controller.getToolsDTO();

		for (ToolDTO toolDTO : toolsDTO)
		{
			toolList.addItem(toolDTO);
			topButtonToolsList.addItem(toolDTO);
		}

		ToolDTO activeTool = controller.getActiveToolDTO();
		if (activeTool != null)
		{
			toolList.setSelectedItem(activeTool);
			topButtonToolsList.setSelectedItem(activeTool);
		} else
		{
			toolList.setSelectedIndex(-1);
			topButtonToolsList.setSelectedIndex(-1);
		}

		showToolInfo(activeTool);


        for (ActionListener listener : listeners) {
            topButtonToolsList.addActionListener(listener);
        }
    }

	@Override
	public void displayMessage(String message, Color color)
	{
		labelInfoData.setForeground(color);
		SwingUtilities.invokeLater(() -> labelInfoData.setText(message));
	}
}
