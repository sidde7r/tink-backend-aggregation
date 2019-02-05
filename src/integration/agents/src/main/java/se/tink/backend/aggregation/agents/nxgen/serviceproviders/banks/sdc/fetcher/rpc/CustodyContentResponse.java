package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCustodyContentGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCustodyDetailsModel;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonObject
public class CustodyContentResponse {

    private List<SdcCustodyContentGroup> depositContentGroups;
    // Deposit details should be the same as those with which this response was retrieved
    private SdcCustodyDetailsModel depositDetails;

    public List<Portfolio> toPortfolios() {
        if (depositDetails == null) {
            return Collections.emptyList();
        }
        return ImmutableList.of(depositDetails.toPortfolio(this));
    }

    public List<Instrument> toInstruments() {
        if (depositContentGroups == null) {
            return Collections.emptyList();
        }
        return depositContentGroups.stream()
                .flatMap(SdcCustodyContentGroup::toInstruments)
                .collect(Collectors.toList());
    }
}
