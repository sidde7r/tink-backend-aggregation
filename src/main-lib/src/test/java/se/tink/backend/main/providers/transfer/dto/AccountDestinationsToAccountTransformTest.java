package se.tink.backend.main.providers.transfer.dto;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.ImageUrls;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.backend.utils.ClearingNumberBankToProviderMap;
import se.tink.backend.utils.ClearingNumberBankToProviderMapImpl;
import se.tink.backend.utils.ProviderDisplayNameFinder;
import se.tink.backend.utils.ProviderImageMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountDestinationsToAccountTransformTest {
    private static final UUID ACC_UUID = UUID.fromString("e7f8d424-87e9-440f-adeb-f9f26c182954");
    private static final UUID ACC_UUID2 = UUID.fromString("abc12324-87e9-440f-adeb-f9f26c182954");

    private static final AccountIdentifier IDENTIFIER_SSN1 = AccountIdentifier
            .create(AccountIdentifier.Type.SE, "33008607015537");
    private static final AccountIdentifier IDENTIFIER_SSN2 = AccountIdentifier
            .create(AccountIdentifier.Type.SE, "33008401141935");
    private static final AccountIdentifier IDENTIFIER_DANSKE = AccountIdentifier
            .create(AccountIdentifier.Type.SE, "1200112233");
    private static final AccountIdentifier IDENTIFIER_IBAN_SN2 = AccountIdentifier
            .create(AccountIdentifier.Type.IBAN, "NDEASESS/SE6730000000008401141935");

    @Test
    public void testToAccountTransform() {
        Account account = createAccount();
        Iterable<Destination> destinations = createDestinations();
        AccountDestinations accountDestinations = new AccountDestinations(
                account,
                destinations,
                Optional.empty()
        );
        ProviderDisplayNameFinder displayNameFinder = mockDisplayNameFinder();
        ProviderImageMap providerImages = mockProviderImageMap();
        ClearingNumberBankToProviderMap clearingNumberBankToProviderMap = new ClearingNumberBankToProviderMapImpl();

        AccountDestinations.ToAccountTransform toAccountTransform = new AccountDestinations.ToAccountTransform(
                displayNameFinder, providerImages, false);
        Account transformedAccount = toAccountTransform.apply(accountDestinations);

        // The account reference should still be based on the same object instance
        assertThat(transformedAccount).isNotNull();
        assertThat(transformedAccount).isSameAs(account);

        // ...but with the destinations added
        List<TransferDestination> transferDestinations = transformedAccount.getTransferDestinations();
        assertThat(transferDestinations).isNotEmpty();
        assertThat(transferDestinations).hasSize(1);

        // ...and the destination should have the expected destination values in it
        TransferDestination transferDestination = transferDestinations.get(0);
        assertThat(transferDestination.getBalance()).isEqualTo(123.12);
        assertThat(transferDestination.getName()).isEqualTo("The name2");
        assertThat(transferDestination.getDisplayBankName()).isEqualTo("Nordea");
        assertThat(transferDestination.getDisplayAccountNumber()).isEqualTo(IDENTIFIER_SSN2.getIdentifier(new DisplayAccountIdentifierFormatter()));

        // ...with also some images to it based on our mock below
        assertThat(transferDestination.getImages()).isNotNull();
        assertThat(transferDestination.getImages().getBanner()).isEqualTo("somebanner");
        assertThat(transferDestination.getImages().getIcon()).isEqualTo("someicon");
    }

    private ProviderImageMap mockProviderImageMap() {
        ImageUrls imageUrlStub = new ImageUrls();
        imageUrlStub.setBanner("somebanner");
        imageUrlStub.setIcon("someicon");

        ProviderImageMap mock = mock(ProviderImageMap.class);

        when(mock.getImagesForAccount("somedisplayname", AccountTypes.SAVINGS))
                .thenReturn(imageUrlStub);

        return mock;
    }

    private ProviderDisplayNameFinder mockDisplayNameFinder() {
        ProviderDisplayNameFinder mock = mock(ProviderDisplayNameFinder.class);

        when(mock.getIndexedByCredentialsId())
                .thenReturn(ImmutableMap.of("TheCred2", "somedisplayname"));

        return mock;
    }

    private Account createAccount() {
        Account account = new Account();
        account.setId(ACC_UUID.toString());
        account.setBalance(1.0);
        account.setCredentialsId("TheCred");
        account.setName("The name");
        account.setType(AccountTypes.CHECKING);

        account.putIdentifier(IDENTIFIER_SSN1);

        return account;
    }

    private Account createAccount2() {
        Account account = new Account();
        account.setId(ACC_UUID2.toString());
        account.setBalance(123.12);
        account.setCredentialsId("TheCred2");
        account.setName("The name2");
        account.setType(AccountTypes.SAVINGS);

        account.putIdentifier(IDENTIFIER_SSN2);
        account.putIdentifier(IDENTIFIER_IBAN_SN2);

        return account;
    }

    private Iterable<Destination> createDestinations() {
        List<Destination> destinations = Lists.newArrayList();
        Destination destination1 = createAccountDestination(createAccount2());
        destinations.add(destination1);
        return destinations;
    }

    private DestinationOfAccount createAccountDestination(Account account) {
        DestinationOfAccount destination = new DestinationOfAccount(account.getIdentifiers());
        destination.setBalance(account.getBalance());
        destination.setCredentialsId(account.getCredentialsId());
        destination.setName(account.getName());
        destination.setType(account.getType());
        return destination;
    }
}
