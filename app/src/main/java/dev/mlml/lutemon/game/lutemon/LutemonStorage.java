package dev.mlml.lutemon.game.lutemon;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LutemonStorage {
    private static final String TAG = "LutemonStorage";
    private static final String SAVE_FILENAME = "lutemons.csv";
    private static final String SCHEMA_FILENAME = "lutemon_schema.json";
    private static List<Lutemon> lutemons = new ArrayList<>();
    private static LutemonStorage instance = null;

    private LutemonStorage() {
    }

    public static LutemonStorage getInstance() {
        if (instance == null) {
            instance = new LutemonStorage();
        }
        return instance;
    }

    public List<Lutemon> getLutemons() {
        return lutemons;
    }

    public Lutemon getLutemonById(String id) {
        for (Lutemon lutemon : lutemons) {
            if (lutemon.getId().equals(id)) {
                return lutemon;
            }
        }
        return null;
    }

    public void addLutemon(Lutemon lutemon) {
        lutemons.add(lutemon);
    }

    public void removeLutemon(String id) {
        Lutemon lutemonToRemove = null;
        for (Lutemon lutemon : lutemons) {
            if (lutemon.getId().equals(id)) {
                lutemonToRemove = lutemon;
                break;
            }
        }
        if (lutemonToRemove != null) {
            lutemons.remove(lutemonToRemove);
        }
    }

    /**
     * Alias for removeLutemon method
     * @param id ID of the Lutemon to remove
     */
    public void removeLutemonById(String id) {
        removeLutemon(id);
    }

    public void saveLutemons(Context context) {
        try {
            File file = new File(context.getFilesDir(), SAVE_FILENAME);
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            
            for (Lutemon lutemon : lutemons) {
                writer.write(lutemon.toFileString());
                writer.newLine();
            }
            
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load saved lutemons from CSV file
    public void loadSavedLutemons(Context context) {
        lutemons.clear();
        try {
            File file = new File(context.getFilesDir(), SAVE_FILENAME);
            if (!file.exists()) {
                Log.d(TAG, "Lutemon save file doesn't exist yet");
                return;
            }
            
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            
            String line;
            int lineCount = 0;
            int lutemonCount = 0;
            
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (line.trim().isEmpty()) {
                    Log.d(TAG, "Skipping empty line " + lineCount);
                    continue;
                }
                
                Lutemon lutemon = Lutemon.fromFileString(line);
                if (lutemon != null) {
                    lutemons.add(lutemon);
                    lutemonCount++;
                    Log.d(TAG, "Loaded Lutemon: " + lutemon.getId() + " - " + lutemon.getName());
                } else {
                    Log.e(TAG, "Failed to parse Lutemon from line " + lineCount + ": " + line);
                }
            }
            
            Log.d(TAG, "Loaded " + lutemonCount + " Lutemons out of " + lineCount + " lines");
            
            reader.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IO error while loading Lutemons: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading Lutemons: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Load lutemon schema definitions from JSON in assets
    public List<Lutemon> loadLutemonSchemas(Context context) {
        List<Lutemon> schemaLutemons = new ArrayList<>();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(SCHEMA_FILENAME);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            
            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Lutemon lutemon = Lutemon.fromJson(jsonObject);
                if (lutemon != null) {
                    schemaLutemons.add(lutemon);
                }
            }
            
            reader.close();
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading lutemon schemas: " + e.getMessage());
        }
        
        return schemaLutemons;
    }
    
    // Create a schema template file to place in assets
    public static String createSchemaTemplate() {
        JSONArray jsonArray = new JSONArray();
        
        try {
            // Example 1
            JSONObject lutemon1 = new JSONObject();
            lutemon1.put("id", "fire_1");
            lutemon1.put("name", "Firemon");
            lutemon1.put("color", "Red");
            lutemon1.put("attack", 10);
            lutemon1.put("defense", 5);
            lutemon1.put("health", 25);
            lutemon1.put("speed", 8);
            jsonArray.put(lutemon1);
            
            // Example 2
            JSONObject lutemon2 = new JSONObject();
            lutemon2.put("id", "water_1");
            lutemon2.put("name", "Watermon");
            lutemon2.put("color", "Blue");
            lutemon2.put("attack", 7);
            lutemon2.put("defense", 8);
            lutemon2.put("health", 30);
            lutemon2.put("speed", 6);
            jsonArray.put(lutemon2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jsonArray.toString();
    }
} 