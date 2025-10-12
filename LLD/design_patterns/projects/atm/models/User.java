package projects.atm.models;

public class User {
    private int balance;
    public User(int balance) {
        this.balance = balance;
    }
    public int getBalance() {
        return balance;
    }
    public void setBalance(int balance) {
        this.balance = balance;
    }
}
