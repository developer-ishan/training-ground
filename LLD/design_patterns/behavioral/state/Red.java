package behavioral.state;

public class Red implements State{
    @Override
    public void next(Context context) {
        System.out.println("Switching from RED to GREEN, Cars go!");
        context.setState(new Green());
    }

    @Override
    public String getColor() {
        return "";
    }
}
