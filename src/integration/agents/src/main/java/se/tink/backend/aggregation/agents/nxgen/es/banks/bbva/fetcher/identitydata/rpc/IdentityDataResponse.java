package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.rpc;

import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity.ExternalPlatformUsersEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity.IdentityDocumentEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity.ManagerEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityDataResponse {
    private CustomerEntity customer;
    private ManagerEntity manager;
    private List<ExternalPlatformUsersEntity> externalPlatformUsers;
    private String encryptedAlias;

    /**
     * Don't know in what cases this list will contain more than one document. Hard fail if list
     * doesn't contain exactly one document since our logic relies on this assumption.
     */
    public IdentityDocumentEntity getIdentityDocument() {

        if (Objects.isNull(customer) || customer.getIdentityDocument().size() != 1) {
            throw new IllegalStateException(
                    String.format(
                            "ES BBVA: Expected one identity document, found: %s",
                            Objects.isNull(customer) ? 0 : customer.getIdentityDocument().size()));
        }

        return customer.getIdentityDocument().get(0);
    }

    public String getName() {
        return Objects.isNull(customer) ? null : customer.getName();
    }

    public String getLastName() {
        return Objects.isNull(customer) ? null : customer.getLastName();
    }

    public String getMothersLastName() {
        return Objects.isNull(customer) ? null : customer.getMothersLastName();
    }

    public String getBirthDate() {
        return Objects.isNull(customer) ? null : customer.getBirthDate();
    }
}
