package ru.mirea.magic.group49;

import ru.mirea.magic.card.Student;
import ru.mirea.magic.gamelogic.GameDisplayContext;
import ru.mirea.magic.gamelogic.NetClient;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Клиент: подключается к серверу, отправляет объект игрока и выводит ответ сервера.
 */
public class SocketNetClient implements NetClient {

    private static final int TIMEOUT_MS = 3000;

    private final GameDisplayContext display;

    public SocketNetClient(GameDisplayContext display) {
        this.display = display;
    }

    @Override
    public void connectToServer(String address, Student player) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, NetSettings.PORT), TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(player);
            out.flush();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            display.displayText("Ответ сервера: " + in.readUTF());
        }
    }
}
