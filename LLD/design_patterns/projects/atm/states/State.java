package projects.atm.states;

import projects.atm.context.ATM;
import projects.atm.context.Card;

public interface State {
    public void insertCard(ATM atm, Card card);
    public void authenticate(ATM atm, String pin);
    public void checkBalance(ATM atm);
}
