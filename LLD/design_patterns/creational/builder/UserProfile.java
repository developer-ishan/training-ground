package creational.builder;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    private final String name;// (required)
    private final int age;// (optional)
    private final String email;// (optional)
    private final String phoneNumber;// (optional)
    private final String address;// (optional)
    private final String bio;// (optional)
    private final List<String> interests;// (optional)

    private UserProfile(Builder builder) {
        this.name = builder.name;
        this.age = builder.age;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.address = builder.address;
        this.bio = builder.bio;
        this.interests = new ArrayList<>(builder.interests);
    }

    public static class Builder {
        private final String name;// (required)
        private int age = 0;// (optional)
        private String email = null;// (optional)
        private String phoneNumber = null;// (optional)
        private String address = null;// (optional)
        private String bio = "No bio available";// (optional)
        private List<String> interests = new ArrayList<>();// (optional)

        public Builder(String name) {
            this.name = name;
        }

        public Builder age(int age) {
            if (age < 0) {
                throw new IllegalArgumentException("Age cannot be negative");
            }
            this.age = age;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder bio(String bio) {
            if (bio == null) {
                this.bio = "No bio available";
                return this;
            }
            this.bio = bio;
            return this;
        }

        public Builder interests(List<String> interests) {
            this.interests = interests;
            return this;
        }

        public UserProfile build() {
            return new UserProfile(this);
        }
    }
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getBio() {
        return bio;
    }

    public List<String> getInterests() {
        return new ArrayList<>(interests);
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", bio='" + bio + '\'' +
                ", interests=" + interests +
                '}';
    }
}
