package se.tink.backend.grpc.v1.converter.statistic;

import java.util.Date;
import org.junit.Test;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.StatisticNode;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import static org.assertj.core.api.Assertions.assertThat;

public class CoreStatisticNodeToGrpcStatisticNodeConverterTest {

    @Test
    public void testConvertWithCurrency() {
        CoreStatisticNodeToGrpcStatisticNodeConverter converter = new CoreStatisticNodeToGrpcStatisticNodeConverter();

        se.tink.backend.grpc.v1.converter.statistic.StatisticNode node = new se.tink.backend.grpc.v1.converter.statistic.StatisticNode();

        node.setAmount(100);
        node.setDescription("description");
        node.setPeriod(createDummyPeriod());

        StatisticNode result = converter.convertWithCurrency(node, "SEK");

        assertThat(result.getNodeValueCase().name()).isEqualToIgnoringCase("amount");

        assertThat(result.getAmount()).isNotNull();
        assertThat(result.getAmount().getCurrencyCode()).isEqualTo("SEK");
        assertThat(result.getAmount().getValue()).isEqualTo(NumberUtils.toExactNumber(100));
    }

    @Test
    public void testConvertWithoutCurrency() {
        CoreStatisticNodeToGrpcStatisticNodeConverter converter = new CoreStatisticNodeToGrpcStatisticNodeConverter();

        se.tink.backend.grpc.v1.converter.statistic.StatisticNode node = new se.tink.backend.grpc.v1.converter.statistic.StatisticNode();

        node.setAmount(100);
        node.setDescription("description");
        node.setPeriod(createDummyPeriod());

        StatisticNode result = converter.convertWithoutCurrency(node);

        assertThat(result.getNodeValueCase().name()).isEqualToIgnoringCase("value");

        assertThat(result.getValue()).isNotNull();
        assertThat(result.getValue()).isEqualTo(NumberUtils.toExactNumber(100));
    }

    private Period createDummyPeriod() {
        Period period = new Period();

        period.setClean(true);
        period.setName("2018-01");
        period.setStartDate(new Date());
        period.setEndDate(new Date());
        period.setResolution(ResolutionTypes.MONTHLY);

        return period;
    }
}
