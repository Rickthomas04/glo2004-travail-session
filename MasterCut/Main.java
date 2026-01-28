package MasterCut;

import java.io.IOException;


public class Main
{
	public static void main(String[] args) throws IOException, InterruptedException
    {
        MasterCut.gui.MainWindow mainWindow = new MasterCut.gui.MainWindow();
            //mainWindow.setExtendedState(mainWindow.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            mainWindow.setVisible(true);

	}
}
