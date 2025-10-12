package projects.atm.states;

import projects.atm.context.ATM;
import projects.atm.context.Card;
import projects.atm.models.User;
import projects.atm.models.UserService;

public class HasCard implements State {

    private final Card card;

    public HasCard(Card card) {
        this.card = card;
    }
    @Override
    public void insertCard(ATM atm, Card card) {
        System.out.println("Already have a card");
    }

    @Override
    public void authenticate(ATM atm, String pin) {
        User user = UserService.getUser(card , pin);
        if(user != null) {
            atm.setCurrentState(new Authenticated(UserService.getUser(card, pin)));
            System.out.println("Successfully authenticated");
        } else{
            System.out.println("Card doesn't match");
            atm.setCurrentState(new Idle());
        }
    }

    @Override
    public void checkBalance(ATM atm) {
        System.out.println("Authenticate the card first");
    }
}
