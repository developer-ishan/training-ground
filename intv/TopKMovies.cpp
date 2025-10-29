#include <bits/stdc++.h>
using namespace std;

// Movie class
class Movie {
private:
    string title;
    int views;

public:
    Movie(string t) : title(t), views(0) {}

    string getTitle() const { return title; }
    int getViews() const { return views; }

    // Increment views when played
    void play() { views++; }

    // Comparator for set
    bool operator<(const Movie &other) const {
        if (views == other.views)
            return title < other.title;
        return views > other.views;
    }
};

// MovieService class
class MovieService {
private:
    unordered_map<string, Movie> movieMap; // fast lookup by title
    set<Movie> moviesByViews;               // sorted by views

public:
    // Add a new movie
    void addMovie(const string &title) {
        Movie m(title);
        movieMap.insert({title, m});
        moviesByViews.insert(m);
    }

    // Play a movie by title
    void playMovie(const string &title) {
        auto it = movieMap.find(title);
        if (it == movieMap.end()) {
            cout << "Movie not found: " << title << endl;
            return;
        }

        // Update views and maintain set ordering
        Movie updated = it->second;
        moviesByViews.erase(updated);
        updated.play();
        moviesByViews.insert(updated);
        it->second = updated;
    }

    // Print all movies sorted by views
    void printMovies() const {
        cout << "Movies sorted by views:\n";
        for (const auto &m : moviesByViews) {
            cout << m.getTitle() << " (" << m.getViews() << " views)\n";
        }
    }
};

int main() {
    MovieService service;

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
    service.playMovie("Avatar");

    // Print results
    service.printMovies();

    return 0;
}
