package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

public class SegmentPositionCounter {

    private int position = 1;

    public int getAndIncrement() {
        return position++;
    }
}
