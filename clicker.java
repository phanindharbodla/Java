class HiLoPri {
    public static void main(String args[]) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        clicker hi = new clicker(Thread.NORM_PRIORITY + 2, "High");
        clicker lo = new clicker(Thread.NORM_PRIORITY - 2, "Low");

        hi.start();
        lo.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted.");
        }

        lo.stop();
        // hi.stop();

//      Wait for child threads to terminate.
        try {
            hi.t.join();
            lo.t.join();
        } catch (InterruptedException e) {
            System.out.println("InterruptedException caught");
        }

        System.out.println("Low-priority thread: " + lo.click);
        System.out.println("High-priority thread: " + hi.click);
    }
}


class clicker implements Runnable {
    long                     click   = 0;
    String                   type    = null;
    private volatile boolean running = true;
    Thread                   t;

    public clicker(int p, String s) {
        t    = new Thread(this);
        type = s;

//      System.out.println("Type :: "+s);
        t.setPriority(p);
    }

    public void run() {

//      System.out.println("Type Run  :: "+type);
        while (running) {
            click++;
        }
    }

    public void stop() {
        running = false;
    }

    public void start() {
        t.start();
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
