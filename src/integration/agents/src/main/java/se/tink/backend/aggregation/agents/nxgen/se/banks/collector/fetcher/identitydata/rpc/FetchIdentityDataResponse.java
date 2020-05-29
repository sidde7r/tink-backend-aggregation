package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.identitydata.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@JsonObject
public class FetchIdentityDataResponse {

    private boolean isCustomer;

    private String lastName;

    private String givenName;

    private Payments payments;

    public boolean isIsCustomer() {
        return isCustomer;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGivenName() {
        return givenName;
    }

    public Payments getPayments() {
        return payments;
    }

    public IdentityData toTinkIdentityData(String ssn) {
        return SeIdentityData.of(givenName, lastName, ssn);
    }

    public class Payments {

        private Object directDebit;

        public Object getDirectDebit() {
            return directDebit;
        }
    }
}
