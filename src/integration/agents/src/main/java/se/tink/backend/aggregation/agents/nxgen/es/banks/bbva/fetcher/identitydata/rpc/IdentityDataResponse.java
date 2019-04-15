package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.TypeEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity.AddressEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity.IdentityDocumentEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity.NationalityEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityDataResponse {
    private List<IdentityDocumentEntity> identityDocument;
    private TypeEntity type;
    private String name;
    private String lastName;
    private String mothersLastName;
    private String birthDate;
    private boolean hasMoreAddresses;
    private List<AddressEntity> addresses;
    private List<NationalityEntity> nationalities;
    private boolean hasContactPersons;
    private boolean hasMoreContactTypes;
    private String scanDNIId;
    private boolean hasIdentityDocuments;
    private boolean hasRelations;
    private boolean hasScanDocuments;
    private boolean isRequiredQuestionnaireGDPR;
    private boolean isRequiredQuestionnaireFatca;
    private String membershipDate;
    private String modificationDate;

    /**
     * Don't know in what cases this list will contain more than one document. Hard fail if list
     * doesn't contain exactly one document since our logic relies on this assumption.
     */
    public IdentityDocumentEntity getIdentityDocument() {

        if (identityDocument.size() != 1) {
            throw new IllegalStateException(
                    String.format(
                            "ES BBVA: Expected one identity document, found: %s",
                            identityDocument.size()));
        }

        return identityDocument.get(0);
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMothersLastName() {
        return mothersLastName;
    }

    public String getBirthDate() {
        return birthDate;
    }
}
