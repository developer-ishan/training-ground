import java.util.*;

class Movie implements Comparable<Movie> {
    private String title;
    private int views;

    public Movie(String title) {
        this.title = title;
        this.views = 0;
    }

    public String getTitle() {
        return title;
    }

    public int getViews() {
        return views;
    }

    public void play() {
        views++;
    }

    // Comparator for TreeSet: descending views, tie-breaker: title ascending
    @Override
    public int compareTo(Movie other) {
        if (this.views != other.views) {
            return Integer.compare(other.views, this.views); // descending
        }
        return this.title.compareTo(other.title); // ascending
    }

    // Required for TreeSet remove() to work properly
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie)) return false;
        Movie movie = (Movie) o;
        return title.equals(movie.title);
    }
}

class MovieService {
    private Map<String, Movie> movieMap = new HashMap<>();
    private TreeSet<Movie> moviesByViews = new TreeSet<>();

    public void addMovie(String title) {
        Movie m = new Movie(title);
        movieMap.put(title, m);
        moviesByViews.add(m);
    }

    public void playMovie(String title) {
        Movie m = movieMap.get(title);
        if (m == null) {
            System.out.println("Movie not found: " + title);
            return;
        }

        // Update TreeSet ordering
        moviesByViews.remove(m);
        m.play();
        moviesByViews.add(m);
    }

    public void printMovies() {
        System.out.println("Movies sorted by views:");
        for (Movie m : moviesByViews) {
            System.out.println(m.getTitle() + " (" + m.getViews() + " views)");
        }
    }
}

class Main {
    public static void main(String[] args) {
        MovieService service = new MovieService();

        // Add movies
        service.addMovie("Inception");
        service.addMovie("Matrix");
        service.addMovie("Tenet");
        service.addMovie("Avatar");

        // Simulate playing movies
        service.playMovie("Inception");
        service.playMovie("Inception");
        service.playMovie("Matrix");
        service.playMovie("Avatar");
        service.playMovie("Avatar");

        // Print results
        service.printMovies();
    }
}
