package com.examples.with.different.packagename.GECCO;

/**
 * @author tridv on 11/2/2023
 * @project evosuite
 */
public class Expint {
    private final static int MAXIT = 100;
    private final static double EULER = 100;
    private final static double FPMIN = 1.0E-30;
    private final static double EPS = 10.E-7;

    public double expint(int n, double x)
//    Evaluates the exponential integral En(x).
    {
        int i, ii, nm1;
        double a, b, c, d, del, fact, h, psi, ans = 0;
        nm1 = n - 1;
        if (n < 0 || x < 0.0 || (x == 0.0 && (n == 0 || n == 1))) {
            System.out.println("bad arguments in expint");
        } else {
            if (n == 0) {
                ans = Math.exp(-x) / x;
            } else {
                if (x == 0.0) {
                    ans = 1.0 / nm1;
                } else {
                    if (x > 1.0) {
                        b = x + n;
                        c = 1.0 / FPMIN;
                        d = 1.0 / b;
                        h = d;
                        for (i = 1; i <= MAXIT; i++) {
                            a = -i * (nm1 + i);
                            b += 2.0;
                            d = 1.0 / (a * d + b);
                            c = b + a / c;
                            del = c * d;
                            h *= del;
                            if (Math.abs(del - 1.0) < EPS) {
                                ans = h * Math.exp(-x);
                                return ans;
                            }
                        }
                        System.out.println("continued fraction failed in expint");
                    } else {
                        ans = (nm1 != 0 ? 1.0 / nm1 : -Math.log(x) - EULER);
                        fact = 1.0;
                        for (i = 1; i <= MAXIT; i++) {
                            fact *= -x / i;
                            if (i != nm1) del = -fact / (i - nm1);
                            else {
                                psi = -EULER;
                                for (ii = 1; ii <= nm1; ii++) psi += 1.0 / ii;
                                del = fact * (-Math.log(x) + psi);
                            }
                            ans += del;
                            if (Math.abs(del) < Math.abs(ans) * EPS) return ans;
                        }
                        System.out.println("series failed in expint");
                    }
                }
            }
        }
        return ans;
    }
}
