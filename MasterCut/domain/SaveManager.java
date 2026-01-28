package MasterCut.domain;

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

public class SaveManager implements Serializable {

    private final SimpleModule module;
    private final ObjectMapper mapper;

    public SaveManager() {
        module = new SimpleModule();
        module.addSerializer(Color.class, new CustomJsonColor.JsonColorSerializer());
        module.addDeserializer(Color.class, new CustomJsonColor.JsonColorDeserializer());
        mapper = new ObjectMapper();
        mapper.registerModule(module);
        //mapper.registerSubtypes(Line.class);
        //mapper.registerSubtypes(RegularCut.class);
        //mapper.registerSubtypes(Border.class);
        mapper.disable(MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS,
                MapperFeature.AUTO_DETECT_SETTERS);
        //mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

    }

    public void SaveTools(List<ToolDTO> list, String path) throws IOException {
        mapper.writeValue(new File(path.concat(".json")), list);
    }

    public List<ToolDTO> LoadTools(String path) throws IOException {
        List<ToolDTO> list = new ArrayList<>();
        list = mapper.readValue(new File(path), new TypeReference<List<ToolDTO>>() {
        });
        return list;
    }

    public void SavePanel(Cnc cnc, String path) throws IOException {
        mapper.writeValue(new File(path.concat(".json")), cnc);
    }

    public Cnc LoadPanel(String path) throws IOException {
        Cnc cnc = mapper.readValue(new File(path), Cnc.class);
        return cnc;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public SimpleModule getModule() {
        return module;
    }
}
