import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;

import java.util.Scanner;

public class Server extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();

        server.requestHandler(request -> {

            HttpServerResponse response = request.response();
            response.putHeader("content-type", "text/plain");

            response.end("Hello World!");
            printResult(request.params().get("param"));
        });
        server.listen(8000);
    }

    public void printResult(String comment) {
        DeliveryOptions optionsD = new DeliveryOptions();
        optionsD.setSendTimeout(51000);
        vertx.eventBus().send("address", comment, optionsD,  (r) ->{
            if (r.succeeded()){
                System.out.println(r.result().body().toString());
            } else {
                System.out.println("redirect " + comment);
                printResult(comment);
            }
        });
    }

    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions(), (event ->
                event.result().deployVerticle(new Server())));
    }
}

