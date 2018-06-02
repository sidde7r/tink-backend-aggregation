package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.entities;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.RequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class ValidateExternalTransferResponseEntity extends BaseMobileResponseEntity {
    private List<RequestEntity> requests;
    private SignatureEntity signature;

    public List<RequestEntity> getRequests() {
        return requests;
    }

    public SignatureEntity getSignature() {
        return signature;
    }

    public Optional<URL> findExecuteTrustedTransferRequest() {
        return findRequest(RequestEntity::isExecuteTrustedTransfer);
    }

    public Optional<URL> findExecuteThirdPartyTransferRequest() {
        return findRequest(RequestEntity::isExecuteThirdPartyTransfer);
    }

    private Optional<URL> findRequest(Predicate<RequestEntity> requestPredicate) {
        return Optional.ofNullable(requests)
                .map(Collection::stream)
                .flatMap(requests -> requests
                        .filter(requestPredicate)
                        .findFirst()
                )
                .map(RequestEntity::asSSORequest);
    }
}
