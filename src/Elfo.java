import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class Elfo implements Runnable {
    int id;

    private static final Random generator = new Random();
    volatile boolean criancasAindaAcrediamNoPapai;
    private final Semaphore filaDeElfos;
    private final CyclicBarrier tresElfos;
    private final CyclicBarrier elfosEstaoInspirados;
    private final Semaphore atencaoDoPapai;

    Elfo(int id, boolean criancasAindaAcrediamNoPapai, Semaphore filaDeElfos, CyclicBarrier tresElfos, CyclicBarrier elfosEstaoInspirados, Semaphore atencaoDoPapai) {
        this.id = id;
        this.criancasAindaAcrediamNoPapai = criancasAindaAcrediamNoPapai;
        this.filaDeElfos = filaDeElfos;
        this.tresElfos = tresElfos;
        this.elfosEstaoInspirados = elfosEstaoInspirados;
        this.atencaoDoPapai = atencaoDoPapai;
    }

    public void run() {
        try {
            Thread.sleep(generator.nextInt(2000));

            while (criancasAindaAcrediamNoPapai) {
                // nao cabem mais do que tres elfos no escritorio do papai noel
                filaDeElfos.acquire();
                System.out.println("Elfo" + id + " ficou sem ideias.");

                // espera ate tres elfos terem um problema
                int elfo = tresElfos.await();

                // o terceiro elfo age pelos tres
                if (elfo == PapaiNoel.TERCEIRO_ELFO)
                    atencaoDoPapai.acquire();

                // espera ate que todos os elfos tenham novas ideias
                Thread.sleep(generator.nextInt(500));
                System.out.println("Elfo" + id + " ficou inspirado");

                elfosEstaoInspirados.await();

                if (elfo == PapaiNoel.TERCEIRO_ELFO)
                    atencaoDoPapai.release();

                /*
                Libera a fila de elfos para que outro grupo de elfos
                sem ideia possam se reunir e acordar o papai noel
                 */
                filaDeElfos.release();

                // fabricar brinquedos ate a inspiracao acabar
                Thread.sleep(generator.nextInt(2000));
            }
        } catch (InterruptedException e) {
            // thread interrompida para limpeza do programa
        } catch (BrokenBarrierException e) {
            // outra thread na barrier foi interrompida
        }

        System.out.println("Elfo " + id + " se aposenta!");
    }
}
