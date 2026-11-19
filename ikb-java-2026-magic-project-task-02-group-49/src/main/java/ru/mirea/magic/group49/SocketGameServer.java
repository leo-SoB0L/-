package ru.mirea.magic.group49;

import ru.mirea.magic.card.Student;
import ru.mirea.magic.gamelogic.GameDisplayContext;
import ru.mirea.magic.gamelogic.GameServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Простой игровой сервер: принимает подключения, читает объект игрока и отвечает приветствием.
 * Каждый клиент обслуживается в отдельном потоке.
 */
public class SocketGameServer implements GameServer {

    private final GameDisplayContext display;

    public SocketGameServer(GameDisplayContext display) {
        this.display = display;
    }

    @Override
    public void startServer() throws IOException {
        try (ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(NetSettings.PORT));
            server.setSoTimeout(500);
            display.displayText("Сервер запущен, порт " + NetSettings.PORT);

            while (!Thread.currentThread().isInterrupted()) {
                Socket client;
                try {
                    client = server.accept();
                } catch (SocketTimeoutException e) {
                    continue;
                }
                Thread handler = new Thread(() -> handleClient(client));
                handler.setDaemon(true);
                handler.start();
            }
        }
    }

    private void handleClient(Socket client) {
        try (client) {
            client.setSoTimeout(3000);
            try (ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                 DataOutputStream out = new DataOutputStream(client.getOutputStream())) {

                Object received = in.readObject();
                if (received instanceof Student student) {
                    display.displayText("Подключился игрок " + student.getFirstName()
                            + " (" + student.getCard().getName() + ")");
                    out.writeUTF("Добро пожаловать, " + student.getFirstName() + "!");
                } else {
                    out.writeUTF("Ожидался объект игрока");
                }
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            display.displayText("Ошибка обработки клиента: " + e.getMessage());
        }
    }
}
