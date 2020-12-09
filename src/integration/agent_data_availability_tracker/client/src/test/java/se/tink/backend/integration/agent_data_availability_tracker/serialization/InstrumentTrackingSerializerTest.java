package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;

public class InstrumentTrackingSerializerTest {

    private static final String SECRET_VALUE = "SecretValue";

    @Test
    public void ensureInstrument_withAllNullValues_doesNotThrowException() {

        new InstrumentTrackingSerializer(new Instrument());
    }

    @Test
    public void ensureSecretAccountFields_areRedacted() {

        ImmutableSet<String> secretFieldKeys =
                ImmutableSet.<String>builder()
                        .add("Instrument<null>.uniqueIdentifier")
                        .add("Instrument<null>.isin")
                        .add("Instrument<null>.marketPlace")
                        .add("Instrument<null>.ticker")
                        .add("Instrument<null>.rawType")
                        .build();

        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(SECRET_VALUE);
        instrument.setIsin(SECRET_VALUE);
        instrument.setMarketPlace(SECRET_VALUE);
        instrument.setTicker(SECRET_VALUE);
        instrument.setRawType(SECRET_VALUE);

        List<FieldEntry> entries = new InstrumentTrackingSerializer(instrument).buildList();

        Assert.assertTrue(
                "Failed: all secret are unlisted",
                TrackingSerializationTestHelper.isAllUnlisted(secretFieldKeys, entries));
    }

    @Test
    public void ensureInstrumentType_isTracked_andIncludedInKey() {

        Instrument instrument = new Instrument();
        instrument.setType(Instrument.Type.STOCK);

        List<FieldEntry> entries = new InstrumentTrackingSerializer(instrument).buildList();

        Assert.assertTrue(
                "Failed: has entry 'Instrument<STOCK>.type' with value == STOCK",
                TrackingSerializationTestHelper.hasFieldWithValue(
                        "Instrument<STOCK>.type", Instrument.Type.STOCK.toString(), entries));
    }

    @Test
    public void ensureFieldsWith_infinitePossibleValues_areNotTracked() {

        ImmutableSet<String> innumerableFieldKeys =
                ImmutableSet.<String>builder()
                        .add("Instrument<null>.averageAcquisitionPrice")
                        .add("Instrument<null>.marketValue")
                        .add("Instrument<null>.price")
                        .add("Instrument<null>.quantity")
                        .add("Instrument<null>.profit")
                        .build();

        Instrument instrument = new Instrument();
        instrument.setAverageAcquisitionPrice(1.0);
        instrument.setMarketValue(1.0);
        instrument.setPrice(1.0);
        instrument.setQuantity(1.0);
        instrument.setProfit(1.0);

        List<FieldEntry> entries = new InstrumentTrackingSerializer(instrument).buildList();

        Assert.assertTrue(
                "Failed: values of numerics are unlisted",
                TrackingSerializationTestHelper.isAllUnlisted(innumerableFieldKeys, entries));
    }

    @Test
    public void ensureCurrency_isTracked() {

        Instrument instrument = new Instrument();
        instrument.setCurrency("SEK");

        List<FieldEntry> entries = new InstrumentTrackingSerializer(instrument).buildList();

        Assert.assertTrue(
                "Failed: currency is listed",
                TrackingSerializationTestHelper.hasFieldWithValue(
                        "Instrument<null>.currency", "SEK", entries));
    }
}
