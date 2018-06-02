package se.tink.backend.main.controllers;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Loan;
import se.tink.backend.core.LoanResponse;
import se.tink.backend.rpc.loans.ListLoansCommand;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoanServiceControllerTest {
    @Mock
    LoanDataRepository loanDataRepository;
    @Mock
    AccountRepository accountRepository;
    @Mock
    LoanDAO loanDao;

    @Test
    public void testListAllLoansWithAllRequirementsFullfilled() {
        LoanServiceController controller = new LoanServiceController(loanDataRepository, accountRepository, loanDao);

        String userId = UUIDUtils.generateUUID();

        Account account = new Account();
        account.setUserId(userId);
        account.setType(AccountTypes.MORTGAGE);

        when(accountRepository.findByUserId(userId)).thenReturn(Lists.newArrayList(account));

        Loan loan = new Loan();
        loan.setUserId(UUIDUtils.fromString(userId));
        loan.setId(UUIDs.timeBased());
        loan.setType(Loan.Type.MORTGAGE);
        loan.setBalance(100D);
        loan.setInterest(0.02D);

        when(loanDataRepository.findMostRecentOneByAccountId(account.getId())).thenReturn(loan);

        LoanResponse response = controller.list(new ListLoansCommand(userId));

        // Expect one loan
        assertThat(response.getLoans().size()).isEqualTo(1);
    }
}
