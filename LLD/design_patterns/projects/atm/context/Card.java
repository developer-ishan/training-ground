package projects.atm.context;

public class Card {
    private String number;
    private String pin;

    public Card(String number, String pin) {
        this.number = number;
        this.pin = pin;
    }

    public String authenticate(String pin) {
        if(pin.equals(this.pin)) {
            return this.number;
        }
        return null;
    }
}
