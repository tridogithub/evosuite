package com.examples.with.different.packagename.GECCO;

/**
 * @author TriDv
 * @date 31/03/2023
 * @project evosuite
 */
public class Betai {
    private final int MAXIT = 100;
    private final double FPMIN = 1.0e-30;
    private final double EPS = 6.0e-8;

    public double betai(double a, double b, double x) {
        double bt;
        if (x < 0.0 || x > 1.0) {
            throw new RuntimeException("Bad x in routine betai");
        }
        if (x == 0.0 || x == 1.0) bt = 0.0f;
        else
            bt = Math.exp(gammln(a + b) - gammln(a) - gammln(b) + a * Math.log(x) + b * Math.log(1.0 - x));
        if (x < (a + 1.0) / (a + b + 2.0))
            return bt * betacf(a, b, x) / a;
        else return 1.0 - bt * betacf(b, a, 1.0f - x) / b;
    }

    private double betacf(double a, double b, double x) {
        int m, m2;
        double aa, c, d, del, h, qab, qam, qap;
        qab = a + b;
        qap = a + 1.0f;
        qam = a - 1.0f;
        c = 1.0f;
        d = 1.0f - qab * x / qap;
        if (Math.abs(d) < FPMIN) d = FPMIN;
        d = 1.0f / d;
        h = d;
        for (m = 1; m <= MAXIT; m++) {
            m2 = 2 * m;
            aa = m * (b - m) * x / ((qam + m2) * (a + m2));
            d = 1.0f + aa * d;
            if (Math.abs(d) < FPMIN) d = FPMIN;
            c = 1.0f + aa / c;
            if (Math.abs(c) < FPMIN) c = FPMIN;
            d = 1.0f / d;
            h *= d * c;
            aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2));
            d = 1.0f + aa * d;
            if (Math.abs(d) < FPMIN) d = FPMIN;
            c = 1.0f + aa / c;
            if (Math.abs(c) < FPMIN) c = FPMIN;
            d = 1.0f / d;
            del = d * c;
            h *= del;
            if (Math.abs(del - 1.0) < EPS) break;
        }
        if (m > MAXIT) throw new RuntimeException("a or b too big, or MAXIT too small in betacf");
        return h;
    }

    private double gammln(double xx) {
        double x, y, tmp, ser;
        double cof[] = {
                76.18009172947146, -86.50532032941677,
                24.01409824083091, -1.231739572450155,
                0.1208650973866179e-2, -0.5395239384953e-5
        };
        int j;
        y = x = xx;
        tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        ser = 1.000000000190015;
        for (j = 0; j <= 5; j++) ser += cof[j] / ++y;
        return -tmp + Math.log(2.5066282746310005 * ser / x);

    }
}
