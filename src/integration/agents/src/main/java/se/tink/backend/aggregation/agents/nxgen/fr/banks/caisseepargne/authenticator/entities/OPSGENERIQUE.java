package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OPSGENERIQUE {

    @JsonProperty("SENDER_NAME")
    private String sENDERNAME;

    @JsonProperty("texte_sms")
    private String texteSms;

    @JsonProperty("snid")
    private String snid;

    @JsonProperty("csid")
    private String csid;

    @JsonProperty("NOTIF_DESC")
    private String nOTIFDESC;

    @JsonProperty("typ_srv")
    private String typSrv;

    @JsonProperty("SEND_NOTIF")
    private String sENDNOTIF;

    @JsonProperty("typ_act")
    private String typAct;

    @JsonProperty("media")
    private String media;

    @JsonProperty("NOTIF_TITLE")
    private String nOTIFTITLE;

    @JsonProperty("DESC")
    private String dESC;

    @JsonProperty("etab")
    private String etab;
}
