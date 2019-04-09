package se.tink.backend.aggregation.annotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.log.AggregationLogger;

@Ignore
public class JsonDoubleTest {

    private static AggregationLogger LOGGER = new AggregationLogger(JsonDoubleTest.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonObject
    private class Entity {
        private double noAnnotation;

        @JsonDouble private double defaultAnnotation;

        @JsonDouble(outputType = JsonDouble.JsonType.NUMERIC)
        private double numeric;

        @JsonDouble(decimals = 0)
        private double noDecimals;

        @JsonDouble(decimals = 5)
        private double fiveDecimals;

        @JsonDouble(trailingZeros = false)
        private double noTrailingZeros;

        @JsonDouble(
                decimalSeparator = ',',
                groupingSeparator = ' ',
                prefix = "$",
                suffix = " (some note)")
        private double numericNotAffected;

        @JsonDouble(outputType = JsonDouble.JsonType.STRING)
        private double stringDefault;

        @JsonDouble(outputType = JsonDouble.JsonType.STRING, decimals = 0)
        private double stringNoDecimals;

        @JsonDouble(outputType = JsonDouble.JsonType.STRING, decimals = 5)
        private double stringFiveDecimals;

        @JsonDouble(outputType = JsonDouble.JsonType.STRING, trailingZeros = false)
        private double stringNoTrailingZeros;

        @JsonDouble(outputType = JsonDouble.JsonType.STRING, decimalSeparator = ',')
        private double stringCommaSeparator;

        @JsonDouble(outputType = JsonDouble.JsonType.STRING, groupingSeparator = ',')
        private double stringGroupedByComma;

        @JsonDouble(outputType = JsonDouble.JsonType.STRING, prefix = "£")
        private double stringPrefix;

        @JsonDouble(outputType = JsonDouble.JsonType.STRING, suffix = " SEK")
        private double stringSuffix;

        @JsonDouble(
                outputType = JsonDouble.JsonType.STRING,
                decimals = 0,
                trailingZeros = false,
                decimalSeparator = ',',
                groupingSeparator = ' ',
                prefix = "400 000 euro motsvarar ungefär i svenska kronor ",
                suffix = " kronor.")
        private double stringJustAnExample;

        public Entity() {}

        private Entity(double value) {
            noAnnotation = value;
            defaultAnnotation = value;
            numeric = value;
            noDecimals = value;
            fiveDecimals = value;
            noTrailingZeros = value;
            numericNotAffected = value;
            stringDefault = value;
            stringNoDecimals = value;
            stringFiveDecimals = value;
            stringNoTrailingZeros = value;
            stringCommaSeparator = value;
            stringGroupedByComma = value;
            stringPrefix = value;
            stringSuffix = value;
            stringJustAnExample = value;
        }
    }

    @Test
    public void zero() throws JsonProcessingException {
        String jsonActual = MAPPER.writeValueAsString(new Entity(0));
        String jsonExpected =
                "{"
                        + "\"noAnnotation\":0.0,"
                        + "\"defaultAnnotation\":0.00,"
                        + "\"numeric\":0.00,"
                        + "\"noDecimals\":0,"
                        + "\"fiveDecimals\":0.00000,"
                        + "\"noTrailingZeros\":0,"
                        + "\"numericNotAffected\":0.00,"
                        + "\"stringDefault\":\"0.00\","
                        + "\"stringNoDecimals\":\"0\","
                        + "\"stringFiveDecimals\":\"0.00000\","
                        + "\"stringNoTrailingZeros\":\"0\","
                        + "\"stringCommaSeparator\":\"0,00\","
                        + "\"stringGroupedByComma\":\"0.00\","
                        + "\"stringPrefix\":\"£0.00\","
                        + "\"stringSuffix\":\"0.00 SEK\","
                        + "\"stringJustAnExample\":\"400 000 euro motsvarar ungefär i svenska kronor 0 kronor.\""
                        + "}";
        compareValues(jsonExpected, jsonActual);
        Assert.assertEquals(jsonExpected, jsonActual);
    }

    @Test
    public void recurringDecimal() throws JsonProcessingException {
        String jsonActual = MAPPER.writeValueAsString(new Entity(2.0 / 3));
        String jsonExpected =
                "{"
                        + "\"noAnnotation\":0.6666666666666666,"
                        + "\"defaultAnnotation\":0.67,"
                        + "\"numeric\":0.67,"
                        + "\"noDecimals\":1,"
                        + "\"fiveDecimals\":0.66667,"
                        + "\"noTrailingZeros\":0.67,"
                        + "\"numericNotAffected\":0.67,"
                        + "\"stringDefault\":\"0.67\","
                        + "\"stringNoDecimals\":\"1\","
                        + "\"stringFiveDecimals\":\"0.66667\","
                        + "\"stringNoTrailingZeros\":\"0.67\","
                        + "\"stringCommaSeparator\":\"0,67\","
                        + "\"stringGroupedByComma\":\"0.67\","
                        + "\"stringPrefix\":\"£0.67\","
                        + "\"stringSuffix\":\"0.67 SEK\","
                        + "\"stringJustAnExample\":\"400 000 euro motsvarar ungefär i svenska kronor 1 kronor.\""
                        + "}";
        compareValues(jsonExpected, jsonActual);
        Assert.assertEquals(jsonExpected, jsonActual);
    }

    @Test
    public void noDecimals() throws JsonProcessingException {
        String jsonActual = MAPPER.writeValueAsString(new Entity(1000));
        String jsonExpected =
                "{"
                        + "\"noAnnotation\":1000.0,"
                        + "\"defaultAnnotation\":1000.00,"
                        + "\"numeric\":1000.00,"
                        + "\"noDecimals\":1000,"
                        + "\"fiveDecimals\":1000.00000,"
                        + "\"noTrailingZeros\":1000,"
                        + "\"numericNotAffected\":1000.00,"
                        + "\"stringDefault\":\"1000.00\","
                        + "\"stringNoDecimals\":\"1000\","
                        + "\"stringFiveDecimals\":\"1000.00000\","
                        + "\"stringNoTrailingZeros\":\"1000\","
                        + "\"stringCommaSeparator\":\"1000,00\","
                        + "\"stringGroupedByComma\":\"1,000.00\","
                        + "\"stringPrefix\":\"£1000.00\","
                        + "\"stringSuffix\":\"1000.00 SEK\","
                        + "\"stringJustAnExample\":\"400 000 euro motsvarar ungefär i svenska kronor 1 000 kronor.\""
                        + "}";
        compareValues(jsonExpected, jsonActual);
        Assert.assertEquals(jsonExpected, jsonActual);
    }

    @Test
    public void impreciseDecimal() throws JsonProcessingException {
        String jsonActual = MAPPER.writeValueAsString(new Entity(13.370000000000001));
        String jsonExpected =
                "{"
                        + "\"noAnnotation\":13.370000000000001,"
                        + "\"defaultAnnotation\":13.37,"
                        + "\"numeric\":13.37,"
                        + "\"noDecimals\":13,"
                        + "\"fiveDecimals\":13.37000,"
                        + "\"noTrailingZeros\":13.37,"
                        + "\"numericNotAffected\":13.37,"
                        + "\"stringDefault\":\"13.37\","
                        + "\"stringNoDecimals\":\"13\","
                        + "\"stringFiveDecimals\":\"13.37000\","
                        + "\"stringNoTrailingZeros\":\"13.37\","
                        + "\"stringCommaSeparator\":\"13,37\","
                        + "\"stringGroupedByComma\":\"13.37\","
                        + "\"stringPrefix\":\"£13.37\","
                        + "\"stringSuffix\":\"13.37 SEK\","
                        + "\"stringJustAnExample\":\"400 000 euro motsvarar ungefär i svenska kronor 13 kronor.\""
                        + "}";
        compareValues(jsonExpected, jsonActual);
        Assert.assertEquals(jsonExpected, jsonActual);
    }

    @Test
    public void veryLargeAmount() throws JsonProcessingException {
        String jsonActual = MAPPER.writeValueAsString(new Entity(40000000000.0));
        String jsonExpected =
                "{"
                        + "\"noAnnotation\":4.0E10,"
                        + "\"defaultAnnotation\":40000000000.00,"
                        + "\"numeric\":40000000000.00,"
                        + "\"noDecimals\":40000000000,"
                        + "\"fiveDecimals\":40000000000.00000,"
                        + "\"noTrailingZeros\":40000000000,"
                        + "\"numericNotAffected\":40000000000.00,"
                        + "\"stringDefault\":\"40000000000.00\","
                        + "\"stringNoDecimals\":\"40000000000\","
                        + "\"stringFiveDecimals\":\"40000000000.00000\","
                        + "\"stringNoTrailingZeros\":\"40000000000\","
                        + "\"stringCommaSeparator\":\"40000000000,00\","
                        + "\"stringGroupedByComma\":\"40,000,000,000.00\","
                        + "\"stringPrefix\":\"£40000000000.00\","
                        + "\"stringSuffix\":\"40000000000.00 SEK\","
                        + "\"stringJustAnExample\":\"400 000 euro motsvarar ungefär i svenska kronor 40 000 000 000 kronor.\""
                        + "}";
        compareValues(jsonExpected, jsonActual);
        Assert.assertEquals(jsonExpected, jsonActual);
    }

    // This method is used solely for providing more helpful logging during
    // testing, as it will detect differences at a attribute-value-pair level,
    // rather than in the serialized JSON as a whole
    private void compareValues(String jsonExpected, String jsonActual) {
        Pattern pattern = Pattern.compile("[{,](\"[a-zA-Z]+\":\"?.+?\"?)[,}]");
        Matcher mExpected = pattern.matcher(jsonExpected);
        Matcher mActual = pattern.matcher(jsonExpected);

        boolean bExpected = mExpected.find();
        boolean bActual = mActual.find();

        if (bExpected && bActual) {
            do {
                Assert.assertEquals(mExpected.group(1), mActual.group(1));
                bExpected = mExpected.find(mExpected.start(1));
                bActual = mActual.find(mActual.start(1));
            } while (bExpected && bActual);
        }
        // Check that both matchers are finished
        Assert.assertEquals(
                (bExpected ? mExpected.group(1) : ""), (bActual ? mActual.group(1) : ""));
    }
}
