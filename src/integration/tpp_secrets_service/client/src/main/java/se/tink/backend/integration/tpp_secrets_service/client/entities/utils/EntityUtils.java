package se.tink.backend.integration.tpp_secrets_service.client.entities.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;
import se.tink.backend.secretservice.grpc.GetAllSecretsResponse;
import se.tink.backend.secretservice.grpc.TppSecret;

public class EntityUtils {
    public static SecretsEntityCore createSecretsEntityCore(GetAllSecretsResponse response) {

        List<TppSecret> allSecretsList = new ArrayList<>();
        allSecretsList.addAll(response.getEncryptedSecretsList());
        allSecretsList.addAll(response.getSecretsList());

        return new SecretsEntityCore.Builder()
                .setSecrets(
                        allSecretsList.stream()
                                .collect(Collectors.toMap(TppSecret::getKey, TppSecret::getValue)))
                .setRedirectUrls(response.getRedirectUrlsList())
                .setScopes(response.getScopesList())
                .setQwac(response.getCertificates().getQwac())
                .setQsealc(response.getCertificates().getQsealc())
                .build();
    }
}
