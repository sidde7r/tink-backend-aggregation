package se.tink.backend.aggregation.nxgen.core.transaction;

import com.google.common.collect.ImmutableMap;
import java.util.Date;
import java.util.Map;
import lombok.Value;

@Value
public class DateFieldMigration implements FieldMigration {
    // previous date value in milisecond
    long originalDate;

    public static Map<String, FieldMigration> version1(Date previousDate) {
        return ImmutableMap.of("v1", new DateFieldMigration(previousDate.getTime()));
    }
}
