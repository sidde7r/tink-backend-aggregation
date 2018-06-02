package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

public class SdcAccountProperties {
    private boolean canRename;
    private boolean showAvailableAmount;
    private boolean debitable;
    private boolean creditable;
    private boolean mayQuery;
    private boolean loan;
    private boolean favorite;
    private boolean likvidityZone;

    public boolean isCanRename() {
        return canRename;
    }

    public boolean isShowAvailableAmount() {
        return showAvailableAmount;
    }

    public boolean isDebitable() {
        return debitable;
    }

    public boolean isCreditable() {
        return creditable;
    }

    public boolean isMayQuery() {
        return mayQuery;
    }

    public boolean isLoan() {
        return loan;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public boolean isLikvidityZone() {
        return likvidityZone;
    }
}
