package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.EntryStatusCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDate;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

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

    private UkOpenBankingApiDefinitions.TransactionMutability transactionMutability;

    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant bookingDateTime;

    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant valueDateTime;

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
        return (Transaction)
                Transaction.builder()
                        .setAmount(getSignedAmount())
                        .setDescription(transactionInformation)
                        .setPending(status == EntryStatusCode.PENDING)
                        .setDate(getDateOfTransaction())
                        .setMutable(isMutable())
                        .addTransactionDates(getTransactionDates())
                        .build();
    }

    public CreditCardTransaction toCreditCardTransaction(CreditCardAccount account) {

        return (CreditCardTransaction)
                CreditCardTransaction.builder()
                        .setCreditAccount(account)
                        .setAmount(getSignedAmount())
                        .setDescription(transactionInformation)
                        .setPending(status == EntryStatusCode.PENDING)
                        .setMutable(isMutable())
                        .setDate(getDateOfTransaction())
                        .addTransactionDates(getTransactionDates())
                        .build();
    }

    private ArrayList<TransactionDate> getTransactionDates() {
        ArrayList<TransactionDate> transactionDates = new ArrayList<>();
        AvailableDateInformation bookingDateInformation = new AvailableDateInformation();
        bookingDateInformation.setInstant(bookingDateTime);
        transactionDates.add(
                TransactionDate.builder()
                        .type(TransactionDateType.BOOKING_DATE)
                        .value(bookingDateInformation)
                        .build());

        if (valueDateTime != null) {
            AvailableDateInformation valueDateInformation = new AvailableDateInformation();
            valueDateInformation.setInstant(valueDateTime);
            transactionDates.add(
                    TransactionDate.builder()
                            .type(TransactionDateType.VALUE_DATE)
                            .value(valueDateInformation)
                            .build());
        }
        return transactionDates;
    }

    private Boolean isMutable() {
        if (transactionMutability != null && status == EntryStatusCode.BOOKED) {
            return transactionMutability.isMutable();
        } else {
            return status == EntryStatusCode.PENDING;
        }
    }

    private ExactCurrencyAmount getSignedAmount() {
        ExactCurrencyAmount unsignedAmount =
                ExactCurrencyAmount.of(amount.getUnsignedAmount(), amount.getCurrency());

        return UkOpenBankingApiDefinitions.CreditDebitIndicator.CREDIT.equals(creditDebitIndicator)
                ? unsignedAmount
                : unsignedAmount.negate();
    }

    /**
     * That is the previous date formatting and we will stay with that formatting in date field
     *
     * @return date in `yyyy-MM-dd` pattern
     */
    private Date getDateOfTransaction() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return simpleDateFormat.parse(bookingDateTime.toString());
        } catch (ParseException e) {
            return Date.from(bookingDateTime);
        }
    }
}
