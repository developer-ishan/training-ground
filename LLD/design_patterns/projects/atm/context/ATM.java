package projects.atm.context;

import projects.atm.states.Idle;
import projects.atm.states.State;

public class ATM {
    private State currentState;

    public ATM() {
        this.currentState = new Idle();
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public void insertCard(Card card) {
        this.currentState.insertCard(this, card);
    }

    public void enterPin(String pin) {
        this.currentState.authenticate(this, pin);
    }

    public void checkBalance() {
        this.currentState.checkBalance(this);
    }
}
