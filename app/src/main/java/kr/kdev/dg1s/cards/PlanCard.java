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

import java.util.ArrayList;

import kr.kdev.dg1s.R;
import kr.kdev.dg1s.cards.provider.PlanProvider;
import kr.kdev.dg1s.cards.provider.datatypes.Plan;

public class PlanCard implements PlanProvider.PlanProviderInterface {

    private final static String TAG = "PlanCard";

    private final int targetDelay = 1000;
    PlanProvider provider;
    Context context;
    ViewGroup viewParent;
    CardView planCard;
    LinearLayout header;
    FrameLayout contents;
    LinearLayout schedule;
    TextView planText;
    LinearLayout summary;
    TextView summaryText;
    private CardViewStatusNotifier statusNotifier;
    private long timeAtLastViewChange;

    public PlanCard(Context arg0, ViewGroup parent, Activity activity) {

        try { // Checks if callback is implemented
            statusNotifier = (CardViewStatusNotifier) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "needs to implement CardViewStatusNotifier");
        }

        context = arg0;

        viewParent = parent;

        planCard = (CardView) LayoutInflater.from(context).inflate(R.layout.card_plan, parent, false);
        header = (LinearLayout) planCard.findViewById(R.id.header);
        contents = (FrameLayout) planCard.findViewById(R.id.contents);

        schedule = (LinearLayout) contents.findViewById(R.id.schedule);

        planText = (TextView) schedule.findViewById(R.id.plans);
        summary = (LinearLayout) schedule.findViewById(R.id.summary_area);
        summaryText = (TextView) summary.findViewById(R.id.summary);

        provider = new PlanProvider(context, this);
        provider.query(false);

        hidePlans();
        timeAtLastViewChange = System.currentTimeMillis() - targetDelay;

        parent.addView(planCard);
    }

    public void onPlanReceived(boolean succeeded, Plan plan, ArrayList<Integer> summary) {
        if (succeeded) {
            statusNotifier.notifyCompletion(this, CardViewStatusNotifier.SUCCESS);
        } else {
            statusNotifier.notifyCompletion(this, CardViewStatusNotifier.FAILURE);
        }
        showSchedules(plan, summary);
    }

    public void requestUpdate(boolean isForced) {
        hidePlans();
        provider.query(isForced);
    }

    long delay() {
        Log.d(TAG + "_AnimationDelay",
                String.valueOf((timeAtLastViewChange + targetDelay) - System.currentTimeMillis()) + " " +
                        "milliseconds delayed");
        return (timeAtLastViewChange + targetDelay) - System.currentTimeMillis();
    }

    void hidePlans() {
        final Handler handler = new Handler();
        long delay = delay();
        if (delay < 0) {
            delay = 0;
        }
        handler.postDelayed(new hideSchedulesAction(), delay);
    }

    void showSchedules(Plan plan, ArrayList<Integer> summaryArray) {
        final Handler handler = new Handler();
        long delay = delay();
        if (delay < 0) {
            delay = 0;
        }
        handler.postDelayed(new showSchedulesAction(plan, summaryArray), delay);
    }

    private void addView(View view, LinearLayout viewParent) {
        if (view.getParent() == null) {
            viewParent.addView(view);
        }
    }

    class hideSchedulesAction implements Runnable {
        @Override
        public void run() {
            schedule.removeAllViews();
        }
    }

    class showSchedulesAction implements Runnable {

        Plan plan;
        ArrayList<Integer> summaryArray;

        showSchedulesAction(Plan planInput, ArrayList<Integer> summaryInput) {
            this.plan = planInput;
            this.summaryArray = summaryInput;
        }

        @Override
        public void run() {
            if ("".equals(plan.getPlans())) {
                planText.setText(context.getString(R.string.plan_card_no_schedules));
            } else {
                planText.setText(plan.getPlans());
            }

            if (summaryArray.get(0) == -1) {
                summaryText.setText(context.getString(R.string.plan_card_summary_set_grade));
            } else {
                summaryText.setText(context.getString(R.string.plan_card_summary_day_total) + summaryArray.get(0) + "\n" +
                        context.getString(R.string.plan_card_summary_day_events) + summaryArray.get(1) + "\n" +
                        context.getString(R.string.plan_card_summary_day_studying) + summaryArray.get(2));
            }

            addView(planText, schedule);
            addView(summary, schedule);
        }
    }

}
