package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 用户输入信息，接收服务器信息线程类
 */
public class UserInputHandler implements Runnable {

    private ChatClient chatClient;

    public UserInputHandler(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run() {
        try {
            //等待用户输入消息,包装为缓存Reader对象
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            //直到用户输入退出
            while (true) {
                //获取用户输入的消息
                String input = consoleReader.readLine();
                //发送给服务器
                chatClient.send(input);

                //检查用户是否退出
                if(chatClient.readyToQuit(input)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
