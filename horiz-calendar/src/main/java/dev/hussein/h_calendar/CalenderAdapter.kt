package dev.hussein.h_calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dev.hussein.h_calendar.databinding.RowCalenderBinding
import java.text.SimpleDateFormat
import java.util.*


abstract class CalenderAdapter :
    RecyclerView.Adapter<CalenderAdapter.Holder>() {

    private var textColor: Int = Color.WHITE
    private var bgColor: Int = Color.BLACK
    private var selectedDate: Date
    private lateinit var recyclerView: RecyclerView

    private var dates: LinkedList<Date> = LinkedList()

    init {
        val cal = Calendar.getInstance()
        selectedDate = cal.time

        for (index in cal.getActualMinimum(Calendar.DAY_OF_MONTH)..cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            cal.set(Calendar.DAY_OF_MONTH, index)
            dates.add(cal.time)
        }
    }

    fun setDate(date: Date) {
        dates.clear()
        val cal = Calendar.getInstance()
        selectedDate = date
        cal.time = date

        for (index in cal.getActualMinimum(Calendar.DAY_OF_MONTH)..cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            cal.set(Calendar.DAY_OF_MONTH, index)
            dates.add(cal.time)
        }
        notifyDataSetChanged()
    }

    fun setColors(textColor: Int, bgColor: Int) {
        this.textColor = textColor
        this.bgColor = bgColor
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder {

        val binding: RowCalenderBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_calender,
            parent,
            false
        )

        return Holder(binding)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView


    }

    fun scrollToCenter() {
        if (::recyclerView.isInitialized) {
            val position = getCurrentPosition()

            val layoutManager = if (recyclerView.layoutManager is LinearLayoutManager)
                (recyclerView.layoutManager as LinearLayoutManager) else null


            val view = recyclerView.findViewHolderForAdapterPosition(position)?.itemView
            if (view == null || view.viewTreeObserver == null) {
                val centerOfScreen: Int =
                    (recyclerView.width.div(2)).minus(100)

                if (recyclerView.layoutManager is LinearLayoutManager)
                    layoutManager?.scrollToPositionWithOffset(
                        getCurrentPosition(),
                        centerOfScreen
                    )

            } else {
                view.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        val width: Int = view.measuredWidth
                        val centerOfScreen: Int =
                            (recyclerView.width.div(2)).minus(width.div(2) ?: 0)

                        if (recyclerView.layoutManager is LinearLayoutManager)
                            layoutManager?.scrollToPositionWithOffset(
                                getCurrentPosition(),
                                centerOfScreen
                            )


                    }
                })
            }


        }
    }

    fun getCurrentPosition(): Int {
        return dates.indexOf(selectedDate)
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int
    ) {
        holder.bind(dates[position])
        holder.itemView.setOnClickListener {
            val lastSelected = dates.indexOf(selectedDate)
            selectedDate = dates[position]
            notifyItemChanged(lastSelected)
            notifyItemChanged(position)
            scrollToCenter()
            onDateSelected(selectedDate)


        }
    }

    override fun getItemCount(): Int {
        return dates.size
    }

    inner class Holder(private val mBinding: RowCalenderBinding) : ViewHolder(mBinding.root) {

        fun bind(currentDate: Date) {


            val isSelected = currentDate == selectedDate

            mBinding.selectedLayout.visibility = if (isSelected) View.VISIBLE else View.GONE
            mBinding.unselectedLayout.visibility = if (!isSelected) View.VISIBLE else View.GONE

            mBinding.selectedNumber.text = dayFormat.format(currentDate)
            mBinding.selectedDay.text = dayNameFormat.format(currentDate)
            mBinding.unselectedNumber.text = dayFormat.format(currentDate)
            mBinding.unselectedDay.text = dayNameFormat.format(currentDate)

            // set colors
            mBinding.selectedDay.setTextColor(textColor)
            mBinding.selectedNumber.setTextColor(textColor)
            mBinding.unselectedNumber.setTextColor(textColor)
            mBinding.unselectedDay.setTextColor(textColor)

            mBinding.selectedLayout.setCardBackgroundColor(bgColor)
            mBinding.unselectedLayout.setBackgroundColor(bgColor)
        }


    }


    abstract fun onDateSelected(date: Date)
}

val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())
val printDateFormat = SimpleDateFormat("EEEE dd / MMM / yyyy", Locale.getDefault())