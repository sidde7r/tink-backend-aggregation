package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class TransactionEntity {
    private String accountId;

    private String transactionId;

    private String transactionReference;

    private String statementReference;

    private AmountEntity amount;

    private AmountEntity chargeAmount;

    private UkOpenBankingApiDefinitions.CreditDebitIndicator creditDebitIndicator;

    private UkOpenBankingApiDefinitions.EntryStatusCode status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDateTime;

    private String transactionInformation;

    private BankTransactionCodeEntity bankTransactionCode;

    private ProprietaryBankTransactionCodeEntity proprietaryBankTransactionCode;

    /*
    TODO: Most banks supply AddressLine as string but Barclays is putting it as String
    array and this needs to be fixed at Barclays end. Until then we are ignoring this property as
    we dont use this. It could also be fixed by custom parser parsing string as well as string array
    to list.


    private List<String> addressLine;

    @JsonCreator
    public TransactionEntity(@JsonProperty("AddressLine") Object addressLine) {
        if (addressLine instanceof ArrayList) {
            this.addressLine = (List<String>) addressLine;
        } else {
            this.addressLine = new ArrayList();
            this.addressLine.add((String) addressLine);
        }
    }*/

    @JsonIgnore private String addressLine;

    private Object merchantDetails;

    private Object creditorAgent;

    private Object debtorAgent;

    private Object cardInstrument;

    private Object supplementaryData;

    private Object quotationDate;

    private Object exchangeRate;

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(getSignedAmount())
                .setDescription(transactionInformation)
                .setPending(status == UkOpenBankingApiDefinitions.EntryStatusCode.PENDING)
                .setDate(bookingDateTime)
                .build();
    }

    public CreditCardTransaction toCreditCardTransaction(CreditCardAccount account) {

        return CreditCardTransaction.builder()
                .setCreditAccount(account)
                .setAmount(getSignedAmount())
                .setDescription(transactionInformation)
                .setPending(status == UkOpenBankingApiDefinitions.EntryStatusCode.PENDING)
                .setDate(bookingDateTime)
                .build();
    }

    private ExactCurrencyAmount getSignedAmount() {
        ExactCurrencyAmount unsignedAmount =
                ExactCurrencyAmount.of(amount.getUnsignedAmount(), amount.getCurrency());

        return UkOpenBankingApiDefinitions.CreditDebitIndicator.CREDIT.equals(creditDebitIndicator)
                ? unsignedAmount
                : unsignedAmount.negate();
    }
}
