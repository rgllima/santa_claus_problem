## Problema 11 - Des. Software Concorrente.
### Santa Claus Problem

```text
Rafael Lima - 399509
Josué Nicholson - 399715
```
O problema do Papai Noel. Desenvolva um algoritmo para simular o seguinte sistema: Papai Noel dorme no Pólo Norte até ser acordado por todas as nove renas ou por um grupo de três entre dez elfos. Ele então realiza uma de duas ações indivisíveis: se acordado pelo grupo de renas, o Papai Noel os atrela a um trenó, entrega os brinquedos e, finalmente, desamarra a rena que sai de férias. Se acordado por um grupo de elfos, o Papai Noel os mostra em seu escritório, consulta-os sobre P&D de brinquedos e, finalmente, mostra-os para que possam voltar a trabalhar na construção de brinquedos. Um grupo de renas em espera deve ser servido pelo Papai Noel antes de um grupo de elfos em espera. Uma vez que o tempo do Papai Noel é extremamente valioso, organizar as renas ou elfos em um grupo não deve ser feito pelo Papai Noel.


### Códigos em Java

Classe Principal
```java
public class Main {
    public static void main(String[] args) {
        new PapaiNoel();
    }
}
```
Classe Papai Noel
```java
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
```

Classe Elfo
```java
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
```

Classe Rena
```java
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
```

Classe Mensagem
```java
public class Mensagem implements Runnable {
    String msg;

    Mensagem(String msg) {
        this.msg = msg;
    }

    public void run() {
        System.out.println(msg);
    }
}
```

Classe Arreios
```java
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
```
Exemplo de Execução do Código
```text
Era uma vez o ano de 2020 :
Elfo0 ficou sem ideias.
Elfo9 ficou sem ideias.
Elfo5 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo0 ficou inspirado
Elfo9 ficou inspirado
Elfo5 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo6 ficou sem ideias.
Elfo3 ficou sem ideias.
Elfo7 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo6 ficou inspirado
*** As renas estão se reunindo para o natal de 2020 ***
Elfo7 ficou inspirado
Elfo3 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo5 ficou sem ideias.
Elfo8 ficou sem ideias.
*** Entregar os presentes de Natal de 2020 ***
*** Todas as renas foram arreadas! ***
*** Todas as renas estão de volta ao estábulo! ***
*** Os brinquedos foram entregues ***
Elfo0 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo8 ficou inspirado
Elfo5 ficou inspirado
Elfo0 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo4 ficou sem ideias.
Elfo2 ficou sem ideias.
Elfo1 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo1 ficou inspirado
Elfo2 ficou inspirado
Elfo4 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo6 ficou sem ideias.
Elfo7 ficou sem ideias.
Elfo9 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo9 ficou inspirado
*** As renas estão se reunindo para o natal de 2021 ***
Elfo7 ficou inspirado
Elfo6 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo3 ficou sem ideias.
Elfo0 ficou sem ideias.
*** Entregar os presentes de Natal de 2021 ***
*** Todas as renas foram arreadas! ***
*** Todas as renas estão de volta ao estábulo! ***
*** Os brinquedos foram entregues ***
Elfo8 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo3 ficou inspirado
Elfo8 ficou inspirado
Elfo0 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo2 ficou sem ideias.
Elfo1 ficou sem ideias.
Elfo5 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo1 ficou inspirado
*** As renas estão se reunindo para o natal de 2022 ***
Elfo5 ficou inspirado
Elfo2 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo6 ficou sem ideias.
Elfo4 ficou sem ideias.
*** Entregar os presentes de Natal de 2022 ***
Elfo7 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
*** Todas as renas foram arreadas! ***
*** Todas as renas estão de volta ao estábulo! ***
*** Os brinquedos foram entregues ***
Elfo4 ficou inspirado
Elfo7 ficou inspirado
Elfo6 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo9 ficou sem ideias.
Elfo3 ficou sem ideias.
Elfo0 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo0 ficou inspirado
Elfo9 ficou inspirado
Elfo3 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo5 ficou sem ideias.
Elfo8 ficou sem ideias.
Elfo6 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo8 ficou inspirado
Elfo5 ficou inspirado
Elfo6 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo4 ficou sem ideias.
Elfo2 ficou sem ideias.
Elfo8 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo8 ficou inspirado
*** As renas estão se reunindo para o natal de 2023 ***
Elfo4 ficou inspirado
Elfo2 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo6 ficou sem ideias.
*** Entregar os presentes de Natal de 2023 ***
*** Todas as renas foram arreadas! ***
*** Todas as renas estão de volta ao estábulo! ***
*** Os brinquedos foram entregues ***
Elfo5 ficou sem ideias.
Elfo1 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo6 ficou inspirado
Elfo5 ficou inspirado
Elfo1 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo3 ficou sem ideias.
Elfo7 ficou sem ideias.
Elfo2 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo3 ficou inspirado
Elfo2 ficou inspirado
Elfo7 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo4 ficou sem ideias.
Elfo0 ficou sem ideias.
Elfo9 ficou sem ideias.
--- 3 elfos estão acordando o Papai Noel ---
Elfo0 ficou inspirado
*** As renas estão se reunindo para o natal de 2024 ***
Elfo9 ficou inspirado
Elfo4 ficou inspirado
--- Os elfos retornaram ao trabalho ---
Elfo1 ficou sem ideias.
Elfo5 ficou sem ideias.
*** Entregar os presentes de Natal de 2024 ***
*** Todas as renas foram arreadas! ***
As crianças não acreditam mais no Papai Noel
Elfo 2 se aposenta!
Elfo 0 se aposenta!
Elfo 6 se aposenta!
Elfo 7 se aposenta!
Elfo 3 se aposenta!
Elfo 9 se aposenta!
Elfo 1 se aposenta!
Rena 7 se aposenta!
Elfo 8 se aposenta!
Elfo 4 se aposenta!
Elfo 5 se aposenta!
```
