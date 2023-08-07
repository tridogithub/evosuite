package com.examples.with.different.packagename;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author TriDv
 * @date 30/01/2023
 * @project evosuite
 */
public class Add {
    private int currentYear;
    private int currentMonth;
    private int currentDay;
    private int currentERA = 0;
    private final int JANUARY = 1;
    private final int MONTH_LENGTH[]
            = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; // 0-based
    private final int LEAP_MONTH_LENGTH[]
            = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; // 0-based

    public Add(int currentMonth, int currentDay, int currentYear) {
        this.currentYear = currentYear;
        this.currentMonth = currentMonth;
        this.currentDay = currentDay;
    }

    public Add(int currentMonth, int currentDay, int currentYear, int ERA) {
        this.currentYear = currentYear;
        this.currentMonth = currentMonth;
        this.currentDay = currentDay;
        if (ERA != 0) {
            this.currentERA = AD;
        } else {
            this.currentERA = ERA;
        }
    }

    private static final int YEAR = 1;
    private static final int MONTH = 2;
    private static final int DAY_OF_MONTH = 3;
    private static final int ERA = 3;
    private static final int BC = 0;
    private static final int AD = 1;

    public String add(int field, int amount) {
        // If amount == 0, do nothing even the given field is out of
        // range. This is tested by JCK.
        if (amount == 0 || !isValidDate(currentDay, currentMonth, currentYear)) {
            return "Nothing changed";
        }

        if (field != YEAR && field != MONTH && field != DAY_OF_MONTH && field != ERA) {
            return "Invalid field";
        }

        if (field == YEAR) {
            int year = this.currentYear;
            if (this.currentERA == AD) {
                year += amount;
                if (year > 0) {
//                    set(YEAR, year);
                    this.currentYear = year;
                } else { // year <= 0
//                    set(YEAR, 1 - year);
                    this.currentYear = 1 - year;
                    // if year == 0, you get 1 BCE.
//                    set(ERA, BCE);
                    this.currentERA = BC;
                }
            } else { // era == BCE
                year -= amount;
                if (year > 0) {
//                    set(YEAR, year);
                    this.currentYear = year;
                } else { // year <= 0
//                    set(YEAR, 1 - year);
                    this.currentYear = 1 - year;
                    // if year == 0, you get 1 CE
//                    set(ERA, CE);
                    this.currentERA = AD;
                }
            }
            pinDayOfMonth();
        } else if (field == MONTH) {
            int month = this.currentMonth + amount;
            int year = this.currentYear;
            int y_amount;

            if (month >= 0) {
                y_amount = month / 12;
            } else {
                y_amount = (month + 1) / 12 - 1;
            }
            if (y_amount != 0) {
                if (this.currentERA == AD) {
                    year += y_amount;
                    if (year > 0) {
//                        set(YEAR, year);
                        this.currentYear = year;
                    } else { // year <= 0
//                        set(YEAR, 1 - year);
                        this.currentYear = 1 - year;
                        // if year == 0, you get 1 BCE
//                        set(ERA, BCE);
                        this.currentERA = BC;
                    }
                } else { // era == BCE
                    year -= y_amount;
                    if (year > 0) {
//                        set(YEAR, year);
                        this.currentYear = year;
                    } else { // year <= 0
//                        set(YEAR, 1 - year);
                        this.currentYear = 1 - year;
                        // if year == 0, you get 1 CE
//                        set(ERA, CE);
                        this.currentERA = AD;
                    }
                }
            }

            if (month >= 0) {
//                set(MONTH, month % 12);
                this.currentMonth = month % 12;
            } else {
                // month < 0
                month %= 12;
                if (month < 0) {
                    month += 12;
                }
//                set(MONTH, JANUARY + month);
                this.currentMonth = JANUARY + month;
            }
            pinDayOfMonth();
        } else if (field == DAY_OF_MONTH) {
            String sDate1 = this.currentYear + "/" + this.currentMonth + "/" + this.currentDay;
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy/MM/dd").parse(sDate1);
            } catch (ParseException e) {
                return "Invalid date";
            }
            long currentMilliseconds = date.getTime();
            currentMilliseconds += amount * 86400000;
            Calendar calendar = (Calendar) Calendar.getInstance();
            calendar.setTimeInMillis(currentMilliseconds);
            this.currentYear = calendar.get(Calendar.YEAR);
            this.currentMonth = calendar.get(Calendar.MONTH) + 1;
            this.currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            this.currentERA = calendar.get(Calendar.ERA);
        } else if (field == currentERA) {
            int era = this.currentERA + amount;
            if (era < 0) {
                era = 0;
            }
            if (era > 1) {
                era = 1;
            }
//            set(ERA, era);
            this.currentERA = era;
        }
        int newDay = this.currentDay;
        int newMonth = this.currentMonth;
        int newYear = this.currentYear;
        String era = (this.currentERA == 0 ? "BC" : "AD");
        return newYear + "/" + newMonth + "/" + newDay + " " + era;
    }

    private void pinDayOfMonth() {
        int year = this.currentYear;
        int monthLen;
        monthLen = monthLength(this.currentMonth);

        int dom = this.currentDay;
        if (dom > monthLen) {
//            set(DAY_OF_MONTH, monthLen);
            this.currentDay = monthLen;
        }
    }

    private int monthLength(int month) {
        int year = this.currentYear;
        if (this.currentERA == BC) {
            year = 1 - year;
        }
        return monthLength(month, year);
    }

    private int monthLength(int month, int year) {
        return isLeapYear(year) ? LEAP_MONTH_LENGTH[month - 1] : MONTH_LENGTH[month - 1];
    }

    private boolean isLeapYear(int year) {
        // Return true if year is
        // a multiple of 4 and not
        // multiple of 100.
        // OR year is multiple of 400.
        return (((year % 4 == 0) &&
                (year % 100 != 0)) ||
                (year % 400 == 0));
    }

    private boolean isLeap(int year) {
        // Return true if year is
        // a multiple of 4 and not
        // multiple of 100.
        // OR year is multiple of 400.
        return (((year % 4 == 0) &&
                (year % 100 != 0)) ||
                (year % 400 == 0));
    }

    // Returns true if given
    // year is valid or not.
    private boolean isValidDate(int d,
                               int m,
                               int y) {
        // If year, month and day
        // are not in given range
        if (m < 1 || m > 12)
            return false;
        if (d < 1 || d > 31)
            return false;

        // Handle February month
        // with leap year
        if (m == 2) {
            if (isLeap(y))
                return (d <= 29);
            else
                return (d <= 28);
        }

        // Months of April, June,
        // Sept and Nov must have
        // number of days less than
        // or equal to 30.
        if (m == 4 || m == 6 ||
                m == 9 || m == 11)
            return (d <= 30);

        return true;
    }

    public void setCurrentYear(int currentYear) {
        this.currentYear = currentYear;
    }

    public void setCurrentMonth(int currentMonth) {
        this.currentMonth = currentMonth;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public void setCurrentERA(int currentERA) {
        this.currentERA = currentERA;
    }

}
