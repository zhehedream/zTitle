package zhehe.Title.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import zhehe.Title.main.ZTitle;

public class Config {
	private static Config config = new Config();
	
	private Config() {
		
	}
	
	public static Config getConfig() {
		return config;
	}
	
	private ZTitle ztitle;
		
	private Path configFile;
	private Path knowledgeFile;
	
	ConfigurationLoader<CommentedConfigurationNode> loader;
	CommentedConfigurationNode rootNode;
	
	public void init(ZTitle in) {
		ztitle = in;
		Path configDir = ztitle.getconfigDir();
		configFile = Paths.get(configDir + "/config.txt");
		knowledgeFile = Paths.get(configDir + "/knowledge.txt");
		loader = HoconConfigurationLoader.builder().setPath(configFile).build();
	}
	
	
	class KnowledgeNode {
		public String Title = "", subTitle = "";
		public KnowledgeNode() {;}
		public KnowledgeNode(String t, String s) {
			Title = t;
			subTitle = s;
		}
	}
	
	
	ArrayList<KnowledgeNode> knowledge;
	Random rand = new Random();
	
	public void getRandomMessage(String[] out) {
		int len = knowledge.size();
		if(len == 0) {
			out[0] = null;
			return;
		}
		int random = rand.nextInt(len);
		try {
			KnowledgeNode node = knowledge.get(random);
			out[0] = node.Title;
			out[1] = node.subTitle;	
		} catch (Exception e) {
			out[0] = null;
		}
	}
	
	public void init_config() {
		init_knowledge();
		ztitle.SendTerminalMessage("Try to read the ZTitle config file.");
		if (!Files.exists(configFile)) {
			try {
				ztitle.SendTerminalMessage("Could not find a valid config file, so will create one.");
				Files.createFile(configFile);
				load_config();
				build();
				save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			load_config();
		}
		
	}
	
	public void init_knowledge() {
		if (!Files.exists(knowledgeFile)) {
			try {
				Files.createFile(knowledgeFile);
				FileWriter writer = new FileWriter(knowledgeFile.toString());
				BufferedWriter bw = new BufferedWriter(writer);
				
				bw.write("#This is the random message file.\n");
				bw.write("\n");
				bw.write("+Title\n");
				bw.write("-Sub Title\n");
				bw.write("\n");
				bw.write("+Title2\n");
				bw.write("+Title3\n");
				
				bw.close();
				writer.close();
				
				load_knowledge();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			load_knowledge();
		}
	}
	
	public void load_knowledge() {
		try {
            FileReader reader = new FileReader(knowledgeFile.toString());
            BufferedReader br = new BufferedReader(reader);
            knowledge = new ArrayList<>();
            
            String str = null, lastLine = null;
            while((str = br.readLine()) != null) {
            	if(str.length() == 0) continue;
            	char c = str.charAt(0);
            	if(c != '+' && c != '-') continue;
            	
            	if(c == '+') {
            		if(lastLine != null) knowledge.add(new KnowledgeNode(lastLine, ""));
            		lastLine = str;
            	} else {
            		knowledge.add(new KnowledgeNode(lastLine, str));
            		lastLine = null;
            	}
            }
            br.close();
            reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load_config() {
		try {
			rootNode = loader.load();

			switch (rootNode.getNode("ConfigVersion").getInt()) {
				case 1: {
					// current version
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void build() {
		rootNode.getNode("ConfigVersion").setValue(1).setComment("Config file version. Do not change it!!");
		rootNode.getNode("firstjoin").setComment("Message on first join");
		rootNode.getNode("firstjoin", "broadcast").setValue(false).setComment("Broadcast the message");
		rootNode.getNode("firstjoin", "title").setValue("&eMinecraft").setComment("Title message");
		rootNode.getNode("firstjoin", "subtitle").setValue("&aWelcome {player_name}!").setComment("Subtitle message");
		rootNode.getNode("firstjoin", "fadein").setValue(15).setComment("Fade in time in TICKS (20 ticks = 1 sec)");
		rootNode.getNode("firstjoin", "staytime").setValue(50).setComment("Stay on screen time in TICKS");
		rootNode.getNode("firstjoin", "fadeout").setValue(15).setComment("Fade out time in TICKS");
		
		rootNode.getNode("rejoin").setComment("Message on rejoin");
		rootNode.getNode("rejoin", "broadcast").setValue(false).setComment("Broadcast the message");
		rootNode.getNode("rejoin", "title").setValue("&eMinecraft").setComment("Title message");
		rootNode.getNode("rejoin", "subtitle").setValue("&aWelcome back {player_name}!").setComment("Subtitle message");
		rootNode.getNode("rejoin", "fadein").setValue(15).setComment("Fade in time in TICKS (20 ticks = 1 sec)");
		rootNode.getNode("rejoin", "staytime").setValue(50).setComment("Stay on screen time in TICKS");
		rootNode.getNode("rejoin", "fadeout").setValue(15).setComment("Fade out time in TICKS");
		
		rootNode.getNode("randommessage").setComment("Random message on server");
		rootNode.getNode("randommessage", "delay").setValue(1800).setComment("Delay in seconds to send random message to every one. 0 means disable.");
		rootNode.getNode("randommessage", "fadein").setValue(15).setComment("Fade in time in TICKS (20 ticks = 1 sec)");
		rootNode.getNode("randommessage", "staytime").setValue(50).setComment("Stay on screen time in TICKS");
		rootNode.getNode("randommessage", "fadeout").setValue(15).setComment("Fade out time in TICKS");
	}
	
	public void save() {
		try {
			loader.save(rootNode);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getTitleFirst() {
		return rootNode.getNode("firstjoin", "title").getString();
	}
	
	public String getTitle() {
		return rootNode.getNode("rejoin", "title").getString();
	}
	
	public String getSubTitleFirst() {
		return rootNode.getNode("firstjoin", "subtitle").getString();
	}
	
	public String getSubTitle() {
		return rootNode.getNode("rejoin", "subtitle").getString();
	}
	
	public int getFadeInFirst() {
		return rootNode.getNode("firstjoin", "fadein").getInt();
	}
	
	public int getFadeIn() {
		return rootNode.getNode("rejoin", "fadein").getInt();
	}
	
	public int getStayTimeFirst() {
		return rootNode.getNode("firstjoin", "staytime").getInt();
	}
	
	public int getStayTime() {
		return rootNode.getNode("rejoin", "staytime").getInt();
	}
	
	public int getFadeOutFirst() {
		return rootNode.getNode("firstjoin", "fadeout").getInt();
	}
	
	public int getFadeOut() {
		return rootNode.getNode("rejoin", "fadeout").getInt();
	}
	
	public int getDelay() {
		return rootNode.getNode("randommessage", "delay").getInt();
	}
	
	public int getrFadeIn() {
		return rootNode.getNode("randommessage", "fadein").getInt();
	}
	
	public int getrStay() {
		return rootNode.getNode("randommessage", "staytime").getInt();
	}
	
	public int getrFadeOut() {
		return rootNode.getNode("randommessage", "fadeout").getInt();
	}
}
