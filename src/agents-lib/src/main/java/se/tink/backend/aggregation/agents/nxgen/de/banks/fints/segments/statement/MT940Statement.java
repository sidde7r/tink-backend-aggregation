package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.statement;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsParser;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class MT940Statement {

    private static final Logger LOGGER = LoggerFactory.getLogger(MT940Statement.class);

    public MT940Statement(String tag_61, String tag_86) {
        this.tag_61 = tag_61;
        this.tag_86 = tag_86;
    }

    private String tag_61;
    private String tag_86;

    public Date getDate() {
        String date = null;
        try {
            date = tag_61.substring(0, 6);
            return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE_COMPACT.parse(date);
        } catch (Exception e) {
            LOGGER.error("{} tag_61: {} date: {}", FinTsConstants.LogTags.DATE_PARSING_ERROR, tag_61, date);
            return dateFallback(date);
        }
    }

    //Sparkasse sometimes sends invalid dates such as 180229
    //This date is not valid since 2018 is not a leap year, and February 29 is a leap day
    public Date dateFallback(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private double getAmount() {
        return FinTsParser.getMT940Amount(tag_61);
    }

    private String getDescription() {
        Map<String, String> elements = getRawDetails();
        return getDescriptionFromRaw(elements);
    }


    private String getDescriptionFromRaw(Map<String, String> elements){
        StringBuilder builder = new StringBuilder();

        //DKB - "00" -> "EIGENE KREDITKARTENABRECHN."
        //Postbank - "00" -> "D GUT SEPA"
        //ING - "00" -> "Gutschrift"
        if(elements.containsKey("00")){
            builder.append(elements.get("00") + " ");
        }

        //Deutche - "20" -> "EREF+5208037201663421051815"
        //Comdirect - "20" -> "ÜBERTRAG/ÜBERWEISUNG"
        //DKB - "20" -> "VISA-ABR. 4930000020723497"
        //ING - "20" -> "SVWZ+play"
        if(elements.containsKey("20") ){
            builder.append(elements.get("20") + " ");
        }

        if(elements.containsKey("21")){
            builder.append(elements.get("21") + " ");
        }

        //Deutche - "24" -> "SVWZ+SUBWAY RESTAURANT PASS"
        //Comdirect - "24" -> "NICHT ANGEGEBEN"
        //Postbank - "22" -> "To own account"
        if(elements.containsKey("24")){
            builder.append(elements.get("24") + " ");
        }

        //Deutche - "32" -> "SG-CardProcess GmbH"
        //Comdirect - "32" -> "AEGEE-PASSAU"
        //DKB - "32" -> "KREDITKARTENABRECHNUNG"
        //Postbank - "32" -> "Jan Gillich"
        // ING - "32" -> "VERBEKEN CEDRIC"
        if(elements.containsKey("32")){
            builder.append(elements.get("32") + " ");
        }

        return builder.toString();
    }

    public Map<String, String> getRawDetails() {
        HashMap<String, String> result = new HashMap<>();
        String[] elements = tag_86.split("\\?");

        for (String s : elements) {
            if (s.length() < 2) {
                continue;
            }
            String key = s.substring(0, 2);
            String value = s.substring(2);
            result.put(key, value);
        }

        return result;
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(new Amount(FinTsConstants.CURRENCY, getAmount()))
                .setDate(getDate())
                .setDescription(getDescription())
                .setRawDetails(getRawDetails())
                .build();
    }

    private boolean hasField(String str, String tag) {
        return str.startsWith(tag) && !Strings.isNullOrEmpty(str.substring(2).trim());
    }

}
