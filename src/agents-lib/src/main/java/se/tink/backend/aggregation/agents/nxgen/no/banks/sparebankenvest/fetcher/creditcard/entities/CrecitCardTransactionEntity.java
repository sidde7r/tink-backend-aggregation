package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CrecitCardTransactionEntity {
    private int id;
    private String transtype;
    private String kundenummer;
    private String kortKontonummer;
    @JsonProperty("bokfoeringsdato")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date accountingDate;
    private String valutadato;
    @JsonProperty("belop")
    private double amount;
    @JsonProperty("alfareferanse")
    private String description;
    private String valuta;
    private double basisBelop;
    private String kategori;
    private int visningsEnhet;

    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setDate(accountingDate)
                .setAmount(Amount.inNOK(amount))
                .setDescription(getDescription())
                .build();
    }

    @JsonIgnore
    String getDescription() {
        if (description != null) {
            return description.trim();
        }

        return transtype;
    }
}
