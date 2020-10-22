public class Mensagem implements Runnable {
    String msg;

    Mensagem(String msg) {
        this.msg = msg;
    }

    public void run() {
        System.out.println(msg);
    }
}
