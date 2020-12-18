package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
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
    private AdressEntity address;

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

    private static class AdressEntity {

        @JsonProperty("utdelningsadress")
        private String distributionAddress;

        @JsonProperty("isUtlandsk")
        private boolean isForeign;

        @JsonProperty("postnummerPostort")
        private String postalCode;
    }

    public static class CustomerEntity {

        @JsonProperty("exception")
        private boolean exception;

        @JsonProperty("epostadress")
        private String email;

        @JsonProperty("meddelandesatt")
        private String typeOfMessage;

        @JsonProperty("mobilnummer")
        private String phoneNumber;

        @JsonProperty("ekundsStatus")
        private String customerStatus;

        @JsonProperty("csnNummer")
        private int csnNumber;

        @JsonProperty("event")
        private Object event;

        @JsonProperty("dtoexception")
        private Object dtoexception;

        @JsonProperty("csnException")
        private Object csnException;

        @JsonProperty("transactionId")
        private Object transactionId;
    }

    private String getAccountId() {
        return Integer.toString(csnNumber);
    }

    public IdentityData getIdentityData() {
        return SeIdentityData.of(name, ssn);
    }

    public IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(getAccountId())
                .withAccountNumber(getAccountId())
                .withAccountName(name)
                .addIdentifier(AccountIdentifier.create(Type.TINK, getAccountId()))
                .build();
    }
}
