package dev.mlml.lutemon.game.lutemon;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrainingManager {
    private static final String TAG = "TrainingManager";
    private static final String PREFS_NAME = "training_prefs";
    private static final String KEY_TRAINING_LUTEMONS = "training_lutemons";
    private static final String KEY_LAST_TRAINING_TIME = "last_training_time";
    
    private static final int BASE_EXPERIENCE_PER_CLICK = 5;
    private static final int EXPERIENCE_VARIATION = 2;
    
    private static TrainingManager instance = null;
    private final List<String> trainingLutemonIds = new ArrayList<>();
    private long lastTrainingTime = 0;
    
    private TrainingManager() {
    }
    
    public static TrainingManager getInstance() {
        if (instance == null) {
            instance = new TrainingManager();
        }
        return instance;
    }
    
    /**
     * Load training state from shared preferences
     * @param context Application context
     */
    public void loadTrainingState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String trainingJson = prefs.getString(KEY_TRAINING_LUTEMONS, "[]");
        lastTrainingTime = prefs.getLong(KEY_LAST_TRAINING_TIME, 0);
        
        try {
            JSONArray jsonArray = new JSONArray(trainingJson);
            trainingLutemonIds.clear();
            
            for (int i = 0; i < jsonArray.length(); i++) {
                trainingLutemonIds.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Save training state to shared preferences
     * @param context Application context
     */
    public void saveTrainingState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        JSONArray jsonArray = new JSONArray();
        for (String id : trainingLutemonIds) {
            jsonArray.put(id);
        }
        
        editor.putString(KEY_TRAINING_LUTEMONS, jsonArray.toString());
        editor.putLong(KEY_LAST_TRAINING_TIME, lastTrainingTime);
        editor.apply();
    }
    
    /**
     * Add a Lutemon to training
     * @param lutemonId The ID of the Lutemon to train
     */
    public void addLutemonToTraining(String lutemonId) {
        if (!trainingLutemonIds.contains(lutemonId)) {
            trainingLutemonIds.add(lutemonId);
            
            StatsManager statsManager = StatsManager.getInstance();
            statsManager.incrementTotalTrainingSessions();
        }
    }
    
    /**
     * Remove a Lutemon from training
     * @param lutemonId The ID of the Lutemon to remove
     */
    public void removeLutemonFromTraining(String lutemonId) {
        trainingLutemonIds.remove(lutemonId);
    }
    
    /**
     * Get IDs of all Lutemons currently in training
     * @return List of training Lutemon IDs
     */
    public List<String> getTrainingLutemonIds() {
        return new ArrayList<>(trainingLutemonIds);
    }
    
    /**
     * Check if a Lutemon is currently in training
     * @param lutemonId The ID to check
     * @return true if the Lutemon is in training
     */
    public boolean isLutemonInTraining(String lutemonId) {
        return trainingLutemonIds.contains(lutemonId);
    }
    
    /**
     * Record the training time
     */
    public void updateTrainingTime() {
        lastTrainingTime = System.currentTimeMillis();
    }
    
    /**
     * Get the last training time
     * @return Timestamp of last training
     */
    public long getLastTrainingTime() {
        return lastTrainingTime;
    }
    
    /**
     * Train the given Lutemon (add experience)
     * @param lutemon The Lutemon to train
     * @param clickCount Number of training clicks
     * @return The amount of experience gained
     */
    public TrainingResult trainLutemon(Lutemon lutemon, int clickCount) {
        if (lutemon == null) return new TrainingResult(0, 0);
        
        StatsManager statsManager = StatsManager.getInstance();
        statsManager.incrementTotalTrainingClicks(clickCount);
        
        int expGained = 0;
        int startLevel = lutemon.getLevel();
        
        for (int i = 0; i < clickCount; i++) {
            int expPerClick = BASE_EXPERIENCE_PER_CLICK + (int)(Math.random() * (EXPERIENCE_VARIATION * 2 + 1)) - EXPERIENCE_VARIATION;
            expGained += expPerClick;
        }
        
        lutemon.addExperience(expGained);
        
        int endLevel = lutemon.getLevel();
        int levelsGained = endLevel - startLevel;
        
        if (endLevel > 0) {
            statsManager.checkAndUpdateHighestLevel(endLevel);
        }
        
        updateTrainingTime();
        
        return new TrainingResult(expGained, levelsGained);
    }

    public class TrainingResult {
        private final int experienceGained;
        private final int levelsGained;

        public TrainingResult(int experienceGained, int levelsGained) {
            this.experienceGained = experienceGained;
            this.levelsGained = levelsGained;
        }

        public int experienceGained() {
            return experienceGained;
        }

        public int levelsGained() {
            return levelsGained;
        }
    }
    
    
} 