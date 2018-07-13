package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountsSummaryResponse {
    private FrontPageHighlightEntity frontPageHighlight;
    private AccountsSummary accountSummary;
    private int paymentsTodoItemCount;
    private PaymentAccountsEntity paymentAccounts;

    public FrontPageHighlightEntity getFrontPageHighlight() {
        return frontPageHighlight;
    }

    public AccountsSummary getAccountSummary() {
        return accountSummary;
    }

    public int getPaymentsTodoItemCount() {
        return paymentsTodoItemCount;
    }

    public PaymentAccountsEntity getPaymentAccounts() {
        return paymentAccounts;
    }

    public List<TransactionalAccount> toTinkAccounts() {
        return accountSummary.toTinkAccounts();
    }
}
