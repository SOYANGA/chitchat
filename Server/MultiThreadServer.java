package Server;


import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: ChatRoomOP
 * @Description:多线程版的服务器，服务器通过线程池调度多条服务器线程来实现多客户端的信息往来，其中用Map来存放客户信息（保证客户端的唯一性）
 * @Author: SOYANGA
 * @Create: 2019-01-13 17:06
 * @Version 1.0
 */


public class MultiThreadServer {
    //用户信息
    private static Map<String, Socket> clientInfo = new ConcurrentHashMap();
    public static int num = 0;

    private static class ExecuteClientRequest implements Runnable {
        private Socket client;

        public ExecuteClientRequest(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                //获取服务器的输入(收取客户端发来的信息)
                Scanner in = new Scanner(client.getInputStream());
                PrintStream out = new PrintStream(client.getOutputStream(), true, "UTF-8");
                while (true) {
                    String msgFromclient = "";
                    if (in.hasNext()) {
                        msgFromclient = in.nextLine();
                        //正则表达式（确定一个同一格式）Windows下的字符处理
                        msgFromclient = getRigntMsg(msgFromclient);
                        //注册流程
                        //userName:String类型 eg userName:SOYANGA
                        if (msgFromclient.startsWith("userName")) {
                            String userName = msgFromclient.split("\\:")[1];
                            if (userName.contains("临时用户")) {
                                out.println("用户名非法，请重新注册！");
                                continue;
                            }
                            registeredUser(userName, client);
                            continue;
                        }
                        //群聊流程
                        //eg: G:hello
                        if (msgFromclient.startsWith("G:")) {

                            String groupMsg = msgFromclient.split("\\:")[1];
                            groupChat(groupMsg);
                            continue;
                        }
                        //私聊流程
                        //P:SOYANGA-Hello,this is a private msg
                        if (msgFromclient.startsWith("P:")) {
                            if (getUserName(client).contains("临时用户")) {
                                out.println("您当前是临时用户，无法进行私聊");
                                continue;
                            }
                            String userName = msgFromclient.split("\\:")[1].split("\\-")[0];
                            String privateMsg = msgFromclient.split("\\:")[1].split("\\-")[1];
                            privateChat(userName, privateMsg);
                            continue;
                        }
                        //退出
                        //SOYANGA:byebye
                        if (msgFromclient.contains("byebye")) {
                            String userName = msgFromclient.split("\\:")[0];
                            if (isOnline(userName)) {
                                userQuit(userName);
                                out.close();
                                in.close();
                                break;
                            }
                            out.println("退出失败，请输入本客户端的用户名");
                        }
                        if (!clientInfo.containsValue(client)) {
                            out.println("当前客户端未注册！，请注册。临时用户只可使用群聊功能！");
                            while (true) {
                                String msg = getRigntMsg(in.next());
                                if (msg.equals("Y")) {
                                    String userName = "临时用户" + num;
                                    registeredUser(userName, client);
                                    break;
                                }
                            }
                            continue;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("服务器通信异常，异常为：" + e);
            }
        }

        //注册流程
        private void registeredUser(String userName, Socket client) {
            //处理要注册的临时用户
            if (getUserName(client).contains("临时用户")) {
                clientInfo.remove(getUserName(client));
            }
            //普通用户，即临时用户注册流程
            clientInfo.put(userName, client);
            System.out.println("用户" + userName + "注册成功！");
            int clientNumber = clientInfo.size();
            System.out.println("当前聊天室共有" + clientNumber + "人");
            try {
                PrintStream out = new PrintStream(client.getOutputStream(), true, "UTF-8");
                out.println("注册成功！");
                out.println("当前聊天室共有" + clientNumber + "人");
                groupChat("用户" + userName + "已上线！");
            } catch (IOException e) {
                System.err.println("注册流程出异常，异常为：" + e);
            }
        }

        //群聊流程
        private void groupChat(String groupMsg) {
            //取出map存储的所有客户端的Socket信息，并将群聊消息遍历发送
            Set<Map.Entry<String, Socket>> clientEntry = clientInfo.entrySet();
            Iterator<Map.Entry<String, Socket>> iterator = clientEntry.iterator();
            while (iterator.hasNext()) {
                //取出Socket
                Socket cilent = iterator.next().getValue();
                try {
                    PrintStream out = new PrintStream(cilent.getOutputStream(), true, "UTF-8");
                    out.println("发送的群聊消息为" + groupMsg);
                } catch (IOException e) {
                    System.err.println("群聊流程出异常，异常为:" + e);
                }
            }
        }

        //私聊流程
        private void privateChat(String userName, String privateMsg) {
            //取出userName对应的Socket
            Socket client = clientInfo.get(userName);
            try {
                PrintStream out = new PrintStream(client.getOutputStream(), true, "UTF-8");
                out.println("私聊信息为:" + privateMsg);
            } catch (IOException e) {
                System.err.println("私聊流程出异常，异常为:" + e);
            }
        }

        //用户退出流程
        private void userQuit(String userName) {
            clientInfo.remove(userName);
            System.out.println(userName + "已下线");
            System.out.println("当前聊天室共" + clientInfo.size() + "人");
            groupChat(userName + "已下线!");
        }

        //判断当前用户是否在线
        private boolean isOnline(String userNmae) {
            if (clientInfo.get(userNmae) != null && clientInfo.get(userNmae).equals(client)) {
                return true;
            }
            return false;
        }

        //由客户端的Socket找到userName
        private String getUserName(Socket client) {
            String userName = null;
            for (String key : clientInfo.keySet()) {
                if (clientInfo.get(key).equals(client)) {
                    userName = key;
                }
            }
            return userName;
        }

        //处理Windos下的字符串问题
        private String getRigntMsg(String msgFromclient) {
            Pattern pattern = Pattern.compile("\r");
            Matcher matcher = pattern.matcher(msgFromclient);
            msgFromclient = matcher.replaceAll("");
            return msgFromclient;
        }
    }


    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(6666);
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        System.out.println("等待用户连接...");
        for (int i = 0; i < 20; i++) {
            Socket client = serverSocket.accept();
            System.out.println("有新用户连接，端口号为：" + client.getPort());
            ExecuteClientRequest executeClientRequest = new ExecuteClientRequest(client);
            executorService.submit(executeClientRequest);
        }
        executorService.shutdown();
        serverSocket.close();
    }
}
