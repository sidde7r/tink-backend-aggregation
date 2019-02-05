package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class PaymentAccountsEntity {
    private List<PaymentAccountEntity> paymentAccounts;
    private List<TransferFromAccountsEntity> ownTransferFromAccounts;
    private List<TransferToAccountsEntity> ownTransferToAccounts;

    public List<PaymentAccountEntity> getPaymentAccounts() {
        return paymentAccounts;
    }

    public List<TransferFromAccountsEntity> getOwnTransferFromAccounts() {
        return ownTransferFromAccounts;
    }

    public List<TransferToAccountsEntity> getOwnTransferToAccounts() {
        return ownTransferToAccounts;
    }
}
