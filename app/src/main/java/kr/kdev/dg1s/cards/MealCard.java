package kr.kdev.dg1s.cards;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.kdev.dg1s.R;
import kr.kdev.dg1s.cards.provider.MealProvider;
import kr.kdev.dg1s.cards.provider.datatypes.Meal;

public class MealCard implements MealProvider.MealProviderInterface {

    MealProvider provider;

    Context context;

    LinearLayout mealMenus;
    CardView mealCard;
    ViewGroup viewParent;
    ViewGroup cardContents;

    LinearLayout breakfastMenu;
    LinearLayout lunchMenu;
    LinearLayout dinnerMenu;
    TextView breakfastText;
    TextView lunchText;
    TextView dinnerText;

    private CardViewStatusNotifier statusNotifier;

    public MealCard(Context arg0, ViewGroup parent, Activity activity) {

        try { // Check if callback is implemented
            statusNotifier = (CardViewStatusNotifier) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "needs to implement CardViewStatusNotifier");
        }

        context = arg0;

        viewParent = parent;

        mealCard = (CardView) LayoutInflater.from(context).inflate(R.layout.card_meal, parent, true)
                .findViewById(R.id.card_meal);
        mealCard.setVisibility(View.GONE);

        cardContents = (ViewGroup) mealCard.findViewById(R.id.card_contents);

        mealMenus = (LinearLayout) View.inflate(context, R.layout.card_meal_content, cardContents).findViewById(R.id.container);

        breakfastMenu = (LinearLayout) mealMenus.findViewById(R.id.breakfast);
        lunchMenu = (LinearLayout) mealMenus.findViewById(R.id.lunch);
        dinnerMenu = (LinearLayout) mealMenus.findViewById(R.id.dinner);

        breakfastText = (TextView) breakfastMenu.findViewById(R.id.breakfast_menu);
        lunchText = (TextView) lunchMenu.findViewById(R.id.lunch_menu);
        dinnerText = (TextView) dinnerMenu.findViewById(R.id.dinner_menu);

        provider = new MealProvider(context, this);
        provider.requestMeal(false);
    }

    public void onMealReceived(boolean succeeded, Meal meal) {
        if (succeeded) {
            statusNotifier.notifyCompletion(CardViewStatusNotifier.SUCCESS);
            updateCards(meal);
        } else {
            statusNotifier.notifyCompletion(CardViewStatusNotifier.FAILURE);
        }
        mealCard.setVisibility(View.VISIBLE);
    }

    public void requestUpdate(boolean isForced) {
        provider.requestMeal(isForced);
    }

    void updateCards(Meal input) {

        breakfastText.setText(input.getBreakfast().toString().replace("[", "").replace("]", "").replaceAll(", ", "\n"));
        lunchText.setText(input.getLunch().toString().replace("[", "").replace("]", "").replaceAll(", ", "\n"));
        dinnerText.setText(input.getDinner().toString().replace("[", "").replace("]", "").replaceAll(", ", "\n"));

        if (breakfastText.getText().equals(context.getString(R.string.no_meal))) { // When there is no meal
            removeView(breakfastMenu, mealMenus);
        } else {
            addView(breakfastMenu, mealMenus);
        }
        if (lunchText.getText().equals(context.getString(R.string.no_meal))) { // When there is no meal
            removeView(lunchMenu, mealMenus);
        } else {
            addView(lunchMenu, mealMenus);
        }
        if (dinnerText.getText().equals(context.getString(R.string.no_meal))) { // When there is no meal
            removeView(dinnerMenu, mealMenus);
        } else {
            addView(lunchMenu, mealMenus);
        }

        TextView textView = (TextView) cardContents.findViewById(R.id.title);
        if (mealMenus.getChildCount() == 0) {
            textView.setText(R.string.no_meal);
        } else {
            textView.setText(R.string.actionbar_meal);
        }
    }

    private void addView(View view, LinearLayout viewParent) {
        if (view.getParent() == null) {
            viewParent.addView(view);
        }
    }

    private void removeView(View view, LinearLayout viewParent) {
        if (view.getParent() != null) {
            viewParent.removeView(view);
        }
    }

}
