package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDtoBase;

public class BoursoramaTransferDestinationFetcher extends FrTransferDestinationFetcher {

    public BoursoramaTransferDestinationFetcher(BoursoramaApiClient apiClient) {
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
