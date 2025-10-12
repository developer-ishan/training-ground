package behavioral.template.with_builder;

import behavioral.template.with_builder.export_strategies.ExportStrategy;

public abstract class ReportGenerator {
    private final ExportStrategy exportStrategy;
    private boolean shouldAnalyse;

    protected ReportGenerator(Builder builder) {
        this.shouldAnalyse = builder.shouldAnalyse;
        this.exportStrategy = builder.exportStrategy;
    }

    //template method
    public final void generateReport() {
        fetchData();
        if(shouldAnalyse) {
            analyseData();
        }
        formatResults();
        exportStrategy.export();
    }

    //customisable methods
    protected abstract void fetchData();
    protected abstract void analyseData();
    protected abstract void formatResults();

    //common methods
    private final void export(){
        System.out.println("Exporting to csv/pdf");
    }

    public static abstract class Builder<T extends Builder> {
        private boolean shouldAnalyse = true;
        private ExportStrategy exportStrategy;

        public T skipAnalysis(){
            shouldAnalyse = false;
            return self();
        }

        public T withExportStrategy(ExportStrategy exportStrategy){
            this.exportStrategy = exportStrategy;
            return self();
        }

        protected abstract T self();
        public abstract ReportGenerator build();
    }
}
