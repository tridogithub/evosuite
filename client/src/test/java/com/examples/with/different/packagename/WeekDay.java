package com.examples.with.different.packagename;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author tridv on 17/1/2023
 * @project evosuite
 */
public class WeekDay {
    // dayOfWeek, a method that determines what day of the week a given day in a
    // given month of a given year is or was. This works by computing the number of
    // days between January 1, 1900 and the given date, not counting either Jan. 1,
    // 1900 or the given date -- e.g., Jan. 1, 1900 would yield a value of 0. The
    // remainder when this number is divided by 7 is how many days in excess of a
    // whole number of weeks the given date is after Jan. 1, 1900. Using this number
    // to look up a weekday name, in a list that starts with whatever day of the
    // week Jan. 1, 1900 was, yields the desired result.

    private String dayOfWeek(int day, int month, int year) {

        // Names of weekdays, starting with Jan. 1, 1900 (a Monday):
        final String days[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        return days[daysSince1900(day, month, year) % 7];
    }


    // daysSince1900, a method that computes the number of days between Jan. 1, 1900, and
    // a given date, not counting either Jan. 1, 1900 or the given date (e.g., this will
    // return 0 if the given date is Jan. 1, 1900). This function calculates the number of
    // days since the base date in three parts:
    //   First, it calculates the days in the completed years between 1900 and "year". Each year
    //     contributes 365 days, every fourth year adds one day for leap year (conveniently, the
    //     first leap year was 1904, 4 years after 1900), every hundredth year removes a day since
    //     years divisible by 100 generally aren't leap years, and the year 2000 and every 400th
    //     year thereafter adds a day back because years divisible by 400 *are* leap years.
    //   Second, this function adds all the days in the months up to but not including "month" to the
    //     result of the first step.
    //   Finally, this function adds "day"-1 to the total from the first two steps. The "-1" reflects
    //     the fact that days since the base date start at 0 (i.e., January 1, 1900 is 0 days
    //     since the base date), but day-of-month values start at 1.

    private int daysSince1900(int day, int month, int year) {

        int fullYears = year - 1900;
        int leapYears = (year - 1901) / 4;        // To calculate *full* leap years, centuries,
        int centuries = (year - 1901) / 100;        // etc. since 1900, I count from 1901 (or 1601,
        int fourCenturies = (year - 1601) / 400;    // for quadruple centuries) to avoid counting the
        // current year, which isn't full yet.
        return fullYears * 365 + leapYears - centuries + fourCenturies
                + daysIntoYear(month, year)
                + day - 1;
    }


    // daysIntoYear, a method that returns the number of days between the beginning of a
    // year and the first day of a given month (not counting the first of the month).
    // This method just adds up the lengths of all the months from January to, but
    // not including, "month". Note that the year is used in "monthLength" to determine
    // whether February has 28 or 29 days.

    private int daysIntoYear(int month, int year) {

        int days = 0;

        for (int m = 1; m < month; m++) {
            days = days + monthLength(m, year);
        }

        return days;
    }


    // monthLength, a method that returns the number of days in a specified
    // month of a specified year. This method looks up the month in an array
    // of lengths, except that if the month is February and the year is a
    // leap year, this function returns 29.

    private int monthLength(int month, int year) {

        final int lengths[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        if (month != 3 || !isLeapYear(year)) {
            return lengths[month - 1];
        } else {
            return 29;
        }
    }


    // isLeapYear, a method that returns true if a year is a leap year, and
    // false if it isn't. A year is a leap year if it is divisible by 4, but
    // not by 100, or if it *is* divisible by 400.

    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
}
