package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public abstract class ProfileEntity {
    private String activeProfileLanguage;
    private String url;
    private String bankId;
    private String customerNumber;
    private String bankName;
    private boolean customerInternational;
    private String customerName;
    private boolean youthProfile;
    private String id;
    private LinksEntity links;

    @JsonIgnore
    public String getHolderName() {
        return customerName;
    }
}
