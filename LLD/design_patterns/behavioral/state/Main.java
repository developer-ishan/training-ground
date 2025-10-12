package behavioral.state;

public class Main {
    public static void main(String[] args) {
        Context trafficLight = new Context();

        trafficLight.next(); // RED -> GREEN

        trafficLight.next(); // GREEN -> YELLOW
        trafficLight.next(); // YELLOW -> RED
        trafficLight.next(); // RED -> GREEN
        // Adding new states like BLINKING or MAINTENANCE is easy now
    }
}
