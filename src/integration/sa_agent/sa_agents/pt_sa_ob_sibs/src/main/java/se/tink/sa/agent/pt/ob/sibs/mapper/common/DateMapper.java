package se.tink.sa.agent.pt.ob.sibs.mapper.common;

import java.util.Calendar;
import java.util.Date;
import org.springframework.stereotype.Component;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

@Component
public class DateMapper implements Mapper<com.google.type.Date, Date> {

    @Override
    public com.google.type.Date map(Date source, MappingContext mappingContext) {
        com.google.type.Date.Builder destBuilder = com.google.type.Date.newBuilder();

        Calendar cal = Calendar.getInstance();
        cal.setTime(source);

        destBuilder.setYear(cal.get(Calendar.YEAR));
        destBuilder.setMonth(cal.get(Calendar.MONTH));
        destBuilder.setDay(cal.get(Calendar.DAY_OF_MONTH));

        return destBuilder.build();
    }
}
