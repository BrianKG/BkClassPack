package com.bexxkie.bkcp.classes;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import com.bexxkie.bkcp.BkCP;
import com.bexxkie.bkcp.classes.interfaces.ClassBase;
import com.bexxkie.bkcp.classes.interfaces.EnergyUser;
import com.bexxkie.bkcp.classes.interfaces.Flier;
import com.bexxkie.bkcp.interaction.Glide;
import com.bexxkie.bkcp.modules.economy.interfaces.EconInterface;
import com.bexxkie.bkcp.util.RandInt;

public class Dragon 
extends ClassBase
implements EnergyUser, Flier,
EconInterface
{
	private double energy;
	private double energyCap;
	private int level;
	private double cool;
	private int fxp;
	private int saveTime;
	private int fxpTarget;
	private int pGol;
	private int pGem;
	private String pCur;
	private double maxHealth;
	private double regenNum;

	public int taskGlide;
	public int taskSave;
	public int taskCool;
	public int taskReEn;

	public Dragon(String name) 
	{
		super(name);
		this.classID = 6;
		//set in classAssign
		this.energyCap = BkCP.playerClass.getConfig().getDouble(this.getPlayer().getUniqueId()+".maxEnergy");
		this.energy = BkCP.playerClass.getConfig().getDouble(this.getPlayer().getUniqueId()+".energy");
		this.fxp = BkCP.playerClass.getConfig().getInt(this.getPlayer().getUniqueId()+".fxp");
		this.fxpTarget = BkCP.advCfg.getConfig().getInt("fxpTarget");
		this.prefix = BkCP.advCfg.getConfig().getString("Prefixes.Dragon.default").replaceAll("&", "�");
		this.level = BkCP.playerClass.getConfig().getInt(Dragon.this.getPlayer().getUniqueId()+".level");;
		this.saveTime = BkCP.advCfg.getConfig().getInt("classDataAutoSave");
		this.maxHealth = BkCP.advCfg.getConfig().getInt("Classes.Dragon.maxHealth");
		this.getPlayer().setMaxHealth(maxHealth);
		if (BkCP.config.getConfig().getBoolean("Module.Economy-Enabled"))
		{
			pGol = BkCP.playerClass.getConfig().getInt(this.getPlayer().getUniqueId().toString()+".currency.gold");
			pGem = BkCP.playerClass.getConfig().getInt(this.getPlayer().getUniqueId().toString()+".currency.gem");
			pCur = pGol+"::"+pGem;
		}
		this.refreshEnergy(6);
		this.showHud();
		this.saveAll();
		this.isGlide();
	}
	public String getClassNm()
	{
		return BkCP.playerClass.getConfig().getString(this.getPlayer().getUniqueId()+".class");
	}

	@SuppressWarnings("deprecation")
	public void showHud() 
	{
		if(Dragon.this.getPlayer().isOnline()&&Dragon.this.getPlayer()!=null&&this.getClassNm().equalsIgnoreCase("Dragon")){
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard drag = manager.getNewScoreboard();
			Objective drags = drag.registerNewObjective("Dragon", Dragon.this.getPlayer().getUniqueId().toString());

			drags.setDisplaySlot(DisplaySlot.SIDEBAR);
			//if(Dragon.this.getPlayer().getDisplayName().length() <31){
			//	drags.setDisplayName(Dragon.this.getPlayer().getDisplayName());
			//}else{
			//	drags.setDisplayName(Dragon.this.getPlayer().getName());
			//}
				drags.setDisplayName("Stats");

			if (BkCP.econEnabled==true)
			{
				Score score2 = drags.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "GLD: "+ChatColor.DARK_PURPLE + this.hudCur()));
				score2.setScore(-5);
			}
			Score sfxp = drags.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "FXP: "+ChatColor.AQUA +getFxp()));
			sfxp.setScore(-4);

			DecimalFormat numberFormat = new DecimalFormat("#.00");
			Score score2 = drags.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "ENP: "+ChatColor.AQUA + numberFormat.format(this.getEnergy()).toString()));
			score2.setScore(1);
			Score flt = drags.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "FLT: "+ChatColor.AQUA+ChatColor.STRIKETHROUGH +numberFormat.format(this.cool)));
			if(this.cool>0)
			{
				flt = drags.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "FLT: "+ChatColor.AQUA +numberFormat.format(this.cool/60)));	
			}
			flt.setScore(0);
			Score level = drags.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "ERG: "+ChatColor.AQUA +"+" + numberFormat.format(regenNum)+"/s"));
			level.setScore(-1);
			Dragon.this.getPlayer().setScoreboard(drag);
		}	

	}

	public void setFxp(int i)
	{
		if(this.fxp<this.fxpTarget)
		{
			this.fxp=this.fxp+i;
		}
		else
		{
			this.setLevel(this.getLevel()+1);
			this.fxp=0;
		}
	}

	public int getFxp()
	{
		return this.fxp;
	}


	public void coolDown()
	{
		taskCool = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BkCP.getInstance(), new Runnable()
		{
			public void run()
			{	
				if(Dragon.this.getPlayer()!=null)
				{
					if(Dragon.this.cool>0.0)
					{
						Dragon.this.setCool(Dragon.this.getCool()-1);
					}
					BkCP.playerClass.getConfig().set(Dragon.this.getPlayer().getUniqueId()+".ftime", Dragon.this.getCool());
					BkCP.playerClass.saveConfig();
					BkCP.playerClass.reloadConfig();
					Dragon.this.coolDown();
				}
			}
		},20l);
	}

	public void setCool(double i)
	{
		this.cool = i;
	}
	public double getCool()
	{
		return this.cool;
	}

	public double getEnergy() 
	{
		return this.energy;
	}

	public void setEnergy(Double newEnergy) 
	{
		this.energy = newEnergy;
		if(this.energy > energyCap)
		{
			this.energy = energyCap;
		}
		this.showHud();
	}

	public void refreshEnergy(final int classID) 
	{
		if ((getPlayer() != null)&&(getPlayer().isOnline())&&this.getClassNm().equalsIgnoreCase("Dragon")) {
			taskReEn = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BkCP.getInstance(), new Runnable()
			{
				@SuppressWarnings("deprecation")
				public void run()
				{
					if(Dragon.this.getPlayer()!=null){
						if(Dragon.this.getPlayer()!=null&&Dragon.this.getPlayer().getGameMode()!=GameMode.SURVIVAL)
						{
							Dragon.this.setEnergy(Dragon.this.getEnergy()+20);
						}
						if(Dragon.this.getPlayer()!=null&&Dragon.this.cool>=0.0||Dragon.this.getEnergy()>=0.0)
						{
							Dragon.this.getPlayer().setAllowFlight(true);
						}
						if(Dragon.this.getPlayer()!=null&&Dragon.this.getEnergy()<=0.0)
						{
							Dragon.this.getPlayer().setAllowFlight(false);
						}
						if(Dragon.this.getPlayer()!=null&&Dragon.this.getPlayer().getFireTicks()>0&&Dragon.this.getPlayer().isOnGround())
						{
							Dragon.this.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0));
							Dragon.this.setEnergy(Dragon.this.getEnergy()+(energyCap*0.050));
							regenNum = energyCap*0.050;
						}
						if(Dragon.this.getPlayer()!=null&&Dragon.this.getPlayer().isFlying())
						{
							Dragon.this.setEnergy(Dragon.this.getEnergy()-(Glide.lCost));
						}
						else 
						{
							if(Dragon.this.getPlayer()!=null&&Dragon.this.getPlayer().isOnGround())
							{
								Dragon.this.setEnergy(Dragon.this.getEnergy()+(energyCap*0.025));
								regenNum =energyCap*0.025;
							}
							if(Dragon.this.getPlayer()!=null&&!Dragon.this.getPlayer().isOnGround()&&Dragon.this.getEnergy()<20)
							{
								Dragon.this.setEnergy(Dragon.this.getEnergy()+(Glide.lCost/10));
								regenNum = energyCap*0.075;
							}
						}
						Dragon.this.refreshEnergy(classID);
						Dragon.this.showHud();
					}
				}
			},10L);
		}
	}
	public void setLevel(int i) 
	{
		this.level = i;
		if(level <10)
		{
			this.getPlayer().playSound(this.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, RandInt.randInt(0, 5));
			this.level = this.getLevel()+1;
			this.showHud();
			BkCP.playerClass.getConfig().set(this.getPlayer().getUniqueId().toString()+".level", this.getLevel());
			BkCP.playerClass.getConfig().set(this.getPlayer().getUniqueId().toString()+".maxEnergy", this.energyCap*1.1);
			BkCP.playerClass.getConfig().set(this.getPlayer().getUniqueId().toString()+".fxp", this.getFxp());
			BkCP.playerClass.saveConfig();
			BkCP.playerClass.reloadConfig();
			this.energyCap = BkCP.playerClass.getConfig().getDouble(this.getPlayer().getUniqueId().toString()+".maxEnergy");
		}else
		{
			this.getPlayer().sendMessage(BkCP.prefix+"You have trained as much as possible.");
		}
	}

	public int getLevel() {
		return this.level;
	}

	public int getCur(String type) 
	{
		switch(type.toLowerCase())
		{
		case "gold":
			return pGol;
		case "gem":
			return pGem;
		default:
			break;
		}
		return 0;
	}

	public void setCur(String type, int amount) 
	{
		switch(type.toLowerCase())
		{
		case "gold":
			pGol = amount;
			break;
		case "gem":
			pGem = amount;
			break;
		default:
			break;
		}
	}

	public String hudCur() {
		this.pCur = ChatColor.YELLOW+""+this.getCur("gold")+ChatColor.DARK_PURPLE+"::"+ChatColor.GREEN+this.getCur("gem");
		return this.pCur;

	}
	public void isGlide() 
	{
		taskGlide = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BkCP.getInstance(), new Runnable()
		{
			public void run()
			{
				if(Dragon.this.getPlayer()!=null){
					Player p = Dragon.this.getPlayer();
					if(BkCP.onlinePlayers.get(p.getName())instanceof Flier)
					{
						if (p.getVelocity().getY() < -.01&&p.isSneaking()){
							if(p.getLocation().getBlock().getRelative(BlockFace.DOWN, 2).getType().equals(Material.AIR)){
								if(((Flier)BkCP.onlinePlayers.get(p.getName())).getEnergy()>10){
									((Flier)BkCP.onlinePlayers.get(p.getName())).setEnergy(((Flier)BkCP.onlinePlayers.get(p.getName())).getEnergy()-1.1);
									((Flier)BkCP.onlinePlayers.get(p.getName())).showHud();
									Vector v = p.getVelocity();
									v.setY(v.getY()* -.1);
									Vector d = p.getLocation().getDirection();
									d.multiply(.1);
									p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 1, (float) (9*v.getY()+Math.random()));
									p.setVelocity(v);
									p.setVelocity(d);
									p.setFallDistance(0);
								}
								p.getWorld().playEffect(p.getLocation().getBlock().getRelative(BlockFace.DOWN,3).getLocation(), Effect.STEP_SOUND, p.getLocation().getBlock().getRelative(BlockFace.DOWN,3).getType());

							}
						}
					}
					Dragon.this.isGlide();
				}
			}
		},10);
	}
	public void saveAll() 
	{
		taskSave = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BkCP.getInstance(), new Runnable()
		{
			public void run()
			{
				if(Dragon.this.getPlayer()!=null){
					Player p = Dragon.this.getPlayer();
					p.sendMessage(BkCP.prefix+"autosaving classData.");
					BkCP.playerClass.getConfig().set(p.getUniqueId()+".energy", Dragon.this.getEnergy());
					BkCP.playerClass.getConfig().set(p.getUniqueId()+".level", Dragon.this.getLevel());
					BkCP.playerClass.getConfig().set(p.getUniqueId()+".fxp", Dragon.this.getFxp());
					BkCP.playerClass.saveConfig();
					BkCP.playerClass.reloadConfig();
					saveAll();
				}
			}
		},saveTime);

	}
	public void stop()
	{
		Bukkit.getScheduler().cancelTask(taskCool);
		Bukkit.getScheduler().cancelTask(taskGlide);
		Bukkit.getScheduler().cancelTask(taskReEn);
		Bukkit.getScheduler().cancelTask(taskSave);
	}

	//TODO unused
	public String getBranch() 
	{return null;}
	//TODO unused
	public void setBranch(String args) 
	{}
}
