package se.tink.backend.common.repository.mysql.main;

import java.util.Date;
import java.util.List;

interface AccountRepositoryCustom {

    void deleteByUserId(String userId);

    void deleteByUserIdAndCredentialsId(String userId, String credentialsId);

    void deleteByIds(List<String> accountIds);

    void addToBalanceById(String accountId, double additionToBalance);

    void setCertainDateById(String accountId, Date certainDate);
}
