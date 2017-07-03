package com.SimplyBallistic.ProBleed;

import java.util.Random;

import com.google.common.base.Stopwatch;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class BleedListener implements Listener {
	private final ProBleedPlugin pl;
	BleedListener(ProBleedPlugin p) {
	this.pl=p;
	}
	@EventHandler
	public void BleedEvent(EntityDamageEvent e){
	if(!(e.getEntity() instanceof LivingEntity)||(e.getEntity() instanceof ArmorStand))return;
		LivingEntity en=(LivingEntity)e.getEntity();
	
	en.getWorld().playEffect(en.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
	if(en instanceof Player){
		
		Player p=(Player)en;
		if(pl.bleeders.containsKey(p.getUniqueId()))return;
		if(new Random().nextFloat()<=pl.getConfig().getDouble("chance", 0.05)&&!(en.hasPermission("probleed.bypass"))){
			StopWatch s=new StopWatch();
			s.start();

			pl.bleeders.put(p.getUniqueId(), pl.newStopWatchArray());
			pl.bleed(p);
			
			}
		
		
		
	}
	
		
		
		
		
	}

}
