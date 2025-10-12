package behavioral.template;

public abstract class ReportGenerator {

    // Template method (final to prevent overriding)
    public final void generateReport() {
        fetchData();
        analyzeData();
        formatResults();
        export();
    }

    // Steps to be implemented by subclasses
    protected abstract void fetchData();
    protected abstract void analyzeData();
    protected abstract void formatResults();

    // Common step
    private void export() {
        System.out.println("Exporting report to PDF/CSV...");
    }
}
