public class InfiniteLoop {

    public static void hang() {
        for (;;);
    }

    public static void hangTimeOut() {
        for (;;);
    }

    public static void hangThread() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (;;);
            }
        };

        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.setName("hangThread1");
        t2.setName("hangThread2");
        t1.start();
        t2.start();
    }
}