package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KycEntity {

    @JsonProperty("usTaxPayer")
    private boolean usTaxPayer;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("firstNationality")
    private String firstNationality;

    @JsonProperty("address")
    private String address;

    @JsonProperty("documents")
    private List<DocumentsItemEntity> documents;

    @JsonProperty("countryOfBirth")
    private String countryOfBirth;

    public boolean isUsTaxPayer() {
        return usTaxPayer;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstNationality() {
        return firstNationality;
    }

    public String getAddress() {
        return address;
    }

    public List<DocumentsItemEntity> getDocuments() {
        return documents;
    }

    public String getCountryOfBirth() {
        return countryOfBirth;
    }
}
