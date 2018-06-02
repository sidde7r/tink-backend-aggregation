package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.UserState;

public interface UserStateRepository extends JpaRepository<UserState, String>, UserStateRepositoryCustom {
    List<UserState> findAllByHaveHadTransactions(boolean haveHadTransactions);
}
