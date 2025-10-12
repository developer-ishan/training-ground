package behavioral.template.with_builder.export_strategies;

public class PdfExportStrategy implements ExportStrategy {
    @Override
    public void export() {
        System.out.println("Exporting as PDF...");
    }
}