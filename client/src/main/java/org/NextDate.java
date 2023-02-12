package org;

public class NextDate {
    private int m;
    private int d;
    private int y;

    public NextDate(int m, int d, int y) {
        //int month, day, year; //varibles holding the month, day and year args
        //Initialize the mont, day, and year, respectively
        this.m = m;
        this.d = d;
        this.y = y;

    }


    /*********************************************************
     **Method: run
     **Returns: string
     **Description: This method will return a string depicting the nextDate in the from MM/DD/YY
     ***********************************************************/

    public String nextDate() {
        int month = m;
        int day = d;
        int year = y;

        if (day < 1 || day >31 || month < 1 || month > 12
                || year < 1801 || year > 9999
        )
            return "invalid Input Date";

        //these variables will hold the proper values for the nextDate's day, month, and year values, respectively
        int tomorrowDay = day;
        int tomorrowMonth = month;
        int tomorrowYear = year;

        //Is this month with 31 days?
        if (isThirtyOneDayMonth(month)) {
            if (day < 31) //if the day is not 31, just increment the day
                tomorrowDay = day + 1;
            else { //day = 31, set tomorrow's day to 1 and increment the month
                tomorrowDay = 1;
                tomorrowMonth = month + 1;
            }
        }
        //is this month a month with 30 days?
        else if (isThirtyDayMonth(month)) {
            if (day < 30) //if the day is not 30, just increment the day
                tomorrowDay = day + 1;
            else {
                if (day == 30) {
                    tomorrowDay = 1;
                    tomorrowMonth = month + 1;
                } else //invalid input ... too many days
                    return "Invalid Input Date";
            }
        }
        //is this month December?
        else if (isDecember(month)) {
            if (day <= 31) //if the day is not 31, just increment the next day
                tomorrowDay = day + 1;
            else { //day is 31, reset the day and month to 1 and increment the year
                tomorrowDay = 1;
                tomorrowMonth = 1;
                tomorrowYear = year + 1;
            }

        }
        //is this month February? we need to check for leap years and such
        else if (isFebruary(month)) {
            if (day < 28) //just a standard day - increment the day
                tomorrowDay = day + 1;
            else {
                if (day == 28) {  //if this is not a leap year, reset day and increment the day
                    if (isLeapYear(year)) //was a leap year
                        tomorrowDay = 29;
                    else {  //was not a leap year
                        tomorrowDay = 1;
                        tomorrowMonth = 3;
                    }
                } else if (day == 29) { //29th date of February
                    if (isLeapYear(year)) {  //AND a leap year - reset the day to 1, month to 3
                        tomorrowDay = 1;
                        tomorrowMonth = 3;
                    } else
                        return "Invalid Input Date";
                } else if (day > 29) //invalid input as February will never have more than 29 days
                    return "Invalid Input Date";
            }
        }
        //return the string representing the nextDate, in the form MM/DD/YY
        return tomorrowMonth + "/" + tomorrowDay + "/" + tomorrowYear;

    }


    /*********************************************
     * Method: isThirtyOneDayMonth
     * @param month
     * @returns boolean
     * Description: This method will return true is 'month' corresponds to a
     * month that contains 31 days, excluding December
     */

    private boolean isThirtyOneDayMonth(int month) {
        return month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10;
    }


    /*************************************************
     * Method: isThirtyDayMonth
     * @param month
     * @returns boolean
     * Description: This method will return true if 'month' corresponds to a
     * month that contains 30 days.  *
     */

    private boolean isThirtyDayMonth(int month) {
        return month == 4 || month == 6 || month == 9 || month == 11;
    }


    /**************************************************
     * Method: isDecember
     * @param month
     * @returns boolean
     * Description: This method will return true if 'month' corresponds to
     * December
     */

    private boolean isDecember(int month) {
        return month == 12;
    }


    /****************************************************
     * Method: isFebruary
     * @param month
     * @returns boolean
     * Description: This method will return true if 'month' corresponds to
     * February
     */

    private boolean isFebruary(int month) {
        return month == 2;
    }


    /*****************************************************
     * Method: isLeapYear
     * @param year
     * @returns boolean
     * Description: This method will return true if 'year' corresponds to a
     * leap year. It works like this:
     *          *If the year is not a century year and divisible by 4,
     *          then it is a leap year
     *          *If the year is a century year, it is a leap year if and only if
     *          it is divisible by 400
     */

    private boolean isLeapYear(int year) {
        if ((year % 100) == 0)
            return (year % 400) == 0;
        else
            return (year % 4) == 0;
    }

    public void setD( int d )
    {
        this.d = d;
    }

    public void setM( int m )
    {
        this.m = m;
    }

    public void set( int y )
    {
        this.y = y;
    }

}
