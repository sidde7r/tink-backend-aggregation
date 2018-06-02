package se.tink.backend.common.repository.mysql.main;

interface BankIdAuthenticationRepositoryCustom {
    int deleteExpiredTokens(int bankidAuthenticationTimeToLive);
}
