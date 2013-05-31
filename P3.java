//~--- JDK imports ------------------------------------------------------------

import java.io.*;

class P3 {
    public static void main(String args[]) throws IOException {
        complex quad  = new complex(3.2, 8.3);
        complex quad1 = new complex(3.3, 4.3);

        quad.printcomplex();
        quad1.printcomplex();
		
        if (quad.equals(quad.plus(quad1.less(quad)))) {
            System.out.println("As quad.equals(quad.plus(quad1.less(quad))) is True...!! \nSo the Plus and Less functions are working fine..!!");
        }
    }
}


class complex {
    private double real;
    private double virtual;

    public complex() {
        this.real    = 0.0;
        this.virtual = 0.0;
    }

    public complex(double x, double y) {
        this.real    = x;
        this.virtual = y;
    }

    public void printcomplex() {
        System.out.format("( %-10.3f , %-10.3f)%n", this.real, this.virtual);
    }

    public complex plus(complex that) {
        this.real    += that.real;
        this.virtual += that.virtual;

        return this;
    }

    public complex less(complex that) {
        this.real    -= that.real;
        this.virtual -= that.virtual;

        return this;
    }
}



