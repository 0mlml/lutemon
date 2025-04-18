package dev.mlml.lutemon.game.lutemon;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootBox {
    public enum Type {
        BASIC,      // Common Lutemons with basic stats
        PREMIUM,    // Better chance for rare Lutemons
        LEGENDARY   // Guaranteed high-quality Lutemon
    }
    
    private static final Random random = new Random();
    
    private final Type type;
    private final int lutemonCount; // Number of Lutemons in the box
    
    /**
     * Creates a new LootBox of the specified type
     * @param type The type of loot box
     */
    public LootBox(Type type) {
        this.type = type;
        
        switch (type) {
            case BASIC:
                this.lutemonCount = 1;
                break;
            case PREMIUM:
                this.lutemonCount = 2;
                break;
            case LEGENDARY:
                this.lutemonCount = 3;
                break;
            default:
                this.lutemonCount = 1;
                break;
        }
    }
    
    /**
     * Opens the loot box and returns the Lutemons contained within
     * @param context App context for loading schemas
     * @return List of Lutemons obtained from the box
     */
    public List<Lutemon> open(Context context) {
        List<Lutemon> rewards = new ArrayList<>();
        LutemonStorage storage = LutemonStorage.getInstance();
        
        List<Lutemon> schemaLutemons = storage.loadLutemonSchemas(context);
        
        for (int i = 0; i < lutemonCount; i++) {
            Lutemon reward = generateRandomLutemon(schemaLutemons);
            rewards.add(reward);
            
            storage.addLutemon(reward);
        }
        
        storage.saveLutemons(context);
        
        return rewards;
    }
    
    /**
     * Generate a random Lutemon based on loot box type
     * @param schemaLutemons List of template Lutemons from schema
     * @return A new randomly generated Lutemon
     */
    private Lutemon generateRandomLutemon(List<Lutemon> schemaLutemons) {
        if (schemaLutemons == null || schemaLutemons.isEmpty()) {
            return generateFallbackLutemon();
        }
        
        Lutemon template = schemaLutemons.get(random.nextInt(schemaLutemons.size()));

        int attack = template.getAttack();
        int defense = template.getDefense();
        int health = template.getMaxHealth();
        int speed = template.getSpeed();
        
        double statMultiplier;
        switch (type) {
            case BASIC:
                statMultiplier = 0.8 + (random.nextDouble() * 0.4); // 0.8 to 1.2
                break;
            case PREMIUM:
                statMultiplier = 1.0 + (random.nextDouble() * 0.5); // 1.0 to 1.5
                break;
            case LEGENDARY:
                statMultiplier = 1.2 + (random.nextDouble() * 0.8); // 1.2 to 2.0
                break;
            default:
                statMultiplier = 1.0;
                break;
        }
        
        attack = (int)(attack * statMultiplier);
        defense = (int)(defense * statMultiplier);
        health = (int)(health * statMultiplier);
        speed = (int)(speed * statMultiplier);
        
        String name = template.getName() + " " + (random.nextInt(100) + 1);
        
        return new Lutemon(template.getId(), name, template.getColor(), attack, defense, health, speed);
    }
    
    /**
     * Generate a completely random Lutemon if no schema templates are available
     * @return A new randomly generated Lutemon
     */
    private Lutemon generateFallbackLutemon() {
        String[] colors = {"Red", "Green", "Blue", "Yellow", "Purple"};
        String[] schemaIds = {"fire_1", "nature_1", "water_1", "electric_1", "psychic_1"};
        String[] names = {"Firemon", "Naturemon", "Watermon", "Electricmon", "Psychicmon"};
        
        int randomIndex = random.nextInt(colors.length);
        String color = colors[randomIndex];
        String schemaId = schemaIds[randomIndex];
        String name = names[randomIndex] + " " + (random.nextInt(100) + 1);
        
        int baseValue;
        switch (type) {
            case BASIC:
                baseValue = 5;
                break;
            case PREMIUM:
                baseValue = 8;
                break;
            case LEGENDARY:
                baseValue = 12;
                break;
            default:
                baseValue = 5;
                break;
        }
        
        int attack = baseValue + random.nextInt(10);
        int defense = baseValue + random.nextInt(8);
        int health = (baseValue * 3) + random.nextInt(20);
        int speed = baseValue + random.nextInt(10);
        
        return new Lutemon(schemaId, name, color, attack, defense, health, speed);
    }
} 