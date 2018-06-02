package se.tink.backend.grpc.v1.converter.account;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.Provider;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.grpc.v1.models.Account;
import se.tink.grpc.v1.models.Accounts;
import se.tink.grpc.v1.models.Images;

public class FirehoseAccountToGrpcAccountConverter {
    private final String currencyCode;
    private Map<String, Provider> providersByCredentialIds;
    private final ProviderImageMap providerImageMap;

    private final CredentialsRepository credentialsRepository;
    private final ProviderDao providerDao;

    public FirehoseAccountToGrpcAccountConverter(String currencyCode,
            Map<String, Provider> providersByCredentialIds, ProviderImageMap providerImageMap,
            CredentialsRepository credentialsRepository, ProviderDao providerDao) {
        this.currencyCode = currencyCode;
        this.providersByCredentialIds = providersByCredentialIds;
        this.providerImageMap = providerImageMap;
        this.credentialsRepository = credentialsRepository;
        this.providerDao = providerDao;
    }

    public Accounts convertFrom(List<se.tink.backend.firehose.v1.models.Account> input) {
        Accounts.Builder accountsBuilder = Accounts.newBuilder();
        for (se.tink.backend.firehose.v1.models.Account account : input) {
            Account.Builder builder = Account.newBuilder();
            ConverterUtils.setIfPresent(account::getId, builder::setId);
            builder.setBalance(NumberUtils.toCurrencyDenominatedAmount(account.getBalance(), currencyCode));
            ConverterUtils.setIfPresent(account::getAccountNumber, builder::setAccountNumber);
            ConverterUtils.setIfPresent(account::getCredentialsId, builder::setCredentialId);
            ConverterUtils.setIfPresent(account::getExcluded, builder::setExcluded);
            ConverterUtils.setIfPresent(account::getFavored, builder::setFavored);
            ConverterUtils.setIfPresent(account::getName, builder::setName);
            ConverterUtils.setIfPresent(account::getOwnership, builder::setOwnership, NumberUtils::toExactNumber);
            ConverterUtils.setIfPresent(account::getType, builder::setType,
                    type -> EnumMappers.FIREHOSE_ACCOUNT_TYPE_TO_GRPC_MAP.getOrDefault(type, Account.Type.TYPE_UNKNOWN));
            ConverterUtils.setIfPresent(account::getIdentifiersList, builder::addAllIdentifiers);

            // Refresh the credentials to provider mapping if the credential is new
            if (providersByCredentialIds.get(account.getCredentialsId()) == null) {
                providersByCredentialIds = getProvidersByCredentialsId(account.getUserId());
            }

            ConverterUtils.setIfPresent(() -> providersByCredentialIds.get(account.getCredentialsId()),
                    builder::setTransactional, Provider::isTransactional);
            builder.setClosed(account.getClosed());
            builder.setImages(getImages(account));

            accountsBuilder.addAccount(builder);
        }
        return accountsBuilder.build();
    }

    /**
     * Return the image urls for the account. Priority is to get the accounts from the firehose message if available
     * and then then fallback to picking them up from the provider.
     */
    private Images.Builder getImages(se.tink.backend.firehose.v1.models.Account account) {
        Images.Builder images = Images.newBuilder();

        // First priority to pick urls from message
        if (!Strings.isNullOrEmpty(account.getBannerUrl()) || !Strings.isNullOrEmpty(account.getIconUrl())) {
            ConverterUtils.setIfPresent(account::getBannerUrl, images::setBannerUrl);
            ConverterUtils.setIfPresent(account::getIconUrl, images::setIconUrl);
            return images;
        }

        Provider provider = providersByCredentialIds.get(account.getCredentialsId());

        if (provider != null) {
            AccountTypes accountType = EnumMappers.CORE_TO_FIREHOSE_ACCOUNT_TYPE_MAPPING.inverse()
                    .getOrDefault(account.getType(), null);

            ImageUrls imageUrls = providerImageMap.getImagesForAccount(provider.getName(), accountType);

            ConverterUtils.setIfPresent(imageUrls::getBanner, images::setBannerUrl);
            ConverterUtils.setIfPresent(imageUrls::getIcon, images::setIconUrl);
        }

        return images;
    }

    /**
     * Get providers by credentials id. Temporary solution. /Erik P
     */
    private Map<String, Provider> getProvidersByCredentialsId(String userId) {
        List<Credentials> credentials = credentialsRepository.findAllByUserId(userId);
        Map<String, Provider> providersByName = providerDao.getProvidersByName();

        return providersByCredentialIds = credentials.stream()
                .filter(credential -> providersByName.containsKey(credential.getProviderName()))
                .collect(Collectors
                        .toMap(Credentials::getId,
                                credential -> providersByName.get(credential.getProviderName())));
    }
}
