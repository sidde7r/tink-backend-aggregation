package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

public enum ConsentStatus {
    RCVD(ThirdPartyAppStatus.WAITING), // Received
    PDNG(ThirdPartyAppStatus.WAITING), // Pending
    PATC(ThirdPartyAppStatus.WAITING),
    ACTC(ThirdPartyAppStatus.DONE), // Accepted
    ACFC(ThirdPartyAppStatus.WAITING),
    ACSP(ThirdPartyAppStatus.WAITING),
    ACSC(ThirdPartyAppStatus.WAITING),
    ACCC(ThirdPartyAppStatus.WAITING),
    RJCT(ThirdPartyAppStatus.CANCELLED), // Rejected
    CANC(ThirdPartyAppStatus.CANCELLED); // Canceled

    private ThirdPartyAppStatus thirdPartyAppStatus;

    ConsentStatus(ThirdPartyAppStatus thirdPartyAppStatus) {
        this.thirdPartyAppStatus = thirdPartyAppStatus;
    }

    public ThirdPartyAppStatus getThirdPartyAppStatus() {
        return thirdPartyAppStatus;
    }

    public boolean isNotFinalStatus() {
        return this != ACTC && this != CANC && this != RJCT;
    }

    public boolean isAcceptedStatus() {
        return this == ACTC;
    }
}
