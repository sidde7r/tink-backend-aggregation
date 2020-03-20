package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.rpc;

import com.google.api.client.util.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.entities.PaymentAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchPaymentAccountResponse {
    private List<PaymentAccountsEntity> paymentAccounts;

    public List<PaymentAccountsEntity> getPaymentAccounts() {
        return Optional.ofNullable(paymentAccounts).orElse(Lists.newArrayList());
    }
}
