package kr.kdev.dg1s;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class Adapter_Timetable extends ArrayAdapter {

    String[] subject = {"기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타"};
    String[] teacher = {"기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타",
            "기타", "기타", "기타", "기타", "기타"};

    Context context;
    ImageView subjecticon_v;

    public Adapter_Timetable(Context context) {
        super(context, 0);
        this.context = context;
    }

    public int getCount() {
        return 35;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.timetableitem, parent, false);

            TextView subject_v = (TextView) row.findViewById(R.id.timetable_cell_subject);
            TextView teacher_v = (TextView) row.findViewById(R.id.timetable_cell_teacher);
            subjecticon_v = (ImageView) row.findViewById(R.id.timetable_cell_icon);

            subject_v.setText(subject[position]);
            teacher_v.setText(teacher[position]);
            SubjectIconChanger(subject[position]);
        } else {
            row = convertView;
        }


        Log.d("Adapter_Timetable", "업데이트");
        return row;
    }

    private void SubjectIconChanger(String subject) {

        if (subject.contains("국어") || subject.contains("문학")) {
            subjecticon_v.setImageResource(R.drawable.subjects_korean);
        } else if (subject.contains("영어")) {
            subjecticon_v.setImageResource(R.drawable.subjects_english);
        } else if (subject.contains("국사")) {
            subjecticon_v.setImageResource(R.drawable.subjects_history);
        } else if (subject.contains("화학")) {
            subjecticon_v.setImageResource(R.drawable.subjects_chemistry);
        } else if (subject.contains("물리")) {
            subjecticon_v.setImageResource(R.drawable.subjects_physics);
        } else if (subject.contains("생물")) {
            subjecticon_v.setImageResource(R.drawable.subjects_biology);
        } else if (subject.contains("지학")) {
            subjecticon_v.setImageResource(R.drawable.subjects_geology);
        } else if (subject.contains("수학")) {
            subjecticon_v.setImageResource(R.drawable.subjects_maths);
        } else {
            subjecticon_v.setImageResource(R.drawable.subjects_etc);
        }
    }
}