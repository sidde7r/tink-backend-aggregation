package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.UserForgotPasswordToken;

public interface UserForgotPasswordTokenRepository extends JpaRepository<UserForgotPasswordToken, String>,
        UserForgotPasswordTokenRepositoryCustom {
    
    List<UserForgotPasswordToken> findByUserId(String userId);
}
