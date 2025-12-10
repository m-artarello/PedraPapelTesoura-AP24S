package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Random;
import org.example.rps.Move;
import org.example.rps.PlayRequest;
import org.example.rps.PlayResponse;
import org.example.rps.RpsGameGrpc;

public class RpsGameServer {
    private final Server server;
    private final int port;

    public RpsGameServer(int port) {
        this.port = port;
        this.server = ServerBuilder
                .forPort(port)
                .addService(new RpsGameServiceImpl())
                .build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println("Servidor RPS gRPC rodando na porta " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Desligando servidor gRPC...");
            RpsGameServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        RpsGameServer server = new RpsGameServer(port);
        server.start();
        server.blockUntilShutdown();
    }

    static class RpsGameServiceImpl extends RpsGameGrpc.RpsGameImplBase {

        private final Random random = new Random();

        @Override
        public void play(PlayRequest request, StreamObserver<PlayResponse> responseObserver) {
            Move playerMove = request.getPlayerMove();
            Move serverMove = randomMove();

            String result = evaluate(playerMove, serverMove);

            PlayResponse response = PlayResponse.newBuilder()
                    .setServerMove(serverMove)
                    .setResult(result)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        private Move randomMove() {
            int r = random.nextInt(3);
            switch (r) {
                case 0:
                    return Move.ROCK;
                case 1:
                    return Move.PAPER;
                case 2:
                default:
                    return Move.SCISSORS;
            }
        }

        private String evaluate(Move player, Move server) {
            if (player == Move.MOVE_UNSPECIFIED) {
                return "JOGADA INVÁLIDA";
            }

            if (player == server) {
                return "EMPATE";
            }

            // Regras:
            // ROCK vence SCISSORS
            // PAPER vence ROCK
            // SCISSORS vence PAPER
            switch (player) {
                case ROCK:
                    return (server == Move.SCISSORS) ? "VITÓRIA" : "DERROTA";
                case PAPER:
                    return (server == Move.ROCK) ? "VITÓRIA" : "DERROTA";
                case SCISSORS:
                    return (server == Move.PAPER) ? "VITÓRIA" : "DERROTA";
                default:
                    return "RESULTADO INDEFINIDO";
            }
        }
    }

}
