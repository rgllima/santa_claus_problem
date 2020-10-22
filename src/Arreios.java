public class Arreios implements Runnable {
    boolean isTrenoAnexado;

    Arreios() {
        isTrenoAnexado = false;
    }

    public void run() {
        isTrenoAnexado = !isTrenoAnexado;

        if (isTrenoAnexado) {
            System.out.println("*** Todas as renas foram arreadas! ***");
        }
        else {
            System.out.println("*** Todas as renas estão de volta ao estábulo! ***");
        }
    }
}
