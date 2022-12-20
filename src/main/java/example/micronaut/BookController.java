package example.micronaut;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.util.Optional;

@Controller("/books")
@ExecuteOn(TaskExecutors.IO)
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Get
    @Timed("books.index")
    Iterable<Book> index() {
        return bookRepository.findAll();
    }

    @Get("/{isbn}")
    @Counted("books.find")
    Optional<Book> findBook(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
}
