package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDtoBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26ApiClient;

public class N26TransferDestinationFetcher extends FrTransferDestinationFetcher {

    public N26TransferDestinationFetcher(N26ApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected List<GeneralAccountEntity> getTrustedBeneficiariesAccounts() {
        return apiClient.getTrustedBeneficiaries()
                .map(TrustedBeneficiariesResponseDtoBase::getBeneficiaries)
                .orElseGet(Collections::emptyList).stream()
                .map(FrTransferDestinationFetcher::convertBeneficiaryDtoToEntity)
                .collect(Collectors.toList());
    }
}
