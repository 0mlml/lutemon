package dev.mlml.lutemon.game.lutemon;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

public class Lutemon {
    private String id;
    private String name;
    private String color;
    private int attack;
    private int defense;
    private int maxHealth;
    private int currentHealth;
    private int speed;
    private int experience;
    private static final Random random = new Random();

    public static final int BASE_EXPERIENCE_THRESHOLD = 100;
    public static final double EXPERIENCE_GROWTH_FACTOR = 1.5;
    private static final int STAT_INCREASE_PER_LEVEL = 2;
    
    private static final double CRITICAL_HIT_CHANCE = 0.15;
    private static final double CRITICAL_HIT_MULTIPLIER = 1.5;
    private static final double EXPERIENCE_MULTIPLIER = 0.01;
    private static final double SPEED_ADVANTAGE_MULTIPLIER = 0.05;
    private static final double TYPE_ADVANTAGE_MULTIPLIER = 0.2;
    private static final double ATTACK_VARIATION = 0.2;
    
    private static final String[][] TYPE_ADVANTAGES = {
            {"Red", "Green"},      // Red is strong against Green
            {"Green", "Blue"},     // Green is strong against Blue
            {"Blue", "Red"},       // Blue is strong against Red
            {"Yellow", "Blue"},    // Yellow is strong against Blue
            {"Purple", "Yellow"},  // Purple is strong against Yellow
            {"Green", "Purple"},   // Green is strong against Purple
            {"Blue", "Yellow"}     // Blue is weak against Yellow (reverse advantage)
    };

    public Lutemon(String id, String name, String color, int attack, int defense, int health, int speed) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.attack = attack;
        this.defense = defense;
        this.maxHealth = health;
        this.currentHealth = health;
        this.speed = speed;
        this.experience = 0;
    }

    public Lutemon(String id, String name, String color, int attack, int defense, int health, int speed, int experience) {
        this(id, name, color, attack, defense, health, speed);
        this.experience = experience;
    }

    public String getId() {
        return id;
    }

    /**
     * Get the schema ID for this Lutemon, used for loading images
     * @return The schema ID (e.g., "fire_1", "water_1") or "unknown" if not found
     */
    public String getSchemaId() {
        // If the ID is already in schema format (e.g., fire_1), use it
        if (id != null && id.contains("_")) {
            return id;
        }
        
        // Default to "unknown"
        return "unknown";
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = Math.min(currentHealth, maxHealth);
    }

    public void heal() {
        this.currentHealth = maxHealth;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void addExperience(int amount) {
        this.experience += amount;
    }
    
    /**
     * Calculate the current level based on total experience
     * @return The current level (starts from 1)
     */
    public int getLevel() {
        int level = 1;
        double threshold = BASE_EXPERIENCE_THRESHOLD;
        int totalExp = 0;
        
        while (totalExp + threshold <= experience) {
            totalExp += threshold;
            level++;
            threshold = BASE_EXPERIENCE_THRESHOLD * Math.pow(EXPERIENCE_GROWTH_FACTOR, level - 1);
        }
        
        return level;
    }
    
    /**
     * Get the current experience progress toward the next level
     * @return Current level progress as a number between 0.0 and 1.0
     */
    public double getLevelProgress() {
        int level = getLevel();
        double currentLevelThreshold = BASE_EXPERIENCE_THRESHOLD * Math.pow(EXPERIENCE_GROWTH_FACTOR, level - 1);
        double prevLevelTotalExp = 0;
        
        for (int i = 1; i < level; i++) {
            prevLevelTotalExp += BASE_EXPERIENCE_THRESHOLD * Math.pow(EXPERIENCE_GROWTH_FACTOR, i - 1);
        }
        
        double currentLevelExp = experience - prevLevelTotalExp;
        
        return currentLevelExp / currentLevelThreshold;
    }
    
    /**
     * Get the total experience needed for the next level
     * @return Experience threshold for the next level
     */
    public int getNextLevelExperience() {
        int level = getLevel();
        return (int) (BASE_EXPERIENCE_THRESHOLD * Math.pow(EXPERIENCE_GROWTH_FACTOR, level - 1));
    }
    
    /**
     * Get the remaining experience needed to reach the next level
     * @return Experience points needed
     */
    public int getExperienceToNextLevel() {
        int level = getLevel();
        double threshold = BASE_EXPERIENCE_THRESHOLD * Math.pow(EXPERIENCE_GROWTH_FACTOR, level - 1);
        double prevLevelTotalExp = 0;
        
        for (int i = 1; i < level; i++) {
            prevLevelTotalExp += BASE_EXPERIENCE_THRESHOLD * Math.pow(EXPERIENCE_GROWTH_FACTOR, i - 1);
        }
        
        return (int) (threshold - (experience - prevLevelTotalExp));
    }

    public boolean takeDamage(int damage) {
        int actualDamage = Math.max(1, damage - defense);
        currentHealth = Math.max(0, currentHealth - actualDamage);
        return currentHealth > 0;
    }

    public AttackResult attack(Lutemon target) {
        double damage = attack;
        boolean isCritical = false;
        
        double experienceBonus = attack * (experience * EXPERIENCE_MULTIPLIER);
        damage += experienceBonus;
        
        double typeBonus = 0;
        if (hasTypeAdvantage(this.color, target.getColor())) {
            typeBonus = attack * TYPE_ADVANTAGE_MULTIPLIER;
            damage += typeBonus;
        } else if (hasTypeAdvantage(target.getColor(), this.color)) {
            damage -= attack * TYPE_ADVANTAGE_MULTIPLIER;
        }
        
        double randomVariation = attack * ((random.nextDouble() * 2 * ATTACK_VARIATION) - ATTACK_VARIATION);
        damage += randomVariation;
        
        if (random.nextDouble() < CRITICAL_HIT_CHANCE) {
            damage *= CRITICAL_HIT_MULTIPLIER;
            isCritical = true;
        }
        
        int finalDamage = Math.max(1, (int)Math.round(damage));
        
        return new AttackResult(finalDamage, isCritical, typeBonus > 0);
    }
    
    private boolean hasTypeAdvantage(String attackerColor, String defenderColor) {
        for (String[] advantage : TYPE_ADVANTAGES) {
            if (advantage[0].equals(attackerColor) && advantage[1].equals(defenderColor)) {
                return true;
            }
        }
        return false;
    }

    public String toFileString() {
        return String.format(Locale.ENGLISH, "%s,%s,%s,%d,%d,%d,%d,%d",
                id, name, color, attack, defense, maxHealth, speed, experience);
    }

    public static Lutemon fromFileString(String fileString) {
        String[] parts = fileString.split(",");
        if (parts.length >= 8) {
            return new Lutemon(
                    parts[0], // id
                    parts[1], // name
                    parts[2], // color
                    Integer.parseInt(parts[3]), // attack
                    Integer.parseInt(parts[4]), // defense
                    Integer.parseInt(parts[5]), // health
                    Integer.parseInt(parts[6]), // speed
                    Integer.parseInt(parts[7])  // experience
            );
        }
        return null;
    }

    /** @noinspection CallToPrintStackTrace*/
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("color", color);
            json.put("attack", attack);
            json.put("defense", defense);
            json.put("health", maxHealth);
            json.put("speed", speed);
            json.put("experience", experience);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /** @noinspection CallToPrintStackTrace*/
    public static Lutemon fromJson(JSONObject json) {
        try {
            String id = json.getString("id");
            String name = json.getString("name");
            String color = json.getString("color");
            int attack = json.getInt("attack");
            int defense = json.getInt("defense");
            int health = json.getInt("health");
            int speed = json.getInt("speed");
            int experience = json.has("experience") ? json.getInt("experience") : 0;
            
            return new Lutemon(id, name, color, attack, defense, health, speed, experience);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return name + " (" + color + ") - HP: " + currentHealth + "/" + maxHealth +
                ", ATK: " + attack + ", DEF: " + defense + ", SPD: " + speed + 
                ", LVL: " + getLevel() + ", XP: " + experience;
    }
    
    public static class AttackResult {
        private final int damage;
        private final boolean isCritical;
        private final boolean hasTypeAdvantage;

        public AttackResult(int damage, boolean isCritical, boolean hasTypeAdvantage) {
            this.damage = damage;
            this.isCritical = isCritical;
            this.hasTypeAdvantage = hasTypeAdvantage;
        }
        
        public int getDamage() {
            return damage;
        }
        
        public boolean isCritical() {
            return isCritical;
        }
        
        public boolean hasTypeAdvantage() {
            return hasTypeAdvantage;
        }
        
        
        @NonNull
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append(damage).append(" damage");
            
            if (isCritical) {
                result.append(" (CRITICAL HIT!)");
            }
            if (hasTypeAdvantage) {
                result.append(" (Type advantage)");
            }
            
            return result.toString();
        }
    }
}
