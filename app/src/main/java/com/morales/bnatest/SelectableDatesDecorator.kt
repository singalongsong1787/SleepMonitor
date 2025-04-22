package com.morales.bnatest

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class SelectableDatesDecorator(private val selectableDates: List<CalendarDay>) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return!selectableDates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.setDaysDisabled(true)
    }
}
