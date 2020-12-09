package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Portfolio;

public class PortfolioTrackingSerializerTest {

    private static final String SECRET_VALUE = "SecretValue";
    private static final String VALUE_NOT_LISTED = TrackingList.Builder.VALUE_NOT_LISTED;

    @Test
    public void ensurePortfolio_withAllNullValues_doesNotThrowException_andAllFieldsAreNull() {

        List<FieldEntry> entries = new PortfolioTrackingSerializer(new Portfolio()).buildList();

        Assert.assertTrue(
                "Failed: all values null",
                entries.stream().map(FieldEntry::getValue).allMatch("null"::equals));
    }

    @Test
    public void ensureSecretAccountFields_areRedacted() {

        ImmutableSet<String> secretFieldKeys =
                ImmutableSet.<String>builder()
                        .add("Portfolio<null>.uniqueIdentifier")
                        .add("Portfolio<null>.rawType")
                        .build();

        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(SECRET_VALUE);
        portfolio.setRawType(SECRET_VALUE);

        List<FieldEntry> entries = new PortfolioTrackingSerializer(portfolio).buildList();

        Assert.assertTrue(
                "Failed: all secret entries are unlisted",
                TrackingSerializationTestHelper.isAllUnlisted(secretFieldKeys, entries));
    }

    @Test
    public void ensurePortfolioType_isTracked_andIncludedInKey() {

        Portfolio portfolio = new Portfolio();
        portfolio.setType(Portfolio.Type.ISK);

        List<FieldEntry> entries = new PortfolioTrackingSerializer(portfolio).buildList();

        Assert.assertTrue(
                "Failed: has entry 'Portfolio<ISK>.type' with value == ISK",
                TrackingSerializationTestHelper.hasFieldWithValue(
                        "Portfolio<ISK>.type", Portfolio.Type.ISK.toString(), entries));
    }

    @Test
    public void ensureFieldsWith_infinitePossibleValues_areNotTracked() {

        ImmutableSet<String> innumerableFieldKeys =
                ImmutableSet.<String>builder()
                        .add("Portfolio<null>.totalProfit")
                        .add("Portfolio<null>.cashValue")
                        .add("Portfolio<null>.totalValue")
                        .build();

        Portfolio portfolio = new Portfolio();
        portfolio.setTotalValue(1.0);
        portfolio.setCashValue(1.0);
        portfolio.setTotalProfit(1.0);

        List<FieldEntry> entries = new PortfolioTrackingSerializer(portfolio).buildList();

        Assert.assertTrue(
                "Failed: values of numerics are unlisted",
                TrackingSerializationTestHelper.isAllUnlisted(innumerableFieldKeys, entries));
    }
}
