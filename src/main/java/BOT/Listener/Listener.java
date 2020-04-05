package BOT.Listener;

import BOT.App;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Timer;
import java.util.TimerTask;

public class Listener extends ListenerAdapter {
    private String ip;
    private String port;

    public Listener(JsonObject jsonObject) {
        this.ip = jsonObject.get("IP").getAsString();
        this.port = jsonObject.get("PORT").getAsString();
    }
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                App.ServerInfo serverInfo = App.serverInfo(ip, port);
                String activityString;
                if(serverInfo.player.equals("not open")) {
                    activityString = "서버 준비중";
                } else {
                    activityString = serverInfo.player;
                }
                event.getJDA().getPresence().setActivity(Activity.playing(activityString));
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,0, 10000);
    }
}
