package MasterCut.gui.panel;

import MasterCut.domain.Intersection;
import MasterCut.domain.cuts.RegularCut;
import MasterCut.domain.dto.*;
import MasterCut.domain.utils.Clickable;
import MasterCut.domain.utils.enumPackage.ClickType;
import MasterCut.domain.utils.enumPackage.ModifierKey;
import MasterCut.drawing.CutDrawer;
import MasterCut.drawing.GridDrawer;
import MasterCut.drawing.IntersectionDrawer;
import MasterCut.drawing.PanelDrawer;
import MasterCut.gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.Serializable;

public class DrawingPanel extends JPanel implements Serializable
{
	protected MainWindow mainWindow;

	private double zoomLevel = .5;
	private int offsetX = 50;
	private int offsetY = 50;
	private Point lastMousePoint;

	private Timer longClickTimer;
	private boolean isDragging = false;
	private long clickStartTime;
	private Point initialClickPoint;


	private static final int LONG_CLICK_THRESHOLD = 500; // Temps en ms pour un long click
	private static final int DRAG_THRESHOLD = 5; // Distance en pixels pour détecter un drag
	private static final double ZOOM_FACTOR = 1.1;

	private final Color BACKGROUND_COLOR = new Color(19, 19, 19);

	public DrawingPanel()
	{
		initializeListeners();
	}

	public DrawingPanel(MainWindow mainWindow)
	{
		this();
		this.mainWindow = mainWindow;
		this.setBackground(BACKGROUND_COLOR);
	}

	private void initializeListeners()
	{

		this.addMouseMotionListener(new MouseAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e) && initialClickPoint != null)
				{
					int dx = e.getX() - initialClickPoint.x;
					int dy = e.getY() - initialClickPoint.y;

					if (!isDragging && (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD))
					{
						isDragging = true;

						if (longClickTimer != null)
							longClickTimer.stop();
						handleMouseClick(e, ClickType.INITDRAG);
					}

					if (isDragging)
					{
						handleMouseClick(e, ClickType.DRAG);
					}
					if (mainWindow.isSnapToGridEnabled())
					{
						Point2D.Double transformedPoint = transformPoint(e.getX(), e.getY());
						int gridSize = mainWindow.controller.getGridDTO().gridSize;
						double offsety = mainWindow.controller.getPanelDTOHeight() % gridSize;
						double snappedX = Math.round(transformedPoint.x / gridSize) * gridSize;
						double snappedY = Math.round(transformedPoint.y / gridSize) * gridSize + offsety;

						mainWindow.updateInfoCursor(new Point2D.Double(snappedX, snappedY));
					} else
					{
						mainWindow.updateInfoCursor(transformPoint(e.getX(), e.getY()));
					}
					mainWindow.initToolsTree();
					repaint();
				}

				if (SwingUtilities.isMiddleMouseButton(e) && lastMousePoint != null)
				{
					offsetX += e.getX() - lastMousePoint.x;
					offsetY += e.getY() - lastMousePoint.y;
					lastMousePoint = e.getPoint();
					mainWindow.initToolsTree();
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent evt)
			{
				mainWindow.updateInfoCursor(transformPoint(evt.getX(), evt.getY()));
				lastMousePoint = evt.getPoint();
			}
		});

		this.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					clickStartTime = System.currentTimeMillis();
					initialClickPoint = e.getPoint();
					isDragging = false;

					longClickTimer = new Timer(LONG_CLICK_THRESHOLD, event -> {
						if (!isDragging)
						{
							handleMouseClick(e, ClickType.LONGCLICK);
						}
					});
					longClickTimer.setRepeats(false);
					longClickTimer.start();
				}

				if (SwingUtilities.isMiddleMouseButton(e))
				{
					lastMousePoint = e.getPoint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{       
				if (SwingUtilities.isLeftMouseButton(e))
				{
					long clickDuration = System.currentTimeMillis() - clickStartTime;

					if (longClickTimer != null)
						longClickTimer.stop();

					if (isDragging)
					{
						isDragging = false;
                                             
					} if (clickDuration < LONG_CLICK_THRESHOLD)
					{     
						handleMouseClick(e, ClickType.SHORTCLICK);
					}
				}

				if (SwingUtilities.isMiddleMouseButton(e))
				{
					lastMousePoint = null;
				}

			}
		});

		this.addMouseWheelListener(e -> {
			int notches = e.getWheelRotation();


			Point mousePoint = lastMousePoint != null ? lastMousePoint : new Point(getWidth() / 2, getHeight() / 2);
			double mouseX = (mousePoint.x - offsetX) / zoomLevel;
			double mouseY = (mousePoint.y - offsetY) / zoomLevel;

			if (notches < 0)
				zoomLevel *= ZOOM_FACTOR; // Zoom in
			else
				zoomLevel /= ZOOM_FACTOR; // Zoom out

			offsetX = (int) (mousePoint.x - mouseX * zoomLevel);
			offsetY = (int) (mousePoint.y - mouseY * zoomLevel);
			mainWindow.initToolsTree();
			repaint();
		});

	}

	private void handleMouseClick(MouseEvent e, ClickType clickType)
	{
		ModifierKey modifierKey;
		Object itemSelected;

		if (e.isAltDown())
			modifierKey = ModifierKey.ALT;
		else if (e.isControlDown())
			modifierKey = ModifierKey.CTRL;
		else if (e.isShiftDown())
			modifierKey = ModifierKey.SHIFT;
		else
			modifierKey = null;

		isDragging = mainWindow.controller.handleMouseClick(transformPoint(e.getX(), e.getY()), clickType, isDragging, modifierKey);

		itemSelected = mainWindow.controller.getItemSelectedDTO();
		mainWindow.updatePanelInfo(itemSelected);
		mainWindow.initToolsTree();
		this.repaint();
	}

	private Point2D.Double transformPoint(int x, int y)
	{
		double adjustedX = (x - offsetX) / zoomLevel;
		double adjustedY = (y - offsetY) / zoomLevel;

		return new Point2D.Double(adjustedX, adjustedY);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (mainWindow != null)
		{
			Graphics2D g2d = (Graphics2D) g;

			PanelDTO panel = mainWindow.controller.getPanelDTO();
			if (panel == null)
				return;

			int panelHeight = (int) panel.dimensions.getHeight();

			g2d.scale(zoomLevel, zoomLevel);
			g2d.translate(offsetX / zoomLevel, offsetY / zoomLevel);

			GridDrawer gridDrawer = new GridDrawer(mainWindow.controller);
			gridDrawer.drawGrid(g2d, panelHeight);

			PanelDrawer panelDrawer = new PanelDrawer(mainWindow.controller);
			panelDrawer.draw(g2d);

			CutDrawer cutDrawer = new CutDrawer(mainWindow.controller);
			cutDrawer.draw(g2d);

			IntersectionDrawer intersectionDrawer = new IntersectionDrawer(mainWindow.controller);
			intersectionDrawer.draw(g2d);
		}
	}


}
