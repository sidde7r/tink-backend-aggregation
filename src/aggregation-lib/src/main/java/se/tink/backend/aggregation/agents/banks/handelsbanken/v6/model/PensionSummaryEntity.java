package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.utils.StringUtils;

@JsonObject
public class PensionSummaryEntity {

    List<Property> items;

    public double toPaymentsMade() {
        if (items == null) {
            return 0;
        }
        return items.stream().filter(item -> "Inbetalningar".equalsIgnoreCase(item.getLabel()))
                .map(item -> StringUtils.parseAmount(item.getValue()))
                .findFirst().orElse(0d);
    }
}
