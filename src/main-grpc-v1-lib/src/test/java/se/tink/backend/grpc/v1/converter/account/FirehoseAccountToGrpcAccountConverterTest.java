package se.tink.backend.grpc.v1.converter.account;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Maps;
import org.junit.Test;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderImage;
import se.tink.backend.utils.ClearingNumberBankToProviderMapImpl;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.grpc.v1.models.Account;
import se.tink.grpc.v1.models.Accounts;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class FirehoseAccountToGrpcAccountConverterTest {
    private static String CURRENCY_CODE = "SEK";
    private static ProviderImageMap PROVIDER_IMAGE_MAP = createProviderImageMap();

    @Test
    public void testImagesFromFirehoseMessage() {
        se.tink.backend.firehose.v1.models.Account account = se.tink.backend.firehose.v1.models.Account.newBuilder()
                .setBannerUrl("banner-url")
                .setIconUrl("icon-url")
                .setCredentialsId(UUIDUtils.generateUUID())
                .build();

        Map<String, Provider> providerByCredentialIdMap = Maps.newHashMap();
        providerByCredentialIdMap.put(account.getCredentialsId(), new Provider());

        FirehoseAccountToGrpcAccountConverter converter = new FirehoseAccountToGrpcAccountConverter(CURRENCY_CODE,
                providerByCredentialIdMap, PROVIDER_IMAGE_MAP, null, null);

        Accounts converted = converter.convertFrom(ImmutableList.of(account));

        assertThat(converted.getAccountList()).isNotEmpty();

        Account convertedAccount = converted.getAccount(0);

        assertThat(convertedAccount.getImages().getBannerUrl()).isEqualTo("banner-url");
        assertThat(convertedAccount.getImages().getIconUrl()).isEqualTo("icon-url");
    }

    @Test
    public void testImagesFromProviders() {
        String credentialsId = UUIDUtils.generateUUID();

        Provider provider = new Provider();
        provider.setName("dummy");

        se.tink.backend.firehose.v1.models.Account account = se.tink.backend.firehose.v1.models.Account.newBuilder()
                .setType(se.tink.backend.firehose.v1.models.Account.Type.TYPE_SAVINGS)
                .setCredentialsId(credentialsId)
                .build();

        Map<String, Provider> providersByCredentialIds = Maps.newHashMap();

        providersByCredentialIds.put(credentialsId, provider);

        FirehoseAccountToGrpcAccountConverter converter = new FirehoseAccountToGrpcAccountConverter(CURRENCY_CODE,
                providersByCredentialIds, PROVIDER_IMAGE_MAP, null, null);

        Accounts converted = converter.convertFrom(ImmutableList.of(account));

        assertThat(converted.getAccountList()).isNotEmpty();

        Account convertedAccount = converted.getAccount(0);

        assertThat(convertedAccount.getImages().getBannerUrl()).isEqualTo("url:banner:dummy.account.savings");
        assertThat(convertedAccount.getImages().getIconUrl()).isEqualTo("url:icon:dummy.account.savings");
    }

    private static ProviderImageMap createProviderImageMap() {
        List<ProviderImage> providerImages = Lists.newArrayList();

        providerImages.add(createProviderImage("icon:dummy.account.savings", "url:icon:dummy.account.savings"));
        providerImages.add(createProviderImage("banner:dummy.account.savings", "url:banner:dummy.account.savings"));

        return new ProviderImageMap(providerImages, new ClearingNumberBankToProviderMapImpl());
    }

    private static ProviderImage createProviderImage(String code, String url) {
        ProviderImage image = new ProviderImage();
        image.setCode(code);
        image.setUrl(url);
        return image;
    }

}
