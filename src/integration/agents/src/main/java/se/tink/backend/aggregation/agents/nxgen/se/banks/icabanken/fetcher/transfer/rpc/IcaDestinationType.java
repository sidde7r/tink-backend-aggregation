package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

public enum IcaDestinationType {
    PAYMENT("PaymentBg", "PaymentPg"),
    TRANSFER("TransferTo", "Transfer");

    private List<String> destinationType;

    IcaDestinationType(String... identifier) {
        destinationType = Lists.newArrayList(Arrays.asList(identifier));
    }

    public boolean contains(String type) {
        return destinationType.contains(type);
    }

    public boolean contains(List<String> types) {
        for (String type : types) {
            if (destinationType.contains(type)) {
                return true;
            }
        }

        return false;
    }
}
