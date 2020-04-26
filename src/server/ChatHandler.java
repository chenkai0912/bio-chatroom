package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * 负责处理消息
 */
public class ChatHandler implements Runnable {

    private ChatServer chatServer;
    //建立连接的客户端socket
    private Socket socket;

    public ChatHandler(ChatServer chatServer,Socket socket){
        this.chatServer = chatServer;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //存储新上线客户
            chatServer.addClient(socket);

            //读取用户发送的消息,获取输入流
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //不停的读取客户端的消息
            String msg = null;
            while ((msg = reader.readLine())!=null) {
                String fwdMsg = "客户端["+socket.getPort()+"]:"+msg+"\n";
                System.out.print(fwdMsg);

                //将消息转发给其他在线用户
                //消息后面加换行符，以便客户端使用readline方法可以获取到消息
                chatServer.forwardMessage(socket,fwdMsg);

                //检查客户端是否发送了退出消息
                if(chatServer.readyToQuit(msg)){
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                chatServer.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
