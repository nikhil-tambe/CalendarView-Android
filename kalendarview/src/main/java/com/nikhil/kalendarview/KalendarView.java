package com.nikhil.kalendarview;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class KalendarView extends LinearLayout {
    // for logging
    private static final String LOGTAG = "Calendar View";

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "MMM yyyy";

    // attrs
    private String dateFormat;
    private boolean allowHighlight;
    private int highLightResource;

    // current displayed month
    private Calendar currentDate = Calendar.getInstance();
    private Calendar currentMonthCalendar = Calendar.getInstance();

    // selected dates set
    HashSet<Date> eventDays;

    //event handling
    private EventHandler eventHandler = null;

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;

    // seasons' rainbow
    int[] rainbow = new int[]{
            R.color.summer,
            R.color.fall,
            R.color.winter,
            R.color.spring
    };

    // month-season association (northern hemisphere, sorry australia :)
    int[] monthSeason = new int[]{2, 2, 3, 3, 3, 0, 0, 0, 1, 1, 1, 2};

    public KalendarView(Context context) {
        super(context);
    }

    public KalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initControl(context, attrs);
    }

    public KalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.kalendar, this);

        eventDays = new HashSet<>();
        currentMonthCalendar.setTime(new Date(System.currentTimeMillis()));
        loadDateAttrs(attrs);
        assignUiElements();
        assignClickHandlers();

        updateCalendar();
    }

    private void loadDateAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.KalendarView);
        try {
            // try to load provided date format, and fallback to default otherwise
            dateFormat = ta.getString(R.styleable.KalendarView_dateFormat);
            if (dateFormat == null)
                dateFormat = DATE_FORMAT;

            allowHighlight = ta.getBoolean(R.styleable.KalendarView_allowHighlight, false);

            highLightResource = ta.getResourceId(R.styleable.KalendarView_setHighlightDrawable, R.drawable.border_circular);

        } finally {
            ta.recycle();
        }
    }

    private void assignUiElements() {
        // layout is inflated, assign local variables to components
        header = (LinearLayout) findViewById(R.id.calendar_header);
        btnPrev = (ImageView) findViewById(R.id.calendar_prev_button);
        btnNext = (ImageView) findViewById(R.id.calendar_next_button);
        txtDate = (TextView) findViewById(R.id.calendar_date_display);
        grid = (GridView) findViewById(R.id.calendar_grid);
    }

    private void assignClickHandlers() {
        // add one month and refresh UI
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        // subtract one month and refresh UI
        btnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });

        // long-pressing a day
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> view, View cell, int position, long id) {
                // handle long-press
                if (eventHandler == null)
                    return false;

                eventHandler.onDayLongPress((Date) view.getItemAtPosition(position));
                return true;
            }
        });

        // day clicked
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (eventHandler == null)
                    return;

                Date date = (Date) parent.getItemAtPosition(position);
                eventHandler.onDayClicked(date);

                if (allowHighlight) {
                    if (eventDays.contains(date)) {
                        eventDays.remove(date);
                    } else {
                        eventDays.add(date);
                    }
                    updateCalendar();
                }

            }
        });
    }

    /**
     * getters for setters for attrs */
    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public boolean isAllowHighlight() {
        return allowHighlight;
    }

    public void setAllowHighlight(boolean allowHighlight) {
        this.allowHighlight = allowHighlight;
    }

    public int getHighLightResource() {
        return highLightResource;
    }

    public void setHighLightResource(int highLightResource) {
        this.highLightResource = highLightResource;
    }

    public HashSet<Date> getEventDays() {
        return eventDays;
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar(HashSet<Date> events) {
        this.eventDays = events;
        updateCalendar();
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar() {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar) currentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT) {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // update grid
        grid.setAdapter(new CalendarAdapter(getContext(), cells));

        // update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        txtDate.setText(sdf.format(currentDate.getTime()));

        // set header color according to current season
        // int month = currentDate.get(Calendar.MONTH);
        // int season = monthSeason[month];
        // int color = rainbow[season];
        // header.setBackgroundColor(ContextCompat.getColor(getContext(), color));
    }

    private class CalendarAdapter extends ArrayAdapter<Date> {
        // days with events
        //private HashSet<Date> eventDays;

        // for view inflation
        private LayoutInflater inflater;
        int colorGrey, colorToday;

        public CalendarAdapter(Context context, ArrayList<Date> days) { //} , HashSet<Date> eventDays) {
            super(context, R.layout.kalendar_day, days);
            //this.eventDays = eventDays;
            inflater = LayoutInflater.from(context);

            colorGrey = ContextCompat.getColor(context, R.color.greyed_out);
            colorToday = ContextCompat.getColor(context, R.color.today);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            // day in question
            Date date = getItem(position);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int day = calendar.get(Calendar.DAY_OF_MONTH);  //date.getDate();
            int month = calendar.get(Calendar.MONTH);       //date.getMonth();
            int year = calendar.get(Calendar.YEAR);         //date.getYear();

            // inflate item if it does not exist yet
            if (view == null)
                view = inflater.inflate(R.layout.kalendar_day, parent, false);

            // default styling
            ((TextView) view).setTypeface(null, Typeface.NORMAL);
            ((TextView) view).setTextColor(Color.BLACK);

            // if this day has an event, specify event image
            view.setBackgroundResource(0);
            if (eventDays != null) {
                for (Date eventDate : eventDays) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(eventDate);
                    if (c.get(Calendar.DAY_OF_MONTH) == day &&
                            c.get(Calendar.MONTH) == month &&
                            c.get(Calendar.YEAR) == year) {
                        // mark this day for event
                        ((TextView) view).setTextColor(Color.WHITE);
                        view.setBackgroundResource(highLightResource);
                        break;
                    }
                }
            }

            // today
            //Calendar today = currentDate;
            if (calendar.get(Calendar.DAY_OF_YEAR) < currentMonthCalendar.get(Calendar.DAY_OF_YEAR)) {
                // if this day is before today, grey it out
                ((TextView) view).setTextColor(colorGrey);
            } else if (month != currentDate.get(Calendar.MONTH) || year != currentDate.get(Calendar.YEAR)) {
                // if this day is outside current month, grey it out
                ((TextView) view).setTextColor(colorGrey);
            } else if (day == currentMonthCalendar.get(Calendar.DAY_OF_MONTH)
                    && month == currentMonthCalendar.get(Calendar.MONTH)
                    && year == currentMonthCalendar.get(Calendar.YEAR)) {
                // if it is today, set it to blue/bold
                ((TextView) view).setTypeface(null, Typeface.BOLD);
                ((TextView) view).setTextColor(colorToday);
            }

            // set text
            ((TextView) view).setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))); //String.valueOf(date.getDate()));

            return view;
        }
    }

    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler {
        void onDayLongPress(Date date);

        void onDayClicked(Date date);

    }
}
