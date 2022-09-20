import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    private static final Map<SocketChannel, ByteBuffer> sockets = new HashMap<>();
    private static Selector selector;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static MyLogger logger = new MyLogger("C:\\Users\\maria\\OneDrive\\Рабочий стол\\NetworkChat\\src\\main\\resources\\serverLog.txt");


    public static void main(String[] args) throws IOException {
        ConfigParser parser = new ConfigParser();

        final ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(parser.getHost(), parser.getPort()));
        serverChannel.configureBlocking(false);

        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        logger.log("Server start");
        try {
            serverStart(serverChannel);
        } catch (IOException err) {
            System.out.println(err.getMessage());
        } finally {
            serverChannel.close();
        }
    }

    private static void serverStart(ServerSocketChannel serverChannel) throws IOException {
        while (true) {
            selector.select();
            for (SelectionKey key : selector.selectedKeys()) {
                keyCheck(key, serverChannel);
            }
            selector.selectedKeys().clear();
        }
    }

    private static void keyCheck(SelectionKey key, ServerSocketChannel serverChannel) {
        if (key.isValid()) {
            try {
                if (key.isAcceptable()) {
                    accept(serverChannel);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            } catch (IOException e) {
                logger.log("error " + e.getMessage());
            }
        }
    }

    private static void accept(ServerSocketChannel serverChannel) throws IOException {
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        logger.log("Connected " + socketChannel.getRemoteAddress());
        sockets.put(socketChannel, ByteBuffer.allocate(1000));
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private static void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = sockets.get(socketChannel);
        int bytesRead = socketChannel.read(buffer);
        logger.log("Reading from " + socketChannel.getRemoteAddress() + ", bytes read=" + bytesRead);


        if (bytesRead == -1) {
            logger.log("Connection closed " + socketChannel.getRemoteAddress());
            sockets.remove(socketChannel);
            socketChannel.close();
        }

        logger.log(new String(buffer.array(), 0, buffer.position()) + sdf.format(new Date(System.currentTimeMillis())));

        if (bytesRead > 0) {
            socketChannel.register(selector, SelectionKey.OP_WRITE);
        }
    }

    private static void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = sockets.get(socketChannel);

        buffer.flip();
        String clientMessage = new String(buffer.array(), 0, buffer.limit());

        String response = clientMessage + " " + sdf.format(new Date(System.currentTimeMillis())) + "\r\n";

        buffer.clear();
        buffer.put(ByteBuffer.wrap(response.getBytes()));
        buffer.flip();

        for(SocketChannel channel : sockets.keySet()){
            int bytesWritten = channel.write(buffer);
            logger.log("Writing to " + channel.getRemoteAddress() + ", bytes writteb=" + bytesWritten);
            if (!buffer.hasRemaining()) {
                buffer.compact();
                channel.register(selector, SelectionKey.OP_READ);
            }
        }
    }
}
