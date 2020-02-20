package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.BPostBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;

public class BPostBankTransactionalAccountFetcherTest {

    private BPostBankApiClient apiClient;
    private BPostBankAuthContext authContext;

    @Before
    public void init() {
        apiClient = Mockito.mock(BPostBankApiClient.class);
        authContext = Mockito.mock(BPostBankAuthContext.class);
    }

    @Test
    public void fetchAccountsShouldReturnAccounts() throws RequestException {
        // given
        BPostBankAccountIdentifierDTO accountIdentifierDTO = new BPostBankAccountIdentifierDTO();
        accountIdentifierDTO.scheme = "IBAN";
        accountIdentifierDTO.id = "BE68539007547034";
        BPostBankAccountDTO accountDTO = new BPostBankAccountDTO();
        accountDTO.accountIdentification = Lists.newArrayList(accountIdentifierDTO);
        accountDTO.alias = "account name";
        accountDTO.availableBalance = "345.00";
        accountDTO.bookedBalance = "344.00";
        accountDTO.clientName = "Name Surname";
        accountDTO.currency = "EUR";
        BPostBankAccountsResponseDTO responseDTO = new BPostBankAccountsResponseDTO();
        responseDTO.currentAccounts = Lists.newArrayList(accountDTO);
        Mockito.when(apiClient.fetchAccounts(authContext)).thenReturn(responseDTO);
        BPostBankTransactionalAccountFetcher objectUnderTest =
                new BPostBankTransactionalAccountFetcher(apiClient, authContext);
        // when
        Collection<TransactionalAccount> result = objectUnderTest.fetchAccounts();
        // then
        TransactionalAccount transactionalAccount = result.iterator().next();
        Assert.assertEquals(accountDTO.alias, transactionalAccount.getName());
        Assert.assertEquals(
                new BigDecimal(accountDTO.bookedBalance),
                transactionalAccount.getExactBalance().getExactValue());
        Assert.assertEquals(
                TransactionalAccountType.CHECKING.toAccountType(), transactionalAccount.getType());
        Assert.assertEquals(
                accountDTO.accountIdentification.get(0).id,
                transactionalAccount.getAccountNumber());
        Assert.assertEquals(
                accountDTO.accountIdentification.get(0).id,
                transactionalAccount.getIdentifiers().stream()
                        .filter(i -> AccountIdentifier.Type.IBAN.equals(i.getType()))
                        .findAny()
                        .get()
                        .getIdentifier());
    }

    @Test(expected = BankServiceException.class)
    public void fetchAccountShouldThrowExceptionWhenWhenCanNotFindIBAN() throws RequestException {
        // given
        BPostBankAccountIdentifierDTO accountIdentifierDTO = new BPostBankAccountIdentifierDTO();
        accountIdentifierDTO.scheme = "INTERNAL";
        accountIdentifierDTO.id = "BE68539007547034";
        BPostBankAccountDTO accountDTO = new BPostBankAccountDTO();
        accountDTO.accountIdentification = Lists.newArrayList(accountIdentifierDTO);
        accountDTO.alias = "account name";
        accountDTO.availableBalance = "345.00";
        accountDTO.bookedBalance = "344.00";
        accountDTO.clientName = "Name Surname";
        accountDTO.currency = "EUR";
        BPostBankAccountsResponseDTO responseDTO = new BPostBankAccountsResponseDTO();
        responseDTO.currentAccounts = Lists.newArrayList(accountDTO);
        Mockito.when(apiClient.fetchAccounts(authContext)).thenReturn(responseDTO);
        BPostBankTransactionalAccountFetcher objectUnderTest =
                new BPostBankTransactionalAccountFetcher(apiClient, authContext);
        // when
        objectUnderTest.fetchAccounts();
    }
}
