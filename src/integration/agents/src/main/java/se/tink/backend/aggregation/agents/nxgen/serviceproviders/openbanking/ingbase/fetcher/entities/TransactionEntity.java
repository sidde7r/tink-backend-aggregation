package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    private String transactionId;
    private String endToEndId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    private String executionDateTime;

    private TransactionAmountEntity transactionAmount;
    private String creditorName;
    private TransactionAccountEntity creditorAccount;
    private String debtorName;
    private TransactionAccountEntity debtorAccount;
    private String transactionType;
    private String remittanceInformationUnstructured;
    private RemittanceInformationEntity remittanceInformationStructured;

    public Transaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    public Transaction toPendingTinkTransaction() {
        return toTinkTransaction(true);
    }

    private Transaction toTinkTransaction(boolean isPending) {

        Date executionDate = null;
        if (!Strings.isNullOrEmpty(executionDateTime)) {
            try {
                executionDate =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(executionDateTime);
            } catch (ParseException e) {
                try {
                    executionDate =
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    .parse(executionDateTime);
                } catch (ParseException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }

        Date date =
                Stream.of(bookingDate, executionDate, valueDate)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(new Date());

        return Transaction.builder()
                .setPending(isPending)
                .setDescription(getDescription(transactionAmount.toAmount()))
                .setAmount(transactionAmount.toAmount())
                .setDate(date)
                // IMPORTANT! Do not change the transaction payload without consulting the
                // enrichment team! They have logic relying on the payload, changing it may cause
                // disruptions with categorization and description cleanup.
                .setPayload(TransactionPayloadTypes.DETAILS, transactionType)
                .setPayload(
                        TransactionPayloadTypes.TRANSFER_ACCOUNT_EXTERNAL, getCounterPartyAccount())
                .setPayload(
                        TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL,
                        getCounterPartyName())
                .setPayload(TransactionPayloadTypes.MESSAGE, getRemittanceInformationUnstructured())
                .build();
    }

    private String getCounterPartyAccount() {
        return Stream.of(creditorAccount, debtorAccount)
                .filter(Objects::nonNull)
                .map(TransactionAccountEntity::getIban)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    private String getCounterPartyName() {
        return Stream.of(creditorName, debtorName)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse("");
    }

    public String getDescription(ExactCurrencyAmount transactionAmount) {

        if (transactionAmount.getExactValue().intValue() > 0) {
            if (StringUtils.isNotEmpty(debtorName)) {
                return debtorName;
            }
        } else if (StringUtils.isNotEmpty(creditorName)) {
            if ((creditorName.toLowerCase().contains("paypal")
                            || creditorName.toLowerCase().contains("klarna"))
                    && StringUtils.isNotEmpty(remittanceInformationUnstructured)) {
                return remittanceInformationUnstructured;
            } else {
                return creditorName;
            }
        }
        return remittanceInformationUnstructured;
    }

    @VisibleForTesting
    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured == null
                ? ""
                : remittanceInformationUnstructured.replace("<br>", "\n");
    }
}
