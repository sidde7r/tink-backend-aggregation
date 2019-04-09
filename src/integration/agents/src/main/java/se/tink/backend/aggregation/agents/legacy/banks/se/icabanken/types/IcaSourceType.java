package se.tink.backend.aggregation.agents.banks.se.icabanken.types;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

public enum IcaSourceType {
    PAYMENT("PaymentFrom"),
    TRANSFER("TransferFrom");

    private List<String> sourceType;

    IcaSourceType(String... sourceType) {
        this.sourceType = Lists.newArrayList(Arrays.asList(sourceType));
    }

    public boolean contains(List<String> types) {
        for (String type : types) {
            if (sourceType.contains(type)) {
                return true;
            }
        }

        return false;
    }
}
