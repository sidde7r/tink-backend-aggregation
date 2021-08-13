package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@JsonObject
@Getter
public class TransactionEntity {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate date;

    private String concept;
    private AmountEntity amount;
    private boolean canSplit;
    private String cardNumber;
    private AmountEntity balance;
    private boolean existDocument;
    private String apuntNumber;
    private String productCode;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate valueDate;

    private String conceptCode;
    private String conceptDetail;
    private String timeStamp;
    private String referencor;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate sessionDate;

    private String returnBillCode;
    private String numPAN;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return (Transaction)
                Transaction.builder()
                        .setAmount(amount.parseToTinkAmount())
                        .setDate(date)
                        .setDescription(concept)
                        .setTransactionReference(apuntNumber)
                        .setProviderMarket(MarketCode.ES.name())
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setValueDate(new AvailableDateInformation(valueDate))
                                        .setBookingDate(new AvailableDateInformation(sessionDate))
                                        .setExecutionDate(new AvailableDateInformation(sessionDate))
                                        .build())
                        .build();
    }

    public boolean isCanSplit() {
        return canSplit;
    }

    public boolean isExistDocument() {
        return existDocument;
    }
}
