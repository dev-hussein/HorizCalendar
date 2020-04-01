package dev.hussein.h_calendar

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class HorizontalCalendar(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        -1
    )



    private var day: Int = 1

    private var month: Month = Month.January
        set(value) {
            field = value
            invalidateDates()
        }


    private var year: Int = 0
        set(value) {
            field = value
            invalidateDates()
        }
    private var textColor: Int = Color.WHITE
    private var errorTextColor: Int = Color.RED
    private var bgColor: Int = Color.BLACK


    private var selectedDate: Date = Calendar.getInstance().time

    private lateinit var calenderAdapter: CalenderAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var errorTextView: AppCompatTextView

    var onDateSelected: OnDateSelected? = null

    init {
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        month = Month.valueOf(Calendar.getInstance().get(Calendar.MONTH))
        year = Calendar.getInstance().get(Calendar.YEAR)

        context.withStyledAttributes(attrs, R.styleable.HorizontalCalendar) {
            if (hasValue(R.styleable.HorizontalCalendar_hc_day))
                day = getInt(R.styleable.HorizontalCalendar_hc_day, day)
            if (hasValue(R.styleable.HorizontalCalendar_hc_month))
                month = Month.valueOf(getInt(R.styleable.HorizontalCalendar_hc_month, month.value))
            if (hasValue(R.styleable.HorizontalCalendar_hc_year))
                year = getInt(R.styleable.HorizontalCalendar_hc_year, year)


            if (hasValue(R.styleable.HorizontalCalendar_hc_text_color))
                textColor = getColor(R.styleable.HorizontalCalendar_hc_text_color, textColor)

            if (hasValue(R.styleable.HorizontalCalendar_hc_error_text_color))
                errorTextColor =
                    getColor(R.styleable.HorizontalCalendar_hc_error_text_color, errorTextColor)

            if (hasValue(R.styleable.HorizontalCalendar_hc_background_color))
                bgColor = getColor(R.styleable.HorizontalCalendar_hc_background_color, bgColor)

        }

        recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(false)
            calenderAdapter = object : CalenderAdapter() {
                override fun onDateSelected(date: Date) {
                    selectedDate = date
                    val cal = Calendar.getInstance()
                    cal.time = date
                    day = cal.get(Calendar.DAY_OF_MONTH)
                    onDateSelected?.onDate(date)
                }

            }
            adapter = calenderAdapter

        }
        addView(
            recyclerView
            ,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        )

        errorTextView = AppCompatTextView(context).apply {
            setTextColor(errorTextColor)
            visibility = View.GONE
            gravity = Gravity.CENTER
        }

        addView(
            errorTextView
            ,
            LayoutParams(LayoutParams.MATCH_PARENT, convertDpToPixel(100)).apply {
                gravity = Gravity.CENTER
            }
        )

        invalidateDates()
        invalidateColors()
        post {
            calenderAdapter.scrollToCenter()
        }


    }


    fun setDay(day: Int, updateUi: Boolean) {
        this.day = day
        if (updateUi)
            invalidateDates()
    }

    fun setMonth(month: Month, updateUi: Boolean) {
        this.month = month
        if (updateUi)
            invalidateDates()
    }

    fun setYear(year: Int, updateUi: Boolean) {
        this.year = year
        if (updateUi)
            invalidateDates()
    }


    private fun invalidateDates() {


        if (year !in minYear..maxYear) {

            setError("You must select year between $minYear .. $maxYear")
            return
        }

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month.value)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val minDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)


        if (day !in minDay..maxDay) {
            setError("You select ${month.name} so you must select day between $minDay .. $maxDay")
            return
        }

        cal.set(Calendar.DAY_OF_MONTH, day)

        selectedDate = cal.time

        if (::errorTextView.isInitialized)
            errorTextView.visibility = View.GONE
        if (::recyclerView.isInitialized)
            recyclerView.visibility = View.VISIBLE

        if (::calenderAdapter.isInitialized) {
            calenderAdapter.setDate(selectedDate)
            if (::recyclerView.isInitialized)
                recyclerView.post {
                    calenderAdapter.scrollToCenter()
                }
        }
    }

    private fun setError(error: String) {
        if (::errorTextView.isInitialized)
            errorTextView.apply {
                text = error
                visibility = View.VISIBLE
            }

        if (::recyclerView.isInitialized)
            recyclerView.visibility = View.GONE
    }


    private fun invalidateColors() {
        if (::calenderAdapter.isInitialized)
            calenderAdapter.setColors(textColor, bgColor)
    }


    enum class Month(val value: Int) {
        January(0),
        February(1),
        March(2),
        April(3),
        May(4),
        June(5),
        July(6),
        August(7),
        September(8),
        October(9),
        November(10),
        December(11);

        companion object {
            fun valueOf(value: Int) = values().first() { it.value == value }
        }

    }

    interface OnDateSelected {
        fun onDate(date: Date)
    }

    companion object {

        val minYear = Calendar.getInstance().getActualMinimum(Calendar.YEAR)
        val maxYear = Calendar.getInstance().getActualMaximum(Calendar.YEAR)
    }
}

fun convertDpToPixel(dp: Int): Int {
    val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        displayMetrics
    ).toInt()
}