package behavioral.state;

public class Context {
    private State currentState;
    public Context() {
        this.currentState = new Red();
    }

    public void setState(State state) {
        this.currentState = state;
    }

    public void next(){
        currentState.next(this);
    }

    public String getColour(){
        return currentState.getColor();
    }
}
