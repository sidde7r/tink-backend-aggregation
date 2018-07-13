package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class AgreementEntity extends AbstractLinkResponse {
    private String documentUrl;
    private String effDt;
    private String effDtRcv;
    private String effTm;
    private String id;
    private String isoCountryCode;
    private String isoLngCode;
    private String tmpltId;
    private String name;
    private String tmpltVrsn;
    private String typeOfStaticDocId;
    private String hashKey;
    private boolean accepted;

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getEffDt() {
        return effDt;
    }

    public void setEffDt(String effDt) {
        this.effDt = effDt;
    }

    public String getEffDtRcv() {
        return effDtRcv;
    }

    public void setEffDtRcv(String effDtRcv) {
        this.effDtRcv = effDtRcv;
    }

    public String getEffTm() {
        return effTm;
    }

    public void setEffTm(String effTm) {
        this.effTm = effTm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsoCountryCode() {
        return isoCountryCode;
    }

    public void setIsoCountryCode(String isoCountryCode) {
        this.isoCountryCode = isoCountryCode;
    }

    public String getIsoLngCode() {
        return isoLngCode;
    }

    public void setIsoLngCode(String isoLngCode) {
        this.isoLngCode = isoLngCode;
    }

    public String getTmpltId() {
        return tmpltId;
    }

    public void setTmpltId(String tmpltId) {
        this.tmpltId = tmpltId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTmpltVrsn() {
        return tmpltVrsn;
    }

    public void setTmpltVrsn(String tmpltVrsn) {
        this.tmpltVrsn = tmpltVrsn;
    }

    public String getTypeOfStaticDocId() {
        return typeOfStaticDocId;
    }

    public void setTypeOfStaticDocId(String typeOfStaticDocId) {
        this.typeOfStaticDocId = typeOfStaticDocId;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

}
