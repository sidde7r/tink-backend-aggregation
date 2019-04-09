package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Response that we get before a transaction is signed with information about the transfer. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SebTransferVerification {
    @JsonProperty("ROW_ID")
    public String RowId;

    @JsonProperty("VERIF_REQ")
    public String VerificationRequired;

    @JsonProperty("DATA1_TEXT")
    public String Data1;

    @JsonProperty("DATA2_TEXT")
    public String Data2;

    @JsonProperty("SIGN_TEXT")
    public String SignText;
}
