package se.tink.backend.insights.accounts;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.insights.accounts.mapper.AccountMapper;
import se.tink.backend.insights.core.valueobjects.Account;
import se.tink.backend.insights.core.valueobjects.UserId;
import static se.tink.backend.core.AccountTypes.CHECKING;

public class AccountQueryServiceImpl implements AccountQueryService {
    private AccountDao accountDao;

    @Inject
    public AccountQueryServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public List<Account> getCheckingAccounts(UserId userId) {
        return accountDao.findByUserId(userId.value()).stream()
                .filter(a -> !a.isExcluded())
                .filter(a -> Objects.equals(a.getType(), CHECKING))
                .map(AccountMapper::translate).collect(Collectors.toList());
    }
}
