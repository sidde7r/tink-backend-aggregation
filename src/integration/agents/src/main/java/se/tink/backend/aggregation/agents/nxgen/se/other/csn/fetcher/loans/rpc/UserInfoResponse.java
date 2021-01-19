package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.entities.AddressEntity;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.entities.CustomerEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@Getter
@JsonObject
public class UserInfoResponse {

    @JsonProperty("avliden")
    private String deceased;

    @JsonProperty("sekretess")
    private String secrecy;

    @JsonProperty("skuldsaneringsFlagga")
    private String debtRestructuringFlag;

    @JsonProperty("ekundStatus")
    private CustomerEntity customerEntity;

    @JsonProperty("antalReturer")
    private int numberOfReturns;

    @JsonProperty("adressFromDatum")
    private long addressFromDate;

    @JsonProperty("framtidaAdressGiltigLandbeteckning")
    private String futureAddressValidCountryDesignation;

    @JsonProperty("fbfAdressFinns")
    private boolean fbfAddressExists;

    @JsonProperty("inbetalningAdress")
    private Object paymentAdress;

    @JsonProperty("kommuntillhorighet")
    private String municipalAffiliation;

    @JsonProperty("namn")
    private String name;

    @JsonProperty("personnummer")
    private String ssn;

    @JsonProperty("adressGiltigLandbeteckning")
    private String addressValidCountryDesignation;

    @JsonProperty("adress")
    private AddressEntity address;

    @JsonProperty("formatPersonnummer")
    private String formatSSN;

    @JsonProperty("verifierad")
    private String verified;

    @JsonProperty("inloggad")
    private boolean isLoggedIn;

    @JsonProperty("inbetalningAdressGiltigLandbeteckning")
    private String paymentAddressValidCountryDesignation;

    @JsonProperty("medborgarskap")
    private String citizenship;

    @JsonProperty("adresstyp")
    private String addressType;

    @JsonProperty("csnNummer")
    private int csnNumber;

    @JsonProperty("fbfstatus")
    private String fbfstatus;

    public IdentityData getIdentityData() {
        return SeIdentityData.of(name, ssn);
    }
}
