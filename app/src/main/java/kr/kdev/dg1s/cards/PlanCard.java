package kr.kdev.dg1s.cards;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kr.kdev.dg1s.R;
import kr.kdev.dg1s.cards.provider.PlanProvider;
import kr.kdev.dg1s.cards.provider.datatypes.Plan;

public class PlanCard implements PlanProvider.PlanProviderInterface {

    PlanProvider provider;

    Context context;

    LinearLayout planList;
    CardView planCard;
    ViewGroup viewParent;
    ViewGroup cardContents;

    TextView cardTitle;

    TextView dateText;
    TextView planText;
    TextView summaryText;

    private CardViewStatusNotifier statusNotifier;

    public PlanCard(Context arg0, ViewGroup parent, Activity activity) {

        try { // Checks if callback is implemented
            statusNotifier = (CardViewStatusNotifier) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "needs to implement CardViewStatusNotifier");
        }

        context = arg0;

        viewParent = parent;

        planCard = (CardView) LayoutInflater.from(context).inflate(R.layout.card_plan, parent, true)
                .findViewById(R.id.card_plan);
        planCard.setVisibility(View.GONE);

        cardTitle = (TextView) planCard.findViewById(R.id.title);

        cardContents = (ViewGroup) planCard.findViewById(R.id.card_contents);

        planList = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.card_plan_content, cardContents, true)
                .findViewById(R.id.planList);

        dateText = (TextView) planList.findViewById(R.id.date);
        planText = (TextView) planList.findViewById(R.id.plans);
        summaryText = (TextView) planList.findViewById(R.id.summary);

        provider = new PlanProvider(context, this);
        provider.requestPlan(false);
    }

    public void onPlanReceived(boolean succeeded, Plan plan, ArrayList<Integer> summary) {
        if (succeeded) {
            statusNotifier.notifyCompletion(CardViewStatusNotifier.SUCCESS);
            updateCards(plan, summary);
        } else {
            statusNotifier.notifyCompletion(CardViewStatusNotifier.FAILURE);
        }
        planCard.setVisibility(View.VISIBLE);
    }

    public void requestUpdate(boolean isForced) {
        provider.requestPlan(isForced);
    }

    void updateCards(Plan plan, ArrayList<Integer> summary) {

        planText.setText(plan.getPlans());

        dateText.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())));

        if (summary.get(0) == -1) {
            Log.e("GRADE", "GRADE NOT SET OR LOADED W/ ERRORS");
            summaryText.setText(context.getString(R.string.set_grade));
        } else {
            summaryText.setText(context.getString(R.string.day_total) + summary.get(0) + "\n" +
                    context.getString(R.string.day_events) + summary.get(1) + "\n" +
                    context.getString(R.string.day_studying) + summary.get(2));
        }

        if ("".equals(planText.getText())) {
            cardTitle.setText(context.getString(R.string.error_no_schudules));
            planText.setVisibility(View.GONE);
        } else {
            cardTitle.setText(context.getString(R.string.actionbar_schedule));
            cardContents.setVisibility(View.VISIBLE);
        }
    }
}
