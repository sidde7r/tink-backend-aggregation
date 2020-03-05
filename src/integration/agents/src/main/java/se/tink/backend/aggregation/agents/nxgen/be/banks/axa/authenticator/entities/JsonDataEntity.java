package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class JsonDataEntity {

    private String channel;
    private String format;

    @JsonProperty("mark_for_whitewash")
    private String markForWhitewash;

    @JsonProperty("ns2:associateId")
    private String associateId;

    @JsonProperty("ns2:axaCardNumber")
    private String axaCardNumber;

    @JsonProperty("ns2:firstName")
    private String firstName;

    @JsonProperty("ns2:guid")
    private String guid;

    @JsonProperty("ns2:lastName")
    private String lastName;

    @JsonProperty("ns2:managingPointOfSaleId")
    private String managingPointOfSaleId;

    @JsonProperty("ns2:panSequenceNumber")
    private String panSequenceNumber;

    @JsonProperty("ns2:partyId")
    private String partyId;

    private String target;
    private String transmitTicketId;

    public String getChannel() {
        return channel;
    }

    public String getFormat() {
        return format;
    }

    public String getMarkForWhitewash() {
        return markForWhitewash;
    }

    public String getAssociateId() {
        return associateId;
    }

    public String getAxaCardNumber() {
        return axaCardNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getGuid() {
        return guid;
    }

    public String getLastName() {
        return lastName;
    }

    public String getManagingPointOfSaleId() {
        return managingPointOfSaleId;
    }

    public String getPanSequenceNumber() {
        return panSequenceNumber;
    }

    public String getPartyId() {
        return partyId;
    }

    public String getTarget() {
        return target;
    }

    public String getTransmitTicketId() {
        return transmitTicketId;
    }
}
