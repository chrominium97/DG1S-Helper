package kr.kdev.dg1s;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class Adapter_Post extends ArrayAdapter<Adapter_PostList> {

    View view;
    private ViewHolder holder;

    public Adapter_Post(Context context, int resource, List<Adapter_PostList> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.postitem, null);
            holder = new ViewHolder(); //Holder를 선언합니다.
            holder.subjecticon = (ImageView) view.findViewById(R.id.subjecticon);
            holder.subject = (TextView) view.findViewById(R.id.subject);
            holder.due = (TextView) view.findViewById(R.id.dueDate);
            holder.assignedTo = (TextView) view.findViewById(R.id.assigned_to);
            holder.homeworkInfo = (TextView) view.findViewById(R.id.homework_info);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        final Adapter_PostList postListAdapter = getItem(position);
        holder.assignedTo.setText(postListAdapter.gradeNum);
        holder.subject.setText(postListAdapter.subject);
        holder.homeworkInfo.setText(postListAdapter.info);
        String locale = Locale.getDefault().getLanguage();
        int phraseOrder = (locale.equals("ko") || locale.equals("ja")) ? 1 : 0;
        switch (phraseOrder) {
            case 1:
                holder.due.setText(postListAdapter.due + getContext().getResources().getString(R.string.phrase_until));
                SubjectIconChanger(postListAdapter.subject);
                break;
            case 0:
                holder.due.setText(getContext().getResources().getString(R.string.phrase_until) + " " + postListAdapter.due);
                SubjectIconChanger(postListAdapter.subject);
                break;
        }
        return view;
    }

    private void SubjectIconChanger(String subject) {
        if (subject.contains("국어") || subject.contains("문학")) {
            holder.subjecticon.setImageResource(R.drawable.subjects_korean);
        } else if (subject.contains("수학")) {
            holder.subjecticon.setImageResource(R.drawable.subjects_maths);
        } else if (subject.contains("영어")) {
            holder.subjecticon.setImageResource(R.drawable.subjects_english);
        } else if (subject.contains("물리")) {
            holder.subjecticon.setImageResource(R.drawable.subjects_physics);
        } else if (subject.contains("화학")) {
            holder.subjecticon.setImageResource(R.drawable.subjects_chemistry);
        } else if (subject.contains("생물")) {
            holder.subjecticon.setImageResource(R.drawable.subjects_biology);
        } else if (subject.contains("지학")) {
            holder.subjecticon.setImageResource(R.drawable.subjects_geology);
        } else if (subject.contains("국사")) {
            holder.subjecticon.setImageResource(R.drawable.subjects_history);
        } else {
            holder.subjecticon.setImageResource(R.drawable.subjects_etc);
        }
    }
}

class ViewHolder {
    TextView assignedTo;
    TextView homeworkInfo;
    TextView subject;
    TextView due;
    ImageView subjecticon;
}
