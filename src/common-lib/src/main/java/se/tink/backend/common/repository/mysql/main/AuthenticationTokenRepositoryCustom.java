package se.tink.backend.common.repository.mysql.main;

interface AuthenticationTokenRepositoryCustom {
    int deleteExpiredTokens(int authenticationTimeToLive);
}
