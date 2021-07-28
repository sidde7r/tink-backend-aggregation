package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.MastercardAgreementEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class GetAccountsResponse {

    private List<BankdataAccountEntity> accounts;
    private List<MastercardAgreementEntity> mastercardAgreements;
}
