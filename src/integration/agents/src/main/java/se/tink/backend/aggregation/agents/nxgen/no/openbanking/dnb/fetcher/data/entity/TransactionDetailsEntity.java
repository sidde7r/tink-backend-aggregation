package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@Getter
public class TransactionDetailsEntity {
    @JsonDeserialize(using = LocalDateDeserializer.class)
    protected LocalDate bookingDate;

    protected String additionalInformation;
    protected String bankTransactionCode;
    protected String checkId;
    protected String creditorName;
    protected String debtorName;
    protected String endToEndId;
    protected String entryReference;
    protected String mandateId;
    protected String proprietaryBankTransactionCode;
    protected String purposeCode;
    protected String remittanceInformationStructured;
    protected String remittanceInformationUnstructured;
    protected String transactionId;
    protected String transactionDetails;
    protected String ultimateCreditor;
    protected String ultimateDebtor;
    protected String creditorId;
    protected AmountEntity transactionAmount;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    protected LocalDate valueDate;
}
