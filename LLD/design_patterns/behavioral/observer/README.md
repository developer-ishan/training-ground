## 💡 LLD Question: Observer Design Pattern

Design a **notification system** using the **Observer Design Pattern**.

### 🧾 Problem Statement

You are tasked with building a **Weather Station** that publishes real-time updates about temperature, humidity, and pressure.

Multiple components (observers) should subscribe to the weather station to receive updates when new data is available.

### 🧱 Requirements

1. The system must have a **WeatherStation (Subject)** that holds temperature, humidity, and pressure.
2. The WeatherStation should allow any number of **observers** to subscribe or unsubscribe at any time.
3. When the weather data is updated, all subscribed observers should be **notified automatically**.
4. Observers could include:
    - A `CurrentConditionsDisplay` that shows the current weather.
    - A `StatisticsDisplay` that tracks average temperature over time.
    - A `ForecastDisplay` that shows predicted weather changes.
5. The design should follow the **Observer Design Pattern** principles.
6. Ensure **loose coupling** between the subject and observers so new types of observers can be added easily.
7. Ensure thread safety if updates are pushed concurrently.

### 🚀 Optional

- Add a feature where observers can **unsubscribe themselves automatically** after a certain number of updates.
- Support **different data sources** (like air quality, wind speed) in the future using a scalable design.
