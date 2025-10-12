package projects.atm.models;

import projects.atm.context.Card;

public class UserService {
    public static User getUser(Card card, String pin) {
        String card_number = card.authenticate(pin);
        if(card_number == null) {
            return null;
        }
        return new User(100);
    }
}
