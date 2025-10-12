package behavioral.template.with_builder;

public class InventoryReport extends ReportGenerator{

    private InventoryReport(Builder builder){
        super(builder);
    }

    @Override
    protected void fetchData() {
        System.out.println("Fetching inventory data...");
    }

    @Override
    protected void analyseData() {
        System.out.println("Analyzing inventory data...");
    }

    @Override
    protected void formatResults() {
        System.out.println("Formatting inventory results...");
    }

    public static class Builder extends ReportGenerator.Builder<Builder>{
        @Override
        public InventoryReport build() {
            return new InventoryReport(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
