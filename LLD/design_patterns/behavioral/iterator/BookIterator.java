package behavioral.iterator;

import java.util.List;

public class BookIterator implements Iteratable<Book> {
    private List<Book> books;
    int position;
    int size;

    public BookIterator(List<Book> books) {
        this.books = books;
        this.position = 0;
        this.size = books.size();
    }
    @Override
    public Book next() {
        return books.get(position++);
    }

    @Override
    public boolean hasNext() {
        return position < size;
    }
}
