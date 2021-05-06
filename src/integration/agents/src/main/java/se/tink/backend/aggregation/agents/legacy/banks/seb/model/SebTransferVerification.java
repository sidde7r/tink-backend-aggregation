package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/** Response that we get before a transaction is signed with information about the transfer. */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class SebTransferVerification {
    @JsonProperty("ROW_ID")
    private String rowId;

    @JsonProperty("VERIF_REQ")
    private String verificationRequired;

    @JsonProperty("DATA1_TEXT")
    private String data1;

    @JsonProperty("DATA2_TEXT")
    private String data2;

    @JsonProperty("SIGN_TEXT")
    private String signText;
}
