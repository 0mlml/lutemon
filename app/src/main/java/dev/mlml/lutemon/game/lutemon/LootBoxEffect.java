package dev.mlml.lutemon.game.lutemon;

import java.util.HashMap;
import java.util.Map;

public class LootBoxEffect {
    public enum Rarity {
        COMMON,
        RARE,
        EPIC,
        LEGENDARY
    }
    
    private static final Map<String, Integer> COLOR_EFFECTS = new HashMap<>();
    
    private static final int[][] RARITY_THRESHOLDS = {
        {8, 7, 25, 7},     // Common threshold
        {12, 10, 35, 10},   // Rare threshold
        {16, 14, 45, 14},   // Epic threshold
        {20, 18, 55, 18}    // Legendary threshold
    };
    
    /**
     * Calculate the rarity of a Lutemon based on its stats
     * @param lutemon The Lutemon to evaluate
     * @return The calculated rarity
     */
    public static Rarity calculateRarity(Lutemon lutemon) {
        int attack = lutemon.getAttack();
        int defense = lutemon.getDefense();
        int health = lutemon.getMaxHealth();
        int speed = lutemon.getSpeed();
        
        if (attack >= RARITY_THRESHOLDS[3][0] || 
            defense >= RARITY_THRESHOLDS[3][1] || 
            health >= RARITY_THRESHOLDS[3][2] || 
            speed >= RARITY_THRESHOLDS[3][3]) {
            return Rarity.LEGENDARY;
        } else if (attack >= RARITY_THRESHOLDS[2][0] || 
                   defense >= RARITY_THRESHOLDS[2][1] || 
                   health >= RARITY_THRESHOLDS[2][2] || 
                   speed >= RARITY_THRESHOLDS[2][3]) {
            return Rarity.EPIC;
        } else if (attack >= RARITY_THRESHOLDS[1][0] || 
                   defense >= RARITY_THRESHOLDS[1][1] || 
                   health >= RARITY_THRESHOLDS[1][2] || 
                   speed >= RARITY_THRESHOLDS[1][3]) {
            return Rarity.RARE;
        } else {
            return Rarity.COMMON;
        }
    }
    
    /**
     * Get a description of the Lutemon's rarity and notable stats
     * @param lutemon The Lutemon to evaluate
     * @return Description string highlighting strong stats
     */
    public static String getRarityDescription(Lutemon lutemon) {
        Rarity rarity = calculateRarity(lutemon);
        StringBuilder description = new StringBuilder();
        
        switch (rarity) {
            case LEGENDARY:
                description.append("LEGENDARY! ");
                break;
            case EPIC:
                description.append("Epic! ");
                break;
            case RARE:
                description.append("Rare! ");
                break;
            case COMMON:
                description.append("Common. ");
                break;
        }
        
        int attack = lutemon.getAttack();
        int defense = lutemon.getDefense();
        int health = lutemon.getMaxHealth();
        int speed = lutemon.getSpeed();
        
        int maxStat = Math.max(Math.max(attack, defense), Math.max(health/3, speed));
        
        if (maxStat == attack) {
            description.append("Has exceptional attack power!");
        } else if (maxStat == defense) {
            description.append("Has strong defensive capabilities!");
        } else if (maxStat == health/3) {
            description.append("Has impressive health reserves!");
        } else if (maxStat == speed) {
            description.append("Is remarkably fast!");
        }
        
        return description.toString();
    }
    
    /**
     * Get the animation delay for opening a loot box based on type
     * @param type The loot box type
     * @return Animation delay in milliseconds
     */
    public static long getAnimationDelay(LootBox.Type type) {
        switch (type) {
            case BASIC:
                return 1000;
            case PREMIUM:
                return 1500;
            case LEGENDARY:
                return 2000;
            default:
                return 1000;
        }
    }
} 