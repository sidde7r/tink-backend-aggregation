package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.DateEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardDetailsResponse extends OmaspBaseResponse {
    private String cardType;
    private String cardRole;
    // `sharedResponsibility` is null - cannot define it!
    private String name;
    private DateEntity cardExpiration;
    private AmountEntity usedCredit;
    private AmountEntity creditBalance;
    private AmountEntity creditLimit;
    private AmountEntity available;
    private AmountEntity coverReservations;
    private String directDebitAccount;
    private AmountEntity debitBalance;
    private DateEntity lastPaymentDate;
    private AmountEntity lastPaymentAmount;
    private DateEntity nextBillingDate;
    private AmountEntity nextBillingAmount;
    private String minInstallmentPercentage;
    private AmountEntity minInstallmentAmount;
    private boolean installmentFreeMonths;
    // `installmentFreeMonthNames` is null - cannot define it!
    private List<CreditCardTransactionEntity> transactions;

    public List<CreditCardTransactionEntity> getTransactions() {
        return transactions;
    }
}
