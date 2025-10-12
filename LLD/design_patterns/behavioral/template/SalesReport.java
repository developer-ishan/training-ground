package behavioral.template;

public class SalesReport extends ReportGenerator {

    @Override
    protected void fetchData() {
        System.out.println("Fetching sales data from database...");
    }

    @Override
    protected void analyzeData() {
        System.out.println("Analyzing sales trends...");
    }

    @Override
    protected void formatResults() {
        System.out.println("Formatting sales data into charts...");
    }
}

