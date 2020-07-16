package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BpcestaQueryParamData {

    @JsonCreator
    public BpcestaQueryParamData(@JsonProperty("cdetab") String cdetab, String typSrv) {
        this.typSrv = typSrv;
        this.cdetab = cdetab;
    }

    @JsonProperty("cdetab")
    private String cdetab;

    @JsonProperty("enseigne")
    private String enseigne = "ce";

    @JsonProperty("csid")
    private String csid = UUID.randomUUID().toString();

    @JsonProperty("typ_srv")
    private String typSrv;

    @JsonProperty("typ_app")
    private String typApp = "rest";

    @JsonProperty("typ_act")
    private String typAct = "sso";

    @JsonProperty("term_id")
    private String termId = UUID.randomUUID().toString();

    @JsonProperty("snid")
    private String snid = "123456";

    @JsonProperty("typ_sp")
    private String typSp = "out-brand";
}
