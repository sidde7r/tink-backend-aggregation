package se.tink.backend.common.repository.mysql.main;

import org.springframework.transaction.annotation.Transactional;

public interface CompanyEngagementsRepositoryCustom {

    @Transactional
    public void truncate();
}
