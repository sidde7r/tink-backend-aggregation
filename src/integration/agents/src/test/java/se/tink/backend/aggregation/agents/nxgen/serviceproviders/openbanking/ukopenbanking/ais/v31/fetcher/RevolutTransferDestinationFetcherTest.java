package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.TrustedBeneficiaryDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.TrustedBeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.revolut.RevolutTransferDestinationAccountsProvider;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class RevolutTransferDestinationFetcherTest {

    private UkOpenBankingTransferDestinationFetcher transferDestinationFetcher;
    private UkOpenBankingApiClient apiClient;
    private List<Account> accounts;
    private Account mockedSourceAccount1;
    private Account mockedSourceAccount2;

    @Before
    public void setUp() {

        accounts = prepareMockAccounts();

        apiClient = mock(UkOpenBankingApiClient.class);
        when(apiClient.fetchV31AccountBeneficiaries(eq("7a435413-8132-42ca-ba23-a5dd59c193c5")))
                .thenReturn(account1Beneficiaries());
        when(apiClient.fetchV31AccountBeneficiaries(eq("0e9bb36b-b8bb-4d15-8a0b-6a9be3556be0")))
                .thenReturn(account2Beneficiaries());

        transferDestinationFetcher =
                new UkOpenBankingTransferDestinationFetcher(
                        new RevolutTransferDestinationAccountsProvider(apiClient),
                        AccountIdentifierType.IBAN,
                        IbanIdentifier.class);
    }

    private List<Account> prepareMockAccounts() {
        mockedSourceAccount1 = mock(Account.class);
        mockedSourceAccount2 = mock(Account.class);
        when(mockedSourceAccount1.getBankId()).thenReturn("7a435413813242caba23a5dd59c193c5");
        AccountIdentifier account1Identifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, "GB40BARC20038075285485");
        when(mockedSourceAccount1.getIdentifiers())
                .thenReturn(ImmutableList.of(account1Identifier));
        when(mockedSourceAccount1.getIdentifier(any(), any())).thenReturn(account1Identifier);
        when(mockedSourceAccount1.getType()).thenReturn(AccountTypes.CHECKING);

        when(mockedSourceAccount2.getBankId()).thenReturn("0e9bb36bb8bb4d158a0b6a9be3556be0");
        AccountIdentifier account2Identifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, "GB71BARC20038054128742");
        when(mockedSourceAccount2.getIdentifiers())
                .thenReturn(ImmutableList.of(account2Identifier));
        when(mockedSourceAccount2.getIdentifier(any(), any())).thenReturn(account2Identifier);
        when(mockedSourceAccount2.getType()).thenReturn(AccountTypes.CHECKING);

        return ImmutableList.of(mockedSourceAccount1, mockedSourceAccount2);
    }

    private List<TrustedBeneficiaryEntity> account1Beneficiaries() {
        TrustedBeneficiaryEntity beneficiary1 =
                new TrustedBeneficiaryEntity(
                        "7a435413-8132-42ca-ba23-a5dd59c193c5",
                        "cf43ad1d-fd38-44c4-b508-8b534abc2c95",
                        new TrustedBeneficiaryDetailsEntity(
                                "UK.OBIE.IBAN", "GB97BARC20035397661636", "Jan Kowalski"));
        TrustedBeneficiaryEntity beneficiary2 =
                new TrustedBeneficiaryEntity(
                        "7a435413-8132-42ca-ba23-a5dd59c193c5",
                        "af14596c-e70e-4fd5-8149-751b4ce5910f",
                        new TrustedBeneficiaryDetailsEntity(
                                "UK.OBIE.IBAN", "GB28BARC20032692589734", "Anna Nowak"));
        return ImmutableList.of(beneficiary1, beneficiary2);
    }

    private List<TrustedBeneficiaryEntity> account2Beneficiaries() {
        return ImmutableList.of(
                new TrustedBeneficiaryEntity(
                        "0e9bb36b-b8bb-4d15-8a0b-6a9be3556be0",
                        "e6b83c04-03f7-408b-b51e-309a2cfb3a14",
                        new TrustedBeneficiaryDetailsEntity(
                                "UK.OBIE.IBAN", "GB09BARC20035344161221", "Jane Doe")));
    }

    @Test
    public void shouldFetchTransferDestinations() {
        // when
        TransferDestinationsResponse response =
                transferDestinationFetcher.fetchTransferDestinationsFor(accounts);
        // then
        assertThat(response).isNotNull();
        assertThat(response.getDestinations()).hasSize(2);

        List<TransferDestinationPattern> account1Patterns =
                response.getDestinations().get(mockedSourceAccount1);
        List<TransferDestinationPattern> account2Patterns =
                response.getDestinations().get(mockedSourceAccount2);
        assertThat(account1Patterns).hasSize(5);
        assertThat(account2Patterns).hasSize(5);
        Set<String> account1BeneficiariesNames =
                account1Patterns.stream()
                        .map(TransferDestinationPattern::getName)
                        .collect(Collectors.toSet());
        assertThat(account1BeneficiariesNames).contains("Jan Kowalski", "Anna Nowak", "Jane Doe");
        Set<String> account1BeneficiariesPatterns =
                account1Patterns.stream()
                        .map(TransferDestinationPattern::getPattern)
                        .collect(Collectors.toSet());
        assertThat(account1BeneficiariesPatterns)
                .contains(
                        ".+",
                        "GB71BARC20038054128742",
                        "GB97BARC20035397661636",
                        "GB28BARC20032692589734",
                        "GB09BARC20035344161221");
    }
}
