package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private final int DEFAULT_PORT = 8888;
    private final String QUIT = "quit";

    //声明线程池
    private ExecutorService executorService;

    private ServerSocket serverSocket;
    //保存当前在线的客户端消息
    //用端口标识在线客户端id
    //key为端口值，value为向这个客户端写消息的Writer对象
    private Map<Integer, Writer> connectedClients;

    public ChatServer() {
        //创建固定线程数量的线程池
        executorService = Executors.newFixedThreadPool(10);
        connectedClients = new HashMap<>();
    }

    //添加在线的客户端以群发
    public synchronized void addClient(Socket socket) throws IOException {
        if(socket!=null){
            //获取客户端端口
            int clientPort = socket.getPort();
            //创建写消息的writer
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connectedClients.put(clientPort,writer);
            System.out.println("客户端["+socket.getPort()+"]已连接到服务器");
        }

    }
    //剔除已下线的客户端
    public synchronized void removeClient(Socket socket) throws IOException {
        if(socket!=null) {
            int clientPort = socket.getPort();
            //检查当前客户端是否存在
            if(connectedClients.containsKey(clientPort)) {
                //获取对应writer对象，关闭
                connectedClients.get(clientPort).close();
            }
            //将客户端对应的Writer对象移除
            connectedClients.remove(clientPort);
            System.out.println("客户端["+socket.getPort()+"]已断开服务器连接");
        }
    }

    //转发到其他客户端
    public synchronized void forwardMessage(Socket socket,String fwdMsg) throws IOException {
        //遍历所有在线客户端，转发消息
        for(Integer id:connectedClients.keySet()) {
            //如果是发送消息的客户端，不转发给自己消息了
            //加非，如果不是当前客户端，则转发消息
            if(!id.equals(socket.getPort())){
                //获取该客户端对应的writer对象
                Writer currentClientWriter = connectedClients.get(id);
                currentClientWriter.write(fwdMsg);
                currentClientWriter.flush();
            }
        }
    }

    //检查客户端是否退出
    public boolean readyToQuit(String msg){
        return QUIT.equalsIgnoreCase(msg);
    }

    //关闭ServerSocket
    public synchronized void close(){
        if(serverSocket!=null){
            try {
                serverSocket.close();
                System.out.println("关闭ServerSocket.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //启动服务器端
    public void start() {

        try {
            //绑定监听端口
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("服务器启动，监听端口:"+DEFAULT_PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                //创建ChatHandler线程,传入chatServer和当前客户端socket
                //new Thread(new ChatHandler(this,socket)).start();

                //从线程池中获取线程,传入线程要执行的对象
                executorService.execute(new ChatHandler(this,socket));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    //启动类
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
