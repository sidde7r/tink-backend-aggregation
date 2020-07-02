package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transfer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDtoBase;

public class BpceGroupTransferDestinationFetcher extends FrTransferDestinationFetcher {

    public BpceGroupTransferDestinationFetcher(BpceGroupApiClient apiClient) {
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
