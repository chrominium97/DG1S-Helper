package kr.kdev.dg1s.cards.provider.datatypes;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by 꽃게 on 2014/11/25.
 */
public class Meal {

    private ArrayList<String> BREAKFAST = new ArrayList<String>();
    private ArrayList<String> LUNCH = new ArrayList<String>();
    private ArrayList<String> DINNER = new ArrayList<String>();

    private int DATE;

    public Meal() {

    }

    public Meal(int id, String breakfast, String lunch, String dinner) {
        this.DATE = id;
        this.BREAKFAST.addAll(Arrays.asList(breakfast.replace("[", "").replace("]", "").split(", ")));
        this.LUNCH.addAll(Arrays.asList(lunch.replace("[", "").replace("]", "").split(", ")));
        this.DINNER.addAll(Arrays.asList(dinner.replace("[", "").replace("]", "").split(", ")));
    }

    public Meal(int id, ArrayList<String> breakfast, ArrayList<String> lunch, ArrayList<String> dinner) {
        this.DATE = id;
        this.BREAKFAST = breakfast;
        this.LUNCH = lunch;
        this.DINNER = dinner;
    }

    @Override
    public String toString() {

        StringBuilder breakfastBuilder = new StringBuilder();
        for (String meal : BREAKFAST) {
            breakfastBuilder.append(meal);
            breakfastBuilder.append(" & ");
        }

        StringBuilder lunchBuilder = new StringBuilder();
        for (String meal : LUNCH) {
            lunchBuilder.append(meal);
            lunchBuilder.append(" & ");
        }

        StringBuilder dinnerBuilder = new StringBuilder();
        for (String meal : DINNER) {
            dinnerBuilder.append(meal);
            dinnerBuilder.append(" & ");
        }

        return "DAY       : " + String.valueOf(DATE) + "\n" +
                "BREAKFAST : " + breakfastBuilder.toString() + "\n" +
                "LUNCH     : " + lunchBuilder.toString() + "\n" +
                "DINNER    : " + dinnerBuilder.toString() + "\n";
    }

    public int getDate() {
        return this.DATE;
    }

    public void setDate(int id) {
        this.DATE = id;
    }

    public ArrayList<String> getBreakfast() {
        return this.BREAKFAST;
    }

    public void setBreakfast(ArrayList<String> content) {
        this.BREAKFAST = content;
    }

    public void addBreakfast(String content) {
        BREAKFAST.add(content);
    }

    public ArrayList<String> getLunch() {
        return this.LUNCH;
    }

    public void setLunch(ArrayList<String> content) {
        this.LUNCH = content;
    }

    public void addLunch(String content) {
        LUNCH.add(content);
    }

    public ArrayList<String> getDinner() {
        return this.DINNER;
    }

    public void setDinner(ArrayList<String> content) {
        this.DINNER = content;
    }

    public void addDinner(String content) {
        DINNER.add(content);
    }
}
