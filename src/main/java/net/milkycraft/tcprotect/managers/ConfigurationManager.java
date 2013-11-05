package net.milkycraft.tcprotect.managers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import org.bukkit.configuration.file.YamlConfiguration;

import net.milkycraft.tcprotect.FileType;
import net.milkycraft.tcprotect.Flags;
import net.milkycraft.tcprotect.TCProtect;

public class ConfigurationManager {
	static final String sep = System.getProperty("line.separator");

	public static void initFiles(TCProtect plugin) {
		try {
			File main = new File(TCProtect.pathMain);
			File data = new File(TCProtect.pathData);
			File config = new File(TCProtect.pathConfig);
			File flagConfig = new File(TCProtect.pathFlagConfig);

			if (!main.exists()) {
				main.mkdir();
				TCProtect.logger.info("Created folder: " + TCProtect.pathMain);
			}
			if (!data.exists()) {
				data.mkdir();
				TCProtect.logger.info("Created folder: " + TCProtect.pathData);
			}
			if (!config.exists()) {
				TCProtect.logger.info("Created file: " + TCProtect.pathConfig);
				config.createNewFile();
				BufferedWriter fr = new BufferedWriter(new FileWriter(config));
				fr.write("#This is the configuration file, feel free to edit it." + sep);
				fr.write("#Types: Integer: Number without period; Boolean: True or false; Struct: One of the described strings."
						+ sep);
				fr.write("#---------" + sep);
				fr.write("#The data type for the regions file, 'ymlgz', 'yml', 'oos', 'oosgz', 'mysql', oosgz is recommended for normal use. (Struct)"
						+ sep);
				fr.write("file-type: oosgz" + sep);
				fr.write("mysql-user-pass:  " + sep);
				fr.write("#MySQL DB info, don't mess with this unless you're using mysql. (Leave password empty if you're not using one.)"
						+ sep);
				fr.write("mysql-db-name: tcprotect" + sep);
				fr.write("mysql-user-name: root" + sep);
				fr.write("mysql-user-pass:  " + sep);
				fr.write("mysql-host: localhost" + sep);
				fr.write("#If the redstone and sign should be removed once the Region is successfully created. (Boolean)"
						+ sep);
				fr.write("remove-blocks: true" + sep);
				fr.write("#If debug messages should be printed to console. (Boolean)" + sep);
				fr.write("debug-messages: false" + sep);
				fr.write("#The preferred permissions system, 'bPerms', 'Perms3', 'PEX', 'GM', 'OP', 'SuperPerms', 'Detect' (Struct)"
						+ sep);
				fr.write("preferred-permissions: Detect" + sep);
				fr.write("#Height the region starts at, it goes from sky to this value, so 0 would be full sky to bedrock, and 40 would be sky to half way through terrain."
						+ sep);
				fr.write("height-start: 0" + sep);
				fr.write("#The ID of the block that you construct regions out of. EX: 55 = Redstone, 85 = Fence (Integer)"
						+ sep);
				fr.write("block-id: 55" + sep);
				fr.write("#Should we backup the database between saves in-case of interruption?"
						+ sep);
				fr.write("backup: true" + sep);
				fr.write("#The ID of the selector wand." + sep);
				fr.write("adminWandID: " + TCProtect.adminWandID + sep);
				fr.write("#The ID of the information wand." + sep);
				fr.write("infoWandID: " + TCProtect.infoWandID + sep);
				fr.write("#These keys define how many regions each group may create. Respective permission nodes take the form \"tcprotect.group.keyname\""
						+ sep);
				fr.write("preferred: 3" + sep);
				fr.write("exclusive: 5" + sep);
				fr.write("platinum: 10" + sep);
				fr.write("#Players may not lock more blocks than this amount" + sep);
				fr.write("lock-limit: 3" + sep);
				fr.write("#How far back should we go before deleting regions?" + sep);
				fr.write("time-back: 3" + sep);
				fr.write("#What unit should we use for the previous key? (First letter, please.)" + sep);
				fr.write("time-unit: d" + sep);
				fr.write("#How many minutes should we wait between purge checks?" + sep);
				fr.write("purge-check: 5" + sep);
				fr.close();
			}
			if (!flagConfig.exists()) {
				flagConfig.createNewFile();
				BufferedWriter fr = new BufferedWriter(new FileWriter(flagConfig));
				fr.write("#This is the flag defaults configuration, feel free to edit it." + sep);
				fr.write("#The flag can have either true or false default value. Users with required permission can manually toggle these in their own regions."
						+ sep);
				fr.write("#---------" + sep);
				fr.write("pvp: false" + sep);
				fr.write("chest: false" + sep);
				fr.write("lever: true" + sep);
				fr.write("button: true" + sep);
				fr.write("door: false" + sep);
				fr.write("mobs: true" + sep);
				fr.write("potions: true" + sep);
				fr.write("invincible: true" + sep);
				fr.write("crops: false" + sep);
				fr.write("items: true" + sep);
				fr.write("superprotect: false" + sep);
				fr.close();
			}

			YamlConfiguration yaml = new YamlConfiguration();
			FileInputStream yamlFis = new FileInputStream(TCProtect.pathConfig);
			yaml.load(yamlFis);
			if (yaml.isSet("debug-messages")) {
				TCProtect.debugMessages = yaml.getBoolean("debug-messages");
			} else {
                TCProtect.logger
                .severe("Configuration path 'debug-messages' not found. Defaulting to false...");
            }

			if (yaml.isSet("file-type")) {
				String fileType = yaml.getString("file-type");
				if (fileType.equalsIgnoreCase("yml")) {
					TCProtect.logger.debug("Selected mode is YAML.");
					TCProtect.fileType = FileType.YML;
				} else if (fileType.equalsIgnoreCase("ymlgz")) {
					TCProtect.logger.debug("Selected mode is YAMLGZ.");
					TCProtect.fileType = FileType.YMLGZ;
				} else if (fileType.equalsIgnoreCase("yaml")) {
					TCProtect.logger.debug("Selected mode is YAML.");
					TCProtect.fileType = FileType.YML;
				} else if (fileType.equalsIgnoreCase("yamlgz")) {
					TCProtect.logger.debug("Selected mode is YAMLGZ.");
					TCProtect.fileType = FileType.YMLGZ;
				} else if (fileType.equalsIgnoreCase("oos")) {
					TCProtect.logger.debug("Selected mode is OOS.");
					TCProtect.fileType = FileType.OOS;
				} else if (fileType.equalsIgnoreCase("oosgz")) {
					TCProtect.logger.debug("Selected mode is OOSGZ.");
					TCProtect.fileType = FileType.OOSGZ;
				} else if (fileType.equalsIgnoreCase("mysql")) {
					TCProtect.logger.debug("Selected mode is MySQL.");
					TCProtect.fileType = FileType.MYSQL;
				} else {
					TCProtect.logger
					.warning("There is an error in your configuration: 'file-type' isn't an acceptable value. Defaulting to YAMLGZ...");

				}
			} else {
                TCProtect.logger
                .warning("Configuration path 'file-type' not found. Defaulting to YAMLGZ....");
            }

			if (yaml.isSet("remove-blocks")) {
				TCProtect.removeBlocks = yaml.getBoolean("remove-blocks");
			}
			if (yaml.isSet("block-id")) {
                try {
                    TCProtect.blockID = yaml.getInt("block-id");
                } catch (NumberFormatException e) {
                    TCProtect.blockID = 55;
                    TCProtect.logger
                    .warning("There is an error in your configuration, 'block-id' isn't a valid integer. Defaulting to Redstone.");
                }
            }
			else {
				TCProtect.logger
				.warning("Configuration option not found: block-id! Defaulting to Redstone.");
			}
			if (yaml.isSet("height-start")) {
                try {
                    TCProtect.heightStart = yaml.getInt("height-start");
                } catch (NumberFormatException e) {
                    TCProtect.heightStart = 0;
                    TCProtect.logger
                    .warning("There is an error in your configuration, 'height-start' isn't a valid integer. Defaulting to 0.");
                }
            }
			else {
				TCProtect.logger
				.warning("Configuration option not found: height-start! Defaulting to 0.");
			}
			if (yaml.isSet("mysql-db-name")) {
				TCProtect.mysqlDatabaseName = yaml.getString("mysql-db-name");
			}
			if (yaml.isSet("mysql-user-name")) {
				TCProtect.mysqlUserName = yaml.getString("mysql-user-name");
			}
			if (yaml.isSet("mysql-user-pass")) {
				TCProtect.mysqlUserPass = yaml.getString("mysql-user-pass");
			}
			if (yaml.isSet("mysql-host")) {
				TCProtect.mysqlHost = yaml.getString("mysql-host");
			}
			TCProtect.backup = yaml.getBoolean("backup");
			if (yaml.isSet("adminWandID")) {
				try {
					TCProtect.adminWandID = yaml.getInt("adminWandID");
				} catch (NumberFormatException e) {
					TCProtect.logger
					.warning("Configuration value 'adminWandID' isn't a valid integer!");
				}
			}
			if (yaml.isSet("infoWandID")) {
				try {
					TCProtect.infoWandID = yaml.getInt("infoWandID");
				} catch (NumberFormatException e) {
					TCProtect.logger
					.warning("Configuration value 'infoWandID' isn't a valid integer!");
				}
			}
			if (yaml.isSet("time-back")){
				TCProtect.timeBack = yaml.getInt("time-back");
			}
			if (yaml.isSet("time-unit")){
				TCProtect.timeUnit = yaml.getString("time-unit");
			}
			if (yaml.isSet("lock-limit")) {
                TCProtect.lockLimit = yaml.getInt("lock-limit");
            }
			if (yaml.isSet("purge-time")) {
                TCProtect.purgeTime = yaml.getInt("purge-check");
            }
			yamlFis.close();
			yaml = new YamlConfiguration();
			yamlFis = new FileInputStream(TCProtect.pathFlagConfig);
			yaml.load(yamlFis);
			if (yaml.isSet("pvp")) {
				Flags.pvp = yaml.getBoolean("pvp");
			}
			if (yaml.isSet("chest")) {
				Flags.chest = yaml.getBoolean("chest");
			} else {
				TCProtect.logger
				.warning("Configuration value \"chest\" isn't initalized, defaulting to false.");
			}
			if (yaml.isSet("lever")) {
				Flags.lever = yaml.getBoolean("lever");
			} else {
				TCProtect.logger
				.warning("Configuration value \"lever\" isn't initalized, defaulting to true.");
			}
			if (yaml.isSet("button")) {
				Flags.button = yaml.getBoolean("button");
			} else {
				TCProtect.logger
				.warning("Configuration value \"button\" isn't initalized, defaulting to true.");
			}
			if (yaml.isSet("door")) {
				Flags.door = yaml.getBoolean("door");
			} else {
				TCProtect.logger
				.warning("Configuration value \"door\" isn't initalized, defaulting to false.");
			}
			if (yaml.isSet("mobs")) {
				Flags.mobs = yaml.getBoolean("mobs");
			} else {
                TCProtect.logger
                .warning("Configuration value \"mobs\" isn't initalized, defaulting to true.");
            }
			if (yaml.isSet("crops")) {
				Flags.crops = yaml.getBoolean("crops");
			} else {
                TCProtect.logger
                .warning("Configuration value \"crops\" isn't initalized, defaulting to false.");
            }

			if (yaml.isSet("items")) {
				if (yaml.getBoolean("items")) {
					Flags.items = true;
				} else {
                    Flags.items = false;
                }
			} else {
                TCProtect.logger
                .warning("Configuration value \"items\" isn't initalized, defaulting to true.");
            }

			if (yaml.isSet("invincible")) {
				if (yaml.getBoolean("invincible")) {
					Flags.invincible = true;
				} else {
                    Flags.invincible = false;
                }
			} else {
                TCProtect.logger
                .warning("Configuration value \"invincible\" isn't initalized, defaulting to true.");
            }

			if (yaml.isSet("potions")) {
				if (yaml.getBoolean("potions")) {
					Flags.potions = true;
				} else {
                    Flags.potions = false;
                }
			} else {
                TCProtect.logger
                .warning("Configuration value \"potions\" isn't initalized, defaulting to true.");
            }

			if (yaml.isSet("superprotect")) {
				if (yaml.getBoolean("superprotect")) {
					Flags.superProtect = true;
				} else {
                    Flags.superProtect = false;
                }
			} else {
                TCProtect.logger
                .warning("Configuration value \"superprotect\" isn't initalized, defaulting to false.");
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		File f = new File(plugin.getDataFolder(), "purge.yml");
		try {
			f.createNewFile();
		}
		catch (Exception ex){
			ex.printStackTrace();
			TCProtect.logger.info("Failed to create purge.yml file!");
		}
		File fe = new File(plugin.getDataFolder(), "purgeexempt.yml");
		try {
			fe.createNewFile();
		}
		catch (Exception ex){
			ex.printStackTrace();
			TCProtect.logger.info("Failed to create purgeexempt.yml file!");
		}
	}
}