package formdata;

import java.util.ArrayList;

public class SimplePassageData {
    public String passageText;
    public String passageTitle;
    public int grade;
    public String source;
    public String category;

    public ArrayList<String> tags = new ArrayList<String>();


    public SimplePassageData() {
    }


    public SimplePassageData(String p, String t, int g, String c, String s) {
        this.passageText = p;
        this.passageTitle = t;
        this.grade = g;
        this.category = c;
        this.source = s;
    }
    public String getPassageText() {
        return this.passageText;
    }

    public String getPassageTitle() {
        return this.passageTitle;
    }

    public String getPassageSource() {
        return this.source;
    }
}