package behavioral.iterator;

public class Main {
    public static void main(String[] args) {
        BookCollection collection = new BookCollection();
        collection.addBook(new Book("1984", "George Orwell"));
        collection.addBook(new Book("Brave New World", "Aldous Huxley"));

        BookIterator iterator = collection.getIterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            System.out.println(book.getTitle() + " by " + book.getAuthor());
        }
    }
}
