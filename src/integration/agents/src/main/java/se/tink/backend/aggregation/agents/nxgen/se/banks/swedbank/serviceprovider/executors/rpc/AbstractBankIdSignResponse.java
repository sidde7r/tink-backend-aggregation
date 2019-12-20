package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;

public abstract class AbstractBankIdSignResponse extends AbstractBankIdResponse {
    private String signingStatus;
    private LinkEntity imageChallenge;
    private boolean qrCodeSigning;

    public boolean isQrCodeSigning() {
        return qrCodeSigning;
    }

    public LinkEntity getImageChallengeData() {
        return imageChallenge;
    }

    @JsonIgnore
    public SwedbankBaseConstants.BankIdResponseStatus getBankIdStatus() {
        return SwedbankBaseConstants.BankIdResponseStatus.fromStatusCode(signingStatus);
    }

    public String getSigningStatus() {
        return signingStatus;
    }
}
