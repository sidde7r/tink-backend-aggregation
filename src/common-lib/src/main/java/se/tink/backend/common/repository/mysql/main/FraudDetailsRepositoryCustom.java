package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import se.tink.backend.core.FraudDetails;

public interface FraudDetailsRepositoryCustom {

    public List<FraudDetails> findAllForIds(List<String> fraudDetailIds);
}
