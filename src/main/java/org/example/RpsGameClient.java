package org.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.rps.Move;
import org.example.rps.PlayRequest;
import org.example.rps.PlayResponse;
import org.example.rps.RpsGameGrpc;

import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class RpsGameClient {
    private final ManagedChannel channel;
    private final RpsGameGrpc.RpsGameBlockingStub blockingStub;

    public RpsGameClient(String host, int port) {
        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext() // sem TLS, vida loka em ambiente de teste
                .build();
        this.blockingStub = RpsGameGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void playRound(Move move) {
        PlayRequest request = PlayRequest.newBuilder()
                .setPlayerMove(move)
                .build();

        PlayResponse response = blockingStub.play(request);

        System.out.println("Você jogou:    " + move);
        System.out.println("Servidor jogou: " + response.getServerMove());
        System.out.println("Resultado:     " + response.getResult());
        System.out.println("---------------------------------------");
    }

    private static Move parseMove(String input) {
        String s = input.toLowerCase(Locale.ROOT).trim();
        return switch (s) {
            case "r", "rock", "pedra" -> Move.ROCK;
            case "p", "paper", "papel" -> Move.PAPER;
            case "s", "scissors", "tesoura" -> Move.SCISSORS;
            default -> Move.MOVE_UNSPECIFIED;
        };
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 50051;

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        RpsGameClient client = new RpsGameClient(host, port);
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Cliente RPS conectado a " + host + ":" + port);
            System.out.println("Digite sua jogada: (pedra/papel/tesoura) ou 'sair'");

            while (true) {
                System.out.print("Sua jogada > ");
                String line = scanner.nextLine();
                if (line == null) break;

                line = line.trim();
                if (line.equalsIgnoreCase("sair") || line.equalsIgnoreCase("exit")) {
                    break;
                }

                Move move = parseMove(line);
                if (move == Move.MOVE_UNSPECIFIED) {
                    System.out.println("Jogada inválida. Use pedra, papel ou tesoura.");
                    continue;
                }

                client.playRound(move);
            }
        } finally {
            client.shutdown();
        }
    }

}
