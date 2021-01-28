package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.entity.CardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    @JsonProperty("einvoice_url")
    private String einvoiceUrl;

    @JsonProperty("einvoice_url_external")
    private String einvoiceUrlExternal;

    @JsonProperty("transaction_id")
    private String transactionId;

    private boolean booked;
    private BigDecimal amount;
    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("booking_date")
    private Date bookingDate;

    @JsonProperty("interest_date")
    private String interestDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("transaction_date")
    private Date transactionDate;

    private String title;
    private String description;

    @JsonProperty("reference_number")
    private String referenceNumber;

    @JsonProperty("archive_id")
    private String archiveId;

    @JsonProperty("balance_after")
    private BigDecimal balanceAfter;

    @JsonProperty("to_account_number")
    private String toAccountNumber;

    @JsonProperty("to_account_name")
    private String toAccountName;

    @JsonProperty("to_account_address")
    private String toAccountAddress;

    @JsonProperty("to_account_country")
    private String toAccountCountry;

    @JsonProperty("from_account_number")
    private String fromAccountNumber;

    @JsonProperty("from_account_name")
    private String fromAccountName;

    @JsonProperty("from_account_address")
    private String fromAccountAddress;

    @JsonProperty("from_account_country")
    private String fromAccountCountry;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("bank_address")
    private String bankAddress;

    @JsonProperty("bank_code")
    private String bankCode;

    private String bic;
    private String message;

    @JsonProperty("bank_giro_information_available")
    private boolean bankGiroInformationAvailable;

    @JsonProperty("eredovisning_information_available")
    private boolean eredovisningInformationAvailable;

    @JsonProperty("actual_payer_name")
    private String actualPayerName;

    @JsonProperty("actual_beneficiary_name")
    private String actualBeneficiaryName;

    @JsonProperty("originators_reference")
    private String originatorsReference;

    @JsonProperty("initial_invoice_archive_id")
    private String initialInvoiceArchiveId;

    @JsonProperty("own_reference")
    private String ownReference;

    @JsonProperty("transfer_type")
    private String transferType;

    @JsonProperty("transfer_type_name")
    private String transferTypeName;

    @JsonProperty("requested_execution_date")
    private String requestedExecutionDate;

    @JsonProperty("counterpart_account_name")
    private String counterpartAccountName;

    @JsonProperty("transaction_type")
    private TransactionTypeEntity transactionType;

    private ExchangeEntity exchange;
    private CardEntity card;
    private MobileEntity mobile;

    @JsonProperty("services_fee")
    private ServicesFeeEntity servicesFee;

    @JsonProperty("additional_transaction_details")
    private String additionalTransactionDetails;

    @JsonProperty("grouping_id")
    private String groupingId;

    public Transaction toTinkTransaction() {
        Builder builder =
                (Builder)
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(amount, currency))
                                .setDate(getDate())
                                .setDescription(getTransactionDescription())
                                .setPending(!booked)
                                .addExternalSystemIds(
                                        TransactionExternalSystemIdType
                                                .PROVIDER_GIVEN_TRANSACTION_ID,
                                        transactionId);
        if (Objects.nonNull(exchange)) {
            builder.setPayload(TransactionPayloadTypes.EXCHANGE_RATE, exchange.getOriginalRate());
            builder.setPayload(
                    TransactionPayloadTypes.LOCAL_CURRENCY, exchange.getOriginalCurrency());
            builder.setPayload(
                    TransactionPayloadTypes.AMOUNT_IN_LOCAL_CURRENCY, exchange.getOriginalAmount());
        }

        return builder.build();
    }

    @JsonIgnore
    private String getTransactionDescription() {
        return description != null ? description : title;
    }

    private Date getDate() {
        if (booked) {
            return bookingDate;
        }
        return transactionDate;
    }
}
