package behavioral.template.with_builder;

public class SalesReport extends ReportGenerator{

    private final String salesman;

    protected SalesReport(Builder builder) {
        super(builder);
        salesman = builder.salesman;
    }

    @Override
    protected void fetchData() {
        System.out.println("Fetching sales data...");
    }

    @Override
    protected void analyseData() {
        System.out.println("Analyzing sales data...");
    }

    @Override
    protected void formatResults() {
        System.out.println("Formatting sales results...");
    }

    public static class Builder extends ReportGenerator.Builder<Builder>{
        private String salesman;

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public SalesReport build() {
            return new SalesReport(this);
        }

        public Builder withSalesman(String salesman) {
            this.salesman = salesman;
            return this;
        }
    }
}
