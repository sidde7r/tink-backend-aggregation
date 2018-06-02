package se.tink.backend.grpc.v1.converter.account;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.core.Provider;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.provider.CoreImageUrlsToGrpcImagesConverter;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.Account;
import se.tink.libraries.account.AccountIdentifier;

public class CoreAccountToGrpcAccountConverter implements Converter<se.tink.backend.core.Account, Account> {
    private final CoreImageUrlsToGrpcImagesConverter coreImageUrlsToGrpcImagesConverter = new CoreImageUrlsToGrpcImagesConverter();
    private final String currencyCode;
    private final Map<String, Provider> providersByCredentialIds;

    public CoreAccountToGrpcAccountConverter(String currencyCode, Map<String, Provider> providersByCredentialIds) {
        this.currencyCode = currencyCode;
        this.providersByCredentialIds = providersByCredentialIds;
    }

    @Override
    public Account convertFrom(se.tink.backend.core.Account input) {
        Account.Builder builder = Account.newBuilder();
        ConverterUtils.setIfPresent(input::getId, builder::setId);
        builder.setBalance(NumberUtils.toCurrencyDenominatedAmount(input.getBalance(), currencyCode));
        ConverterUtils.setIfPresent(input::getAccountNumber, builder::setAccountNumber);
        ConverterUtils.setIfPresent(input::getCredentialsId, builder::setCredentialId);
        ConverterUtils.setIfPresent(input::isExcluded, builder::setExcluded);
        ConverterUtils.setIfPresent(input::isFavored, builder::setFavored);
        ConverterUtils.setIfPresent(input::getName, builder::setName);
        ConverterUtils.setIfPresent(input::getOwnership, builder::setOwnership, NumberUtils::toExactNumber);
        ConverterUtils.setIfPresent(input::getType, builder::setType,
                type -> EnumMappers.CORE_ACCOUNT_TYPE_TO_GRPC_MAP.getOrDefault(type, Account.Type.TYPE_UNKNOWN));
        ConverterUtils
                .setIfPresent(input::getImages, builder::setImages, coreImageUrlsToGrpcImagesConverter::convertFrom);
        ConverterUtils
                .setIfPresent(() -> providersByCredentialIds.get(input.getCredentialsId()), builder::setTransactional,
                        Provider::isTransactional);
        builder.setClosed(input.isClosed());
        builder.addAllIdentifiers(getAccountIdentifiers(input.getIdentifiers()));

        return builder.build();
    }

    private Set<String> getAccountIdentifiers(List<AccountIdentifier> identifiers) {
        if (identifiers == null || identifiers.isEmpty()) {
            return Collections.emptySet();
        }

        return identifiers.stream()
                .map(AccountIdentifier::toURI)
                .filter(Objects::nonNull)
                .map(URI::toString)
                .collect(Collectors.toSet());
    }
}
