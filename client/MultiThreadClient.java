package client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.Thread.sleep;

/**
 * @program: ChatRoomOP
 * @Description: 多线程版的客户端，用于模拟多个客户端共同连接服务器，并实现读写分离（群聊基础）
 * @Author: SOYANGA
 * @Create: 2019-01-13 14:57
 * @Version 1.0
 */
class ReceiveMsgFromSever implements Runnable {
    private Socket client;

    public ReceiveMsgFromSever(Socket client) {
        this.client = client;
    }

    Scanner in = null;

    @Override
    public void run() {

        try {
            in = new Scanner(client.getInputStream());
            String receiveMsg = "";
            while (true) {
                if (client.isClosed()) {
                    System.out.println("客户端已经退出，请重新登陆后再发送信息");
//                    in.close();
                    break;
                }
                if (in.hasNext()) {
                    receiveMsg = in.nextLine();
                    System.out.println("服务器发来信息:" + receiveMsg);
                }
            }
        } catch (IOException e) {
            System.err.println("客户端接收信息线程有异常，异常为" + e);
        } finally {
            in.close();
        }
    }
}

class SendMsgtoSever implements Runnable {
    private Socket client;


    public SendMsgtoSever(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        PrintStream out = null;
        try {
            //获取输出流向服务端发送信息
            out = new PrintStream(client.getOutputStream(),
                    true, "UTF-8");
            //获取用户输入
            while (true) {
                System.out.println("请输入要发送的内容");
                String SendMsg = "";
                if (scanner.hasNext()) {
                    SendMsg = scanner.nextLine();
                    out.println(SendMsg);
                    if (SendMsg.contains("byebye")) {
                        sleep(1500);
                        System.out.println("客户端退出聊天室");
//                        scanner.close();
//                        out.close();
                        client.close();
                        break;
                    }
                    sleep(1000);
                }
            }
            //异常处理
        } catch (Exception e) {
            System.err.println("客户端发送信息线程异常，异常为：" + e);
        } finally {
            out.close();
            scanner.close();
        }
    }
}


public class MultiThreadClient {
    public static void main(String[] args) throws Exception {
        Socket client = new Socket("127.0.0.1", 6666);
        //生成客户端的读写线程
        Thread sendThread = new Thread(new SendMsgtoSever(client));
        Thread receiveThread = new Thread(new ReceiveMsgFromSever(client));
        //启动客户端的读写线程
        sendThread.start();
        receiveThread.start();
    }

}
