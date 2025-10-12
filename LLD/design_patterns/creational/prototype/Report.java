package creational.prototype;

public class Report extends Document{

    private String department;

    @Override
    public Document clone() {
        Report clonedReport = new Report();
        clonedReport.setTitle(this.getTitle());
        clonedReport.setDepartment(this.getDepartment());
        return clonedReport;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
