package se.tink.backend.aggregation.agents.banks.sbab.model.response;

public class MakeTransferResponse {

    private String id;
    private String token;
    private String strutsTokenName;
    private String acceptUrl;
    private String referer;
    private boolean isBetweenUserAccounts;
    private String deleteUrl;
    private TransferEntity transferEntity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStrutsTokenName() {
        return strutsTokenName;
    }

    public void setStrutsTokenName(String strutsTokenName) {
        this.strutsTokenName = strutsTokenName;
    }

    public String getAcceptUrl() {
        return acceptUrl;
    }

    public void setAcceptUrl(String acceptUrl) {
        this.acceptUrl = acceptUrl;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public boolean isBetweenUserAccounts() {
        return isBetweenUserAccounts;
    }

    public void setIsBetweenUserAccounts(boolean isBetweenUserAccounts) {
        this.isBetweenUserAccounts = isBetweenUserAccounts;
    }

    public void setDeleteUrl(String deleteUrl) {
        this.deleteUrl = deleteUrl;
    }

    public String getDeleteUrl() {
        return deleteUrl;
    }

    public void setTransferEntity(TransferEntity transferEntity) {
        this.transferEntity = transferEntity;
    }

    public TransferEntity getTransferEntity() {
        return transferEntity;
    }
}
