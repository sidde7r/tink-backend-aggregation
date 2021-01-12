package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode
class TotalKreditLoan {
    private String title;
    private String description;
    private TotalKreditLoanAmount amount;
    private List<TotalKreditLoanDetail> details = new ArrayList<>();
}
