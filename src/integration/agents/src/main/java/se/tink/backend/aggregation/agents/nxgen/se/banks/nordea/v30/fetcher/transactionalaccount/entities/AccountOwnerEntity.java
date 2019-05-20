package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@JsonObject
public class AccountOwnerEntity {
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty private String role;

    public boolean isOwner() {
        return role.equalsIgnoreCase("owner");
    }

    @JsonIgnore
    public HolderName getHolderName() {
        return new HolderName(lastName);
    }

    @JsonIgnore
    public String getOwnerName() {
        return lastName;
    }

    @JsonIgnore
    public IdentityData toIdentity(final Credentials credentials) {
        String[] names = lastName.split(",");
        IdentityData result;
        if (names.length != 2) {
            result = SeIdentityData.of(lastName, credentials.getField(Field.Key.USERNAME));
        } else {
            result = SeIdentityData.of(names[1], names[0], credentials.getField(Key.USERNAME));
        }
        return result;
    }
}
