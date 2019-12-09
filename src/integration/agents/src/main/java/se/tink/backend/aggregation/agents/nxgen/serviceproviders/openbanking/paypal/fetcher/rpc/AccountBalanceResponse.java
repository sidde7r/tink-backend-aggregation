package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.balance.BalanceAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountBalanceResponse {

    @JsonProperty("total_available")
    private AmountEntity totalAvailable;

    @JsonProperty("total_reserved")
    private AmountEntity totalReserved;

    @JsonProperty("balance_accounts")
    private List<BalanceAccountEntity> balanceAccounts;

    private List<LinkEntity> links;

    public Amount getAvailableBalance() {
        return totalAvailable.toTinkAmount();
    }
}
