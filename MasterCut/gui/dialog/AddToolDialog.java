package MasterCut.gui.dialog;

import MasterCut.domain.dto.ToolDTO;
import MasterCut.domain.utils.enumPackage.Unit;


import javax.swing.*;
import java.awt.*;

public class AddToolDialog extends JDialog
{

	private JTextField toolNameField;
	private JTextField diameterField;
	private JTextField rpmField;
	private JTextField feedrateField;
	private JComboBox<Unit> unitComboBox;
	private JButton addButton;
	private JButton cancelButton;
	JLabel titleLabel;

	private ToolDTO tool;

	public AddToolDialog(Frame parent, ToolDTO tool)
	{
		super(parent, "Add Tool", true);

		// Setup the layout
		setLayout(new BorderLayout(10, 10));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Title Panel
		titleLabel = new JLabel("Enter Tool Information", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		add(titleLabel, BorderLayout.NORTH);

		// Form Panel
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Tool Name
		gbc.gridx = 0;
		gbc.gridy = 0;
		formPanel.add(new JLabel("Tool Name:"), gbc);

		gbc.gridx = 1;
		toolNameField = new JTextField(15);
		formPanel.add(toolNameField, gbc);

		// Diameter
		gbc.gridx = 0;
		gbc.gridy = 1;
		formPanel.add(new JLabel("Diameter:"), gbc);

		gbc.gridx = 1;
		diameterField = new JTextField(15);
		formPanel.add(diameterField, gbc);

		// RPM
		gbc.gridx = 0;
		gbc.gridy = 2;
		formPanel.add(new JLabel("RPM:"), gbc);

		gbc.gridx = 1;
		rpmField = new JTextField(15);
		formPanel.add(rpmField, gbc);

		// Feedrate
		gbc.gridx = 0;
		gbc.gridy = 3;
		formPanel.add(new JLabel("Feedrate:"), gbc);

		gbc.gridx = 1;
		feedrateField = new JTextField(15);
		formPanel.add(feedrateField, gbc);

		// Unit
		gbc.gridx = 0;
		gbc.gridy = 4;
		formPanel.add(new JLabel("Unit:"), gbc);

		gbc.gridx = 1;
		unitComboBox = new JComboBox<>(Unit.values());
		formPanel.add(unitComboBox, gbc);

		add(formPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		addButton = new JButton("Add");
		cancelButton = new JButton("Cancel");

		buttonPanel.add(addButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);

		if(!(tool.usedUnit == null) && !(tool.uuid == null))
		{
			setToEditTool(tool);
		}

		addButton.addActionListener(e -> {
			if (validateInput())
			{

				this.tool = tool;
				this.tool.name = toolNameField.getText();
				this.tool.diameter = Double.parseDouble(diameterField.getText());
				this.tool.rpm = Integer.parseInt(rpmField.getText());
				this.tool.feedrate = Double.parseDouble(feedrateField.getText());
				this.tool.usedUnit = (Unit) unitComboBox.getSelectedItem();
				this.tool.uuid = tool.uuid;

				dispose();
			} else
			{
				JOptionPane.showMessageDialog(this, "Please fill out all fields correctly.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		cancelButton.addActionListener(e -> dispose());

		setSize(400, 350);
		setLocationRelativeTo(parent);
	}

	private boolean validateInput()
	{
		double diameter;
		double feedrate;
		int rpm;


		if (toolNameField.getText().isEmpty())
		{
			return false;
		}

		try
		{
			diameter = Double.parseDouble(diameterField.getText());
		} catch (NumberFormatException e)
		{
			return false;
		}

		try
		{
			rpm = Integer.parseInt(rpmField.getText());
		} catch (NumberFormatException e)
		{
			return false;
		}

		try
		{
			feedrate = Double.parseDouble(feedrateField.getText());
		} catch (NumberFormatException e)
		{
			return false;
		}

		if(diameter <= 0 || rpm <= 0 || feedrate <= 0)
			return false;

		return true;
	}

	public ToolDTO getTool()
	{
		return tool;
	}

	private void setToEditTool(ToolDTO tool)
	{
		toolNameField.setText(tool.name);
		diameterField.setText(Double.toString(tool.diameter));
		rpmField.setText(Integer.toString(tool.rpm));
		feedrateField.setText(Double.toString(tool.feedrate));
		unitComboBox.setSelectedItem(tool.usedUnit);

		addButton.setText("Save");
		titleLabel.setText("Edit Tool Information");
	}
}

