package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerDataItemEntity {
    private String lastName;
    private GenderEntity gender;
    private BirthDataEntity birthData;
    private int newNotificationsNumber;
    private List<NationalitiesItemEntity> nationalities;
    private String membershipDate;
    private String firstName;
    private BankEntity bank;
    private String customerId;
    private List<IdentityDocumentsItemEntity> identityDocuments;
    private PersonalTitleEntity personalTitle;
    private ResidenceEntity residence;
    private MaritalStatusEntity maritalStatus;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
