package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ParticipantAccountEntity {
    private String id;
    private List<IdentityDocumentEntity> identityDocument;
    private String lastName;
    private String mothersLastName;
    private String name;
    private RelationshipEntity relationship;
    private SignatureEntity signature;
    private SignatureContractDetailEntity signatureContractDetail;
    private String type;
}
