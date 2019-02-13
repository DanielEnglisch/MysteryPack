package at.jellybit.mc;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Plugin(name="MysteryPack", version="1.0")
@Author("Daniel Englisch")
@Website("https://jellybit.at/")
@Commands(
        @Command(name = "sell")
)

public class MysteryPack extends JavaPlugin implements CommandExecutor {

    private static final double MAX_VALUE = 100;

    private File bank = new File(getDataFolder(), File.separator + "accounts.yaml");
    private File valueFile = new File(getDataFolder(), File.separator + "values.yaml");

    private FileConfiguration accounts = YamlConfiguration.loadConfiguration(bank);
    private FileConfiguration values = YamlConfiguration.loadConfiguration(valueFile);

    @Override
    public void onEnable() {


        if(!bank.exists()){
            accounts.createSection("accounts");
            try {
                accounts.save(bank);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(!valueFile.exists()){
            values.createSection("materials");

           for(Material m : Material.values()){
               try {
                   values.set("materials."+m.name(),0.0);
               }catch (Exception e){
                    e.printStackTrace();
               }
           }

           /* Probe Chunks */


            double total = 0.0;
            Map<Material, Integer> stats = new HashMap<Material, Integer>();



            int radius = 2;
            double numChunks = Math.pow( 1.0 + (2*radius), 2.0);
            int currentChnuk =  1;
            // ((1 + (2*2)) ^ 2

            for(int i = -radius ; i <= radius; i++){
                for(int j = -radius ; j <= radius; j++){
                    Chunk chunk = getServer().getWorlds().get(0).getChunkAt(i,j);
                    getLogger().info("Analyzing chunk ("+i+","+j+") " + currentChnuk + "/" + numChunks);

                    for(int x = 0; x < 16; x++){
                        for(int y = 0; y < 128; y++){
                            for(int z = 0; z < 16; z++){
                                Block b = chunk.getBlock(x,y,z);
                                total++;
                                int current = 0;
                                if(stats.containsKey(b.getType())){
                                    current = stats.get(b.getType());
                                }

                                stats.put(b.getType(),current+1);

                            }
                        }
                    }
                    currentChnuk++;

                }
            }






            for(Material key : stats.keySet()){
                values.set("materials."+key.name(), stats.get(key)/total);
            }


            try {
                values.save(valueFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void saveAccounts(){
        try {
            accounts.save(bank);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setBalance(Player p, double balance){

        String playerId = p.getUniqueId().toString();
        this.accounts.set("accounts."+ playerId, balance);

    }

    private double getValue(Material m){
        double value = 0;
        try{
            value = this.values.getDouble("materials." + m.name());
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return Math.abs(1-value)*MAX_VALUE;
    }

    private double getBalance(Player p){

        String playerId = p.getUniqueId().toString();

        double balance = 0;

        try {
            balance = this.accounts.getDouble("accounts."+ playerId);
        }catch (NullPointerException ex){
            this.accounts.set("accounts."+ playerId, 0.0);
            saveAccounts();
        }

        return balance;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if(!(sender instanceof Player)){
            return false;
        }

        Player p = (Player)sender;

        /* Process /sell command */
        if(label.equalsIgnoreCase("sell")){

            double balance = getBalance(p);
            int amount = p.getInventory().getItemInMainHand().getAmount();
            double value = getValue(p.getInventory().getItemInMainHand().getType());

            balance += amount*value;

            setBalance(p, balance);
            p.getInventory().getItemInMainHand().setAmount(0);

            p.sendMessage("Sold for " + value + " * " + amount + " = " + value*amount + " €");
            p.sendMessage("New balance: " + balance + " €");

        }

        return true;
    }
}
