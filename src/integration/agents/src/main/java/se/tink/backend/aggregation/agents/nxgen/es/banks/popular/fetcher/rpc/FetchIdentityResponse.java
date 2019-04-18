package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.IdentityEntity;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData.EsIdentityDataBuilder;

@SuppressWarnings("unused")
public class FetchIdentityResponse extends BancoPopularResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchIdentityResponse.class);

    @JsonProperty("customBtn7ECOAS937F")
    private IdentityEntity identity;

    public IdentityData toTinkIdentity() {
        if (identity == null) {
            return null;
        }

        EsIdentityDataBuilder builder = EsIdentityData.builder();

        if (!Strings.isNullOrEmpty(identity.getNif())) {
            builder.setNifNumber(identity.getNif());
        } else {
            LOGGER.warn(
                    "Non-NIF document number, assuming passport number: {}",
                    identity.getDocumentNumber());
            builder.setPassportNumber(identity.getDocumentNumber());
        }

        return builder.addFirstNameElement(identity.getFirstName())
                .addSurnameElement(identity.getLastname1())
                .setDateOfBirth(null)
                .build();
    }
}
