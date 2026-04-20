package src.models;

public class User {
  private final String id;
  private final String name;
  private final String email;
  public User(String id, String name, String email) {
    this.id = id;
    this.name = name;
    this.email = email;
  }
  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public String getEmail() {
    return email;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;
    return id.equals(((User) o).id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
