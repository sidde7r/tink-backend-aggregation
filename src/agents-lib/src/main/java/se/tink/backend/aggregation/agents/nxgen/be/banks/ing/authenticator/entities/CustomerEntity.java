package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class CustomerEntity {
    private String ingid;
    private String title;
    private String firstName;
    private String lastName;
    private String language;
    private String branchId;
    private String emailAddress;
    private String address;
    private String city;
    private String readOnly;
    private String bcsiId;
    private String privateBanker;
    private String token;
    private String tokenmol;
    private String appPbMStatus;
    private String bblAgreementFlag;
    @JsonProperty("CodeMktProfile")
    private String codeMktProfile;
    private String isOver18;

    public String getIngid() {
        return this.ingid;
    }

    public String getTitle() {
        return this.title;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getBranchId() {
        return this.branchId;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public String getAddress() {
        return this.address;
    }

    public String getCity() {
        return this.city;
    }

    public String getReadOnly() {
        return this.readOnly;
    }

    public String getBcsiId() {
        return this.bcsiId;
    }

    public String getPrivateBanker() {
        return this.privateBanker;
    }

    public String getToken() {
        return this.token;
    }

    public String getTokenmol() {
        return this.tokenmol;
    }

    public String getAppPbMStatus() {
        return this.appPbMStatus;
    }

    public String getBblAgreementFlag() {
        return this.bblAgreementFlag;
    }

    public String getCodeMktProfile() {
        return this.codeMktProfile;
    }

    public String getIsOver18() {
        return this.isOver18;
    }

    public Optional<HolderName> getHolderName() {
        return Optional.of(StringUtils.trimToEmpty(this.firstName) + " " + StringUtils.trimToEmpty(this.lastName))
                .map(StringUtils::trimToNull)
                .map(HolderName::new);
    }
}
