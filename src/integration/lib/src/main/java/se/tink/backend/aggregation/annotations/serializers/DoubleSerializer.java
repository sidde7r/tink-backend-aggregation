package se.tink.backend.aggregation.annotations.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.CharBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonDouble.JsonType;

public class DoubleSerializer extends StdSerializer<Double> implements ContextualSerializer {

    private JsonType outputType;
    private int decimals;
    private boolean trailingZeros;
    private char decimalSeparator;
    private char groupingSeparator;
    private String prefix;
    private String suffix;

    public DoubleSerializer(JsonType outputType, int decimals, boolean trailingZeros,
            char decimalSeparator, char groupingSeparator, String prefix, String suffix) {
        super(Double.class);
        this.outputType = outputType;
        this.decimals = decimals;
        this.trailingZeros = trailingZeros;
        this.decimalSeparator = decimalSeparator;
        this.groupingSeparator = groupingSeparator;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public DoubleSerializer() {
        super(Double.class);
    }

    public DoubleSerializer(Class<Double> t) {
        super(t);
    }

    @Override
    public void serialize(Double value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        switch (outputType) {
        case NUMERIC:
            jgen.writeRawValue(getNumericValue(value));
            return;
        case STRING:
            jgen.writeString(getStringValue(value));
            return;
        default:
            // Unknown type, handle as standard double serialization
            jgen.writeRawValue(new BigDecimal(value).toPlainString());
        }
    }

    private String getNumericValue(Double value) {
        BigDecimal bd = new BigDecimal(value).setScale(decimals, BigDecimal.ROUND_HALF_UP);
        if (!trailingZeros) {
            bd = bd.stripTrailingZeros();
        }
        return bd.toPlainString();
    }

    private String getStringValue(Double value) {
        DecimalFormat formatter = new DecimalFormat(String.format("0%s%s",
                (decimals > 0 ? "." : ""),
                CharBuffer.allocate(decimals).toString().replace('\0', (trailingZeros ? '0' : '#'))));

        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        if (groupingSeparator != Character.MIN_VALUE) {
            formatter.setGroupingUsed(true);
            formatter.setGroupingSize(3);
            symbols.setGroupingSeparator(groupingSeparator);
        }
        if (decimalSeparator != Character.MIN_VALUE) {
            symbols.setDecimalSeparator(decimalSeparator);
        }
        formatter.setDecimalFormatSymbols(symbols);

        String number = formatter.format(value);
        return String.format("%s%s%s", prefix, number, suffix);
    }


    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws
            JsonMappingException {
        JsonType outputType = null;
        int decimals = 0;
        boolean trailingZeros = false;
        char decimalSeparator = '\0';
        char groupingSeparator = '\0';
        String prefix = null;
        String suffix = null;

        JsonDouble jsonDouble = null;
        if (property != null) {
            jsonDouble = property.getAnnotation(JsonDouble.class);
        }
        if (jsonDouble != null) {
            outputType = jsonDouble.outputType();
            decimals = jsonDouble.decimals();
            trailingZeros = jsonDouble.trailingZeros();
            decimalSeparator = jsonDouble.decimalSeparator();
            groupingSeparator = jsonDouble.groupingSeparator();
            prefix = jsonDouble.prefix();
            suffix = jsonDouble.suffix();
        }
        return new DoubleSerializer(outputType,
                decimals, trailingZeros, decimalSeparator, groupingSeparator, prefix, suffix);
    }
}