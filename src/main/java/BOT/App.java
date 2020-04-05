package BOT;

import BOT.Listener.Listener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

public class App {

    public static class ServerInfo {
        public String ip;
        public String port;
        public String player;
    }

    private static List<JsonObject> jsonObjects;

    private final Random random = new Random();
    public static List<JsonObject> jsonObjectList;

    public App() {
        WebUtils.setUserAgent("Chrome 75.0.3770.100 Shr4ky SCP Server / kirito5572#5572");

        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                .setColor(getRandomColor())
                .setFooter("Made By kirito5572#5572", null)
        );
        Logger logger = LoggerFactory.getLogger(App.class);
        String data = get();
        if(!data.equals("error")) {
            jsonObjectList = new Gson().fromJson(new JsonParser().parse(data), new TypeToken<ArrayList<JsonObject>>(){}.getType());
        }
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                String data = get();
                if(!data.equals("error")) {
                    jsonObjectList = new Gson().fromJson(new JsonParser().parse(data), new TypeToken<ArrayList<JsonObject>>(){}.getType());
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 10000);

        try {
            File file = new File("config.json");
            FileReader fileReader = new FileReader(file);
            jsonObjects = new Gson().fromJson(new JsonReader(fileReader), new TypeToken<ArrayList<JsonObject>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            logger.info("부팅");
            for(JsonObject jsonObject : jsonObjects) {
                Listener listener = new Listener(jsonObject);
                JDA jda = JDABuilder.createDefault(jsonObject.get("TOKEN").getAsString())
                        .setAutoReconnect(true)
                        .addEventListeners(listener)
                        .build().awaitReady();
                logger.info("부팅완료");
            }
        } catch (LoginException | InterruptedException e) {

            StackTraceElement[] eStackTrace = e.getStackTrace();
            StringBuilder a = new StringBuilder();
            for (StackTraceElement stackTraceElement : eStackTrace) {
                a.append(stackTraceElement).append("\n");
            }
            logger.warn(a.toString());
        }

    }

    private Color getRandomColor() {
        float r = random.nextFloat();
        float g = random.nextFloat();
        float b = random.nextFloat();

        return new Color(r, g, b);
    }

    public static void main(String[] args) {
        new App();
    }

    private String get() {
        try {
            HttpClient client = HttpClientBuilder.create().build(); // HttpClient 생성
            HttpGet getRequest = new HttpGet("https://api.scpslgame.com/lobbylist.php?format=json"); //GET 메소드 URL 생성

            HttpResponse response = client.execute(getRequest);

            //Response 출력
            if (response.getStatusLine().getStatusCode() == 200) {
                ResponseHandler<String> handler = new BasicResponseHandler();
                return handler.handleResponse(response);
            } else {
                return "error";
            }

        } catch (Exception e) {
            return "error";
        }
    }

    public static ServerInfo serverInfo(String ip, String port) {
        ServerInfo serverInfo = new ServerInfo();
        String address;
        try {
            InetAddress Address = InetAddress.getByName(ip);
            address = Address.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            address = "error";
        }
        serverInfo.ip = address;
        serverInfo.port = port;
        boolean serverEnable = false;
        for(JsonObject jsonObject : jsonObjectList) {
            if(jsonObject.get("ip").getAsString().equals(address)) {
                if(jsonObject.get("port").getAsString().equals(port)) {
                    serverEnable = true;
                    serverInfo.player = jsonObject.get("players").getAsString();
                }
            }
        }
        if(!serverEnable) {
            serverInfo.player = "not open";
        }
        if(serverInfo.player.startsWith("0/")) {
            serverInfo.player = "just open";
        }
        return serverInfo;
    }

}
