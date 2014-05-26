package kr.kdev.dg1s.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import kr.kdev.dg1s.R;

public class Adapters {

    public static class WeatherAdapter {
        public String weather;
        public String time;
        public String temperature;
    }

    public static class TimeTableAdapter extends ArrayAdapter {

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
        ImageView subjectIcon_v;

        public TimeTableAdapter(Context context) {
            super(context, 0);
            this.context = context;
        }

        public int getCount() {
            return 35;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = null;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.timetableitem, parent, false);

            TextView subject_v = (TextView) row.findViewById(R.id.timetable_cell_subject);
            TextView teacher_v = (TextView) row.findViewById(R.id.timetable_cell_teacher);
            subjectIcon_v = (ImageView) row.findViewById(R.id.timetable_cell_icon);

            subject_v.setText(subject[position]);
            teacher_v.setText(teacher[position]);
            SubjectIconChanger(subject[position]);

            Log.d("Adapter_Timetable", "업데이트");
            return row;
        }

        private void SubjectIconChanger(String subject) {

            if (subject.contains("국어") || subject.contains("문학")) {
                subjectIcon_v.setImageResource(R.drawable.subjects_korean);
            } else if (subject.contains("영어")) {
                subjectIcon_v.setImageResource(R.drawable.subjects_english);
            } else if (subject.contains("국사")) {
                subjectIcon_v.setImageResource(R.drawable.subjects_history);
            } else if (subject.contains("화학")) {
                subjectIcon_v.setImageResource(R.drawable.subjects_chemistry);
            } else if (subject.contains("물리")) {
                subjectIcon_v.setImageResource(R.drawable.subjects_physics);
            } else if (subject.contains("생물")) {
                subjectIcon_v.setImageResource(R.drawable.subjects_biology);
            } else if (subject.contains("지학")) {
                subjectIcon_v.setImageResource(R.drawable.subjects_geology);
            } else if (subject.contains("수학")) {
                subjectIcon_v.setImageResource(R.drawable.subjects_maths);
            } else {
                subjectIcon_v.setImageResource(R.drawable.subjects_etc);
            }
        }
    }

    public static class TimeTableListAdapter {
        String subject_v;
        String teacher_v;
    }

    public static class MealAdapter {
        String Breakfast;
        String Lunch;
        String Dinner;
    }

    public static class PostAdapter extends ArrayAdapter<PostListAdapter> {

        View view;
        private ViewHolder holder;

        public PostAdapter(Context context, int resource, List<PostListAdapter> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.postitem, null);
                holder = new ViewHolder();
                holder.subjectIcon = (ImageView) view.findViewById(R.id.subjectIcon);
                holder.subject = (TextView) view.findViewById(R.id.subject);
                holder.due = (TextView) view.findViewById(R.id.dueDate);
                holder.assignedTo = (TextView) view.findViewById(R.id.assigned_to);
                holder.homeworkInfo = (TextView) view.findViewById(R.id.homework_info);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            final PostListAdapter postListAdapter = getItem(position);
            holder.assignedTo.setText(postListAdapter.gradeNumber);
            holder.subject.setText(postListAdapter.subject);
            holder.homeworkInfo.setText(postListAdapter.information);
            String locale = Locale.getDefault().getLanguage();
            int phraseOrder = (locale.equals("ko") || locale.equals("ja")) ? 1 : 0;
            switch (phraseOrder) {
                case 1:
                    holder.due.setText(postListAdapter.dueDate + getContext().getResources().getString(R.string.phrase_until));
                    SubjectIconChanger(postListAdapter.subject);
                    break;
                case 0:
                    holder.due.setText(getContext().getResources().getString(R.string.phrase_until) + " " + postListAdapter.dueDate);
                    SubjectIconChanger(postListAdapter.subject);
                    break;
            }
            return view;
        }

        private void SubjectIconChanger(String subject) {
            if (subject.contains("국어") || subject.contains("문학")) {
                holder.subjectIcon.setImageResource(R.drawable.subjects_korean);
            } else if (subject.contains("수학")) {
                holder.subjectIcon.setImageResource(R.drawable.subjects_maths);
            } else if (subject.contains("영어")) {
                holder.subjectIcon.setImageResource(R.drawable.subjects_english);
            } else if (subject.contains("물리")) {
                holder.subjectIcon.setImageResource(R.drawable.subjects_physics);
            } else if (subject.contains("화학")) {
                holder.subjectIcon.setImageResource(R.drawable.subjects_chemistry);
            } else if (subject.contains("생물")) {
                holder.subjectIcon.setImageResource(R.drawable.subjects_biology);
            } else if (subject.contains("지학")) {
                holder.subjectIcon.setImageResource(R.drawable.subjects_geology);
            } else if (subject.contains("국사")) {
                holder.subjectIcon.setImageResource(R.drawable.subjects_history);
            } else {
                holder.subjectIcon.setImageResource(R.drawable.subjects_etc);
            }
        }
    }

    static class ViewHolder {
        TextView assignedTo;
        TextView homeworkInfo;
        TextView subject;
        TextView due;
        ImageView subjectIcon;
    }

    public static class PostListAdapter {
        public String gradeNumber;
        public String classNumber;
        public String subject;
        public String information;
        public String dueDate;
    }
}
