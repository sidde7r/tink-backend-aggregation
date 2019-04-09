package se.tink.libraries.concurrency;

import com.google.common.base.Ticker;
import java.util.concurrent.atomic.AtomicLong;

public class FakeTicker extends Ticker {

    public AtomicLong now = new AtomicLong();

    public FakeTicker(long i) {
        now = new AtomicLong(i);
    }

    public FakeTicker() {
        now = new AtomicLong();
    }

    @Override
    public long read() {
        return now.get();
    }
}
