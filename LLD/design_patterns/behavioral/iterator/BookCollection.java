package behavioral.iterator;

import java.util.ArrayList;
import java.util.List;

public class BookCollection{
    private final List<Book> books;

    public BookCollection() {
        books = new ArrayList<>();
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public BookIterator getIterator() {
        return new BookIterator(books);
    }
}
