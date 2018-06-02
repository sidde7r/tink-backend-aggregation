package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.tink.backend.core.UserDemographics;

@Repository
public interface UserDemographicsRepository extends JpaRepository<UserDemographics, String>, UserDemographicsRepositoryCustom {

}
