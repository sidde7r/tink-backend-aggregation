package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.UserOrigin;

public interface UserOriginRepository extends JpaRepository<UserOrigin, Long>, UserOriginRepositoryCustom {
    public UserOrigin findOneByUserId(String userId);
}
