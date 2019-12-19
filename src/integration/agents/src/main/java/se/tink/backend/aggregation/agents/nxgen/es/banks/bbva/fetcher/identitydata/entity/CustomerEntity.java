package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerEntity {

    private TypeEntity type;
    private List<IdentityDocumentEntity> identityDocument;
    private String name;
    private String lastName;
    private String mothersLastName;
    private String birthDate;
    private boolean isCustomer;
    private ExtendedDataEntity extendedData;
    private List<ContactInformationEntity> contactsInformation;
    private ClassificationEntity classification;
    private ManagementDataEntity managementData;
    private String economicData;
    private TypeEntity branch;
    private EmployeeDataEntity employeeData;
    private ProductMarksEntity productMarks;
    private MultichannelPassportEntity multichannelPassport;
    private TypeEntity belongingBank;
    private List<CollectivesEntity> collectives;

    public List<IdentityDocumentEntity> getIdentityDocument() {
        return identityDocument;
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
