package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities.AccountSummaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities.PaymentAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsSummaryResponse {
    private AccountSummaryEntity accountSummary;
    private int paymentsTodoItemCount;
    private PaymentAccountsEntity paymentAccounts;

    public AccountSummaryEntity getAccountSummary() {
        return accountSummary;
    }

    public int getPaymentsTodoItemCount() {
        return paymentsTodoItemCount;
    }

    public PaymentAccountsEntity getPaymentAccounts() {
        return paymentAccounts;
    }
}
