package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
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

public class BPostBankTransactionalAccountFetcherTest {

    private BPostBankApiClient apiClient;
    private BPostBankAuthContext authContext;
    private Random random = new Random();

    @Before
    public void init() {
        apiClient = Mockito.mock(BPostBankApiClient.class);
        authContext = Mockito.mock(BPostBankAuthContext.class);
    }

    @Test
    public void fetchAccountsShouldNotReturnAnyCheckingAccount() throws RequestException {
        // given
        BPostBankAccountDTO regularAccount = createDummyAccount();
        BPostBankAccountsResponseDTO responseDTO = new BPostBankAccountsResponseDTO();
        responseDTO.currentAccounts = Lists.newArrayList(regularAccount);
        responseDTO.savingsAccounts = new LinkedList<>();
        Mockito.when(apiClient.fetchAccounts(authContext)).thenReturn(responseDTO);
        BPostBankTransactionalAccountFetcher objectUnderTest =
                new BPostBankTransactionalAccountFetcher(apiClient, authContext);
        // when
        Collection<TransactionalAccount> result = objectUnderTest.fetchAccounts();
        // then
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void fetchAccountsShouldReturnSavingAccount() throws RequestException {
        // given
        BPostBankAccountDTO savingAccount = createDummyAccount();
        BPostBankAccountsResponseDTO responseDTO = new BPostBankAccountsResponseDTO();
        responseDTO.currentAccounts = new LinkedList<>();
        responseDTO.savingsAccounts = Lists.newArrayList(savingAccount);
        Mockito.when(apiClient.fetchAccounts(authContext)).thenReturn(responseDTO);
        BPostBankTransactionalAccountFetcher objectUnderTest =
                new BPostBankTransactionalAccountFetcher(apiClient, authContext);
        // when
        Collection<TransactionalAccount> result = objectUnderTest.fetchAccounts();
        // then
        TransactionalAccount transactionalAccount = result.iterator().next();
        Assert.assertEquals(savingAccount.alias, transactionalAccount.getName());
        Assert.assertEquals(
                new BigDecimal(savingAccount.bookedBalance),
                transactionalAccount.getExactBalance().getExactValue());
        Assert.assertEquals(
                TransactionalAccountType.SAVINGS.toAccountType(), transactionalAccount.getType());
        Assert.assertEquals(
                savingAccount.accountIdentification.get(0).id,
                transactionalAccount.getAccountNumber());
        Assert.assertEquals(
                savingAccount.accountIdentification.get(0).id,
                transactionalAccount.getIdModule().getUniqueId());
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
        responseDTO.savingsAccounts = Lists.newArrayList(accountDTO);
        Mockito.when(apiClient.fetchAccounts(authContext)).thenReturn(responseDTO);
        BPostBankTransactionalAccountFetcher objectUnderTest =
                new BPostBankTransactionalAccountFetcher(apiClient, authContext);
        // when
        objectUnderTest.fetchAccounts();
    }

    private BPostBankAccountDTO createDummyAccount() {
        BPostBankAccountIdentifierDTO accountIdentifierDTO = new BPostBankAccountIdentifierDTO();
        accountIdentifierDTO.scheme = "IBAN";
        accountIdentifierDTO.id = "BE685390075470" + String.format("%02d", random.nextInt(100));
        BPostBankAccountDTO accountDTO = new BPostBankAccountDTO();
        accountDTO.accountIdentification = Lists.newArrayList(accountIdentifierDTO);
        accountDTO.alias = "account name";
        accountDTO.availableBalance = "" + random.nextDouble();
        accountDTO.bookedBalance = "" + random.nextDouble();
        accountDTO.clientName = "Name Surname";
        accountDTO.currency = "EUR";
        return accountDTO;
    }
}
