package com.examples.with.different.packagename.GECCO;

/**
 * @author tridv on 1/7/2023
 * @project evosuite
 */
public class Rc {
    private final double ERRTOL = 0.04;
    private final double TINY = 1.69e-38;
    private final double SQRTNY = 1.3e-19;
    private final double BIG = 3.e37;

    private final double TNBG = TINY * BIG;
    private final double COMP1 = 2.236 / SQRTNY;
    private final double COMP2 = TNBG * TNBG / 25.0;
    private final double THIRD = 1.0 / 3.0;
    private final double C1 = 3.0;
    private final double C2 = 1.0 / 7.0;
    private final double C3 = 0.375;
    private final double C4 = 9.0 / 22.0;

    public double rc(double x, double y) {
        double alamb, ave=0.0, s=0.0, w, xt, yt;

        if (x < 0.0 || y == 0.0 || (x + Math.abs(y)) < TINY || (x + Math.abs(y)) > BIG ||
                (y < -COMP1 && x > 0.0 && x < COMP2))
            throw new RuntimeException("invalid arguments in rc");
        if (y > 0.0) {
            xt = x;
            yt = y;
            w = 1.0;
        } else {
            xt = x - y;
            yt = -y;
            w = Math.sqrt(x) / Math.sqrt(xt);
        }
        do {
            alamb = 2.0 * Math.sqrt(xt) * Math.sqrt(yt) + yt;
            xt = 0.25 * (xt + alamb);
            yt = 0.25 * (yt + alamb);
            ave = THIRD * (xt + yt + yt);
            s = (yt - ave) / ave;
        } while (Math.abs(s) > ERRTOL);
        return w * (1.0 + s * s * (C1 + s * (C2 + s * (C3 + s * C4)))) / Math.sqrt(ave);
    }
}
