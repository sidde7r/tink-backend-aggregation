package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit;

import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode
class TotalKreditLoanDetail {
    private String label;
    private String value;

    boolean is(final String desiredLabel) {
        return desiredLabel != null && desiredLabel.equals(label);
    }

    boolean isSimilar(final String desiredLabel) {
        return label != null && label.startsWith(desiredLabel);
    }

    String value() {
        return value;
    }
}
