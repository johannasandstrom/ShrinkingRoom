public class Timer {
    static Thread thread = new Thread();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("GO GO GO");
        for (int sek = 60; sek >= 0; sek--) {
            thread.sleep(1000);
            System.out.println(sek);
        }
    }
}