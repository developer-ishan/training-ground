package projects.atm.states;

import projects.atm.context.ATM;
import projects.atm.context.Card;
import projects.atm.models.User;

public class Authenticated implements State {

    private User user;

    public Authenticated(User user) {
        this.user = user;
    }

    @Override
    public void insertCard(ATM atm, Card card) {
        System.out.println("Card Already present");
    }

    @Override
    public void authenticate(ATM atm, String pin) {
        System.out.println("Already authenticated");
    }

    @Override
    public void checkBalance(ATM atm) {
        System.out.println("Balance is " + user.getBalance());
        atm.setCurrentState(new Idle());
    }


}
