package MasterCut.gui.panel;

import MasterCut.domain.dto.DimensionsDTO;
import MasterCut.domain.dto.IntersectionDTO;
import MasterCut.domain.dto.ToolDTO;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.dto.cuts.BorderCutDTO;
import MasterCut.domain.dto.cuts.LCutDTO;
import MasterCut.domain.dto.cuts.RectangularCutDTO;
import MasterCut.domain.dto.cuts.ForbiddenZoneDTO;
import MasterCut.domain.dto.cuts.RegularCutDTO;
import MasterCut.domain.utils.TextParser;
import MasterCut.domain.utils.UnitConverter;
import MasterCut.domain.utils.enumPackage.Unit;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

public class InfoPanel extends JPanel
{
	private final DrawingPanel drawingPanel;
	private JTextField textFieldDistance;
	private JTextField textFieldDepth;
	private JTextField textFieldWidth;
	private JTextField textFieldHeight;
	private JTextField textFieldDistanceFromReferenceX;
	private JTextField textFieldDistanceFromReferenceY;
	private JComboBox<ToolDTO> toolComboBox;


	public InfoPanel(DrawingPanel drawingPanel)
	{
		this.drawingPanel = drawingPanel;
		setLayout(new GridBagLayout());
	}

	private void showInfoRegularCut(RegularCutDTO cut)
	{
		removeAll();


		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0;


		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		add(createLabel("Cut Type: " + cut.getClass().getSimpleName()), gbc);


		gbc.gridy = 1;
		gbc.gridwidth = 1;
		add(createLabel("Select Tool:"), gbc);

		toolComboBox = new JComboBox<>();
		List<ToolDTO> tools = drawingPanel.mainWindow.controller.getToolsDTO();
		tools.forEach(toolComboBox::addItem);
		if (cut.tool != null)
		{
			tools.stream().filter(tool -> tool.uuid.equals(cut.tool.uuid)).findFirst().ifPresent(toolComboBox::setSelectedItem);
		}
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		add(toolComboBox, gbc);


		Unit usedUnit = drawingPanel.mainWindow.controller.getPanelDTO().usedUnit;
		double distance;
		double depth = (usedUnit == Unit.METRIC) ? cut.depth : UnitConverter.convertToImperial(cut.depth);

		Point2D.Double distancePoint = drawingPanel.mainWindow.controller.getDistanceFromReference(cut);

		if (cut.isHorizontal)
			distance = (usedUnit == Unit.METRIC) ? distancePoint.getY() : UnitConverter.convertToImperial(distancePoint.getY());
		else
			distance = (usedUnit == Unit.METRIC) ? distancePoint.getX() : UnitConverter.convertToImperial(distancePoint.getX());

		DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));


		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		add(createLabel("Depth:"), gbc);

		textFieldDepth = createTextField(df.format(depth));
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		add(textFieldDepth, gbc);


		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		add(createLabel("Distance from reference:"), gbc);

		textFieldDistance = createTextField(df.format(distance));
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		add(textFieldDistance, gbc);


		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.CENTER;

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		buttonPanel.add(createButton("Update", e -> updateRegularCut(cut)));
		buttonPanel.add(createButton("Delete Cut", e -> deleteCut(cut)));
		add(buttonPanel, gbc);
		gbc.gridy++;

		JPanel buttonChangeReferencePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonChangeReferencePanel.add(createButton("Change Reference", e -> changeReference()));
		add(buttonChangeReferencePanel, gbc);


		revalidate();
		repaint();
	}

	private void updateLCut(LCutDTO cut, String stringWitdh, String stringHeight, String depth)
	{
		try
		{
			Unit usedUnit = drawingPanel.mainWindow.controller.getPanelDTO().usedUnit;
			ToolDTO selectedTool = (ToolDTO) toolComboBox.getSelectedItem();

			double newWidth = TextParser.parseInputWithUnits(stringWitdh, usedUnit).getValueInMm();
			double newHeight = TextParser.parseInputWithUnits(stringHeight, usedUnit).getValueInMm();
			double newDepth = TextParser.parseInputWithUnits(depth, usedUnit).getValueInMm();


			if (selectedTool == null)
			{
				JOptionPane.showMessageDialog(this, "Please select a tool.", "Invalid Tool", JOptionPane.ERROR_MESSAGE);
				return;
			}

			DimensionsDTO panelDimension = drawingPanel.mainWindow.controller.getPanelDTO().dimensions;
			if (newWidth >= panelDimension.getWidth() || newHeight >= panelDimension.getHeight())
			{
				JOptionPane.showMessageDialog(this, "Lengths exceed panel dimensions.", "Invalid Dimension", JOptionPane.ERROR_MESSAGE);
				return;
			}
			drawingPanel.mainWindow.controller.setTool(cut, selectedTool);
			drawingPanel.mainWindow.controller.editCutDistance(cut, new Point2D.Double(newWidth, newHeight));
			drawingPanel.mainWindow.controller.editCutDepth(cut, newDepth);
			drawingPanel.repaint();
			cleanInfo();

		} catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void showInfoLCut(LCutDTO cut)
	{
		removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 10, 5, 10);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;


		gbc.gridwidth = 2;
		add(createLabel("Cut Type: L-Cut"), gbc);
		gbc.gridy++;


		gbc.gridwidth = 1;
		gbc.weightx = 0.3;
		add(createLabel("Select Tool:"), gbc);

		toolComboBox = new JComboBox<>();
		List<ToolDTO> tools = drawingPanel.mainWindow.controller.getToolsDTO();
		tools.forEach(toolComboBox::addItem);


		if (cut.tool != null)
		{
			tools.stream().filter(tool -> tool.uuid.equals(cut.tool.uuid)).findFirst().ifPresent(toolComboBox::setSelectedItem);
		}

		gbc.gridx = 1;
		gbc.weightx = 0.7;
		add(toolComboBox, gbc);
		gbc.gridy++;
		gbc.gridx = 0;


		Unit usedUnit = drawingPanel.mainWindow.controller.getPanelDTO().usedUnit;

		double depth = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(cut.depth) : cut.depth;
		double width = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(drawingPanel.mainWindow.controller.getDistanceFromReference(cut).getX()) : drawingPanel.mainWindow.controller.getDistanceFromReference(cut).getX();
		double height = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(drawingPanel.mainWindow.controller.getDistanceFromReference(cut).getY()) : drawingPanel.mainWindow.controller.getDistanceFromReference(cut).getY();

		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		DecimalFormat df = new DecimalFormat("#.##", symbols);


		JTextField textFieldDepth = createTextField(df.format(depth));
		JTextField textFieldWidth = createTextField(df.format(width));
		JTextField textFieldheight = createTextField(df.format(height));

		addLabelAndField("Depth:", textFieldDepth, gbc);
		addLabelAndField("Width:", textFieldWidth, gbc);
		addLabelAndField("Height:", textFieldheight, gbc);


		gbc.gridwidth = 2;
		gbc.gridx = 0;
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(createButton("Update", e -> updateLCut(cut, textFieldWidth.getText(), textFieldheight.getText(), textFieldDepth.getText())));
		buttonPanel.add(createButton("Delete Cut", e -> deleteCut(cut)));
		add(buttonPanel, gbc);

		revalidate();
		repaint();
	}

	private void addLabelAndField(String labelText, JTextField textField, GridBagConstraints gbc)
	{
		// Label
		gbc.gridx = 0;
		gbc.weightx = 0.3;
		add(createLabel(labelText), gbc);

		// TextField
		gbc.gridx = 1;
		gbc.weightx = 0.7;
		add(textField, gbc);

		// Move to next row
		gbc.gridy++;
	}

	private void showInfoRectangularCut(RectangularCutDTO cut, boolean isRectangle)
	{
		removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 5, 2, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;

		// Title
		gbc.gridwidth = 2;
		if (isRectangle)
		{
			add(createLabel("Cut Type: Rectangular Cut"), gbc);
		} else
		{
			add(createLabel("Forbidden Zone"), gbc);
		}
		gbc.gridy++;

		if (isRectangle)
		{
			// Tool selection ComboBox
			gbc.gridwidth = 1;
			gbc.weightx = 0.3;
			add(createLabel("Select Tool:"), gbc);

			List<ToolDTO> tools = drawingPanel.mainWindow.controller.getToolsDTO();

			toolComboBox = new JComboBox<>();

			for (ToolDTO tool : tools)
			{
				toolComboBox.addItem(tool);
			}

			// Set the selected tool if one exists
			if (cut.tool != null)
				tools.stream().filter(tool -> tool.uuid.equals(cut.tool.uuid)).findFirst().ifPresent(toolComboBox::setSelectedItem);


			gbc.gridx = 1;
			gbc.weightx = 0.7;
			add(toolComboBox, gbc);
			gbc.gridy++;

		}

		Unit usedUnit = drawingPanel.mainWindow.controller.getPanelDTO().usedUnit;
		Point2D.Double distanceReferencePoint = drawingPanel.mainWindow.controller.getDistanceFromReference(cut);
		double depth = 0;
		if (isRectangle)
		{
			depth = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(cut.depth) : cut.depth;
		}
		double width = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(cut.width) : cut.width;
		double height = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(cut.height) : cut.height;
		double distanceFromReferenceX = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(distanceReferencePoint.x) : distanceReferencePoint.x;
		double distanceFromReferenceY = usedUnit == Unit.IMPERIAL ? UnitConverter.convertToImperial(distanceReferencePoint.y) : distanceReferencePoint.y;


		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		DecimalFormat df = new DecimalFormat("#.##", symbols);

		// layout for label-textfield pairs
		gbc.gridwidth = 1;
		gbc.weightx = 0.3; // Label column
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// create all textfields
		if (isRectangle)
		{
			textFieldDepth = createTextField(df.format(depth));
		}
		textFieldWidth = createTextField(df.format(width));
		textFieldHeight = createTextField(df.format(height));
		textFieldDistanceFromReferenceX = createTextField(df.format(distanceFromReferenceX));
		textFieldDistanceFromReferenceY = createTextField(df.format(distanceFromReferenceY));

		// add all pairs with same layout
		if (isRectangle)
		{
			addLabelAndField("Depth:", textFieldDepth, gbc);
		}
		addLabelAndField("Width:", textFieldWidth, gbc);
		addLabelAndField("Height:", textFieldHeight, gbc);
		addLabelAndField("Dist. to ref. X:", textFieldDistanceFromReferenceX, gbc);
		addLabelAndField("Dist. to ref. Y:", textFieldDistanceFromReferenceY, gbc);


		// buttons panel
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		buttonPanel.add(createButton("Update", e -> updateRectangularCut(cut, isRectangle)));


		buttonPanel.add(createButton("Delete", e -> deleteCut(cut)));
		add(buttonPanel, gbc);

		// change reference button panel
		if (isRectangle)
		{
			gbc.gridy++;
			JPanel buttonChangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			buttonChangePanel.add(createButton("Change Reference", e -> changeReference()));
			add(buttonChangePanel, gbc);
		}

		revalidate();
		repaint();
	}


	private void changeReference()
	{
		drawingPanel.mainWindow.controller.changeReferenceMode();
		drawingPanel.repaint();
	}

	private void updateRectangularCut(RectangularCutDTO cut, boolean isRectangle)
	{
		if (isRectangle)
		{
			try
			{
				//isRectangle == True
				ToolDTO differentTool = (ToolDTO) toolComboBox.getSelectedItem();

				Unit usedUnit = drawingPanel.mainWindow.controller.getPanelDTO().usedUnit;

				double newWidth = TextParser.parseInputWithUnits(textFieldWidth.getText(), usedUnit).getValueInMm();
				double newHeight = TextParser.parseInputWithUnits(textFieldHeight.getText(), usedUnit).getValueInMm();
				double newDepth = TextParser.parseInputWithUnits(textFieldDepth.getText(), usedUnit).getValueInMm();
				double newDistanceFromReferenceY = TextParser.parseInputWithUnits(textFieldDistanceFromReferenceY.getText(), usedUnit).getValueInMm();
				double newDistanceFromReferenceX = TextParser.parseInputWithUnits(textFieldDistanceFromReferenceX.getText(), usedUnit).getValueInMm();


				DimensionsDTO panelDimension = drawingPanel.mainWindow.controller.getPanelDTO().dimensions;
				if (newWidth > panelDimension.getWidth() || newHeight > panelDimension.getHeight())
				{
					JOptionPane.showMessageDialog(this, "Width or height exceeds panel dimensions.", "Invalid Dimension", JOptionPane.ERROR_MESSAGE);
					return;
				}

				//			if (differentTool != null)
				//			{
				drawingPanel.mainWindow.controller.setTool(cut, differentTool);
				drawingPanel.mainWindow.controller.editRectangularCut(cut, newWidth, newHeight, newDepth);
				drawingPanel.mainWindow.controller.editCutDistance(cut, new Point2D.Double(newDistanceFromReferenceX, newDistanceFromReferenceY));
				//			} else
				//			{
				//				throw new IllegalArgumentException("New tool was detected for the cut");
				//			}

				drawingPanel.repaint();

			} catch (NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
			}
		} else
		{

			try
			{


				Unit usedUnit = drawingPanel.mainWindow.controller.getPanelDTO().usedUnit;
				double newWidth = (Double.parseDouble(textFieldWidth.getText()));
				double newHeight = (Double.parseDouble(textFieldHeight.getText()));


				double newDepth = 0;


				double newDistanceFromReferenceY = Double.parseDouble(textFieldDistanceFromReferenceY.getText());
				double newDistanceFromReferenceX = Double.parseDouble(textFieldDistanceFromReferenceX.getText());

				if (usedUnit == Unit.IMPERIAL)
				{
					newWidth = UnitConverter.convertToMetric(newWidth);
					newHeight = UnitConverter.convertToMetric(newHeight);
					newDepth = UnitConverter.convertToMetric(newDepth);
					newDistanceFromReferenceY = UnitConverter.convertToMetric(newDistanceFromReferenceY);
					newDistanceFromReferenceX = UnitConverter.convertToMetric(newDistanceFromReferenceX);
				}


				DimensionsDTO panelDimension = drawingPanel.mainWindow.controller.getPanelDTO().dimensions;
				if (newWidth > panelDimension.getWidth() || newHeight > panelDimension.getHeight())
				{
					JOptionPane.showMessageDialog(this, "Width or height exceeds panel dimensions.", "Invalid Dimension", JOptionPane.ERROR_MESSAGE);
					return;
				}

				//			if (differentTool != null)
				//			{

				drawingPanel.mainWindow.controller.editRectangularCut(cut, newWidth, newHeight, newDepth);
				drawingPanel.mainWindow.controller.editCutDistance(cut, new Point2D.Double(newDistanceFromReferenceX, newDistanceFromReferenceY));
				//			} else
				//			{
				//				throw new IllegalArgumentException("New tool was detected for the cut");
				//			}

				drawingPanel.repaint();

			} catch (NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
			}

		}


	}


	private JLabel createLabel(String text)
	{
		return new JLabel(text);
	}

	private JTextField createTextField(String text)
	{
		JTextField textField = new JTextField(text);
		textField.setPreferredSize(new Dimension(100, 20));
		return textField;
	}

	private JButton createButton(String text, ActionListener actionListener)
	{
		JButton button = new JButton(text);
		button.addActionListener(actionListener);
		return button;
	}

	private void updateRegularCut(RegularCutDTO cut)
	{
		try
		{
			Unit usedUnit = drawingPanel.mainWindow.controller.getPanelDTO().usedUnit;
			double newDistance = Double.parseDouble(textFieldDistance.getText());
			double newDepth = Math.abs(Double.parseDouble(textFieldDepth.getText()));

			if (usedUnit == Unit.IMPERIAL)
			{
				newDistance = UnitConverter.convertToMetric(newDistance);
				newDepth = UnitConverter.convertToMetric(newDepth);
			}

			ToolDTO selectedTool = (ToolDTO) toolComboBox.getSelectedItem();

			if (cut != null)
			{
				drawingPanel.mainWindow.controller.setTool(cut, selectedTool);
				drawingPanel.mainWindow.controller.editCutDistance(cut, new Point2D.Double(newDistance, newDistance));
				drawingPanel.mainWindow.controller.editCutDepth(cut, newDepth);
				drawingPanel.repaint();
			}

		} catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, "Please enter a valid numeric distance.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void deleteCut(BaseCutDTO cut)
	{
		drawingPanel.mainWindow.controller.removeCut(cut);
		drawingPanel.repaint();
		cleanInfo();
	}

	public void showInfo(Object object)
	{
		switch (object)
		{
			case RegularCutDTO regularCut -> showInfoRegularCut(regularCut);
			case LCutDTO lCut -> showInfoLCut(lCut);
			case ForbiddenZoneDTO forbiddenZone -> showInfoRectangularCut(forbiddenZone, false);
			case RectangularCutDTO rectangularCut -> showInfoRectangularCut(rectangularCut, true);
			case IntersectionDTO intersection -> showInfoIntersection(intersection);
			case BorderCutDTO borderCut -> showInfoBorderCut(borderCut);

			case null, default -> cleanInfo();
		}
	}

	private void showInfoIntersection(IntersectionDTO intersection)
	{
		removeAll();

		Unit usedUnit = drawingPanel.mainWindow.controller.getPanelDTO().usedUnit;

		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		DecimalFormat df = new DecimalFormat("#.##", symbols);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 5, 2, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		add(createLabel("Intersection Details"), gbc);
		gbc.gridy++;

		String xString;
		String yString;

		// Display coordinates of the intersection
		if (usedUnit == Unit.METRIC)
		{
			xString = String.valueOf(df.format(intersection.point.x));
			yString = String.valueOf(df.format(intersection.point.y));
		} else
		{
			xString = String.valueOf(df.format(UnitConverter.convertToImperial(intersection.point.x)));
			yString = String.valueOf(df.format(UnitConverter.convertToImperial(intersection.point.y)));
		}

		add(createLabel("Coordinates (X, Y): [" + xString + ", " + yString + "]"), gbc);
		gbc.gridy++;

		gbc.gridwidth = 2;
		gbc.gridx = 0;

		revalidate();
		repaint();
	}

	private void cleanInfo()
	{
		removeAll();
		revalidate();
		repaint();
	}

	private void updateBorderCut(BorderCutDTO cut)
	{
		try
		{
			//			double newDepth = Math.abs(Double.parseDouble(textFieldDepth.getText()));
			//			double newDistance = Math.abs(Double.parseDouble(textFieldDistance.getText()));
			Unit usedUnit = drawingPanel.mainWindow.controller.getPanelDTO().usedUnit;
			double newDistance = TextParser.parseInputWithUnits(textFieldDistance.getText(), usedUnit).getValueInMm();
			double newDepth = TextParser.parseInputWithUnits(textFieldDepth.getText(), usedUnit).getValueInMm();
			ToolDTO selectedTool = (ToolDTO) toolComboBox.getSelectedItem();
			DimensionsDTO dimensionMax = drawingPanel.mainWindow.controller.getPanelDTO().dimensions;
			if (newDistance > dimensionMax.getWidth() / 2.0d || newDistance > dimensionMax.getHeight() / 2.0d || newDistance <= 0)
			{
				JOptionPane.showMessageDialog(this, "Invalid distance", "Invalid Dimension", JOptionPane.ERROR_MESSAGE);
				return;
			}
			double newWidth;
			double newHeight;
			double panelHeight = drawingPanel.mainWindow.controller.getPanelDTO().dimensions.getHeight();
			double panelWidth = drawingPanel.mainWindow.controller.getPanelDTO().dimensions.getWidth();
			newWidth = panelWidth - (2 * newDistance);
			newHeight = panelHeight - (2 * newDistance);
			drawingPanel.mainWindow.controller.editBorderCut(cut, newDepth, newWidth, newHeight);
			drawingPanel.mainWindow.controller.editCutDepth(cut, newDepth);
			drawingPanel.mainWindow.controller.setTool(cut, selectedTool);
			drawingPanel.repaint();
		} catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this, "Enter valid values", "Invalid input", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void showInfoBorderCut(BorderCutDTO cut)
	{
		removeAll();
		GridBagConstraints gbc = new GridBagConstraints();
		double panelHeight = drawingPanel.mainWindow.controller.getPanelDTOHeight();
		double distance = (panelHeight - cut.getNewDimensions().getHeight()) / 2.0d;
		gbc.insets = new Insets(2, 5, 2, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(createLabel("Cut Type: BorderCut"), gbc);
		gbc.gridwidth = 2;
		gbc.gridy++;

		// Tool selection ComboBox
		gbc.gridwidth = 1;
		gbc.weightx = 0.3;
		add(createLabel("Select Tool:"), gbc);

		List<ToolDTO> tools = drawingPanel.mainWindow.controller.getToolsDTO();

		toolComboBox = new JComboBox<>();

		for (ToolDTO tool : tools)
		{
			toolComboBox.addItem(tool);
		}

		// Set the selected tool if one exists
		System.out.println("current tool :" + drawingPanel.mainWindow.controller.getActiveToolDTO());

		if (cut.tool != null)
		{
			tools.stream().filter(tool -> tool.uuid.equals(cut.tool.uuid)).findFirst().ifPresent(toolComboBox::setSelectedItem);
		}

		gbc.gridx = 1;
		gbc.weightx = 0.7;
		add(toolComboBox, gbc);
		gbc.gridy = gbc.gridy + 1;
		Unit usedUnit = drawingPanel.mainWindow.controller.getPanelDTO().usedUnit;
		double depth = cut.depth;
		double width = cut.getNewDimensions().getWidth();
		double height = cut.getNewDimensions().getHeight();
		if (usedUnit == Unit.IMPERIAL)
		{
			depth = UnitConverter.convertToImperial(depth);
			width = UnitConverter.convertToImperial(width);
			height = UnitConverter.convertToImperial(height);
		}
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		DecimalFormat decimalFormat = new DecimalFormat("#.##", symbols);
		gbc.gridx = 0;
		textFieldDepth = createTextField(String.valueOf(decimalFormat.format(depth)));
		add(createLabel("Depth: "), gbc);
		gbc.gridx = 1;
		add(textFieldDepth, gbc);
		gbc.gridy = gbc.gridy + 1;

		gbc.gridx = 0;
		JTextField textFieldWidth = createTextField(String.valueOf(decimalFormat.format(width)));
		add(createLabel("Width: " + width), gbc);
		gbc.gridy = gbc.gridy + 1;

		gbc.gridx = 0;
		JTextField textFieldHeight = createTextField(String.valueOf(decimalFormat.format(height)));
		add(createLabel("Height: " + height), gbc);
		gbc.gridy = gbc.gridy + 1;

		gbc.gridx = 0;
		textFieldDistance = createTextField(String.valueOf(decimalFormat.format(distance)));
		add(createLabel("Distance to panel: "), gbc);
		gbc.gridx = 1;
		add(textFieldDistance, gbc);
		gbc.gridy = gbc.gridy + 1;

		JButton buttonUpdate = createButton("Update", e -> updateBorderCut(cut));
		JButton buttonDelete = createButton("Delete Cut", e -> deleteCut(cut));
		gbc.gridy = gbc.gridy + 1;
		gbc.gridwidth = 2;
		gbc.gridx = 0;

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(buttonUpdate);
		buttonPanel.add(buttonDelete);
		add(buttonPanel, gbc);

		revalidate();
		repaint();
	}

}
