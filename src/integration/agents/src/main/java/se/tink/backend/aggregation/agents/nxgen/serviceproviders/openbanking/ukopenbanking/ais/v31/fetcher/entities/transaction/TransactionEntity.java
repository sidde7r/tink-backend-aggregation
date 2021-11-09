package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.TransactionDateMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkObInstantDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.EntryStatusCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.TransactionMutability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.MerchantDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Getter
public class TransactionEntity {
    private String accountId;

    private String transactionId;

    private String transactionReference;

    private AmountEntity amount;

    private AmountEntity chargeAmount;

    private UkOpenBankingApiDefinitions.CreditDebitIndicator creditDebitIndicator;

    private UkOpenBankingApiDefinitions.EntryStatusCode status;

    private UkOpenBankingApiDefinitions.TransactionMutability transactionMutability =
            TransactionMutability.UNDEFINED;

    @JsonDeserialize(using = UkObInstantDeserializer.class)
    private Instant bookingDateTime;

    @JsonDeserialize(using = UkObInstantDeserializer.class)
    private Instant valueDateTime;

    private String transactionInformation;

    private BankTransactionCodeEntity bankTransactionCode;

    private ProprietaryBankTransactionCodeEntity proprietaryBankTransactionCode;

    private SupplementaryData supplementaryData;
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

    private MerchantDetailsEntity merchantDetails;

    private Object creditorAgent;

    private Object debtorAgent;

    private Object cardInstrument;

    private Object quotationDate;

    private Object exchangeRate;

    public Optional<String> getTransactionId() {
        return Optional.ofNullable(transactionId);
    }

    public Optional<MerchantDetailsEntity> getMerchantDetails() {
        return Optional.ofNullable(merchantDetails);
    }

    public Optional<String> getProprietaryBankTransactionCode() {
        return Optional.ofNullable(proprietaryBankTransactionCode)
                .map(ProprietaryBankTransactionCodeEntity::getCode);
    }

    public TransactionDates getTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setBookingDate(TransactionDateMapper.prepareTransactionDate(bookingDateTime));

        if (valueDateTime != null) {
            builder.setValueDate(TransactionDateMapper.prepareTransactionDate(valueDateTime));
        }

        return builder.build();
    }

    public Boolean isNotRejected() {
        return status != EntryStatusCode.REJECTED;
    }

    public Boolean isNotDeclined() {
        return !isDeclined();
    }

    public TransactionMutability getMutability() {
        return transactionMutability;
    }

    public ExactCurrencyAmount getSignedAmount() {
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
    public Date getDateOfTransaction() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return simpleDateFormat.parse(bookingDateTime.toString());
        } catch (ParseException e) {
            return Date.from(bookingDateTime);
        }
    }

    public boolean isPending() {
        return status == EntryStatusCode.PENDING;
    }

    public String getDescription() {
        return Optional.ofNullable(getTransactionInformation()).orElse(transactionReference);
    }

    @JsonIgnore
    public Boolean isNotZeroBalancingTransaction() {
        if (bankTransactionCode != null) {
            return !(bankTransactionCode.getCode().equalsIgnoreCase("AccountBalancing")
                    && bankTransactionCode.getSubCode().equalsIgnoreCase("ZeroBalancing"));
        }
        return false;
    }

    private Boolean isDeclined() {
        return supplementaryData != null
                && Optional.ofNullable(supplementaryData.getDeclined()).orElse(Boolean.FALSE);
    }

    // enriched getter with removing unnecessary marking of EndToEndID when it is not provided
    private String getTransactionInformation() {
        return transactionInformation != null
                ? transactionInformation
                        .replace("EndToEndID", "")
                        .replace("\n: NOTPROVIDED", "")
                        .replace("\n: NOT PROVIDED", "")
                : null;
    }
}
