package creational.prototype;

import java.util.ArrayList;
import java.util.List;

public class Resume extends Document{
    private List<String> skills;

    @Override
    public Document clone(){
        Resume resume = new Resume();
        resume.setSkills(new ArrayList<>(this.getSkills()));
        resume.setTitle(this.getTitle());
        return resume;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}
