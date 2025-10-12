package behavioral.template;

public class InventoryReport extends ReportGenerator {

    @Override
    protected void fetchData() {
        System.out.println("Fetching inventory data from warehouse DB...");
    }

    @Override
    protected void analyzeData() {
        System.out.println("Calculating stock levels and reorder alerts...");
    }

    @Override
    protected void formatResults() {
        System.out.println("Structuring inventory tables for the report...");
    }
}

