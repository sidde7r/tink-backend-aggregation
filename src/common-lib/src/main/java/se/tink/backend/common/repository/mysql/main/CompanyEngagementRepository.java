package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.CompanyEngagement;

public interface CompanyEngagementRepository extends JpaRepository<CompanyEngagement, String>, CompanyEngagementsRepositoryCustom {

    public List<CompanyEngagement> findAllByPersonNumber(String personNumber);

}
