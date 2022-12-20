package example.micronaut.crypto;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class CryptoService {

    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    private final PriceClient priceClient;
    private final AtomicInteger latestPriceUsd = new AtomicInteger(0);
    private final Counter checks;
    private final Timer time;

    public CryptoService(PriceClient priceClient,
                         MeterRegistry meterRegistry) {
        this.priceClient = priceClient;

        checks = meterRegistry.counter("bitcoin.price.checks");
        time = meterRegistry.timer("bitcoin.price.time");
        meterRegistry.gauge("bitcoin.price.latest", latestPriceUsd);
    }

    @Scheduled( fixedRate = "${crypto.updateFrequency:1h}",
                initialDelay = "${crypto.initialDelay:0s}")
    public void updatePrice() {
        time.record(
                () -> {
                    try {
                        checks.increment();
                        latestPriceUsd.set(
                                (int)priceClient.latestInUSD().getPrice()
                        );
                    } catch (Exception e) {
                        log.error("Problem checking price", e);
                    }
                }
        );

    }
}
