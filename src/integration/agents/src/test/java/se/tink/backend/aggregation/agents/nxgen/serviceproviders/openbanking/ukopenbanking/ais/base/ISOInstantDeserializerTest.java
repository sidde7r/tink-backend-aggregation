package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;

public class ISOInstantDeserializerTest {
    private ObjectMapper mapper;

    private static final String TRANSACTION_DATE_WITH_ZULU_TIME =
            "{\"transactionDate\": \"2002-04-11T12:15:30.345Z\"}";
    private static final String TRANSACTION_DATE_WITH_UTC_1_TIME =
            "{\"transactionDate\": \"2002-04-11T12:15:30.345+01:00\"}";
    private static final String DEFAULT_TRANSACTION_DATE_WITH_ZULU_TIME =
            "{\"transactionDate\": \"2002-04-11T00:00:00.000Z\"}";

    @Before
    public void setUp() {
        mapper = new ObjectMapper(new JsonFactory());
    }

    @Test
    public void shouldDeserializeDefaultDateWithZuluTime() throws JsonProcessingException {
        TestEntity entity =
                mapper.readValue(DEFAULT_TRANSACTION_DATE_WITH_ZULU_TIME, TestEntity.class);

        assertThat(entity.getTransactionDate())
                .isEqualTo(Instant.parse("2002-04-11T00:00:00.000Z"));
    }

    @Test
    public void shouldDeserializeDateWithZuluTime() throws JsonProcessingException {
        TestEntity entity = mapper.readValue(TRANSACTION_DATE_WITH_ZULU_TIME, TestEntity.class);

        assertThat(entity.getTransactionDate())
                .isEqualTo(Instant.parse("2002-04-11T12:15:30.345Z"));
    }

    @Test
    public void shouldDeserializeDateWithUTC1Time() throws JsonProcessingException {
        TestEntity entity = mapper.readValue(TRANSACTION_DATE_WITH_UTC_1_TIME, TestEntity.class);

        assertThat(entity.getTransactionDate())
                .isEqualTo(Instant.parse("2002-04-11T11:15:30.345Z"));
    }

    @Getter
    private static class TestEntity {

        @JsonDeserialize(using = ISOInstantDeserializer.class)
        private Instant transactionDate;
    }
}
