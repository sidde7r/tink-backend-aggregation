package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.RequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitEnrollResponseEntity extends BaseMobileResponseEntity {
    private String virtualCardNumber;
    private String virtualCardExpirationDate;
    private String pin;
    private String registrationCode;
    private String authorisationTrustedBeneficiaries;
    private String authorisationTrustedBeneficiariesPlus;
    private String authorisationThirdPartyTransfer;
    private String authorisationThirdPartyTransferPlus;
    private String authorisationSignByTwo;
    private String authorisationSignByTwoPlus;
    private List<RequestEntity> requests;

    public String getVirtualCardNumber() {
        return virtualCardNumber;
    }

    public String getVirtualCardExpirationDate() {
        return virtualCardExpirationDate;
    }

    public String getPin() {
        return pin;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public String getAuthorisationTrustedBeneficiaries() {
        return authorisationTrustedBeneficiaries;
    }

    public String getAuthorisationTrustedBeneficiariesPlus() {
        return authorisationTrustedBeneficiariesPlus;
    }

    public String getAuthorisationThirdPartyTransfer() {
        return authorisationThirdPartyTransfer;
    }

    public String getAuthorisationThirdPartyTransferPlus() {
        return authorisationThirdPartyTransferPlus;
    }

    public String getAuthorisationSignByTwo() {
        return authorisationSignByTwo;
    }

    public String getAuthorisationSignByTwoPlus() {
        return authorisationSignByTwoPlus;
    }

    public List<RequestEntity> getRequests() {
        return requests;
    }
}
