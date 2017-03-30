package com.ms_square.debugoverlay.modules;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ms_square.debugoverlay.DebugOverlay;
import com.ms_square.debugoverlay.OverlayViewDelegate;
import com.ms_square.debugoverlay.OverlayViewDelegateFactory;
import com.ms_square.debugoverlay.R;

public class LogcatViewDelegate extends BaseOverlayViewDelegate<LogcatLine>  {

    private static final String TAG = LogcatViewDelegate.class.getSimpleName();

    private static final int DEFAULT_MAX_LINE_ITEMS = 15;

    private final int maxLineItems;

    private final LogcatLineFilter lineFilter;

    private final LogcatLineColorScheme colorScheme;

    private LogcatLineArrayAdapter adapter;

    private LogcatViewDelegate(@LayoutRes int layoutResId, @Size(min=1,max=100) int maxLineItems,
                               LogcatLineFilter lineFilter, LogcatLineColorScheme colorScheme) {
        super(layoutResId);
        this.maxLineItems = maxLineItems;
        this.lineFilter = lineFilter;
        this.colorScheme = colorScheme;
    }

    @Override
    public void onDataAvailable(LogcatLine logcatLine) {
        if (lineFilter.shouldFilterOut(logcatLine.getPriority(), logcatLine.getTag())) {
            return;
        }
        if (adapter.getCount() >= maxLineItems) {
            adapter.remove(adapter.getItem(0));
        }
        adapter.add(logcatLine);
    }

    @Override
    public View createView(ViewGroup root, @ColorInt int textColor, float textSize, float textAlpha) {
        ListView listView = (ListView) LayoutInflater.from(root.getContext()).inflate(layoutResId, root, false);
        adapter = new LogcatLineArrayAdapter(root.getContext(), textColor, textAlpha, colorScheme);
        listView.setAdapter(adapter);

        if (textColor != DebugOverlay.DEFAULT_TEXT_COLOR) {
            Log.d(TAG, "textColor passed will be ignored for logcat priority/tag and message.\n" +
                    "Please use LogcatLineColorScheme for such purpose.");
        }
        if (Float.compare(textSize, DebugOverlay.DEFAULT_TEXT_SIZE) != 0) {
            Log.d(TAG, "textSize passed will be ignored for this view.");
        }

        return listView;
    }

    static class LogcatLineArrayAdapter extends ArrayAdapter<LogcatLine> {

        private final int textColor;

        private final float textAlpha;

        private final LogcatLineColorScheme colorScheme;

        public LogcatLineArrayAdapter(@NonNull Context context, @ColorInt int textColor, float textAlpha,
                                      LogcatLineColorScheme colorScheme) {
            super(context, 0);
            this.textColor = textColor;
            this.textAlpha = textAlpha;
            this.colorScheme = colorScheme;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.line_logcat, parent, false);
                TextView dateAndTime = (TextView) convertView.findViewById(R.id.date_and_time);
                TextView priorityAndTag = (TextView) convertView.findViewById(R.id.priority_and_tag);
                TextView message = (TextView) convertView.findViewById(R.id.message);

                dateAndTime.setTextColor(textColor);

                dateAndTime.setAlpha(textAlpha);
                priorityAndTag.setAlpha(textAlpha);
                message.setAlpha(textAlpha);

                holder = new ViewHolder();
                holder.date_and_time = dateAndTime;
                holder.priority_and_tag = priorityAndTag;
                holder.message = message;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            LogcatLine line = getItem(position);

            holder.date_and_time.setText(line.getDate() + " " + line.getTime());
            holder.priority_and_tag.setText(line.getPriority().getValue() + "/" + line.getTag());
            holder.message.setText(line.getMessage());

            int textColor = colorScheme.getTextColor(line.getPriority(), line.getTag());
            holder.priority_and_tag.setTextColor(textColor);
            holder.message.setTextColor(textColor);

            return convertView;
        }

        private static class ViewHolder {
            TextView date_and_time;
            TextView priority_and_tag;
            TextView message;
        }
    }

    public static class Factory implements OverlayViewDelegateFactory<LogcatLine> {

        @LayoutRes
        private final int layoutResId;

        private final int maxLineItems;

        private final LogcatLineFilter lineFilter;

        private final LogcatLineColorScheme colorScheme;

        public Factory() {
            this(R.layout.logcat, DEFAULT_MAX_LINE_ITEMS,
                    new LogcatLineFilter.SimpleLogcatLineFilter(LogcatLine.Priority.VERBOSE),
                    LogcatLineColorScheme.DEFAULT_COLOR_SCHEME);
        }

        public Factory(@Size(min=1,max=100) int maxLineItems) {
            this(R.layout.logcat, maxLineItems,
                    new LogcatLineFilter.SimpleLogcatLineFilter(LogcatLine.Priority.VERBOSE),
                    LogcatLineColorScheme.DEFAULT_COLOR_SCHEME);
        }

        public Factory(@Size(min=1,max=100) int maxLineItems, LogcatLineFilter lineFilter) {
            this(R.layout.logcat, maxLineItems, lineFilter,
                    LogcatLineColorScheme.DEFAULT_COLOR_SCHEME);
        }

        public Factory(@Size(min=1,max=100) int maxLineItems, LogcatLineFilter lineFilter,
                       LogcatLineColorScheme colorScheme) {
            this(R.layout.logcat, maxLineItems, lineFilter, colorScheme);
        }

        private Factory(@LayoutRes int layoutResId, @Size(min=1,max=100) int maxLineItems,
                       LogcatLineFilter lineFilter, LogcatLineColorScheme colorScheme) {
            this.layoutResId = layoutResId;
            this.maxLineItems = maxLineItems;
            this.lineFilter = lineFilter;
            this.colorScheme = colorScheme;
        }

        @Override
        public OverlayViewDelegate<LogcatLine> create() {
            return new LogcatViewDelegate(layoutResId, maxLineItems, lineFilter, colorScheme);
        }
    }
}