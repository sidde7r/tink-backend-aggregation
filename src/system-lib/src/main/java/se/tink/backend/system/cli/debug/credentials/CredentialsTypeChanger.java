package se.tink.backend.system.cli.debug.credentials;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.utils.LogUtils;

public class CredentialsTypeChanger {
    private static final LogUtils log = new LogUtils(CredentialsTypeChanger.class);

    private CredentialsRepository credentialsRepository;
    private ProviderRepository providerRepository;
    private boolean isProvidersOnAggregation;
    private AggregationControllerCommonClient aggregationControllerClient;

    public CredentialsTypeChanger(CredentialsRepository credentialsRepository, ProviderRepository providerRepository,
            boolean isProvidersOnAggregation, AggregationControllerCommonClient aggregationControllerClient) {
        this.credentialsRepository = credentialsRepository;
        this.providerRepository = providerRepository;
        this.isProvidersOnAggregation = isProvidersOnAggregation;
        this.aggregationControllerClient = aggregationControllerClient;
    }

    public void changeCredentialsType(Credentials credentials, CredentialsTypes typeToChangeTo) {
        CredentialsTypes oldCredentialsType = credentials.getType();

        log.info(String.format("Changing type for credentials with id '%s'.", credentials.getId()));

        String newProviderName = constructNewProviderName(credentials.getProviderName(), typeToChangeTo);
        if (isProvidersOnAggregation) {
            Preconditions.checkNotNull(aggregationControllerClient.getProviderByName(newProviderName),
                    String.format("New provider name does not exist: %s", newProviderName));
        } else {
            Preconditions.checkNotNull(providerRepository.findByName(newProviderName),
                    String.format("New provider name does not exist: %s", newProviderName));
        }


        credentials.setProviderName(newProviderName);
        credentials.setType(typeToChangeTo);

        credentialsRepository.saveAndFlush(credentials);

        log.info(String.format(
                "Credentials type changed from '%s' to '%s'.", oldCredentialsType, credentials.getType()));
    }

    public String constructNewProviderName(String currentProviderName, CredentialsTypes typeToChangeTo) {
        String newProviderName;

        if (Objects.equal(CredentialsTypes.MOBILE_BANKID, typeToChangeTo)) {
            newProviderName = currentProviderName + "-bankid";
        } else if (Objects.equal(CredentialsTypes.PASSWORD, typeToChangeTo)) {
            newProviderName = currentProviderName.replaceAll("-bankid$", "");
        } else {
            throw new IllegalArgumentException(
                    String.format("The command does not support change to type '%s' at this moment.",
                            typeToChangeTo));
        }

        return newProviderName;
    }
}
