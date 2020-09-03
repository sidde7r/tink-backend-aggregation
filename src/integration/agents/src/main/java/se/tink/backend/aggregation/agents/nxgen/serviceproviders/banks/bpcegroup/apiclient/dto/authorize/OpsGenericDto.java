package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class OpsGenericDto {

    @JsonProperty("SENDER_NAME")
    private String senderName;

    @JsonProperty("texte_sms")
    private String smsText;

    private String snid;

    private String csid;

    @JsonProperty("NOTIF_DESC")
    private String notifDesc;

    @JsonProperty("typ_srv")
    private String typSrv;

    @JsonProperty("SEND_NOTIF")
    private String sendNotif;

    @JsonProperty("typ_act")
    private String typAct;

    private String media;

    @JsonProperty("NOTIF_TITLE")
    private String notifTitle;

    @JsonProperty("DESC")
    private String desc;

    private String etab;
}
