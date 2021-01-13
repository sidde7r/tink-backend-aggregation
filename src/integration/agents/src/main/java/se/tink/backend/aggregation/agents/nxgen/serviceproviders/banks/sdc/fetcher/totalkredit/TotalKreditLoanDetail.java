package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit;

import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode
class TotalKreditLoanDetail {
    private String label;
    private String value;
}
