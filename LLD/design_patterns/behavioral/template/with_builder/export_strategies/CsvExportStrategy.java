package behavioral.template.with_builder.export_strategies;


public class CsvExportStrategy implements ExportStrategy {
    @Override
    public void export() {
        System.out.println("Exporting as CSV...");
    }
}