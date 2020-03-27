package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIPINS;

@Slf4j
public class TanByOperationLookup {
    private Map<SegmentType, Boolean> operations = new HashMap<>();

    public TanByOperationLookup(HIPINS hipins) {
        List<Pair<String, Boolean>> operationsData = hipins.getOperations();
        operationsData.forEach(op -> operations.put(SegmentType.of(op.getLeft()), op.getRight()));
    }

    public boolean doesOperationRequireTAN(SegmentType segmentType) {
        Boolean operationRequiresTan = operations.get(segmentType);
        return Optional.ofNullable(operationRequiresTan)
                .orElseGet(
                        () -> {
                            log.warn("Could not find {}", segmentType);
                            return true;
                        });
    }

    public boolean isOperationSupported(SegmentType segmentType) {
        return operations.containsKey(segmentType);
    }
}
