package me.dbizzzle.SkyrimRPG;

import me.dbizzzle.SkyrimRPG.Skill.Skill;
import me.dbizzzle.SkyrimRPG.Skill.SkillManager;
import me.dbizzzle.SkyrimRPG.Spell;
import org.bukkit.command.CommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkyrimCmd implements CommandExecutor
{
	private SpellManager sm;
	private SkyrimRPG plugin;
	private ConfigManager cm;
	public SkillManager sk;
	public SkyrimCmd(SpellManager sm, SkyrimRPG plugin, ConfigManager cm, SkillManager sk)
	{
		this.sm = sm;
		this.sk = sk;
		this.cm = cm;
		this.plugin = plugin;
	}
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		Player player = null;
		if(sender instanceof Player)player = (Player)sender;
		if (command.getName().equalsIgnoreCase("bindspell")) {
			if (player == null) 
			{
				sender.sendMessage(ChatColor.RED + "Console can not use magic.");
			} 
			else if (player.hasPermission("skyrimrpg.bindspell")) 
			{
				if(args.length != 2)
				{
					player.sendMessage(ChatColor.RED + "Usage: /bindspell <left/right> <spell>");
				}
				else
				{
					int mode = 1;
					if(args[0].equalsIgnoreCase("left"))mode = 1;
					else if(args[0].equalsIgnoreCase("right"))mode = 2;
					else if(args[0].equalsIgnoreCase("both"))mode = 3;
					if(args[1].equalsIgnoreCase("none"))
					{
						if(mode == 1 ||mode == 3)plugin.getSpellManager().bindLeft(player, null);
						if(mode == 2 ||mode == 3)plugin.getSpellManager().bindRight(player, null);
						if(mode == 3)player.sendMessage(ChatColor.GREEN + "Spell bindings removed from both hands");
						else player.sendMessage(ChatColor.GREEN + "Spell bindings removed from " + (mode == 1 ? "left" : "right") + " hand");
						return true;
					}
					Spell s = null;
					try{s = Spell.valueOf(args[1].toUpperCase());}
					catch(IllegalArgumentException iae){if(s == null)sender.sendMessage("No such spell!");return true;}
					if(!sm.hasSpell(player, s))
					{
						player.sendMessage(ChatColor.RED + "You have not yet learned this spell!");
						return true;
					}
					if(plugin.getConfigManager().useSpellPermissions && !player.hasPermission("skyrimrpg.spells.*"))
					{
						if(!player.hasPermission("skyrimrpg.spells." + s.getDisplayName()))
						{
							player.sendMessage(ChatColor.RED + "You are not allowed to use this spell!");return true;
						}
					}
					else if(s.getPartner() != null)
					{
						plugin.getSpellManager().bindLeft(player, s);
						plugin.getSpellManager().bindRight(player, s.getPartner());
					}
					else
					{
						if(mode == 1 ||mode == 3)plugin.getSpellManager().bindLeft(player, s);
						if(mode == 2 ||mode == 3)plugin.getSpellManager().bindRight(player, s);
					}
					if(mode != 3)player.sendMessage(ChatColor.GREEN + s.toString() + " bound to " + (mode == 1 ? "left" : "right") + " hand");
					else player.sendMessage(ChatColor.GREEN + s.toString() + " bound to both hands");
				}
			}
			else
			{
				player.sendMessage("You aren't allowed to bind spells.");
			}
		}
		else if (command.getName().equalsIgnoreCase("addspell")) 
		{
			if(!sender.hasPermission("skyrimrpg.addspell"))return msgret(sender, ChatColor.RED + "You do not have permission to do this!");
			if(args.length != 2 && args.length != 1)
			{
				if(player == null)sender.sendMessage(ChatColor.RED + "Usage: /addspell <player> <spell>");
				else sender.sendMessage(ChatColor.RED + "Usage: /addspell [player] <spell>");
				return true;
			}
			if(args.length == 1)
			{
				if(player == null)return noConsole(sender);
				Spell s = null;
				try{s = Spell.valueOf(args[0].toUpperCase());}catch(IllegalArgumentException iae){return msgret(sender, ChatColor.RED + "No such spell: " + args[0] + "!");}
				if(sm.hasSpell(player, s))return msgret(sender, ChatColor.RED + "You already have the spell " + s.toString() + "!");
				sm.addSpell(player, s);
				player.sendMessage(ChatColor.GREEN + "You have given " + ChatColor.BLUE + s.toString() + ChatColor.GREEN + " to yourself");
			}
			else if(args.length == 2)
			{
				Player t = sender.getServer().getPlayer(args[0]);
				if(t == null)return msgret(sender, ChatColor.RED + "No such player: " + args[0] + "!");
				Spell s = null;
				try{s = Spell.valueOf(args[1].toUpperCase());}catch(IllegalArgumentException iae){return msgret(sender, ChatColor.RED + "No such spell: " + args[1] + "!");}
				if(sm.hasSpell(t, s))return msgret(sender, ChatColor.RED + "\"" + t.getName() + "\" already has the spell " + s.toString() + "!");
				sm.addSpell(t, s);
				sender.sendMessage(ChatColor.GREEN + t.getName() + " has been given the spell " + s.toString());
				t.sendMessage(ChatColor.GREEN + "You have been given the spell " + s.toString());
			}
		}
		else if (command.getName().equalsIgnoreCase("removespell")) 
		{
			if(!sender.hasPermission("skyrimrpg.removespell"))return msgret(sender, ChatColor.RED + "You do not have permission to do this!");
			if(args.length != 2 && args.length != 1)
			{
				if(player == null)sender.sendMessage(ChatColor.RED + "Usage: /removespell <player> <spell>");
				else sender.sendMessage(ChatColor.RED + "Usage: /removespell [player] <spell>");
				return true;
			}
			if(args.length == 1)
			{
				if(player == null)return noConsole(sender);
				Spell s = null;
				try{s = Spell.valueOf(args[0].toUpperCase());}catch(IllegalArgumentException iae){return msgret(sender, ChatColor.RED + "No such spell: " + args[0] + "!");}
				if(!sm.hasSpell(player, s))return msgret(sender, ChatColor.RED + "You don't have the spell " + s.toString() + "!");
				sm.removeSpell(player, s);
				player.sendMessage(ChatColor.GREEN + "You have removed " + ChatColor.BLUE + s.toString() + ChatColor.GREEN + " from yourself");
			}
			else if(args.length == 2)
			{
				Player t = sender.getServer().getPlayer(args[0]);
				if(t == null)return msgret(sender, ChatColor.RED + "No such player: " + args[0] + "!");
				Spell s = null;
				try{s = Spell.valueOf(args[1].toUpperCase());}catch(IllegalArgumentException iae){return msgret(sender, ChatColor.RED + "No such spell: " + args[1] + "!");}
				if(!sm.hasSpell(t, s))return msgret(sender, ChatColor.RED + "Doesn't have the spell " + s.toString() + "!");
				sm.addSpell(t, s);
				sender.sendMessage(ChatColor.GREEN + t.getName() + " has had the spell " + s.toString() + " taken away");
				t.sendMessage(ChatColor.GREEN + "You have had the spell " + s.toString() + " removed from you");
			}
		}
		else if (command.getName().equalsIgnoreCase("listspells")) 
		{
			if (player == null) 
			{
				if (args.length == 0) 
				{
					sender.sendMessage("You don't have any spells");
				} 
				else if (args.length > 0) 
				{
					Player spell = plugin.getServer().getPlayer(args[0]);
					if (spell != null) 
					{
						plugin.log.info("[SkyrimRPG]Spells " + spell.getName() + " know");
						for(Spell s:sm.getSpells(spell))
						{
							plugin.log.info("[SkyrimRPG]" + s.getDisplayName());
						}
					} 
					else 
					{
						sender.sendMessage(ChatColor.RED + args[0] + " is currently not available or not online.");
					}
				}
			} 
			else if(player.hasPermission("skyrimrpg.listspells"))
			{
				if (args.length == 0) 
				{
					player.sendMessage(ChatColor.BLUE + "Spells you know");
					for(Spell s:sm.getSpells(player))
					{
						player.sendMessage(ChatColor.GREEN + s.getDisplayName());
					}
				} 
				else if (args.length > 0) 
				{
					Player spell = player.getServer().getPlayer(args[0]);
					if (spell != null) 
					{
						player.sendMessage(ChatColor.BLUE + "Spells " + spell.getName() + " knows");
						for(Spell s:sm.getSpells(spell))
						{
							player.sendMessage(ChatColor.GREEN + s.getDisplayName());
						}
					} 
					else 
					{
						player.sendMessage(ChatColor.RED + args[0] + " is currently not available or not online.");
					}
				}
			} 
			else
			{
				player.sendMessage(ChatColor.RED + "You don't have permissions to use this command.");
			}
		}
		else if(command.getName().equalsIgnoreCase("skyrimrpg") || label.equalsIgnoreCase("srpg"))
		{
			if(player == null)
			{
				System.out.println("[SkyrimRPG]SkyrimRPG version " + plugin.getDescription().getVersion());
				System.out.println("[SkyrimRPG]Made by dbizzle, ThatBox and Technius");
			}
			else
			{
				switch(args.length)
				{
				case 0:
					player.sendMessage(ChatColor.YELLOW + "SkyrimRPG version " + plugin.getDescription().getVersion());
					player.sendMessage(ChatColor.GREEN + "Made by dbizzle and Technius");
					if(plugin.getVersionManager().compareVersion(plugin.latestversion, plugin.getDescription().getVersion())&& player.hasPermission("skyrimrpg.newversion"))
					{
						if(plugin.latestversion.indexOf("DEV") > -1 && !cm.announceDevBuild);
						else player.sendMessage(ChatColor.RED + "!!!!" + ChatColor.GOLD + "A new " + (plugin.latestversion.indexOf("DEV") > -1 ? "dev build" : "release") 
								+ " is available: " + plugin.latestversion + ChatColor.RED + "!!!!");
					}
					player.sendMessage("========================");
					player.sendMessage(ChatColor.RED + "/skystats <page>" + ChatColor.YELLOW + " - displays your stats");
					if(player.hasPermission("skyrimrpg.setlevel"))player.sendMessage(ChatColor.RED + "/skyrimrpg setlevel <skill> <level>" + ChatColor.YELLOW + " - sets the level of the specified skill");
					if(player.hasPermission("skyrimrpg.reload"))player.sendMessage(ChatColor.RED + "/skyrimrpg reload" + ChatColor.YELLOW + " - reloads the configuration file");
					if(player.hasPermission("skyrimrpg.refresh"))player.sendMessage(ChatColor.RED + "/skyrimrpg refresh" + ChatColor.YELLOW + " - refreshes the configuration file by adding new values, useful when updating");
					player.sendMessage(ChatColor.RED + "/perk" + ChatColor.YELLOW + " - shows the perk menu");
					break;
				case 3:
					if(args[0].equalsIgnoreCase("setlevel"))
					{
						if(!player.hasPermission("skyrimrpg.setlevel"))
						{
							player.sendMessage(ChatColor.RED + "You don't have permission to do this");
							return true;
						}
						Skill skill;
						try
						{
							skill = Skill.valueOf(args[1].toUpperCase());
						}
						catch(IllegalArgumentException iae)
						{
							player.sendMessage(ChatColor.RED + "No such skill!");
							return true;
						}
						int l = plugin.getSkillManager().getSkillLevel(skill, player);
						try{ l = Integer.parseInt(args[2]);}catch(NumberFormatException nfe){sender.sendMessage(ChatColor.RED + "That is not a valid number."); return true;}
						sk.setLevel(skill, player, l);
						player.sendMessage(ChatColor.GREEN + skill.getName() + " set to level " + l);
						return true;
					}
					break;
				case 1:
					if(args[0].equalsIgnoreCase("reload"))
					{
						if(!player.hasPermission("skyrimrpg.reload"))
						{
							player.sendMessage(ChatColor.RED + "You don't have permission to do this");
							return true;
						}
						if(!plugin.checkFiles())
						cm.loadConfig();
						player.sendMessage(ChatColor.GREEN + "Configuration file reloaded successfully.");
					}
					else if(args[0].equalsIgnoreCase("refresh"))
					{
						if(!player.hasPermission("skyrimrpg.refresh"))
						{
							player.sendMessage(ChatColor.RED + "You don't have permission to do this");
							return true;
						}
						cm.refreshConfig();
						player.sendMessage(ChatColor.GREEN + "Configuration file refreshed successfully.");
					}
					break;
				}
			}
		}
		else if(command.getName().equalsIgnoreCase("skystats"))
		{
			if(player == null)
			{
				System.out.println("[SkyrimRPG]You don't have stats!");
			}
			else
			{
				int page = 1;
				if(args.length != 0 && args.length != 1)
				{
					player.sendMessage(ChatColor.RED + "Usage: /skystats <page>");
					return true;
				}
				if(args.length == 1)
				{
					try{page = Integer.parseInt(args[0]);}catch(NumberFormatException nfe){page = 1;}
				}
				if(page <= 0)page = 1;
				switch(page)
				{
				case 1:
					player.sendMessage(ChatColor.GOLD + "Stats Page 1 of 2");
					player.sendMessage(ChatColor.RED + "Combat" + ChatColor.WHITE + "|" + ChatColor.BLUE + "Magic" + ChatColor.WHITE + "|" + ChatColor.GRAY + "Stealth");
					player.sendMessage(ChatColor.GREEN + "Level: " + plugin.getSkillManager().getLevel(player));
					player.sendMessage(ChatColor.BLUE + "Magicka: " + plugin.getSpellManager().getMagicka(player));
					player.sendMessage(ChatColor.RED + "Archery: Level " + plugin.getSkillManager().getSkillLevel(Skill.ARCHERY, player));
					player.sendMessage(ChatColor.RED + "Swordsmanship: Level " + plugin.getSkillManager().getSkillLevel(Skill.SWORDSMANSHIP, player));
					player.sendMessage(ChatColor.RED + "Axecraft: Level " + plugin.getSkillManager().getSkillLevel(Skill.AXECRAFT, player));
					player.sendMessage(ChatColor.RED + "Blocking: Level " + plugin.getSkillManager().getSkillLevel(Skill.BLOCKING, player));
					player.sendMessage(ChatColor.RED + "Armor: Level " + plugin.getSkillManager().getSkillLevel(Skill.ARMOR, player));
					player.sendMessage(ChatColor.BLUE + "Destruction: Level " + plugin.getSkillManager().getSkillLevel(Skill.DESTRUCTION, player));
					break;
				case 2:
					player.sendMessage(ChatColor.GOLD + "Stats Page 2 of 2");
					player.sendMessage(ChatColor.BLUE + "Conjuration: Level " + plugin.getSkillManager().getSkillLevel(Skill.CONJURATION, player));
					player.sendMessage(ChatColor.BLUE + "Restoration: Level " + plugin.getSkillManager().getSkillLevel(Skill.RESTORATION, player));
					player.sendMessage(ChatColor.GRAY + "Pickpocketing: Level " + plugin.getSkillManager().getSkillLevel(Skill.PICKPOCKETING, player));
					player.sendMessage(ChatColor.GRAY + "Lockpicking: Level " + plugin.getSkillManager().getSkillLevel(Skill.LOCKPICKING, player));
					player.sendMessage(ChatColor.GRAY + "Sneak: Level " + plugin.getSkillManager().getSkillLevel(Skill.SNEAK, player));
					break;
				default:
					player.sendMessage(ChatColor.GOLD + "Stats Page " + page + " of 2");
					break;
				}
			}
		}
		return true;
	}
	private boolean msgret(CommandSender sender, String message)
	{
		sender.sendMessage(message);
		return true;
	}
	private boolean noConsole(CommandSender sender)
	{
		sender.sendMessage(ChatColor.RED + "Console cannot use this command!");
		return true;
	}
}
