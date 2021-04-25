public class Main {
    static Object mon = new Object();
    static final int cnt = 5;
    static volatile char symbol = 'A';

    public static void main(String[] args) {

        new Thread(() -> {
            try {
                for (int i = 0; i < cnt; i++) {
                    synchronized (mon) {
                        while (symbol != 'A') {
                            mon.wait();
                        }
                        System.out.print(symbol);
                        symbol = 'B';
                        mon.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                for (int i = 0; i < cnt; i++) {
                    synchronized (mon) {
                        while (symbol != 'B') {
                            mon.wait();
                        }
                        System.out.print(symbol);
                        symbol = 'C';
                        mon.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                for (int i = 0; i < cnt; i++) {
                    synchronized (mon) {
                        while (symbol != 'C') {
                            mon.wait();
                        }
                        System.out.print(symbol);
                        symbol = 'A';
                        mon.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
