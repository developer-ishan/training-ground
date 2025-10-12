package behavioral.state;

class Green implements State {
    @Override
    public void next(Context context) {
        System.out.println("Switching from GREEN to YELLOW. Slow down!");
        context.setState(new Yellow());
    }
    @Override
    public String getColor() {
        return "GREEN";
    }
}

