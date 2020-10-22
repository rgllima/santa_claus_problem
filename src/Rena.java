import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class Rena implements Runnable {
    int id;
    volatile boolean criancasAindaAcrediamNoPapai;
    private final CyclicBarrier todasRenas;
    private final CyclicBarrier treno;
    private final Semaphore atencaoDoPapai;
    private final Semaphore descrenca;

    private static Random generator = new Random();

    Rena(int id, boolean criancasAindaAcrediamNoPapai, CyclicBarrier todasRenas, CyclicBarrier treno, Semaphore atencaoDoPapai, Semaphore descrenca) {
        this.id = id;
        this.criancasAindaAcrediamNoPapai = criancasAindaAcrediamNoPapai;
        this.todasRenas = todasRenas;
        this.treno = treno;
        this.atencaoDoPapai = atencaoDoPapai;
        this.descrenca = descrenca;
    }

    public void run() {
        while (criancasAindaAcrediamNoPapai) {
            try {
                // Espera até a chegada do Natal
                Thread.sleep(900 + generator.nextInt(200));

                // O Papai Noel só pode ser acordado quanto todas as renas estiverem juntas
                int rena = todasRenas.await();

                // the last rena to return to North Pole must wake Santa
                if (rena == PapaiNoel.ULTIMA_RENA) {
                    atencaoDoPapai.acquire();
                    System.out.println("*** Entregar os presentes de Natal de " + PapaiNoel.ano + " ***");
                    if (PapaiNoel.ano.incrementAndGet() == PapaiNoel.FIM_DA_FE) {
                        criancasAindaAcrediamNoPapai = false;
                        descrenca.release();
                    }
                }

                // Para entregar os brinquedos, as renas precisam se arreadas no trenó
                treno.await();

                // A entrega é imediata
                Thread.sleep(generator.nextInt(20));

                // "desamarrar" pode usar a mesma barrier que "amarrar",
                // porque a barreira e ciclica
                rena = treno.await();
                if (rena == PapaiNoel.ULTIMA_RENA) {
                    atencaoDoPapai.release();
                    System.out.println("*** Os brinquedos foram entregues ***");
                }
            } catch (InterruptedException e) {
                // thread interrupted for program cleanup
            } catch (BrokenBarrierException e) {
                // another thread in the barrier was interrupted
            }
        }
        System.out.println("Rena " + id + " se aposenta!");
    }
}

