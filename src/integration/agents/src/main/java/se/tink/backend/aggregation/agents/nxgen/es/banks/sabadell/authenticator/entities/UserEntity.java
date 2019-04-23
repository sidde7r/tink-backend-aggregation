package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.IdentityTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.countries.EsIdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData.EsIdentityDataBuilder;

@SuppressWarnings("unused")
@JsonObject
public class UserEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEntity.class);

    private String dni;
    private String name;
    private String sex;
    private String phoneNumber;
    private String contractNumber;
    private boolean hasMoreContracts;
    private String customization;
    private boolean isNewUser;
    private int pendingSignOperations;
    private String personNumber;
    private String idType;
    private int loginType;
    private String cardId;
    private int refreshActiveAgent;
    private String signatureType;
    private String vTPCInscriptionKey;
    private String firstName;
    private String personalizacion;
    private String lastName;
    private boolean updateDNI;
    private int brand;
    private int activeManagementType;
    private boolean activeManagement;
    private String status;
    private String initials;
    private boolean newUser;
    private boolean isBrandActiveAgentUpdated;

    public FetchIdentityDataResponse toTinkIdentity() {
        EsIdentityDataBuilder builder = EsIdentityData.builder();

        switch (idType) {
            case IdentityTypes.NIF:
                builder.setNifNumber(dni);
                break;
            default:
                LOGGER.warn(
                        "ES Sabadell: Unhandled document type: {} (maybe NIE or passport?)",
                        idType);
        }

        return new FetchIdentityDataResponse(
                builder.addFirstNameElement(firstName)
                        .addSurnameElement(lastName)
                        .setDateOfBirth(null)
                        .build());
    }
}
