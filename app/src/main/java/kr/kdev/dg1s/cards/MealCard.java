package kr.kdev.dg1s.cards;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.kdev.dg1s.R;
import kr.kdev.dg1s.cards.provider.MealProvider;
import kr.kdev.dg1s.cards.provider.datatypes.Meal;

public class MealCard implements MealProvider.MealProviderInterface {

    private final int targetDelay = 1000;
    private MealProvider provider;
    private LinearLayout menuList;
    private LinearLayout mealMessage;
    private LinearLayout breakfastMenu;
    private LinearLayout lunchMenu;
    private LinearLayout dinnerMenu;
    private TextView breakfastText;
    private TextView lunchText;
    private TextView dinnerText;
    private CardViewStatusNotifier statusNotifier;
    private long timeAtLastViewChange;

    public MealCard(Context context, ViewGroup viewParent, Activity activity) {

        try { // Check if callback is implemented
            statusNotifier = (CardViewStatusNotifier) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "needs to implement CardViewStatusNotifier");
        }

        CardView mealCard = (CardView) LayoutInflater.from(context).inflate(R.layout.card_meal, viewParent, false);
        FrameLayout contents = (FrameLayout) mealCard.findViewById(R.id.contents);

        menuList = (LinearLayout) contents.findViewById(R.id.meals);
        mealMessage = (LinearLayout) menuList.findViewById(R.id.no_meal_message);

        breakfastMenu = (LinearLayout) menuList.findViewById(R.id.breakfast);
        lunchMenu = (LinearLayout) menuList.findViewById(R.id.lunch);
        dinnerMenu = (LinearLayout) menuList.findViewById(R.id.dinner);

        breakfastText = (TextView) breakfastMenu.findViewById(R.id.breakfast_menu);
        lunchText = (TextView) lunchMenu.findViewById(R.id.lunch_menu);
        dinnerText = (TextView) dinnerMenu.findViewById(R.id.dinner_menu);

        provider = new MealProvider(context, this);
        provider.requestMeal(false);

        hideMeals();
        timeAtLastViewChange = System.currentTimeMillis() - targetDelay;

        viewParent.addView(mealCard);
    }

    public void onMealReceived(boolean succeeded, Meal meal) {
        if (succeeded) {
            statusNotifier.notifyCompletion(this, CardViewStatusNotifier.SUCCESS);
        } else {
            statusNotifier.notifyCompletion(this, CardViewStatusNotifier.FAILURE);
        }
        showMeals(meal);
    }

    public void requestUpdate(boolean isForced) {
        hideMeals();
        provider.requestMeal(isForced);
    }

    long delay() {
        Log.d("WeatherCardAnimationDelay",
                String.valueOf((timeAtLastViewChange + targetDelay) - System.currentTimeMillis()) + " " +
                        "milliseconds delayed");
        return (timeAtLastViewChange + targetDelay) - System.currentTimeMillis();
    }

    void hideMeals() {
        final Handler handler = new Handler();
        long delay = delay();
        if (delay < 0) {
            delay = 0;
        }
        handler.postDelayed(new hideMealAction(), delay);
    }

    void showMeals(Meal input) {
        final Handler handler = new Handler();
        long delay = delay();
        if (delay < 0) {
            delay = 0;
        }
        handler.postDelayed(new showMealAction(input), delay);
    }

    private void addView(View view, LinearLayout viewParent) {
        if (view.getParent() == null) {
            viewParent.addView(view);
        }
    }

    class hideMealAction implements Runnable {
        @Override
        public void run() {
            menuList.removeAllViews();

            timeAtLastViewChange = System.currentTimeMillis();
        }
    }

    class showMealAction implements Runnable {

        Meal meal;

        showMealAction(Meal input) {
            this.meal = input;
        }

        @Override
        public void run() {
            breakfastText.setText(meal.getBreakfast().toString().replace("[", "").replace("]", "").replaceAll(", ", "\n"));
            lunchText.setText(meal.getLunch().toString().replace("[", "").replace("]", "").replaceAll(", ", "\n"));
            dinnerText.setText(meal.getDinner().toString().replace("[", "").replace("]", "").replaceAll(", ", "\n"));

            if ("".equals(breakfastText.getText())) {
                breakfastMenu.setVisibility(View.GONE);
            } else {
                breakfastMenu.setVisibility(View.VISIBLE);
            }
            if ("".equals(lunchText.getText())) {
                lunchMenu.setVisibility(View.GONE);
            } else {
                lunchText.setVisibility(View.VISIBLE);
            }
            if ("".equals(dinnerText.getText())) {
                dinnerMenu.setVisibility(View.GONE);
            } else {
                dinnerMenu.setVisibility(View.VISIBLE);
            }

            if (breakfastMenu.getVisibility() == View.GONE &&
                    lunchMenu.getVisibility() == View.GONE &&
                    dinnerMenu.getVisibility() == View.GONE) {
                mealMessage.setVisibility(View.VISIBLE);
            } else {
                mealMessage.setVisibility(View.GONE);
            }

            addView(mealMessage, menuList);
            addView(breakfastMenu, menuList);
            addView(lunchMenu, menuList);
            addView(dinnerMenu, menuList);
        }
    }

}
