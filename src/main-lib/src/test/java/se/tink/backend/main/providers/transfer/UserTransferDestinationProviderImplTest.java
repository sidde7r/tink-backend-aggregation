package se.tink.backend.main.providers.transfer;

import com.google.common.collect.Lists;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.CompareEqual;
import se.tink.backend.common.repository.cassandra.UserTransferDestinationRepository;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.User;
import se.tink.backend.core.account.UserTransferDestination;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.backend.utils.ProviderImageMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserTransferDestinationProviderImplTest {
    private static final String TEST_USER_ID = "88b7297c-f11f-37f4-8671-37cdd0cdb0c1";
    private static final String TEST_BANK_NAME = "Danske Bank";
    private static final String TEST_CLEARING_NUMBER = "1200";
    private static final String TEST_ACCOUNT_NUMBER = "11223344";
    private static final String TEST_ACCOUNT_NAME = "Testnamn";
    private static final AccountIdentifier.Type TEST_ACCOUNT_TYPE = AccountIdentifier.Type.SE;

    private UserTransferDestinationProviderImpl service;

    public UserTransferDestinationProviderImplTest() {
        UserTransferDestinationRepository mockedRepository = mock(UserTransferDestinationRepository.class);
        service = new UserTransferDestinationProviderImpl(mockedRepository,
                mockProviderImageMap());
    }

    @Test
    public void ShouldSaveTransferDestinationToRepository_Once() throws URISyntaxException {
        UserTransferDestinationRepository mockedRepository = mock(UserTransferDestinationRepository.class);
        UserTransferDestinationProviderImpl service = new UserTransferDestinationProviderImpl(mockedRepository,
                mockProviderImageMap());

        service.createDestination(stubUser(), stubCreateTransferUri(), TEST_ACCOUNT_NAME);

        verify(mockedRepository, times(1)).save(isA(UserTransferDestination.class));
    }

    @Test
    public void ShouldSaveTransferDestinationToRepository_WithExpectedData() throws URISyntaxException {
        UserTransferDestinationRepository mockedRepository = mock(UserTransferDestinationRepository.class);
        UserTransferDestinationProviderImpl service = new UserTransferDestinationProviderImpl(mockedRepository,
                mockProviderImageMap());

        service.createDestination(stubUser(), stubCreateTransferUri(), TEST_ACCOUNT_NAME);

        ArgumentCaptor<UserTransferDestination> destination = ArgumentCaptor.forClass(UserTransferDestination.class);
        verify(mockedRepository).save(destination.capture());
        assertThat(destination.getValue().getType()).isEqualTo(TEST_ACCOUNT_TYPE);
        assertThat(destination.getValue().getIdentifier()).isEqualTo(TEST_CLEARING_NUMBER + TEST_ACCOUNT_NUMBER);
        assertThat(destination.getValue().getName()).isEqualTo(TEST_ACCOUNT_NAME);
        assertThat(destination.getValue().getUserId().toString()).isEqualTo(TEST_USER_ID);
    }

    @Test
    public void ShouldUnpadSwedbank8xxxxAccountNumbers() throws URISyntaxException {
        UserTransferDestinationRepository mockedRepository = mock(UserTransferDestinationRepository.class);

        String accountNumber = "0031270465";
        URI paddedSwedbankUri = stubCreateTransferUri("84228", accountNumber);

        UserTransferDestinationProviderImpl service = new UserTransferDestinationProviderImpl(mockedRepository,
                mockProviderImageMap());
        service.createDestination(stubUser(), paddedSwedbankUri, accountNumber);

        ArgumentCaptor<UserTransferDestination> destination = ArgumentCaptor.forClass(UserTransferDestination.class);
        verify(mockedRepository).save(destination.capture());
        assertThat(destination.getValue().getIdentifier()).isEqualTo("84228" + "31270465");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldThrow_WhenGivenBadAccountNumber() throws URISyntaxException {
        service.createDestination(stubUser(), new URI("se://12345"), TEST_ACCOUNT_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldThrow_WhenGivenBadClearingNumber() throws URISyntaxException {
        service.createDestination(stubUser(), new URI("se://0000123456"), TEST_ACCOUNT_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldThrow_WhenGivenEmptyName() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "");
    }

    @Test
    public void ShouldAccept_NameWithSwedishCharacters() throws URISyntaxException {
        TransferDestination destination = service.createDestination(stubUser(), stubCreateTransferUri(), "åäöÅÄÖ");
        assertThat(destination.getName()).isEqualTo("åäöÅÄÖ");
    }

    @Test
    public void ShouldAccept_NameWithAmpersandAsSeparator() throws URISyntaxException {
        TransferDestination destination = service.createDestination(stubUser(), stubCreateTransferUri(), "Ö&B AB");
        assertThat(destination.getName()).isEqualTo("Ö&B AB");
    }

    @Test
    public void ShouldAccept_NameWithAmpersandSurroundedBySpaces() throws URISyntaxException {
        TransferDestination destination = service.createDestination(stubUser(), stubCreateTransferUri(), "This & that");
        assertThat(destination.getName()).isEqualTo("This & that");
    }

    @Test
    public void ShouldAccept_NameWithHyphenSurroundedBySpaces() throws URISyntaxException {
        TransferDestination destination = service.createDestination(stubUser(), stubCreateTransferUri(), "This - that");
        assertThat(destination.getName()).isEqualTo("This - that");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_NameWithMultipleAmpersands() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "Ö&&B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_AmpersandInBeginningOfWord() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "This &that");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_SeparatorsInBeginningOfName1() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "&This is a name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_SeparatorsInBeginningOfName2() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "-This is a name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_SeparatorsInBeginningOfName3() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), " This is a name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_SeparatorsInBeginningOfName4() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), " & This is a name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_SeparatorsInBeginningOfName5() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "+This is a name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_SeparatorsInBeginningOfName6() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "\\This is a name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_SeparatorsInBeginningOfName7() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "_This is a name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_SeparatorsInBeginningOfName8() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "/This is a name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldNotAccept_DoubleSeparators() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "This  that");
    }

    @Test
    public void ShouldAccept_NamesWithSpaces() throws URISyntaxException {
        TransferDestination destination = service.createDestination(stubUser(), stubCreateTransferUri(), "a a a a");
        assertThat(destination.getName()).isEqualTo("a a a a");
    }

    @Test
    public void ShouldAccept_SomeStringSeparators() throws URISyntaxException {
        TransferDestination destination = service
                .createDestination(stubUser(), stubCreateTransferUri(), "a b-c d+f e\\f g/h i_j k&l");
        assertThat(destination.getName()).isEqualTo("a b-c d+f e\\f g/h i_j k&l");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldThrow_WhenGivenNameWithOnlySpaces() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldThrow_WhenGivenNameWithWeirdChars() throws URISyntaxException {
        service.createDestination(stubUser(), stubCreateTransferUri(), "aæøa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldThrow_WhenGivenBadSwedbankClearing() throws URISyntaxException {
        String accountName = "31270465";
        URI bad8xxxClearingCheckDigit = stubCreateTransferUri("84220", accountName);

        service.createDestination(stubUser(), bad8xxxClearingCheckDigit, accountName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShouldThrow_WhenGivenBadSwedbankAccountNumber() throws URISyntaxException {
        String accountNumber = "31270460";
        URI bad8xxxAccountNumberCheckDigit = stubCreateTransferUri("84228", accountNumber);

        service.createDestination(stubUser(), bad8xxxAccountNumberCheckDigit, accountNumber);
    }

    @Test
    public void ShouldGetDestinationsFromRepository_WithExpectedUUID() {
        UserTransferDestinationRepository mockedRepository = mock(UserTransferDestinationRepository.class);

        UserTransferDestinationProviderImpl service = new UserTransferDestinationProviderImpl(mockedRepository,
                mockProviderImageMap());
        service.getDestinations(stubUser());

        verify(mockedRepository).findAllByUserId(argThat(new CompareEqual<>(UUID.fromString(TEST_USER_ID))));
    }

    @Test
    public void ShouldGetDestinationsFromRepository_AndReturnExpectedList() {
        UserTransferDestinationRepository mockedRepository = mock(UserTransferDestinationRepository.class);
        List<UserTransferDestination> stubTransferDestinations = Lists.newArrayList();
        stubTransferDestinations.add(stubTransferDestination());
        stubTransferDestinations.add(stubTransferDestination());
        when(mockedRepository.findAllByUserId(isA(UUID.class))).thenReturn(stubTransferDestinations);

        UserTransferDestinationProviderImpl service = new UserTransferDestinationProviderImpl(mockedRepository,
                mockProviderImageMap());
        List<UserTransferDestination> destinations = service.getDestinations(stubUser());

        assertThat(destinations.size()).isEqualTo(2);
        assertThat(destinations.get(0)).isEqualByComparingTo(stubTransferDestination());
        assertThat(destinations.get(0).getName()).isEqualTo(TEST_ACCOUNT_NAME);
        assertThat(destinations.get(0).getType()).isEqualTo(TEST_ACCOUNT_TYPE);
    }

    @Test
    public void ShouldReturnATransferDestination_WhenCreatingANewDestination() throws URISyntaxException {
        UserTransferDestinationRepository mockedRepository = mock(UserTransferDestinationRepository.class);
        UserTransferDestinationProviderImpl service = new UserTransferDestinationProviderImpl(mockedRepository,
                mockProviderImageMap());

        TransferDestination destination = service
                .createDestination(stubUser(), stubCreateTransferUri(), TEST_ACCOUNT_NAME);

        assertThat(destination.getBalance()).isNull();
        assertThat(destination.getDisplayAccountNumber()).isEqualTo("1200-11223344");
        assertThat(destination.getDisplayBankName()).isEqualTo(TEST_BANK_NAME);
        assertThat(destination.getUri().toString()).isEqualTo("se://120011223344?name=Testnamn");
        assertThat(destination.getImages()).isNotNull();
        assertThat(destination.getImages().getBanner()).isEqualTo("SomeBanner");
        assertThat(destination.getImages().getIcon()).isEqualTo("SomeIcon");
        assertThat(destination.getName()).isEqualTo("Testnamn");
        assertThat(destination.getType()).isEqualTo("EXTERNAL");
    }

    private UserTransferDestination stubTransferDestination() {
        UserTransferDestination stubTransferDestination = new UserTransferDestination();
        stubTransferDestination.setUserId(UUID.fromString(TEST_USER_ID));
        stubTransferDestination.setType(TEST_ACCOUNT_TYPE);
        stubTransferDestination.setIdentifier(TEST_CLEARING_NUMBER + TEST_ACCOUNT_NUMBER);
        stubTransferDestination.setName(TEST_ACCOUNT_NAME);
        return stubTransferDestination;
    }

    private static User stubUser() {
        User user = new User();
        user.setId(UUIDUtils.toTinkUUID(UUID.fromString(TEST_USER_ID)));
        return user;
    }

    private static URI stubCreateTransferUri() throws URISyntaxException {
        return stubCreateTransferUri(TEST_CLEARING_NUMBER, TEST_ACCOUNT_NUMBER);
    }

    private static URI stubCreateTransferUri(String clearingNumber, String accountNumber) throws URISyntaxException {
        return new URIBuilder()
                .setScheme("se")
                .setHost(clearingNumber + accountNumber)
                .build();
    }

    private ProviderImageMap mockProviderImageMap() {
        ImageUrls imageUrls = new ImageUrls();
        imageUrls.setBanner("SomeBanner");
        imageUrls.setIcon("SomeIcon");

        ProviderImageMap mock = mock(ProviderImageMap.class);

        when(mock.getImagesForAccountIdentifier(any(AccountIdentifier.class)))
                .thenReturn(imageUrls);

        return mock;
    }
}
