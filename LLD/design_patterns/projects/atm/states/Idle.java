package projects.atm.states;

import projects.atm.context.ATM;
import projects.atm.context.Card;

public class Idle implements State {
    @Override
    public void insertCard(ATM atm, Card card) {
        if(card!=null) {
            atm.setCurrentState(new HasCard(card));
            System.out.println("Card inserted successfully");
        }
        System.out.println("Invalid card");
    }

    @Override
    public void authenticate(ATM atm, String pin) {
        System.out.println("Insert Card First");

    }

    @Override
    public void checkBalance(ATM atm) {
        System.out.println("Not Authenticated");

    }
}
