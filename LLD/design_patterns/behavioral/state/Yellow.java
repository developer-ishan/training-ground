package behavioral.state;

class Yellow implements State {
    @Override
    public void next(Context context) {
        System.out.println("Switching from YELLOW to RED. Stop!");
        context.setState(new Red());
    }
    @Override
    public String getColor() {
        return "YELLOW";
    }
}
