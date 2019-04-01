import io.vertx.core.*;
import io.vertx.core.eventbus.Message;

import java.util.Scanner;
import java.util.concurrent.*;

public class Client extends AbstractVerticle {
    @Override
    public void start()  {
        try {
            vertx.eventBus().consumer("address").handler((e) -> {
                vertx.executeBlocking(future -> {
                    System.out.println("its ok? " + e.body());
                    String result = input();

                    future.complete(result);
                }, res -> {
                    if (res.result() != "") {
                        e.reply(e.body() + " " + res.result());
                        System.out.println(e.body() + " " + res.result());
                    }
                    else {
                        System.out.println(e.body() + " не обработано");
                    }
                });
            });
        } catch (VertxException e) {
            System.out.println("Error");
            throw new VertxException(e);
        }
    }

    private String input() {
        FutureTask<String> readNextLine = new FutureTask<String>(() -> {
            String scanner = new Scanner(System.in).nextLine();
            return scanner;
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(readNextLine);
        String token = "";
        try {
            token = readNextLine.get(50000L, TimeUnit.MILLISECONDS);
            executor.shutdown();
        } catch (TimeoutException e) {
            readNextLine.cancel(true);
            executor.shutdownNow();
            executor.shutdown();
            System.out.println("Выполнение потока прервано" + System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Выполнение потока прервано" + System.currentTimeMillis());
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.out.println("Выполнение потока прервано 2" + System.currentTimeMillis());
        }
        executor.shutdown();
        return token;
    }

    public static void main(String[] args){
        Vertx.clusteredVertx(new VertxOptions(), (event ->
                event.result().deployVerticle(new Client(), new DeploymentOptions().setWorker(false))));
    }
}


