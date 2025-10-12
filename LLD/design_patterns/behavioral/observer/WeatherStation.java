package behavioral.observer;

import java.util.ArrayList;
import java.util.List;

public class WeatherStation implements Observable{
    private List<Observer> observers;
    private String temperature;
    private String humidity;
    private String pressure;

    public WeatherStation() {
        observers = new ArrayList<>();
    }
    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
        notifyObservers();
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
        notifyObservers();
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
        notifyObservers();
    }

    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getPressure() {
        return pressure;
    }

    @Override
    public String toString() {
        return "WeatherStation{" +
                "observers=" + observers +
                ", temperature='" + temperature + '\'' +
                ", humidity='" + humidity + '\'' +
                ", pressure='" + pressure + '\'' +
                '}';
    }
}
