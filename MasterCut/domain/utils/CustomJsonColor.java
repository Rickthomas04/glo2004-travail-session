package MasterCut.domain.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.awt.Color;
import java.io.IOException;

public class CustomJsonColor {

    public static class JsonColorSerializer extends JsonSerializer<Color> {

        @Override
        public void serialize(Color color, JsonGenerator generator, SerializerProvider provider) throws IOException {
            if (color == null) {
                generator.writeNull();
            } else {
                generator.writeNumber(color.getRGB());

            }
        }
    }

    public static class JsonColorDeserializer extends JsonDeserializer<Color> {

        @Override
        public Color deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            Color color = new Color(parser.getValueAsInt());
            return color;
        }
    }
}
