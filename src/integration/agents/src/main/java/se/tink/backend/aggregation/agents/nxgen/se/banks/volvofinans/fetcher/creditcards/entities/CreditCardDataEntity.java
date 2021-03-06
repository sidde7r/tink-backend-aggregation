package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardDataEntity {

    @Getter
    @JsonProperty("kontoId")
    private String accountId;

    @Getter
    @JsonProperty("kortNamn")
    private String cardName;

    private String kortId;
    private String kontonummer;
    private int kortlopnummer;
    private String kortalias;
    private int kortPanSequenceNbr;
    private String trunkeratKortnummer;
    private String kortaktiveringsstatus;
    private String produktnamn;
    private String produktkod;
    private boolean kanAktiveras;
    private String aktiveringsanledning;
    private boolean kanBestallaKopiaPin;
    private boolean kanBestallaNyttKort;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date aktiveringsdatum;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date utgangsdatum;

    private boolean kanSparras;
    private boolean kanRegionsparras;
    private boolean kanTokeniseras;
    private boolean sparrat;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date sparrDatum;
}
