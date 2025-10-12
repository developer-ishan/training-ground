package behavioral.observer;

public class CurrentConditionsDisplay implements Observer{
    @Override
    public void update(Observable observable) {
        System.out.println(observable);
    }
}
