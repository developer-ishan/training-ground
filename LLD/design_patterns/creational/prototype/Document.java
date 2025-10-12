package creational.prototype;

public abstract class Document implements Cloneable {
    private String title;
    public abstract Document clone();
    public String getTitle(){
        return title;
    };
    public void setTitle(String title){
        this.title = title;
    }
}
