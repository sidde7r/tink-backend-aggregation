package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.countries.EsIdentityData;

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
    private SecurityOutputEntity securityOutput;
    private String authenticationType;

    public FetchIdentityDataResponse toTinkIdentity() {
        return new FetchIdentityDataResponse(
                EsIdentityData.builder()
                        .setDocumentNumber(dni)
                        .addFirstNameElement(firstName)
                        .addSurnameElement(lastName)
                        .setDateOfBirth(null)
                        .build());
    }

    public String getSignatureType() {
        return signatureType;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public SecurityOutputEntity getSecurityOutput() {
        return securityOutput;
    }
}
