package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Date;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class NordeaHashMapSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(
            java.lang.String input, JsonGenerator jgen, SerializerProvider serializerProvider)
            throws IOException {
        writeString(jgen, input);
    }

    public static class String extends JsonSerializer<java.lang.String> {
        @Override
        public void serialize(
                java.lang.String input,
                JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider)
                throws IOException {
            writeString(jsonGenerator, input);
        }
    }

    public static class DailyDate extends JsonSerializer<Date> {
        @Override
        public void serialize(
                Date input, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            writeString(jsonGenerator, ThreadSafeDateFormat.FORMATTER_DAILY.format(input));
        }
    }

    public static class Boolean extends JsonSerializer<java.lang.Boolean> {
        @Override
        public void serialize(
                java.lang.Boolean input,
                JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider)
                throws IOException {
            writeString(jsonGenerator, input.toString());
        }
    }

    public static class YesNo extends JsonSerializer<java.lang.Boolean> {
        @Override
        public void serialize(
                java.lang.Boolean input,
                JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider)
                throws IOException {
            writeString(jsonGenerator, input ? "Yes" : "No");
        }
    }

    public static class Double extends JsonSerializer<java.lang.Double> {
        @Override
        public void serialize(
                java.lang.Double input,
                JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider)
                throws IOException {
            writeString(jsonGenerator, input.toString());
        }
    }

    public static class Integer extends JsonSerializer<java.lang.Integer> {
        @Override
        public void serialize(
                java.lang.Integer input,
                JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider)
                throws IOException {
            writeString(jsonGenerator, input.toString());
        }
    }

    private static void writeString(JsonGenerator jsonGenerator, java.lang.String stringValue)
            throws IOException {
        jsonGenerator.writeStartObject();

        if (!Strings.isNullOrEmpty(stringValue)) {
            jsonGenerator.writeStringField("$", stringValue);
        }

        jsonGenerator.writeEndObject();
    }
}
