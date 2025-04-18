package dev.mlml.lutemon.game.lutemon;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleManager {
    private static final String TAG = "BattleManager";
    private static final String PREFS_NAME = "battle_prefs";
    private static final String KEY_CURRENT_BATTLE = "current_battle";
    
    private static final int MAX_TURNS = 20; // Max battle turns before a draw
    private static final int AI_DECISION_ATTACK_WEIGHT = 70; // 70% chance to attack
    private static final int AI_DECISION_DEFEND_WEIGHT = 30; // 30% chance to defend
    
    private static BattleManager instance = null;
    private final Random random = new Random();
    
    private Lutemon playerLutemon;
    private Lutemon aiLutemon;
    private boolean isBattleActive = false;
    private int turnCount = 0;
    private boolean isPlayerTurn = true;
    private List<BattleAction> battleLog = new ArrayList<>();
    
    private int playerDefenseBonus = 0;
    private int aiDefenseBonus = 0;
    
    private BattleManager() {
    }
    
    public static BattleManager getInstance() {
        if (instance == null) {
            instance = new BattleManager();
        }
        return instance;
    }
    
    /**
     * Start a new battle between player's Lutemon and an AI Lutemon
     * @param playerLutemon The player's Lutemon
     * @param aiLutemon The AI's Lutemon
     * @return True if battle started successfully
     */
    public boolean startBattle(Lutemon playerLutemon, Lutemon aiLutemon) {
        if (playerLutemon == null || aiLutemon == null) {
            return false;
        }
        
        this.playerLutemon = playerLutemon;
        this.aiLutemon = aiLutemon;
        
        playerLutemon.heal();
        aiLutemon.heal();
        
        isBattleActive = true;
        turnCount = 0;
        battleLog.clear();
        
        isPlayerTurn = playerLutemon.getSpeed() >= aiLutemon.getSpeed();
        if (playerLutemon.getSpeed() == aiLutemon.getSpeed()) {
            isPlayerTurn = random.nextBoolean();
        }
        
        playerDefenseBonus = 0;
        aiDefenseBonus = 0;
        
        String startMessage = "Battle started between " + playerLutemon.getName() + 
                " and " + aiLutemon.getName() + "!\n" +
                (isPlayerTurn ? playerLutemon.getName() : aiLutemon.getName()) + 
                " goes first!";
        battleLog.add(new BattleAction(BattleActionType.INFO, startMessage));
        
        return true;
    }
    
    /**
     * Generate a random AI opponent
     * @param context Application context
     * @param playerLevel Player's Lutemon level to match against
     * @return A randomly generated AI Lutemon
     */
    public Lutemon generateAiOpponent(Context context, int playerLevel) {
        LutemonStorage storage = LutemonStorage.getInstance();
        List<Lutemon> schemas = storage.loadLutemonSchemas(context);
        
        if (schemas.isEmpty()) {
            return createBasicOpponent(playerLevel);
        }
        
        Lutemon template = schemas.get(random.nextInt(schemas.size()));
        
        int levelFactor = Math.max(1, playerLevel - 1);
        int scaledAttack = template.getAttack() + (levelFactor * 2);
        int scaledDefense = template.getDefense() + (levelFactor);
        int scaledHealth = template.getMaxHealth() + (levelFactor * 5);
        int scaledSpeed = template.getSpeed() + (levelFactor);
        
        String aiId = "ai_" + System.currentTimeMillis();
        
        int aiXp = calculateXpForLevel(playerLevel);
        
        return new Lutemon(aiId, "AI " + template.getName(), template.getColor(), 
                scaledAttack, scaledDefense, scaledHealth, scaledSpeed, aiXp);
    }
    
    /**
     * Create a basic opponent if no schemas are available
     * @param playerLevel Player's Lutemon level to match against
     * @return A basic AI Lutemon
     */
    private Lutemon createBasicOpponent(int playerLevel) {
        String aiId = "ai_" + System.currentTimeMillis();
        String[] colors = {"Red", "Blue", "Green", "Yellow", "Purple"};
        String color = colors[random.nextInt(colors.length)];
        
        int baseAttack = 8 + random.nextInt(5);  // 8-12
        int baseDefense = 5 + random.nextInt(4); // 5-8
        int baseHealth = 25 + random.nextInt(11); // 25-35
        int baseSpeed = 5 + random.nextInt(4);   // 5-8
        
        int levelFactor = Math.max(1, playerLevel - 1);
        int scaledAttack = baseAttack + (levelFactor * 2);
        int scaledDefense = baseDefense + (levelFactor);
        int scaledHealth = baseHealth + (levelFactor * 5);
        int scaledSpeed = baseSpeed + (levelFactor);
        
        int aiXp = calculateXpForLevel(playerLevel);
        
        return new Lutemon(aiId, "AI Lutemon", color, 
                scaledAttack, scaledDefense, scaledHealth, scaledSpeed, aiXp);
    }
    
    /**
     * Calculate the XP needed for a given level
     * @param level The target level
     * @return XP amount
     */
    private int calculateXpForLevel(int level) {
        if (level <= 1) return 0;
        
        int totalXp = 0;
        double threshold = Lutemon.BASE_EXPERIENCE_THRESHOLD;
        
        for (int i = 1; i < level; i++) {
            totalXp += threshold;
            threshold = Lutemon.BASE_EXPERIENCE_THRESHOLD * Math.pow(Lutemon.EXPERIENCE_GROWTH_FACTOR, i);
        }
        
        return totalXp;
    }
    
    /**
     * Execute player's attack action
     * @return BattleResult with action outcome
     */
    public BattleResult playerAttack() {
        if (!isBattleActive || !isPlayerTurn) {
            return new BattleResult(false, "Not your turn!");
        }
        
        Lutemon.AttackResult attackResult = playerLutemon.attack(aiLutemon);
        int damage = attackResult.getDamage();
        
        damage = Math.max(1, damage - aiDefenseBonus);
        boolean isAlive = aiLutemon.takeDamage(damage);
        aiDefenseBonus = 0; 
        
        String attackMessage = playerLutemon.getName() + " attacks " + aiLutemon.getName() + 
                " for " + damage + " damage";
        if (attackResult.isCritical()) {
            attackMessage += " (Critical hit!)";
        }
        if (attackResult.hasTypeAdvantage()) {
            attackMessage += " (Type advantage!)";
        }
        
        battleLog.add(new BattleAction(BattleActionType.PLAYER_ATTACK, attackMessage));
        
        BattleResult result;
        if (!isAlive) {
            battleLog.add(new BattleAction(BattleActionType.VICTORY, 
                    playerLutemon.getName() + " defeated " + aiLutemon.getName() + "!"));
            result = endBattle(true);
        } else {
            turnCount++;
            isPlayerTurn = false;
            result = new BattleResult(true, attackMessage);
            
            if (turnCount >= MAX_TURNS) {
                battleLog.add(new BattleAction(BattleActionType.DRAW, 
                        "Battle ended in a draw after " + MAX_TURNS + " turns!"));
                result = endBattle(false);
            }
        }
        
        return result;
    }
    
    /**
     * Execute player's defend action
     * @return BattleResult with action outcome
     */
    public BattleResult playerDefend() {
        if (!isBattleActive || !isPlayerTurn) {
            return new BattleResult(false, "Not your turn!");
        }
        
        playerDefenseBonus = playerLutemon.getDefense() / 2;
        
        String defendMessage = playerLutemon.getName() + " defends, gaining " + 
                playerDefenseBonus + " bonus defense for the next attack";
        battleLog.add(new BattleAction(BattleActionType.PLAYER_DEFEND, defendMessage));
        
        turnCount++;
        isPlayerTurn = false;
        
        if (turnCount >= MAX_TURNS) {
            battleLog.add(new BattleAction(BattleActionType.DRAW, 
                    "Battle ended in a draw after " + MAX_TURNS + " turns!"));
            return endBattle(false);
        }
        
        return new BattleResult(true, defendMessage);
    }
    
    /**
     * Execute AI's turn
     * @return BattleResult with action outcome
     */
    public BattleResult executeAiTurn() {
        if (!isBattleActive || isPlayerTurn) {
            return new BattleResult(false, "Not AI's turn!");
        }
        
        boolean shouldAttack = makeAiDecision();
        
        BattleResult result;
        if (shouldAttack) {
            Lutemon.AttackResult attackResult = aiLutemon.attack(playerLutemon);
            int damage = attackResult.getDamage();
            
            damage = Math.max(1, damage - playerDefenseBonus);
            boolean isAlive = playerLutemon.takeDamage(damage);
            playerDefenseBonus = 0; 
            
            String attackMessage = aiLutemon.getName() + " attacks " + playerLutemon.getName() + 
                    " for " + damage + " damage";
            if (attackResult.isCritical()) {
                attackMessage += " (Critical hit!)";
            }
            if (attackResult.hasTypeAdvantage()) {
                attackMessage += " (Type advantage!)";
            }
            
            battleLog.add(new BattleAction(BattleActionType.AI_ATTACK, attackMessage));
            
            if (!isAlive) {
                battleLog.add(new BattleAction(BattleActionType.DEFEAT, 
                        aiLutemon.getName() + " defeated " + playerLutemon.getName() + "!"));
                result = endBattle(false);
            } else {
                result = new BattleResult(true, attackMessage);
            }
        } else {
            aiDefenseBonus = aiLutemon.getDefense() / 2;
            
            String defendMessage = aiLutemon.getName() + " defends, gaining " + 
                    aiDefenseBonus + " bonus defense for the next attack";
            battleLog.add(new BattleAction(BattleActionType.AI_DEFEND, defendMessage));
            
            result = new BattleResult(true, defendMessage);
        }
        
        turnCount++;
        isPlayerTurn = true;
        
        if (turnCount >= MAX_TURNS) {
            battleLog.add(new BattleAction(BattleActionType.DRAW, 
                    "Battle ended in a draw after " + MAX_TURNS + " turns!"));
            return endBattle(false);
        }
        
        return result;
    }
    
    /**
     * AI decision making - whether to attack or defend
     * @return true for attack, false for defend
     */
    private boolean makeAiDecision() {
        int decision = random.nextInt(100);
        
        return decision < AI_DECISION_ATTACK_WEIGHT;
    }
    
    /**
     * End the battle and calculate rewards
     * @param playerVictory true if player won
     * @return BattleResult with outcome
     */
    private BattleResult endBattle(boolean playerVictory) {
        isBattleActive = false;
        
        String resultMessage;
        
        if (playerVictory) {
            int experienceGained = calculateExperienceReward(aiLutemon);
            
            playerLutemon.addExperience(experienceGained);
            
            resultMessage = "Victory! " + playerLutemon.getName() + " gained " + 
                    experienceGained + " experience!";
            
            StatsManager.getInstance().incrementBattlesWon();
        } else {
            int lostExp = playerLutemon.getExperience();
            playerLutemon.setExperience(0);
            
            playerLutemon.heal();
            
            resultMessage = "Defeat! " + playerLutemon.getName() + " lost all experience.";
            
            StatsManager.getInstance().incrementBattlesLost();
        }
        
        StatsManager.getInstance().incrementTotalBattles();
        
        return new BattleResult(true, resultMessage, battleLog, playerVictory);
    }
    
    /**
     * Calculate experience reward from defeating an opponent
     * @param opponent The defeated Lutemon
     * @return Experience points
     */
    private int calculateExperienceReward(Lutemon opponent) {
        int baseReward = opponent.getLevel() * 20;
        
        int statBonus = (opponent.getAttack() + opponent.getDefense() + 
                opponent.getMaxHealth() / 10 + opponent.getSpeed()) / 2;
        
        int randomFactor = random.nextInt(baseReward / 5) - (baseReward / 10);
        
        return Math.max(10, baseReward + statBonus + randomFactor);
    }
    
    /**
     * Save current battle state to preferences
     * @param context Application context
     */
    public void saveBattleState(Context context) {
        if (!isBattleActive) return;
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            JSONObject battleState = new JSONObject();
            battleState.put("is_active", isBattleActive);
            battleState.put("turn_count", turnCount);
            battleState.put("is_player_turn", isPlayerTurn);
            battleState.put("player_defense_bonus", playerDefenseBonus);
            battleState.put("ai_defense_bonus", aiDefenseBonus);
            
            if (playerLutemon != null) {
                battleState.put("player_lutemon", playerLutemon.toJson());
            }
            
            if (aiLutemon != null) {
                battleState.put("ai_lutemon", aiLutemon.toJson());
            }
            
            JSONArray logArray = new JSONArray();
            for (BattleAction action : battleLog) {
                JSONObject actionJson = new JSONObject();
                actionJson.put("type", action.type.toString());
                actionJson.put("message", action.message);
                logArray.put(actionJson);
            }
            battleState.put("battle_log", logArray);
            
            editor.putString(KEY_CURRENT_BATTLE, battleState.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load battle state from preferences
     * @param context Application context
     * @return true if a battle was loaded
     */
    public boolean loadBattleState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String battleStateJson = prefs.getString(KEY_CURRENT_BATTLE, null);
        
        if (battleStateJson == null) {
            return false;
        }
        
        try {
            JSONObject battleState = new JSONObject(battleStateJson);
            
            isBattleActive = battleState.getBoolean("is_active");
            if (!isBattleActive) {
                return false;
            }
            
            turnCount = battleState.getInt("turn_count");
            isPlayerTurn = battleState.getBoolean("is_player_turn");
            playerDefenseBonus = battleState.getInt("player_defense_bonus");
            aiDefenseBonus = battleState.getInt("ai_defense_bonus");
            
            if (battleState.has("player_lutemon")) {
                JSONObject playerLutemonJson = battleState.getJSONObject("player_lutemon");
                playerLutemon = Lutemon.fromJson(playerLutemonJson);
            }
            
            if (battleState.has("ai_lutemon")) {
                JSONObject aiLutemonJson = battleState.getJSONObject("ai_lutemon");
                aiLutemon = Lutemon.fromJson(aiLutemonJson);
            }
            
            battleLog.clear();
            JSONArray logArray = battleState.getJSONArray("battle_log");
            for (int i = 0; i < logArray.length(); i++) {
                JSONObject actionJson = logArray.getJSONObject(i);
                BattleActionType type = BattleActionType.valueOf(actionJson.getString("type"));
                String message = actionJson.getString("message");
                battleLog.add(new BattleAction(type, message));
            }
            
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Clear any saved battle state
     * @param context Application context
     */
    public void clearBattleState(Context context) {
        isBattleActive = false;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_CURRENT_BATTLE);
        editor.apply();
    }
    
    // Getters
    public Lutemon getPlayerLutemon() {
        return playerLutemon;
    }
    
    public Lutemon getAiLutemon() {
        return aiLutemon;
    }
    
    public boolean isBattleActive() {
        return isBattleActive;
    }
    
    public int getTurnCount() {
        return turnCount;
    }
    
    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }
    
    public List<BattleAction> getBattleLog() {
        return new ArrayList<>(battleLog);
    }
    
    // Inner classes for battle results
    public static class BattleResult {
        private final boolean success;
        private final String message;
        private final List<BattleAction> battleLog;
        private final boolean playerVictory;
        private final boolean battleEnded;
        
        public BattleResult(boolean success, String message) {
            this(success, message, new ArrayList<>(), false);
        }
        
        public BattleResult(boolean success, String message, List<BattleAction> battleLog, boolean playerVictory) {
            this.success = success;
            this.message = message;
            this.battleLog = battleLog;
            this.playerVictory = playerVictory;
            this.battleEnded = !battleLog.isEmpty();
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public List<BattleAction> getBattleLog() {
            return battleLog;
        }
        
        public boolean isPlayerVictory() {
            return playerVictory;
        }
        
        public boolean isBattleEnded() {
            return battleEnded;
        }
    }
    
    public static class BattleAction {
        private final BattleActionType type;
        private final String message;
        
        public BattleAction(BattleActionType type, String message) {
            this.type = type;
            this.message = message;
        }
        
        public BattleActionType getType() {
            return type;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    public enum BattleActionType {
        INFO,
        PLAYER_ATTACK,
        PLAYER_DEFEND,
        AI_ATTACK,
        AI_DEFEND,
        VICTORY,
        DEFEAT,
        DRAW
    }
} 