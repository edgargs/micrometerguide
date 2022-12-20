package example.micronaut

import example.micronaut.crypto.CryptoService
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@Slf4j
class CryptoUpdatesSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer kucoinServer = ApplicationContext.run(EmbeddedServer,
        ['spec.name': 'MetricsTestKucoin'])

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer,
        ['micronaut.http.services.kucoin.url': "http://localhost:${kucoinServer.port}"])

    void "test crypto updates"() {
        given:
        CryptoService cryptoService = embeddedServer.applicationContext.getBean(CryptoService)
        MeterRegistry meterRegistry = embeddedServer.applicationContext.getBean(MeterRegistry)

        Counter counter = meterRegistry.counter('bitcoin.price.checks')

        when:
        int checks = 3
        (1..checks).each {
            log.info "$it"
            cryptoService.updatePrice()
        }

        then:
        checks == counter.count()
    }

    @Requires(property = 'spec.name', value = 'MetricsTestKucoin')
    @Controller
    static class MockKucoinController {

        private static final String RESPONSE = '''\
{
   "code":"200000",
   "data":{
      "time":1654865889872,
      "sequence":"1630823934334",
      "price":"29670.4",
      "size":"0.00008436",
      "bestBid":"29666.4",
      "bestBidSize":"0.16848947",
      "bestAsk":"29666.5",
      "bestAskSize":"2.37840044"
   }
}
'''
        @Get('/api/v1/market/orderbook/level1')
        String latest(@QueryValue String symbol) {
            RESPONSE
        }
    }
}
