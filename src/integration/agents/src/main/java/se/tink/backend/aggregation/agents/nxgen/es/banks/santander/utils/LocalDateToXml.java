package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.DateEntity;

public class LocalDateToXml {
    public static DateEntity serializeLocalDateToXml(LocalDate date) {
        final DateEntity dateEntity = new DateEntity();
        dateEntity.setDay(String.valueOf(date.getDayOfMonth()));
        dateEntity.setMonth(String.valueOf(date.getMonthValue()));
        dateEntity.setYear(String.valueOf(date.getYear()));
        return dateEntity;
    }

    public static String convertXmlToString(DateEntity dataEntity) {
        final String serialized;
        try {
            final XmlMapper xmlMapper = new XmlMapper();
            serialized = xmlMapper.writeValueAsString(dataEntity);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        return removeTopLevelXml(serialized);
    }

    private static String removeTopLevelXml(String serialized) {
        final Pattern p = Pattern.compile("^<[^>]+>(.*)<\\/\\w+>$", Pattern.DOTALL);
        final Matcher matcher = p.matcher(serialized);
        return matcher.replaceFirst("$1");
    }
}
