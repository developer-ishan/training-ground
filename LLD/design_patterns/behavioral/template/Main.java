package behavioral.template;

public class Main {
    public static void main(String[] args) {
        ReportGenerator salesReport = new SalesReport();
        System.out.println("Generating Sales Report:");
        salesReport.generateReport();

        System.out.println("\nGenerating Inventory Report:");
        ReportGenerator inventoryReport = new InventoryReport();
        inventoryReport.generateReport();
    }
}
