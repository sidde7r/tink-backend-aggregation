package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ManagerEntity {
    private String id;
    private TypeEntity branch;
    private String name;
    private String lastName;
    private String mothersLastName;
    private List<ContactInformationEntity> contactInformation;
    private TypeEntity position;
    private TypeEntity rank;
    private String userCode;
    private String globalSegment;
    private TypeEntity portfolioUnallocationType;
    private String allocationDate;
}
