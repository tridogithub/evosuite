package com.examples.with.different.packagename.GECCO;

/**
 * @author TriDv
 * @date 31/03/2023
 * @project evosuite
 */
public class EI {
    private final float EULER = 0.57721566f;
    private final int MAXIT = 100;
    private final double FPMIN = 1.0e-30;
    private final double EPS = 6.0e-8;

    double ei(double x) {
        int k;
        float fact, prev, sum, term;
        if (x <= 0.0) System.out.println("Bad argument in ei");
        if (x < FPMIN) return Math.log(x) + EULER;
        if (x <= -Math.log(EPS)) {
            sum = 0.0f;
            fact = 1.0f;
            for (k = 1; k <= MAXIT; k++) {
                fact *= x / k;
                term = fact / k;
                sum += term;
                if (term < EPS * sum) break;
            }
            if (k > MAXIT) System.out.println("Series failed in ei");
            return sum + Math.log(x) + EULER;
        } else {
            sum = 0.0f;
            term = 1.0f;
            for (k = 1; k <= MAXIT; k++) {
                prev = term;
                term *= k / x;
                if (term < EPS) break;
                if (term < prev) sum += term;
                else {
                    sum -= prev;
                    break;
                }
            }
            return Math.exp(x) * (1.0 + sum) / x;
        }
    }
}
