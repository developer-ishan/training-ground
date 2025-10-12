package behavioral.state;

public interface State {
    void next(Context context);
    String getColor();
}
