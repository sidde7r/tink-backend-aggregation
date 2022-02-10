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

public class UkObInstantDeserializerTest {
    private ObjectMapper mapper;
    // ArbuthnotLatham
    private static final String DEFAULT_TRANSACTION_DATE_WITHOUT_ZULU_ZONE_ID_WITHOUT_MILLISECONDS =
            "{\"transactionDate\": \"2021-09-13T00:00:00\"}";
    private static final String DEFAULT_TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITHOUT_MILLISECONDS =
            "{\"transactionDate\": \"2021-09-13T00:00:00Z\"}";
    private static final String DEFAULT_TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITH_MILLISECONDS =
            "{\"transactionDate\": \"2021-09-13T00:00:00.000Z\"}";
    private static final String TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITHOUT_MILLISECONDS =
            "{\"transactionDate\": \"2021-09-13T11:16:40Z\"}";
    private static final String TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITH_MILLISECONDS =
            "{\"transactionDate\": \"2021-09-13T11:16:40.345Z\"}";
    // Revolut, TSB
    private static final String TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITH_MICROSECONDS =
            "{\"transactionDate\": \"2021-09-13T11:16:40.345678Z\"}";
    // Creation
    private static final String TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITH_NANOSECONDS =
            "{\"transactionDate\": \"2021-09-13T11:16:40.3456789Z\"}";
    // HSBC Kinetic
    private static final String TRANSACTION_DATE_WITH_UTC_1_OFFSET_WITHOUT_SECONDS =
            "{\"transactionDate\": \"2021-09-13T11:16+01:00\"}";
    private static final String TRANSACTION_DATE_WITH_UTC_1_OFFSET =
            "{\"transactionDate\": \"2021-09-13T11:16:40.345+01:00\"}";
    // NatWest
    private static final String TRANSACTION_DATE_WITH_HH_OFFSET =
            "{\"transactionDate\": \"2021-09-13T11:16:40.345+01\"}";
    // Santander
    private static final String TRANSACTION_DATE_WITH_ZERO_OFFSET =
            "{\"transactionDate\": \"2021-09-13T10:16:40.345+0000\"}";

    @Before
    public void setUp() {
        mapper = new ObjectMapper(new JsonFactory());
    }

    @Test
    public void shouldDeserializeDefaultDateWithoutZuluZoneIdWithoutMilliseconds()
            throws JsonProcessingException {
        TestEntity entity =
                mapper.readValue(
                        DEFAULT_TRANSACTION_DATE_WITHOUT_ZULU_ZONE_ID_WITHOUT_MILLISECONDS,
                        TestEntity.class);

        assertThat(entity.getTransactionDate()).isEqualTo(Instant.parse("2021-09-13T00:00:00Z"));
    }

    @Test
    public void shouldDeserializeDefaultDateWithZuluZoneIdWithoutMilliseconds()
            throws JsonProcessingException {
        TestEntity entity =
                mapper.readValue(
                        DEFAULT_TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITHOUT_MILLISECONDS,
                        TestEntity.class);

        assertThat(entity.getTransactionDate()).isEqualTo(Instant.parse("2021-09-13T00:00:00Z"));
    }

    @Test
    public void shouldDeserializeDefaultDateWithZuluZoneIdWithMilliseconds()
            throws JsonProcessingException {
        TestEntity entity =
                mapper.readValue(
                        DEFAULT_TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITH_MILLISECONDS,
                        TestEntity.class);

        assertThat(entity.getTransactionDate())
                .isEqualTo(Instant.parse("2021-09-13T00:00:00.000Z"));
    }

    @Test
    public void shouldDeserializeDateWithZuluZoneIdWithoutMilliseconds()
            throws JsonProcessingException {
        TestEntity entity =
                mapper.readValue(
                        TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITHOUT_MILLISECONDS, TestEntity.class);

        assertThat(entity.getTransactionDate()).isEqualTo(Instant.parse("2021-09-13T11:16:40Z"));
    }

    @Test
    public void shouldDeserializeDateWithZuluZoneIdWithMilliseconds()
            throws JsonProcessingException {
        TestEntity entity =
                mapper.readValue(
                        TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITH_MILLISECONDS, TestEntity.class);

        assertThat(entity.getTransactionDate())
                .isEqualTo(Instant.parse("2021-09-13T11:16:40.345Z"));
    }

    @Test
    public void shouldDeserializeDateWithZuluZoneIdWithMicroseconds()
            throws JsonProcessingException {
        TestEntity entity =
                mapper.readValue(
                        TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITH_MICROSECONDS, TestEntity.class);

        assertThat(entity.getTransactionDate())
                .isEqualTo(Instant.parse("2021-09-13T11:16:40.345678Z"));
    }

    @Test
    public void shouldDeserializeDateWithZuluZoneIdWith7DigitNanoseconds()
            throws JsonProcessingException {
        TestEntity entity =
                mapper.readValue(
                        TRANSACTION_DATE_WITH_ZULU_ZONE_ID_WITH_NANOSECONDS, TestEntity.class);

        assertThat(entity.getTransactionDate())
                .isEqualTo(Instant.parse("2021-09-13T11:16:40.3456789Z"));
    }

    @Test
    public void shouldDeserializeDateWithUTC1OffsetWithoutSeconds() throws JsonProcessingException {
        TestEntity entity =
                mapper.readValue(
                        TRANSACTION_DATE_WITH_UTC_1_OFFSET_WITHOUT_SECONDS, TestEntity.class);

        assertThat(entity.getTransactionDate()).isEqualTo(Instant.parse("2021-09-13T10:16:00Z"));
    }

    @Test
    public void shouldDeserializeDateWithUTC1Offset() throws JsonProcessingException {
        TestEntity entity = mapper.readValue(TRANSACTION_DATE_WITH_UTC_1_OFFSET, TestEntity.class);

        assertThat(entity.getTransactionDate())
                .isEqualTo(Instant.parse("2021-09-13T10:16:40.345Z"));
    }

    @Test
    public void shouldDeserializeDateWithHHOffset() throws JsonProcessingException {
        TestEntity entity = mapper.readValue(TRANSACTION_DATE_WITH_HH_OFFSET, TestEntity.class);

        assertThat(entity.getTransactionDate())
                .isEqualTo(Instant.parse("2021-09-13T10:16:40.345Z"));
    }

    @Test
    public void shouldDeserializeDateWithNoOffset() throws JsonProcessingException {
        TestEntity entity = mapper.readValue(TRANSACTION_DATE_WITH_ZERO_OFFSET, TestEntity.class);

        assertThat(entity.getTransactionDate())
                .isEqualTo(Instant.parse("2021-09-13T10:16:40.345Z"));
    }

    @Getter
    private static class TestEntity {

        @JsonDeserialize(using = UkObInstantDeserializer.class)
        private Instant transactionDate;
    }
}
