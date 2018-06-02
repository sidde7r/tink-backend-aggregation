package se.tink.backend.common.repository.cassandra;

import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.BankFeeStatistics;

import java.util.List;

public interface BankFeeStatisticsRepositoryCustom extends Creatable {
    List<BankFeeStatistics> findAllByProviderNameAndYear(String providerName, int year);
}
