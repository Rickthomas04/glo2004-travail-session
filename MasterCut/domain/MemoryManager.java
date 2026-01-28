package MasterCut.domain;

import java.util.Stack;
import MasterCut.domain.utils.DeepCopy;
import java.io.Serializable;
import java.util.Stack;


import MasterCut.domain.dto.ToolDTO;
import MasterCut.domain.utils.CustomJsonColor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Stack;


public class MemoryManager implements Serializable {

    private final ObjectMapper mapper;
    private final SimpleModule module;
    private final Stack<byte[]> saveStack; 
    private final Stack<byte[]> redoStack; 

    private final Stack<byte[]> toolSaveStack;
    private final Stack<byte[]> toolRedoStack;
    private Controller controller; 
    private boolean isLoading = false;
    public MemoryManager(Controller controller) {
        
        module = new SimpleModule();
        module.addSerializer(Color.class, new CustomJsonColor.JsonColorSerializer());
        module.addDeserializer(Color.class, new CustomJsonColor.JsonColorDeserializer());

        mapper = new ObjectMapper();
        mapper.registerModule(module);

        mapper.disable(
            MapperFeature.AUTO_DETECT_CREATORS,
            MapperFeature.AUTO_DETECT_FIELDS,
            MapperFeature.AUTO_DETECT_GETTERS,
            MapperFeature.AUTO_DETECT_IS_GETTERS,
            MapperFeature.AUTO_DETECT_SETTERS
        );
        this.controller = controller;
        saveStack = new Stack<>();
        redoStack = new Stack<>();
        

        toolSaveStack = new Stack<>();
        toolRedoStack = new Stack<>();
    }


    public void add(Cnc cnc, List<ToolDTO> tools) {
        if (isLoading) return;

        try (
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            String json = mapper.writeValueAsString(cnc);
            byte[] jsonBytes = json.getBytes("UTF-8");
            oos.writeInt(jsonBytes.length);
            oos.write(jsonBytes);
            oos.flush();

            saveStack.push(bos.toByteArray());
            redoStack.clear();

        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde en mémoire : " + e.getMessage());
            e.printStackTrace();
        }
    }

public void addredo(Cnc cnc, List<ToolDTO> tools) {
    try (
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)
    ) {
        String json = mapper.writeValueAsString(cnc);
        byte[] jsonBytes = json.getBytes("UTF-8");

        oos.writeInt(jsonBytes.length);
        oos.write(jsonBytes);
        oos.flush();

        redoStack.push(bos.toByteArray());

        System.out.println("Cnc sauvegardé en mémoire (redo) : " + json);
    } catch (IOException e) {
        System.err.println("Erreur lors de la sauvegarde en mémoire redo : " + e.getMessage());
        e.printStackTrace();
    }
}


//    public Cnc loadFromMemory() {
//        if (saveStack.isEmpty()) {
//            System.err.println("Aucune sauvegarde disponible.");
//            return null;
//        }
//
//        try (
//
//            ByteArrayInputStream bis = new ByteArrayInputStream(saveStack.peek());
//            ObjectInputStream ois = new ObjectInputStream(bis)
//        ) {
//            String json = ois.readUTF();
//            ObjectMapper mapper = new ObjectMapper();
//            System.out.println("voici le json " + json);
//            Map<String, Object> rootObject = mapper.readValue(json, Map.class);
//
//            // Extract tools from the map
//            Map<String, ToolDTO> toolsMap = mapper.convertValue(rootObject.get("tools"), new TypeReference<Map<String, ToolDTO>>() {});
//
//            // Clear existing tools
//            List<ToolDTO> existingTools = controller.getToolsDTO();
//            for (ToolDTO tool : existingTools) {
//                controller.removeTool(tool);
//            }
//
//            // Add tools from the loaded map
//            for (ToolDTO tool : toolsMap.values()) {
//                controller.addTool(tool);
//            }
//            System.out.println("voici le json que je veux ramener" + json);
//            return mapper.readValue(json, Cnc.class);
//        } catch (IOException e) {
//            System.err.println("Erreur lors du chargement de la memoire : " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//    }
public Cnc loadFromMemory() {
    isLoading = true;
    if (saveStack.isEmpty()) {
        System.err.println("Aucune sauvegarde disponible.");
        return null;
    }

    try (
        ByteArrayInputStream bis = new ByteArrayInputStream(saveStack.peek());
        ObjectInputStream ois = new ObjectInputStream(bis)
    ) {

        int length = ois.readInt();
        byte[] jsonBytes = new byte[length];
        ois.readFully(jsonBytes);

        String json = new String(jsonBytes, "UTF-8");

        Map<String, Object> rootObject = mapper.readValue(json, Map.class);
        Map<String, ToolDTO> toolsMap = mapper.convertValue(rootObject.get("tools"), new TypeReference<Map<String, ToolDTO>>() {});
        

        List<ToolDTO> existingTools = controller.getToolsDTO();
        for (ToolDTO tool : existingTools) {
            controller.removeTool(tool);
        }


        for (ToolDTO tool : toolsMap.values()) {

            controller.addTool(tool);
        }

        isLoading = false;
        return mapper.readValue(json, Cnc.class);

    } catch (IOException e) {
        System.err.println("Erreur lors du chargement de la mémoire : " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}



public Cnc undo() {
    if (saveStack.isEmpty()) {
        System.err.println("Aucune sauvegarde à annuler.");
        return null;
    }

    System.out.println("Dernière sauvegarde annulée et déplacée vers la pile d'annulation (undoStack).");


    Cnc revertedCnc = loadFromMemory();

    byte[] data = saveStack.pop();
    undoTools();
    return revertedCnc;
}


    public Cnc redo() {
        if (redoStack.isEmpty()) {
            System.err.println("Aucune sauvegarde annulée à rétablir.");
            return null;
        }

        try {
            byte[] data = redoStack.pop();
            saveStack.push(data);
            redoTools();
            return loadFromMemory();
        } catch (Exception e) {
            System.err.println("Erreur lors du rétablissement : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean isSaveStackEmpty() {
        return saveStack.isEmpty();
    }

    public boolean isredostackEmpty() {
        return redoStack.isEmpty();
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
    
    public SimpleModule getModule() {
        return module;
    }
    public void remove() {
       saveStack.pop() ;

    }
    public void clearStacks(){
        saveStack.clear();
        redoStack.clear();
    }

    public void addTools(List<ToolDTO> tools) {
        try (
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            String json = mapper.writeValueAsString(tools);
            oos.writeUTF(json);
            oos.flush();
            toolSaveStack.push(bos.toByteArray());
            toolRedoStack.clear();

        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des outils en mémoire : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<ToolDTO> loadToolsFromMemory() {
        if (toolSaveStack.isEmpty()) {
            System.err.println("Aucune sauvegarde d'outils disponible.");
            return null;
        }

        try (
            ByteArrayInputStream bis = new ByteArrayInputStream(toolSaveStack.peek());
            ObjectInputStream ois = new ObjectInputStream(bis)
        ) {
            String json = ois.readUTF();
            return mapper.readValue(json, new TypeReference<List<ToolDTO>>() {});
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la mémoire outils : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<ToolDTO> undoTools() {
        if (toolSaveStack.isEmpty()) {
            System.err.println("Aucune sauvegarde d'outils à annuler.");
            return null;
        }

        System.out.println("Dernière sauvegarde d'outils annulée.");
        List<ToolDTO> revertedTools = loadToolsFromMemory();
        toolSaveStack.pop();
        return revertedTools;
    }

    public List<ToolDTO> redoTools() {
        if (toolRedoStack.isEmpty()) {
            System.err.println("Aucune sauvegarde d'outils annulée à rétablir.");
            return null;
        }
        System.out.println("sucesss");
        try {
            byte[] data = toolRedoStack.pop();
            toolSaveStack.push(data);
            return loadToolsFromMemory();
        } catch (Exception e) {
            System.err.println("Erreur lors du rétablissement des outils : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
