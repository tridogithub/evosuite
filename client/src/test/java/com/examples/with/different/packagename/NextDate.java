package com.examples.with.different.packagename;/*
 * This code was generated by ojc.
 */


public class NextDate
{

    private int month;

    private int day;

    private int year;

    public NextDate( int m, int d, int y )
    {
        this.month = m;
        this.day = d;
        this.year = y;
    }

    public String nextDate()
    {
        if (day < 1 || month < 1 || month > 12 || day > 31 || year < 1) {
            return "invalid Input Date";
        } else {
        }
        int tomorrowDay = day;
        int tomorrowMonth = month;
        int tomorrowYear = year;
        if (isThirtyOneDayMonth( month )) {
            if (day < 31) {
                tomorrowDay = day + 1;
            } else {
                tomorrowDay = 1;
                tomorrowMonth = month + 1;
            }
        } else {
            if (isThirtyDayMonth( month )) {
                if (day < 30) {
                    tomorrowDay = day + 1;
                } else {
                    if (day == 30) {
                        tomorrowDay = 1;
                        tomorrowMonth = month + 1;
                    } else {
                        return "Invalid Input Date";
                    }
                }
            } else {
                if (isDecember( month )) {
                    if (day <= 31) {
                        tomorrowDay = day + 1;
                    } else {
                        tomorrowDay = 1;
                        tomorrowMonth = 1;
                        tomorrowYear = year + 1;
                    }
                } else {
                    if (isFebruary( month )) {
                        if (day < 28) {
                            tomorrowDay = day + 1;
                        } else {
                            if (day == 28) {
                                if (isLeapYear( year )) {
                                    tomorrowDay = 29;
                                } else {
                                    tomorrowDay = 1;
                                    tomorrowMonth = 3;
                                }
                            } else {
                                if (day == 29) {
                                    if (isLeapYear( year )) {
                                        tomorrowDay = 1;
                                        tomorrowMonth = 3;
                                    } else {
                                        return "Invalid Input Date";
                                    }
                                } else {
                                    if (day > 29) {
                                        return "Invalid Input Date";
                                    } else {
                                    }
                                }
                            }
                        }
                    } else {
                    }
                }
            }
        }
        return tomorrowMonth + "/" + tomorrowDay + "/" + tomorrowYear;
    }

    private boolean isThirtyOneDayMonth( int month )
    {
        return month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10;
    }

    private boolean isThirtyDayMonth( int month )
    {
        return month == 4 || month == 6 || month == 9 || month == 11;
    }

    private boolean isDecember( int month )
    {
        return month == 12;
    }

    private boolean isFebruary( int month )
    {
        return month == 2;
    }

    private boolean isLeapYear( int year )
    {
        if (year % 100 == 0) {
            return year % 400 == 0;
        } else {
            return year % 4 == 0;
        }
    }

    public void setDay( int d )
    {
        this.day = d;
    }

    public void setMonth( int m )
    {
        this.month = m;
    }

    public void setYear( int y )
    {
        this.year = y;
    }

}
