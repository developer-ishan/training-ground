### 🧩 LLD Question: Iterator Design Pattern

You are designing a library management system that manages a collection of books. Each book has a title and an author.

You need to implement a custom collection class `BookCollection` that stores `Book` objects. Your goal is to allow clients to iterate over this collection without exposing the internal data structure used to store the books.

#### 🔧 Requirements:

1. Create a `Book` class with the fields `title` and `author`.
2. Implement the `BookCollection` class that stores a list of books and provides:
    - A method to add a book.
    - A method to return an iterator to iterate over the collection.
3. Create a `BookIterator` class that implements a custom `Iterator` interface.
4. Ensure that clients can iterate over the `BookCollection` using the iterator.
5. The iteration logic should be decoupled from the internal storage mechanism.
6. Bonus: Implement both forward and reverse iteration.

#### 🔍 Example Usage:

```java
BookCollection collection = new BookCollection();
collection.addBook(new Book("1984", "George Orwell"));
collection.addBook(new Book("Brave New World", "Aldous Huxley"));

BookIterator iterator = collection.getIterator();
while (iterator.hasNext()) {
    Book book = iterator.next();
    System.out.println(book.getTitle() + " by " + book.getAuthor());
}
