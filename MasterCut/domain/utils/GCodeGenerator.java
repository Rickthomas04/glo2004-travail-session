package MasterCut.domain.utils;

import MasterCut.domain.Cnc;
import MasterCut.domain.Intersection;
import MasterCut.domain.cuts.RegularCut;
import MasterCut.domain.dto.ToolDTO;
import MasterCut.domain.utils.enumPackage.CutType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GCodeGenerator {

    private GCodeGenerator() {
    }

    public static void generateGCodeFile(Cnc cnc, String path) throws IOException {
        File file = createFile(path);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        Map<UUID, RegularCut> cutList = cnc.getPanel().getRegularCuts();
        List<ToolDTO> toolList = cnc.getToolsDTO();
        ToolDTO currentTool = toolList.get(0);
        int i = 0;
        for (var entry : cutList.entrySet()) {
            RegularCut currentCut = entry.getValue();
            if (!(currentCut.getCutType().equals(CutType.FORBIDDEN_ZONE))) {
            if (i == 0) {
                writer.write("%\n");
                writer.write("G21 G17 G90 F" + currentCut.getTool().getFeedrate() + "\n");
                writer.write("M03 S" + currentCut.getTool().getRpm() + "\n");
                i = 1;
            }
            ToolDTO tempTool = new ToolDTO(currentCut.getTool());
            if (!(tempTool.equals(currentTool))) {
                currentTool = tempTool;
                int position = findToolPosition(toolList, currentTool);
                writer.write(changeTool(position) + "\n");
                writer.write(changeRPM(currentTool.rpm) + "\n");
            }
            writer.write(moveWithoutCutting(currentCut.getStart()) + "\n");
            writer.write("G01 Z-" + currentCut.getDepth() + "\n");
            writer.write(moveWhileCutting(currentCut.getEnd(), currentTool.feedrate, currentCut.getDepth()) + "\n");
            writer.write("G00 Z3\n");
            }
        }
        writer.write("G28 X0 Y0\n");
        writer.write("M05\n");
        writer.write("M30\n");
        writer.write("%\n");
        writer.close();
    }

    static private int findToolPosition(List<ToolDTO> list, ToolDTO tool) {
        int i = 0;
        for (ToolDTO element : list) {
            if (element.equals(tool)) {
                break;
            }
            i++;
        }
        return i;
    }
    
    static private String moveWithoutCutting(Intersection intersection) {
        double x = intersection.getX();
        double y = intersection.getY();
        return ("G00 X" + x + " Y" + y);
    }

    static private String changeTool(int newTool) {
        return ("T" + newTool + " M06");
    }

    static private String moveWhileCutting(Intersection intersection, double feedrate, double depth) {
        double x = intersection.getX();
        double y = intersection.getY();
        return ("G01 X" + x + " Y" + y + " Z-" + depth + " F" + feedrate);
    }

    static private String changeRPM(int newRPM) {
        return ("G97 S" + newRPM + " M03");
    }

    static private File createFile(String path) throws IOException {
        File file = new File(path + ".gcode");
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Could not create file");
        }
        return file;
    }
}
