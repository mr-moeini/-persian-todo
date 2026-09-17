package ir.moeini.persiantodo.util

import java.time.LocalDate

/**
 * Converts between Gregorian (java.time.LocalDate) and the Jalali / Shamsi (Hijri Shamsi)
 * calendar used in Iran and Afghanistan. Pure math, no external library needed.
 *
 * Algorithm reference: the standard 33-year leap-cycle Jalali conversion
 * (same approach used by jdf.js / PersianCalendar implementations).
 */
data class JalaliDate(val year: Int, val month: Int, val day: Int) : Comparable<JalaliDate> {

    /** 1 = فروردین ... 12 = اسفند */
    fun monthName(): String = PersianCalendarUtils.monthNames[month - 1]

    fun weekdayName(): String {
        val g = PersianCalendarUtils.toGregorian(this)
        // java.time DayOfWeek: Monday=1 ... Sunday=7
        val dow = g.dayOfWeek.value % 7 // Sunday -> 0
        return PersianCalendarUtils.weekdayNames[dow]
    }

    fun formatFull(): String = "${toPersianDigits(day)} ${monthName()} ${toPersianDigits(year)}"

    fun formatShort(): String =
        "${toPersianDigits(year)}/${toPersianDigits(month.toString().padStart(2, '0'))}/${toPersianDigits(day.toString().padStart(2, '0'))}"

    override fun compareTo(other: JalaliDate): Int {
        if (year != other.year) return year - other.year
        if (month != other.month) return month - other.month
        return day - other.day
    }

    companion object {
        fun today(): JalaliDate = PersianCalendarUtils.toJalali(LocalDate.now())
    }
}

fun toPersianDigits(input: Any): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val sb = StringBuilder()
    for (c in input.toString()) {
        if (c.isDigit()) sb.append(persianDigits[c - '0']) else sb.append(c)
    }
    return sb.toString()
}

object PersianCalendarUtils {

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val weekdayNames = listOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )

    private fun div(a: Int, b: Int) = a / b

    /** Converts a Gregorian LocalDate to a JalaliDate. */
    fun toJalali(g: LocalDate): JalaliDate {
        val gy = g.year
        val gm = g.monthValue
        val gd = g.dayOfMonth

        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gy2 = if (gm > 2) gy + 1 else gy
        val days = 355666 + (365 * gy) +
                div(gy2 + 3, 4) - div(gy2 + 99, 100) +
                div(gy2 + 399, 400) + gd +
                gDaysInMonth.take(gm - 1).sum()

        var jy = -1595 + (33 * div(days, 12053))
        var rem = days % 12053
        jy += 4 * div(rem, 1461)
        rem %= 1461
        if (rem > 365) {
            jy += div(rem - 1, 365)
            rem = (rem - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (rem < 186) {
            jm = 1 + div(rem, 31)
            jd = 1 + (rem % 31)
        } else {
            jm = 7 + div(rem - 186, 30)
            jd = 1 + ((rem - 186) % 30)
        }
        return JalaliDate(jy, jm, jd)
    }

    /** Converts a JalaliDate back to a Gregorian LocalDate. */
    fun toGregorian(j: JalaliDate): LocalDate {
        val jy = j.year + 1595
        var days = -355668 + (365 * jy) + (div(jy, 33) * 8) + div((jy % 33) + 3, 4) +
                j.day + if (j.month < 7) (j.month - 1) * 31 else ((j.month - 7) * 30) + 186

        var gy = 400 * div(days, 146097)
        days %= 146097
        if (days > 36524) {
            gy += 100 * div(days - 1, 36524)
            days = (days - 1) % 36524
            if (days >= 365) days += 1
        }
        gy += 4 * div(days, 1461)
        days %= 1461
        if (days > 365) {
            gy += div(days - 1, 365)
            days = (days - 1) % 365
        }

        var gd = days + 1
        val gDaysInMonth = intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 12 && gd > gDaysInMonth[gm]) {
            gd -= gDaysInMonth[gm]
            gm++
        }
        return LocalDate.of(gy, gm + 1, gd)
    }

    /** Number of days in a given Jalali month/year (handles leap Esfand = 30 days). */
    fun daysInMonth(year: Int, month: Int): Int {
        if (month <= 6) return 31
        if (month <= 11) return 30
        return if (isLeapJalaliYear(year)) 30 else 29
    }

    fun isLeapJalaliYear(year: Int): Boolean {
        val rem = (((year - if (year > 0) 474 else 473) % 2820) + 2820) % 2820
        return (((rem + 474) + 38) * 682) % 2816 < 682
    }
}
