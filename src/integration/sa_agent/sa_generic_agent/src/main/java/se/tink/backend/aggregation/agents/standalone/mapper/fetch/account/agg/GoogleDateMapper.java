package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import java.util.Calendar;
import java.util.Date;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class GoogleDateMapper implements Mapper<Date, com.google.type.Date> {

    @Override
    public Date map(com.google.type.Date source, MappingContext context) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, source.getYear());
        calendar.set(Calendar.MONTH, source.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, source.getDay());
        return calendar.getTime();
    }
}
