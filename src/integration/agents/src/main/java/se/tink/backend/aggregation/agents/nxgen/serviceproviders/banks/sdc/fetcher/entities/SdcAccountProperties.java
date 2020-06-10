package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

public class SdcAccountProperties {
    private Boolean canRename;
    private Boolean showAvailableAmount;
    private Boolean debitable;
    private Boolean creditable;
    private Boolean mayQuery;
    private Boolean loan;
    private Boolean favorite;
    private Boolean likvidityZone;

    public Boolean isCanRename() {
        return canRename;
    }

    public Boolean isShowAvailableAmount() {
        return showAvailableAmount;
    }

    public Boolean isDebitable() {
        return debitable;
    }

    public Boolean isCreditable() {
        return creditable;
    }

    public Boolean isMayQuery() {
        return mayQuery;
    }

    public Boolean isLoan() {
        return loan;
    }

    public Boolean isFavorite() {
        return favorite;
    }

    public Boolean isLikvidityZone() {
        return likvidityZone;
    }
}
