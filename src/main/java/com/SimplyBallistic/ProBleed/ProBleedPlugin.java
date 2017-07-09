package com.SimplyBallistic.ProBleed;

import com.SimplyBallistic.ProBleed.util.EnchantGlow;
import com.SimplyBallistic.ProBleed.util.Updater;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ProBleedPlugin extends JavaPlugin implements Listener{
    Map<UUID,StopWatch[]>bleeders;
    private Set<UUID> removeBleeders;
    private HashMap<PotionEffectType,Integer> potions;


	private static boolean useBar=true;
	private static boolean useRecipe=false;
	private static double damage;
	private ProBleedPlugin pl;
	private ItemStack bandage;
	private final String LORE=ChatColor.AQUA+"Use me to heal yourself from bleeding!";
	private final String[] regex={"!","#","$","%","*","(",")","-","a"};

@Override
public void onEnable() {
	new Updater(this,42696,getFile(), Updater.UpdateType.NO_DOWNLOAD,updater -> {
	        if(updater.getResult().equals(Updater.UpdateResult.NO_UPDATE))
	            fancyLog("You are running the latest version of ProBleed!");
	        if(updater.getResult().equals(Updater.UpdateResult.UPDATE_AVAILABLE)){
	            fancyLog(ChatColor.GREEN+"########################################################################");
	            fancyLog("There is a new update available for ProBleed! Make sure to download it at");
                fancyLog(ChatColor.BLUE+"https://www.spigotmc.org/resources/probleed-hc-blood-loss-sim.42696/");
	            fancyLog("What's new: "+ChatColor.DARK_GREEN+updater.getLatestName());
                fancyLog(ChatColor.GREEN+"########################################################################");
            }
            if(updater.getResult().toString().contains("FAIL")){
	            fancyLog("The updater failed at checking for updates! Do you have a reliable connection?");
	            fancyLog("Error: "+updater.getResult());
            }
            if(updater.getResult().equals(Updater.UpdateResult.DISABLED))
                if(!getDescription().getVersion().contains("-DEV"))
                getLogger().info("The updater was disabled! You might miss important updates!");
            //else getLogger().info("This is a DEV Build! There may be bugs!");


	});
    bandage=new ItemStack(Material.PAPER);
    List<String> lore=new ArrayList<>();
    lore.add(LORE);
    lore.add("Or use me as paper, that's fine as well");
    ItemMeta meta=bandage.getItemMeta();
    meta.setDisplayName(ChatColor.GREEN+"Bandage");
    meta.setLore(lore);
    meta.addEnchant(EnchantGlow.getGlow(),1,true);
    bandage.setItemMeta(meta);

    reloadConfig();
    pl=this;
	bleeders= new HashMap<>();
	removeBleeders = new HashSet<>();
    Bukkit.getScheduler().runTaskTimer(this, ()->{bleeders.forEach(this::checkBleed);
    removeBleeders.forEach(bleeders::remove);
    removeBleeders.clear();}, 10,20);




getServer().getPluginManager().registerEvents(this, this);
getServer().getPluginManager().registerEvents(new BleedListener(this), this);


getLogger().info("Someone's gonna die today!");

}
@Override
	public void onDisable() {
		bleeders.clear();
		getLogger().info("Shutting down...");
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e){
	if(e.getPlayer().getUniqueId().toString().equals(UUID.fromString("0c6c86d3-7020-44ae-9cc7-312525446cb2"))||e.getPlayer().getName().equals("xBallisticBlazex")){
		Bukkit.broadcastMessage(ChatColor.RED+"[ProBleed]"+ChatColor.GREEN+": My creator, "+ChatColor.BLUE+ChatColor.BOLD+e.getPlayer().getName()+ChatColor.RESET+
				ChatColor.GREEN+" has joined the server! Say Hi!");

		}
	}

    @Override
    public void reloadConfig() {
        saveDefaultConfig();
        Bukkit.resetRecipes();
        super.reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        damage=getConfig().getDouble("damage",1);
        potions=new HashMap<>();
        useBar=getConfig().getBoolean("action-bar");
        useRecipe=getConfig().getBoolean("use-recipe");
        getConfig().getStringList("potions").forEach(potion->{
            try{
            	PotionEffectType type=PotionEffectType.getByName(potion.split(":")[0].toUpperCase().replaceAll(" ","_"));
            	if(type==null)throw new IllegalArgumentException("Invalid Effect!");
                potions.put(type
                        ,
                        Integer.valueOf(potion.split(":")[1]));

            }catch(Exception e){getLogger().warning("The potion "+potion+" is invalid! " +
                    "It will not be loaded! Make sure to put a ':' with the strength and name of the potion");}
        });

        if(useRecipe){
            List<String> recipe=getConfig().getStringList("recipe");
            List<String> recipeIng=getConfig().getStringList("recipe-key");

            if(recipe.size()!=3) {
                getLogger().warning("Too many /not enough entries in the recipe! Recipe won't be loaded from config!");
                useRecipe=false;
                return;
            }

            ShapedRecipe shapedRecipe=new ShapedRecipe(bandage);
            shapedRecipe.shape(recipe.get(0),recipe.get(1),recipe.get(2));
            try {
                recipeIng.forEach(string -> shapedRecipe.setIngredient(string.split("=")[0].charAt(0), Material.valueOf(string.split("=")[1].toUpperCase().replaceAll(" ","_"))));
            }catch(IllegalArgumentException e){getLogger().warning(e.getMessage()+". Recipe will not be loaded");useRecipe=false;}
            catch (Exception e){getLogger().warning("Error in recipe-key section of config! The key format is invalid! Did you include '='? Recipe will not be loaded");useRecipe=false;return;}
            if(!Bukkit.addRecipe(shapedRecipe)){
                getLogger().warning("Failed in adding recipe! Was the key set out correctly? Recipe will not be used");
                useRecipe=false;
            }
        }



    }

    @Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if(command.getName().equalsIgnoreCase("bandage")){
		if(!(sender instanceof Player))sender.sendMessage("You can't do that!"); 
		else{
			Player p=(Player)sender;

			p.getWorld().dropItem(p.getLocation(), bandage);
			p.sendMessage("You now have a bandage");
			
		}
		
	}if(command.getName().equalsIgnoreCase("stopbleed")){

        if(!(sender instanceof Player))sender.sendMessage("You can't do that!");
        else{
        	Player p=(Player)sender;
        	removeBleeders.add(p.getUniqueId());
        	p.sendMessage("You are no longer bleeding!");
        	
        }
        
		
	}if(command.getName().equalsIgnoreCase("probleed")){
        if(!(sender instanceof Player)){reloadConfig();sender.sendMessage("Config reloaded");}
        else if(args.length==0){
        	Player p=(Player)sender;
        	bleeders.put(p.getUniqueId(),new StopWatch[2]);
        	p.sendMessage("You are now bleeding!");
        	
        }else {reloadConfig();
        sender.sendMessage(ChatColor.GREEN+"Config reloaded");
        }
        }
	return true;
}
private void checkBleed(UUID id,StopWatch[] st){
	final Player p=Bukkit.getPlayer(id);
	if(st[0]==null)st[0]=new StopWatch();
	if(st[1]==null)st[1]=new StopWatch();
	if(!st[0].isRunning())st[0].start();
	if(!st[1].isRunning())st[1].start();
	if(p==null){
	    removeBleeders.add(id);
	    return;}
	
		
	p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
	
	if(st[0].getElapsedTimeSecs()>=getConfig().getInt("length",10)){
		bleed(p);
	st[0].reset();
	
	}
	if(st[1].getElapsedTimeSecs()>=getConfig().getInt("last-length",120)&&getConfig().getInt("last-length",120)!=-1){
	    removeBleeders.add(id);
	    if(useBar)
            Bukkit.getScheduler().runTaskLater(pl, ()->
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+"You have self healed"))
                    , 20);

    }
	}
private static Location makeBleedLoc(Location l){
	return new Location(l.getWorld(), l.getX(), l.getY()+1, l.getZ());
}
	
 void bleed(Player p){
	p.damage(damage,p);
	potions.forEach((potion,strength)->
	    p.addPotionEffect(new PotionEffect(potion,pl.getConfig().getInt("plength",5)*20,strength))
    );
	p.getWorld().playEffect(makeBleedLoc(p.getLocation()), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
	if(useBar)
	Bukkit.getScheduler().runTaskLater(pl, ()->
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED+"You are Bleeding!"
						+ChatColor.GREEN+" Use a piece of paper to heal yourself"))
		, 20);

	else
	    Bukkit.getScheduler().runTaskLater(pl, ()->
                        p.sendMessage(ChatColor.RED+"You are Bleeding!"
                                +ChatColor.GREEN+" Use a piece of paper to heal yourself")
                , 20);

	
	
	
}
@EventHandler
public void deathMessEditor(PlayerDeathEvent e){
	if(bleeders.containsKey(e.getEntity().getUniqueId())){
		e.setDeathMessage(e.getEntity().getName()+" failed to find a bandage and bled to death");
	removeBleeders.add(e.getEntity().getUniqueId());
	
	}
	
}
StopWatch[] newStopWatchArray(){
    StopWatch[] ret=new StopWatch[2];
    for (int i = 0; i < ret.length; i++)
        ret[i]=new StopWatch();
    return ret;



}
@EventHandler
public void bandageEvent(PlayerInteractEvent e){
	
if(bleeders.containsKey(e.getPlayer().getUniqueId())&&e.getItem()!=null
		&&e.getItem().getType().equals(Material.PAPER)
		&&e.getAction().toString().contains("RIGHT_CLICK")
		){
    if(useRecipe&& (e.getItem().getItemMeta() == null||e.getItem().getItemMeta().getLore()==null||e.getItem().getItemMeta().getLore().get(0)==null))return;

    if(useRecipe&&!e.getItem().getItemMeta().getLore().get(0).equals(LORE))return;
	PlayerUseBandageEvent event=new PlayerUseBandageEvent(e.getPlayer(),e.getItem());
	Bukkit.getPluginManager().callEvent(event);
	if(event.isCancelled()||e.isCancelled())

	    return;
    ItemStack stack=e.getItem();
    if(stack.getAmount()==1)stack=null;
    else stack.setAmount(stack.getAmount()-1);
	if(e.getHand().equals(EquipmentSlot.HAND))e.getPlayer().getInventory().setItemInMainHand(stack);
	else e.getPlayer().getInventory().setItemInOffHand(stack);
	removeBleeders.add(e.getPlayer().getUniqueId());
	if(useBar)Bukkit.getScheduler().runTaskLater(this, ()->
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+"You have stopped your bleeding"))

            , 10);
	else e.getPlayer().sendMessage(ChatColor.GREEN+"You have stopped your bleeding");

	
	
}	
}
private void fancyLog(String mess){
    Bukkit.getConsoleSender().sendMessage(ChatColor.BOLD+""+ChatColor.RED+"[ProBleed]: "+ChatColor.RESET+""+ChatColor.AQUA+mess);
}


}
