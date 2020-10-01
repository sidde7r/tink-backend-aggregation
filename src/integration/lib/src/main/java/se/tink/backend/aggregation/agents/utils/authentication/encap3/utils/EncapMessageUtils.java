package se.tink.backend.aggregation.agents.utils.authentication.encap3.utils;

import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.IdentificationEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.RegistrationResultEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.enums.AuthenticationMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface EncapMessageUtils {
    String buildRegistrationMessage();

    String buildActivationMessage(RegistrationResultEntity registrationResultEntity);

    String buildIdentificationMessage(@Nullable String authenticationId);

    String buildAuthenticationMessage(
            IdentificationEntity identificationEntity, AuthenticationMethod authenticationMethod);

    String encryptSoapAndSend(URL url, String soapMessage);

    <T> T encryptAndSend(String plainTextMessage, Class<T> responseType);
}
