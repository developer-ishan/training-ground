package creational.builder;

public class Main {
    public static void main(String[] args) {
        UserProfile profile = new UserProfile.Builder("Alice")
                .age(28)
                .email("alice@example.com")
                .bio("Loves photography")
                .build();

        System.out.println(profile);
    }
}
