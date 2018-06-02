package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import se.tink.backend.core.AbnAmroBufferedAccount;

public interface AbnAmroBufferedAccountRepositoryCustom {
    
    public void deleteByCredentialsId(String credentialsId);
    
    public List<AbnAmroBufferedAccount> findByCredentialsId(String credentialsId);
}
