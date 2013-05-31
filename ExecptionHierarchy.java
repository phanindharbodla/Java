abstract class ExceptionA extends Exception {
    public ExceptionA() {}
    public ExceptionA(final String exceptionMessage) {
        super(exceptionMessage);
    }
    public abstract void test() throws ExceptionA;
}
class ExceptionB extends ExceptionA {
    public ExceptionB() {}
    public ExceptionB(final String exceptionMessage) {
        super(exceptionMessage);
    }
    public void test() throws ExceptionB {
        throw new ExceptionB("ExceptionB raised..!!");
    }
}
class ExceptionC extends ExceptionB {
    public ExceptionC() {}
    public ExceptionC(final String exceptionMessage) {
        super(exceptionMessage);
    }
    public void test() throws ExceptionC {
        throw new ExceptionC("ExceptionC raised..!!");
    }
}
public class ExecptionHierarchy {
    public static void main(String[] args) {
        ExceptionB b = new ExceptionB();
        ExceptionC c = new ExceptionC();
        try {
            b.test();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        try {
            c.test();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
		try {
            b.test();
        } catch (ExceptionA e) {
            e.printStackTrace(System.err);
        }
        try {
            c.test();
        } catch (ExceptionA e) {
            e.printStackTrace(System.err);
        }
		try {
            c.test();
        } catch (ExceptionB e) {
            e.printStackTrace(System.err);
        }
		System.out.println("So as we observed ..!!");
		System.out.println("We tried catching Exception of subclass with their super class catch block..!!");
		System.out.println("But what we observed is that their corresponding execptions are raised......!!");
		System.out.println("By its super class..!!");
    }
}
