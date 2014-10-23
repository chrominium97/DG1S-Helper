package kr.kdev.dg1s.card;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import kr.kdev.dg1s.R;
import kr.kdev.dg1s.card.provider.MealProvider;

public class MealView {

    Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    try {
                        updateCards();
                        viewAlert.notifyCompletion(CardViewStatusNotifier.SUCCESS);
                    } catch (IOException e) {
                        viewAlert.notifyCompletion(CardViewStatusNotifier.FAILURE);
                    }
                    mealCard.setVisibility(View.VISIBLE);
            }
        }
    };

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

    private CardViewStatusNotifier viewAlert;

    public MealView(Context arg0, ViewGroup parent, Activity activity) {

        try { // Check if callback is implemented
            viewAlert = (CardViewStatusNotifier) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "needs to implement ViewAlert");
        }

        context = arg0;

        viewParent = parent;

        mealCard = (CardView) LayoutInflater.from(context).inflate(R.layout.card_meal, parent, true)
                .findViewById(R.id.card);
        mealCard.setVisibility(View.INVISIBLE);

        cardContents = (ViewGroup) mealCard.findViewById(R.id.card_contents);

        mealMenus = (LinearLayout) View.inflate(context, R.layout.card_meal_content, cardContents).findViewById(R.id.container);

        breakfastMenu = (LinearLayout) mealMenus.findViewById(R.id.breakfast);
        lunchMenu = (LinearLayout) mealMenus.findViewById(R.id.lunch);
        dinnerMenu = (LinearLayout) mealMenus.findViewById(R.id.dinner);

        breakfastText = (TextView) breakfastMenu.findViewById(R.id.breakfast_menu);
        lunchText = (TextView) lunchMenu.findViewById(R.id.lunch_menu);
        dinnerText = (TextView) dinnerMenu.findViewById(R.id.dinner_menu);

        update(false);
    }

    class NetOps extends Thread {
        public boolean forceUpdate;

        public void run() {
            try {
                MealProvider provider = new MealProvider(context);
                if (forceUpdate) {
                    provider.forceRefresh();
                }
                refreshHandler.sendEmptyMessage(0);
            } catch (IOException e) {
                viewAlert.notifyCompletion(CardViewStatusNotifier.FAILURE);
            }
        }
    }

    public void update(boolean forced) {
        NetOps netOps = new NetOps();
        netOps.forceUpdate = forced;
        netOps.start();
    }

    void updateCards() throws IOException {
        MealProvider provider = new MealProvider(context);
        MealProvider.Meal meal = provider.getMeal();

        breakfastText.setText(meal.getBreakfast());
        lunchText.setText(meal.getLunch());
        dinnerText.setText(meal.getDinner());

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
