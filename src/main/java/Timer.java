public class Timer {
    static Thread thread = new Thread();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("GO GO GO");
        for (int i = 60; i >= 0; i--) {
            thread.sleep(1000);
            System.out.println(i);
        }
    }
}