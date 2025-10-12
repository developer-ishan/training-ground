package projects.atm;

import projects.atm.context.ATM;
import projects.atm.context.Card;

public class Main {
    public static void main(String[] args) {
        ATM atm = new ATM();
        Card card = new Card("1234", "****");

        atm.insertCard(card);
        atm.enterPin("****");
        atm.checkBalance();


        atm.insertCard(card);
        atm.checkBalance();
    }
}
