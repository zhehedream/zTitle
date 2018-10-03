package zhehe.Title.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.title.Title;

import com.google.inject.Inject;

import zhehe.Title.config.*;

@Plugin(id = ZTitle.PLUGIN_ID, name = ZTitle.PLUGIN_NAME, version = ZTitle.PLUGIN_VERSION, authors = "zhehe")
public class ZTitle {
	public static final String PLUGIN_ID = "ztitle";
	public static final String PLUGIN_NAME = "ZTitle";
	public static final String PLUGIN_VERSION = "1.0";
	
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	
	@Inject
	private Logger logger;
	
	@Inject
	private static ZTitle instance;

	
	@Inject
	private Game game;
	    
    private Task task = null;
    
    public Path getconfigDir() {
    	return configDir;
    }
    public void SendTerminalMessage(String str) {
    	logger.info(str);
    }
    
    
	@Listener
	public void onGamePreInitializationEvent(GamePreInitializationEvent e) {
		instance = this;
		Config.getConfig().init(instance);
		
		logger.info("zhehe's Title good to go!");
		// Create Configuration Directory for CustomPlayerCount
		if (!Files.exists(configDir)) {
			try {
				Files.createDirectories(configDir);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		Config.getConfig().init_config();
		logger.info("[ZTitle] Config has been loaded.");
		
		CommandSpec randomMessageCommandSpec = CommandSpec.builder()
			    .description(Text.of("Send Random Message Title to every one"))
			    .permission("ztitle.send_rnd_cmd")
			    .executor(new RandomMessageCommand())
			    .build();

		Sponge.getCommandManager().register(instance, randomMessageCommandSpec, "send_random_title", "snt");

		CommandSpec sendTitleCommandSpec = CommandSpec.builder()
			    .description(Text.of("Send Title to player"))
			    .permission("ztitle.send_title")
			    .arguments(
			    		GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
			    		GenericArguments.remainingJoinedStrings(Text.of("message"))
			    )
			    .executor(new SendTitleCommand())
			    .build();

		Sponge.getCommandManager().register(instance, sendTitleCommandSpec, "sendtitle", "st");

	}
	
	public class RandomMessageCommand implements CommandExecutor {

	    @Override
	    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	        ZTitle.instance.sendRandomMessage();
	    	src.sendMessage(Text.of("Random Message has been sent."));
	        return CommandResult.success();
	    }
	}
	
	public class SendTitleCommand implements CommandExecutor {

	    @Override
	    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	    	Player player = args.<Player>getOne("player").get();
	    	String title = args.<String>getOne("message").get();	    	
			Title built = Title.builder().title(TextSerializers.FORMATTING_CODE.deserialize(title))
					.build();
			player.sendTitle(built);
						
	        return CommandResult.success();
	    }
	}
	
	

	
	@Listener
	public void onReload(GameReloadEvent e) {
		Config.getConfig().init_config();
	}
	
	
	public void randomMessageBuilder() {
		if(task != null) task.cancel();
		int delay = Config.getConfig().getDelay();
		if(delay == 0) {
			task = null;
			return;
		}
		task = Task.builder().execute(() -> sendRandomMessage())
	    .async().delay(delay, TimeUnit.SECONDS).interval(delay, TimeUnit.SECONDS)
	    .submit(this);
	}
	
	public void sendRandomMessage() {
		ArrayList<Player> players = new ArrayList<>(game.getServer().getOnlinePlayers());
		String[] out = {null, null};
		Config config = Config.getConfig();
		config.getRandomMessage(out);
		int fadeIn = config.getrFadeIn();
		int stayTime = config.getrStay();
		int fadeOut = config.getrFadeOut();
		if(out[0] == null) return;
		if(out[0] == "" && out[1] == "") return;
		
		
		for(int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			Title built = Title.builder().title(Title_Format(out[0], p)).subtitle(Title_Format(out[1], p)).fadeIn(fadeIn)
					.stay(stayTime).fadeOut(fadeOut).build();
			p.sendTitle(built);
		}
	}

    
	@Listener
	public void onClientConnectionEventJoin(ClientConnectionEvent.Join e) {
		Player p = e.getTargetEntity();
		Config config = Config.getConfig();
		String title = config.getTitle();
		String subTitle = config.getSubTitle();
		int fadeIn = config.getFadeIn();
		int stayTime = config.getStayTime();
		int fadeOut = config.getFadeOut();
		String Ftitle = config.getTitleFirst();
		String FsubTitle = config.getSubTitleFirst();
		int FfadeIn = config.getFadeInFirst();
		int FstayTime = config.getStayTimeFirst();
		int FfadeOut = config.getFadeOutFirst();
		
		
		if (p.hasPermission("ztitle.show")) {
			if (p.hasPlayedBefore()) {
				// ReJoin
				Title built = Title.builder().title(Title_Format(title, p)).subtitle(Title_Format(subTitle, p)).fadeIn(fadeIn)
						.stay(stayTime).fadeOut(fadeOut).build();
				p.sendTitle(built);
			} else {
				// First Join
				Title built = Title.builder().title(Title_Format(Ftitle, p)).subtitle(Title_Format(FsubTitle, p)).fadeIn(FfadeIn)
						.stay(FstayTime).fadeOut(FfadeOut).build();
				p.sendTitle(built);
			}
		}
	}
    
    private Text Title_Format(String s, Player p) {
    	s = s.replace("{player_name}", p.getName());
    	int current_time = (int) (p.getWorld().getProperties().getWorldTime() % 24000L);
    	current_time = current_time /1000;
    	s = s.replace("{dimension_time}", Integer.toString(current_time));
    	return TextSerializers.FORMATTING_CODE.deserialize(s);
    }

}
