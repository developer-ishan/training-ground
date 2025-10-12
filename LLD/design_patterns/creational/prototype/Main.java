package creational.prototype;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // Create the registry
        DocumentRegistry registry = new DocumentRegistry();

        // Create and register a Report prototype
        Report reportPrototype = new Report();
        reportPrototype.setTitle("Monthly Sales Report");
        reportPrototype.setDepartment("Sales");
        registry.register("report", reportPrototype);

        // Create and register a Resume prototype
        Resume resumePrototype = new Resume();
        resumePrototype.setTitle("Software Engineer Resume");
        resumePrototype.setSkills(Arrays.asList("Java", "Spring", "SQL"));
        registry.register("resume", resumePrototype);

        // Clone a Report
        Document clonedReport = registry.getClonedDocument("report");
        if (clonedReport instanceof Report) {
            Report report = (Report) clonedReport;
            System.out.println("Cloned Report Title: " + report.getTitle());
            System.out.println("Cloned Report Department: " + report.getDepartment());
        }

        // Clone a Resume
        Document clonedResume = registry.getClonedDocument("resume");
        if (clonedResume instanceof Resume) {
            Resume resume = (Resume) clonedResume;
            System.out.println("Cloned Resume Title: " + resume.getTitle());
            System.out.println("Cloned Resume Skills: " + resume.getSkills());
        }

        // Try cloning an unregistered document type
        Document unknown = registry.getClonedDocument("presentation");
        System.out.println("Unknown document: " + unknown); // Should print null
    }
}
