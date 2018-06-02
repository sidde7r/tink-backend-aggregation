package se.tink.backend.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Giro;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderImage;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber;

public class ProviderImageMap {

    private static final LogUtils log = new LogUtils(ProviderImage.class);
    private final Map<String, ProviderImage> providerImageByCode;
    private final ClearingNumberBankToProviderMap clearingNumberToProviderMap;

    public ProviderImageMap(List<ProviderImage> images, ClearingNumberBankToProviderMap clearingNumberToProviderMap) {
        this.providerImageByCode = Maps.uniqueIndex(images, ProviderImage::getCode);
        this.clearingNumberToProviderMap = clearingNumberToProviderMap;
    }

    private ProviderImage findByCode(ProviderImage.Type imageType, String code) {
        String typePrefix = imageType.toString() + ":";

        if (Strings.isNullOrEmpty(code)) {
            ProviderImage defaultImage = providerImageByCode.get(typePrefix + "default");
            if (defaultImage == null) {
                // to prevent NPEs
                log.error("Default provider image not found--was database seeded?");
                return new ProviderImage();
            }
            return defaultImage;
        }

        if (providerImageByCode.containsKey(typePrefix + code)) {
            return providerImageByCode.get(typePrefix + code);
        } else {
            int pos = code.lastIndexOf('.');
            return findByCode(imageType, code.substring(0, pos >= 0 ? pos : 0));
        }
    }

    public ProviderImage find(ProviderImage.Type imageType, String providerName) {
        if (Strings.isNullOrEmpty(providerName)) {
            return findByCode(imageType, null);
        }
        return findByCode(imageType, providerName.toLowerCase().replace("-bankid", ""));
    }

    public ProviderImage find(ProviderImage.Type type, AccountIdentifier identifier) {
        if (identifier == null || !identifier.isValid()) {
            return findByCode(type, null);
        }

        StringBuilder sb = new StringBuilder();

        if (identifier.is(AccountIdentifier.Type.SE_SHB_INTERNAL)) {
            sb.append(clearingNumberToProviderMap.getProviderForBank(ClearingNumber.Bank.HANDELSBANKEN).orElse(null));
        } else if (identifier.is(AccountIdentifier.Type.SE)) {
            SwedishIdentifier swedishIdentifier = identifier.to(SwedishIdentifier.class);
            sb.append(clearingNumberToProviderMap.getProviderForBank(swedishIdentifier.getBank()).orElse(null));
        } else {
            sb.append(identifier.getType().toString());

            if (identifier.getName().isPresent()) {
                sb.append('.').append(identifier.getName().get().toLowerCase());
            }
        }

        return findByCode(type, sb.toString());
    }

    public ProviderImage find(ProviderImage.Type imageType, Giro giro) {
        if (giro == null) {
            return findByCode(imageType, null);
        }

        StringBuilder sb = new StringBuilder()
                .append(giro.getType().toAccountIdentifierType().toString());

        if (!Strings.isNullOrEmpty(giro.getName())) {
            sb.append('.').append(giro.getName().toLowerCase());
        }

        return findByCode(imageType, sb.toString());
    }


    private ProviderImage find(ProviderImage.Type imageType, String providerName, AccountTypes type) {
        if (Strings.isNullOrEmpty(providerName)) {
            return findByCode(imageType, ":");
        }

        String account = "";
        if (type != null) {
            account = ".account." + type.toString().toLowerCase();
        }
        return findByCode(imageType, providerName.toLowerCase().replace("-bankid", "") + account);
    }

    public ImageUrls getImagesForAccountIdentifier(AccountIdentifier identifier) {
        ProviderImage icon = find(ProviderImage.Type.ICON, identifier);
        ProviderImage banner = find(ProviderImage.Type.BANNER, identifier);

        ImageUrls images = new ImageUrls();
        images.setIcon(icon.getUrl());
        images.setBanner(banner.getUrl());

        return images;
    }

    public ImageUrls getImagesForGiro(Giro giro) {
        ProviderImage icon = find(ProviderImage.Type.ICON, giro);
        ProviderImage banner = find(ProviderImage.Type.BANNER, giro);

        ImageUrls images = new ImageUrls();
        images.setIcon(icon.getUrl());
        images.setBanner(banner.getUrl());

        return images;
    }

    public ImageUrls getImagesForAccount(String providerName, Account account) {
        return getImagesForAccount(providerName, account.getType());
    }

    public ImageUrls getImagesForAccount(String providerName, AccountTypes accountType) {
        ProviderImage icon = find(ProviderImage.Type.ICON, providerName, accountType);
        ProviderImage banner = find(ProviderImage.Type.BANNER, providerName, accountType);

        ImageUrls images = new ImageUrls();
        images.setIcon(icon.getUrl());
        images.setBanner(banner.getUrl());

        return images;
    }

    public void populateImagesForCredentials(List<Credentials> credentials) {
        for (Credentials c : credentials) {
            ProviderImage icon = find(ProviderImage.Type.ICON, c.getProviderName());
            ProviderImage banner = find(ProviderImage.Type.BANNER, c.getProviderName());

            ImageUrls images = new ImageUrls();
            images.setIcon(icon.getUrl());
            images.setBanner(banner.getUrl());

            c.setImages(images);
        }
    }

    public void populateImagesForProviders(List<Provider> providers) {
        providers.forEach(this::populateImagesForProvider);
    }

    public void populateImagesForProvider(Provider provider) {
        ProviderImage icon = getIconImageforProvider(provider.getName());
        ProviderImage banner = getBannerImageforProvider(provider.getName());

        ImageUrls images = new ImageUrls();
        images.setIcon(icon.getUrl());
        images.setBanner(banner.getUrl());

        provider.setImages(images);
    }

    public ProviderImage getIconImageforProvider(String provider) {
        return find(ProviderImage.Type.ICON, provider);
    }

    public ProviderImage getBannerImageforProvider(String provider) {
       return find(ProviderImage.Type.BANNER, provider);
    }

}
