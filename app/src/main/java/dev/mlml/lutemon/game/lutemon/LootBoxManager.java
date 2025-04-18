package dev.mlml.lutemon.game.lutemon;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

/**
 * Manages the player's loot boxes, currency, and reward system
 */
public class LootBoxManager {
    private static final String PREFS_NAME = "LutemonPrefs";
    private static final String KEY_CURRENCY = "playerCurrency";
    
    private static final int BASIC_BOX_COST = 100;
    private static final int PREMIUM_BOX_COST = 300;
    private static final int LEGENDARY_BOX_COST = 1000;
    
    private static LootBoxManager instance;
    
    private int playerCurrency;
    
    private LootBoxManager() {
    }
    
    /**
     * Get the singleton instance of LootBoxManager
     * @return The LootBoxManager instance
     */
    public static LootBoxManager getInstance() {
        if (instance == null) {
            instance = new LootBoxManager();
        }
        return instance;
    }
    
    /**
     * Initialize the manager by loading saved currency
     * @param context Application context
     */
    public void initialize(Context context) {
        loadPlayerCurrency(context);
    }
    
    /**
     * Get the player's current currency amount
     * @return Current currency
     */
    public int getPlayerCurrency() {
        return playerCurrency;
    }
    
    /**
     * Add currency to the player's account
     * @param amount Amount to add
     */
    public void addCurrency(int amount) {
        playerCurrency += amount;
    }

    
    /**
     * Get the cost of a specific loot box type
     * @param type Loot box type
     * @return Cost in currency
     */
    public int getLootBoxCost(LootBox.Type type) {
        switch (type) {
            case BASIC:
                return BASIC_BOX_COST;
            case PREMIUM:
                return PREMIUM_BOX_COST;
            case LEGENDARY:
                return LEGENDARY_BOX_COST;
            default:
                return BASIC_BOX_COST;
        }
    }
    
    /**
     * Check if the player can afford a specific loot box type
     * @param type Loot box type
     * @return true if player has enough currency
     */
    public boolean canAffordLootBox(LootBox.Type type) {
        return playerCurrency >= getLootBoxCost(type);
    }
    
    /**
     * Purchase and open a loot box
     * @param context Application context
     * @param type Type of loot box to purchase
     * @return List of Lutemons received, or null if purchase failed
     */
    public List<Lutemon> purchaseLootBox(Context context, LootBox.Type type) {
        int cost = getLootBoxCost(type);
        
        if (playerCurrency < cost) {
            return null;
        }
        
        playerCurrency -= cost;
        savePlayerCurrency(context);
        
        StatsManager statsManager = StatsManager.getInstance();
        statsManager.incrementTotalLootBoxesOpened();
        
        LootBox lootBox = new LootBox(type);
        List<Lutemon> rewards = lootBox.open(context);
        
        if (rewards != null && !rewards.isEmpty()) {
            statsManager.incrementTotalLutemonsCollected(rewards.size());
            
            for (Lutemon lutemon : rewards) {
                statsManager.checkAndUpdateHighestLevel(lutemon.getLevel());
            }
        }
        
        statsManager.saveStats(context);
        
        return rewards;
    }
    
    /**
     * Get a free basic loot box (e.g., for daily rewards, tutorial, etc.)
     * @param context Application context
     * @return List of Lutemons received
     */
    public List<Lutemon> getFreeLootBox(Context context) {
        StatsManager statsManager = StatsManager.getInstance();
        statsManager.incrementTotalLootBoxesOpened();
        
        LootBox lootBox = new LootBox(LootBox.Type.BASIC);
        List<Lutemon> rewards = lootBox.open(context);
        
        if (rewards != null && !rewards.isEmpty()) {
            statsManager.incrementTotalLutemonsCollected(rewards.size());
            
            for (Lutemon lutemon : rewards) {
                statsManager.checkAndUpdateHighestLevel(lutemon.getLevel());
            }
        }
        
        statsManager.saveStats(context);
        
        return rewards;
    }
    
    /**
     * Load the player's currency from SharedPreferences
     */
    private void loadPlayerCurrency(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        playerCurrency = prefs.getInt(KEY_CURRENCY, 800); // Default starting currency
    }
    
    /**
     * Save the player's currency to SharedPreferences
     */
    public void savePlayerCurrency(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_CURRENCY, playerCurrency);
        editor.apply();
    }
} 