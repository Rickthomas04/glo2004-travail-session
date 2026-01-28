package MasterCut.gui.dialog;

import java.awt.Frame;
import java.io.File;
import javax.swing.JFileChooser;

public class FileChooserDialog {

    public static String GetPath(JFileChooser chooser) {
        File file = chooser.getSelectedFile();
        String path = file.getPath();
        return path;
    }

    public static int ShowOpenFileChooser(Frame parent, JFileChooser chooser) {
        int valide = chooser.showOpenDialog(parent);
        return valide;
    }

    public static int ShowSaveFileChooser(Frame parent, JFileChooser chooser) {
        int valide = chooser.showSaveDialog(parent);
        return valide;
    }

}
