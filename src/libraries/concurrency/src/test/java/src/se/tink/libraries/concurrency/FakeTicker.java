package src.se.tink.libraries.concurrency;

import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Ticker;

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
