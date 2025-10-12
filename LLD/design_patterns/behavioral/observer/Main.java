package behavioral.observer;

public class Main {
    public static void main(String[] args) {
        WeatherStation weatherStation = new WeatherStation();
        CurrentConditionsDisplay currentConditionsDisplay = new CurrentConditionsDisplay();

        weatherStation.addObserver(currentConditionsDisplay);

        weatherStation.setHumidity("10");
        weatherStation.setTemperature("20");
        weatherStation.setPressure("1");
    }
}
