package se.tink.backend.common.repository.mysql.main;

public interface UserForgotPasswordTokenRepositoryCustom {
    public void deleteByUserId(String userId);
}
