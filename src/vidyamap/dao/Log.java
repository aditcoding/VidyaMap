/*
 * Author : Nidhi Tyagi (NT)
 * email : nityagi@cs.wisc.edu
 * Project : VidyaMap @ Compass Group, WCER
 */

package vidyamap.dao;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Log {

    
    @Id
    private int id;
    private int student_id;
    private int group_id;
    private String time;
    private String question;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getStudentid() {
        return student_id;
    }
    public void setLevel(int Student_id) {
        this.student_id = Student_id;
    }
    public int getGroupid() {
        return group_id;
    }
    public void setGroup(int group_id) {
        this.group_id = group_id;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
}
