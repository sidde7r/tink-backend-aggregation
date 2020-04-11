package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

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

        String description =
                Stream.of(
                                Objects.nonNull(remittanceInformationUnstructured)
                                        ? cleanUnstructuredInformation(
                                                remittanceInformationUnstructured)
                                        : null,
                                creditorName,
                                debtorName)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(
                                Stream.of(creditorAccount, debtorAccount)
                                        .filter(Objects::nonNull)
                                        .findFirst()
                                        .map(TransactionAccountEntity::getIban)
                                        .orElse(""))
                        .replace("<br>", "\n");

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
                .setDescription(description)
                .setAmount(transactionAmount.toAmount())
                .setDate(date)
                .setPayload(TransactionPayloadTypes.DETAILS, transactionType)
                .setPayload(
                        TransactionPayloadTypes.TRANSFER_ACCOUNT_EXTERNAL, getCounterPartyAccount())
                .setPayload(
                        TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL,
                        getCounterPartyName())
                .setPayload(
                        TransactionPayloadTypes.MESSAGE,
                        cleanUnstructuredInformation(remittanceInformationUnstructured))
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

    private String cleanUnstructuredInformation(String data) {
        if (data.contains("<br>")) {
            return data.substring(0, data.indexOf("<")).replace("Naam: ", "").trim();
        }
        if (data.contains("\\n")) {
            return data.substring(0, data.indexOf("\\n")).replace("Naam: ", "").trim();
        }
        return data;
    }
}
