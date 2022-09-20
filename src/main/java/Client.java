import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static String name;

    public static void main(String[] args) {
        SocketChannel socketChannel;
        setName();

        try {
            socketChannel = connectToServer();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        Thread readThread = new Thread(() -> readMsg(socketChannel));
        Thread writeThread = new Thread(() -> writeMsg(socketChannel));
        readThread.start();
        writeThread.start();
        try {
            readThread.join();
            writeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static SocketChannel connectToServer() throws IOException {
        ConfigParser parser = new ConfigParser();

        InetSocketAddress socketAddress = new InetSocketAddress(parser.getHost(), parser.getPort());
        final SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(socketAddress);

        return socketChannel;
    }

    private static void setName() {
        Scanner in = new Scanner(System.in);

        System.out.println("Please, enter your name: ");
        name = in.nextLine();
    }

    private static void writeMsg(SocketChannel socketChannel) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String text = scanner.nextLine();
                String msg = name + ": " + text;
                if(text.isEmpty())
                    continue;
                if (msg.equals("/exit")) {
                    socketChannel.close();
                    break;
                }
                socketChannel.write(ByteBuffer.wrap(
                        msg.getBytes(StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readMsg(SocketChannel socketChannel) {
        MyLogger logger = new MyLogger("C:\\Users\\maria\\OneDrive\\Рабочий стол\\NetworkChat\\src\\main\\resources\\clientLog.txt");

        ByteBuffer inputBuffer = ByteBuffer.allocate(1000);
        while (true) {
            int bytesCount;
            try {
                bytesCount = socketChannel.read(inputBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if(bytesCount <= -1)
                return;

            logger.log(new String(inputBuffer.array(), 0, bytesCount, StandardCharsets.UTF_8).trim());
            
            inputBuffer.clear();
        }
    }
}
