package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.DateEntity;

public class LocalDateToXml {
    public static DateEntity serializeLocalDateToXml(LocalDate date) {
        DateEntity dateEntity = new DateEntity();
        dateEntity.setDay(String.valueOf(date.getDayOfMonth()));
        dateEntity.setMonth(String.valueOf(date.getMonthValue()));
        dateEntity.setYear(String.valueOf(date.getYear()));
        return dateEntity;
    }
}
