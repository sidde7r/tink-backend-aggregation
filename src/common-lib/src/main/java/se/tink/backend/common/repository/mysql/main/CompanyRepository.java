package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.Company;

public interface CompanyRepository extends JpaRepository<Company, String> {

    public Company findByOrgNumber(String orgNumber);

}
