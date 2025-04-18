package dev.mlml.lutemon.game.lutemon;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Comparator;

/**
 * Manages game statistics tracking
 */
public class StatsManager {
    private static final String TAG = "StatsManager";
    private static final String PREFS_NAME = "stats_prefs";
    
    private static final String KEY_TOTAL_BATTLES = "total_battles";
    private static final String KEY_BATTLES_WON = "battles_won";
    private static final String KEY_BATTLES_LOST = "battles_lost";
    private static final String KEY_TOTAL_TRAINING_SESSIONS = "total_training_sessions";
    private static final String KEY_TOTAL_TRAINING_CLICKS = "total_training_clicks";
    private static final String KEY_TOTAL_LOOT_BOXES_OPENED = "total_loot_boxes_opened";
    private static final String KEY_TOTAL_LUTEMONS_COLLECTED = "total_lutemons_collected";
    private static final String KEY_HIGHEST_LEVEL_REACHED = "highest_level_reached";
    
    private static StatsManager instance = null;
    
    private int totalBattles = 0;
    private int battlesWon = 0;
    private int battlesLost = 0;
    private int totalTrainingSessions = 0;
    private int totalTrainingClicks = 0;
    private int totalLootBoxesOpened = 0;
    private int totalLutemonsCollected = 0;
    private int highestLevelReached = 1;
    
    private StatsManager() {
    }
    
    public static StatsManager getInstance() {
        if (instance == null) {
            instance = new StatsManager();
        }
        return instance;
    }
    
    /**
     * Load all stats from shared preferences
     * @param context Application context
     */
    public void loadStats(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        totalBattles = prefs.getInt(KEY_TOTAL_BATTLES, 0);
        battlesWon = prefs.getInt(KEY_BATTLES_WON, 0);
        battlesLost = prefs.getInt(KEY_BATTLES_LOST, 0);
        totalTrainingSessions = prefs.getInt(KEY_TOTAL_TRAINING_SESSIONS, 0);
        totalTrainingClicks = prefs.getInt(KEY_TOTAL_TRAINING_CLICKS, 0);
        totalLootBoxesOpened = prefs.getInt(KEY_TOTAL_LOOT_BOXES_OPENED, 0);
        totalLutemonsCollected = prefs.getInt(KEY_TOTAL_LUTEMONS_COLLECTED, 0);
        highestLevelReached = prefs.getInt(KEY_HIGHEST_LEVEL_REACHED, 1);
    }
    
    /**
     * Save all stats to shared preferences
     * @param context Application context
     */
    public void saveStats(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putInt(KEY_TOTAL_BATTLES, totalBattles);
        editor.putInt(KEY_BATTLES_WON, battlesWon);
        editor.putInt(KEY_BATTLES_LOST, battlesLost);
        editor.putInt(KEY_TOTAL_TRAINING_SESSIONS, totalTrainingSessions);
        editor.putInt(KEY_TOTAL_TRAINING_CLICKS, totalTrainingClicks);
        editor.putInt(KEY_TOTAL_LOOT_BOXES_OPENED, totalLootBoxesOpened);
        editor.putInt(KEY_TOTAL_LUTEMONS_COLLECTED, totalLutemonsCollected);
        editor.putInt(KEY_HIGHEST_LEVEL_REACHED, highestLevelReached);
        
        editor.apply();
    }
    
    /**
     * Reset all statistics to default values
     * @param context Application context to save the reset stats
     */
    public void resetStats(Context context) {
        totalBattles = 0;
        battlesWon = 0;
        battlesLost = 0;
        totalTrainingSessions = 0;
        totalTrainingClicks = 0;
        totalLootBoxesOpened = 0;
        totalLutemonsCollected = 0;
        highestLevelReached = 1;
        
        saveStats(context);
    }
    
    public void incrementTotalBattles() {
        totalBattles++;
    }
    
    public void incrementBattlesWon() {
        battlesWon++;
    }
    
    public void incrementBattlesLost() {
        battlesLost++;
    }
    
    public void incrementTotalTrainingSessions() {
        totalTrainingSessions++;
    }
    
    public void incrementTotalTrainingClicks() {
        totalTrainingClicks++;
    }
    
    public void incrementTotalTrainingClicks(int count) {
        totalTrainingClicks += count;
    }
    
    public void incrementTotalLootBoxesOpened() {
        totalLootBoxesOpened++;
    }
    
    public void incrementTotalLutemonsCollected() {
        totalLutemonsCollected++;
    }
    
    public void incrementTotalLutemonsCollected(int count) {
        totalLutemonsCollected += count;
    }
    
    /**
     * Check if a Lutemon's level is higher than the highest recorded and update if so
     * @param level The level to check
     * @return true if this is a new highest level
     */
    public boolean checkAndUpdateHighestLevel(int level) {
        if (level > highestLevelReached) {
            highestLevelReached = level;
            return true;
        }
        return false;
    }
    
    public int getTotalBattles() {
        return totalBattles;
    }
    
    public int getBattlesWon() {
        return battlesWon;
    }
    
    public int getBattlesLost() {
        return battlesLost;
    }
    
    public int getTotalTrainingSessions() {
        return totalTrainingSessions;
    }
    
    public int getTotalTrainingClicks() {
        return totalTrainingClicks;
    }
    
    public int getTotalLootBoxesOpened() {
        return totalLootBoxesOpened;
    }
    
    public int getTotalLutemonsCollected() {
        return totalLutemonsCollected;
    }
    
    public int getHighestLevelReached() {
        return highestLevelReached;
    }
    
    /**
     * Get win rate as a percentage
     * @return Win rate 0-100
     */
    public int getWinRate() {
        if (totalBattles == 0) return 0;
        return (int) ((float) battlesWon / totalBattles * 100);
    }
} 