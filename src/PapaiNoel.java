import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class PapaiNoel {
    /*
    O 'volatile' é um modificador que garante que o valor do atributo esteja sempre
    disponível para outras threads, sendo gravado na memória principal assim que atualizado.
    Desse modo, evitando que ocorram leitura/escrita de dados inconsistentes. Para simulação do
    algoritmo é criado a variável 'criancasAindaAcrediamNoPapai' que gera um loop e permite que renas
    e elfos atuem durante um determinado tempo.
     */
    private volatile boolean criancasAindaAcrediamNoPapai = true;

    private Semaphore descrenca = new Semaphore(0);

    // Determina o fim da fé no Papai Noel, ou seja, quando o algoritmo para de executar
    final static int FIM_DA_FE = 2025;

    /*
    Determina o ano incial do qual o algoritmo começará a executar.
    Esse valor será incrementado durante a execução até alcançar o FIM_DA_FE.
     */
    static AtomicInteger ano = new AtomicInteger(2020);

    // Determina o número de Renas
    private final static int NUMERO_DE_RENAS = 9;

    // Determina o número de Elfos
    private final static int NUMERO_DE_ELFOS = 10;

    // Determina o número mínimo de Elfos que podem acordar o Papai Noel
    private final static int ELFOS_PRECISANDO_ACORDAR_PAPAI = 3;

    static int ULTIMA_RENA = 0;
    static int TERCEIRO_ELFO = 0;

    public PapaiNoel() {
        /*
        O semáforo abaixo evita que um segundo grupo de elfos ou as renas em espera
        chamem a atenção do Papai Noel. Enfim, é um semáforo justo que permite que
        apenas uma das condições de chamada de atenção do Papai Noel sejam atendidas.
         */
        Semaphore atencaoDoPapai = new Semaphore(1, true);

        /*
        Criação de um semáforo justo para enfileirar até 3 elfos
         */
        Semaphore filaDeElfos = new Semaphore(ELFOS_PRECISANDO_ACORDAR_PAPAI, true);

        /*
        O 'CyclicBarrier' é um sincronizador que permite que conjunto de
        threads esperem umas pelas outras em um determinado ponto.
        O 'tresElfos' é criado para que os 3 elfos se juntem para
        acodar o Papai Noel
         */
        CyclicBarrier tresElfos = new CyclicBarrier(ELFOS_PRECISANDO_ACORDAR_PAPAI, new Mensagem("--- " + ELFOS_PRECISANDO_ACORDAR_PAPAI + " elfos estão acordando o Papai Noel ---"));

        /*
        O 'elfosEstaoInspirados' é criado para que os elfos (threads)
        aguardem até que possam retornar ao trabalho
         */
        CyclicBarrier elfosEstaoInspirados = new CyclicBarrier(ELFOS_PRECISANDO_ACORDAR_PAPAI, new Mensagem("--- Os elfos retornaram ao trabalho ---"));

        /*
        O 'todasRenas' é criado para que as renas se juntem e esperem
        até poder acordar o Papai Noel.
         */
        CyclicBarrier todasRenas = new CyclicBarrier(NUMERO_DE_RENAS, () -> System.out.println("*** As renas estão se reunindo para o natal de " + ano + " ***"));

        /*
        O 'treno' (trenó) é criado para acomodar as Renas após elas
        acordarem o Papai Noel e serem arreadas ao trenó
         */
        CyclicBarrier treno = new CyclicBarrier(NUMERO_DE_RENAS, new Arreios());

        /*
        Lista de threads
         */
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUMERO_DE_ELFOS; ++i)
            threads.add(new Thread(new Elfo(i, criancasAindaAcrediamNoPapai, filaDeElfos, tresElfos, elfosEstaoInspirados, atencaoDoPapai)));

        for (int i = 0; i < NUMERO_DE_RENAS; ++i)
            threads.add(new Thread(new Rena(i, criancasAindaAcrediamNoPapai, todasRenas, treno, atencaoDoPapai, descrenca)));

        System.out.println("Era uma vez o ano de " + ano + " :");

        // As threads são inciadas
        for (Thread t : threads)
            t.start();

        try {
            // espera até !criancasAindaAcrediamNoPapai
            descrenca.acquire();

            System.out.println("As crianças não acreditam mais no Papai Noel");

            for (Thread t : threads)
                t.interrupt();

            for (Thread t : threads)
                t.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("O Papai Noel desapareceu no mundo!");
    }
}
