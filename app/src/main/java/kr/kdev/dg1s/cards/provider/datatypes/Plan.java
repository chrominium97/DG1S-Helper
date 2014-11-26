package kr.kdev.dg1s.cards.provider.datatypes;

/**
 * Created by 꽃게 on 2014/11/26.
 */
public class Plan {

    int DATE;
    String CONTENT = "";

    public Plan() {

    }

    public Plan(int id, String plan) {
        this.DATE = id;
        this.CONTENT = plan;
    }

    @Override
    public String toString() {
        return "DAY      : " + String.valueOf(DATE) + "\n" +
                "CONTENT : " + CONTENT.replaceAll("\n", " & ") + "\n";
    }

    // getting ID
    public int getDate() {
        return this.DATE;
    }

    // setting ID
    public void setDate(int id) {
        this.DATE = id;
    }

    public String getPlans() {
        return this.CONTENT;
    }

    public void setPlans(String content) {
        this.CONTENT = content;
    }

    public void addPlans(String content) {
        this.CONTENT = CONTENT + "\n" + content;
    }
}
