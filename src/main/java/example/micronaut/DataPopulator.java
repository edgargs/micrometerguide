package example.micronaut;

import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;

import javax.transaction.Transactional;

@Singleton
public class DataPopulator {

    private final BookRepository bookRepository;

    DataPopulator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @EventListener
    @Transactional
    void init(StartupEvent event) {
        if (bookRepository.count() == 0 ) {
            bookRepository.save(new Book("1491950358", "Building Microservices"));
            bookRepository.save(new Book("1680502395", "Release It!"));
            bookRepository.save(new Book("0321601912", "Continuous Delivery"));
        }
    }
}
