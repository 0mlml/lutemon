package dev.mlml.lutemon.ui.home;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import dev.mlml.lutemon.game.lutemon.LootBox;
import dev.mlml.lutemon.game.lutemon.LootBoxManager;
import dev.mlml.lutemon.game.lutemon.Lutemon;
import dev.mlml.lutemon.game.lutemon.LutemonStorage;
import dev.mlml.lutemon.game.lutemon.StatsManager;
import dev.mlml.lutemon.game.lutemon.TrainingManager;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Lutemon>> lutemons;
    private final MutableLiveData<Integer> currency;
    private final MutableLiveData<Boolean> loading;
    private final MutableLiveData<String> errorMessage;
    
    private final LutemonStorage lutemonStorage;
    private final LootBoxManager lootBoxManager;
    private final StatsManager statsManager;
    private final Application application;

    public HomeViewModel(Application application) {
        super(application);
        this.application = application;
        
        lutemons = new MutableLiveData<>(new ArrayList<>());
        currency = new MutableLiveData<>(0);
        loading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>("");
        
        lutemonStorage = LutemonStorage.getInstance();
        lootBoxManager = LootBoxManager.getInstance();
        statsManager = StatsManager.getInstance();
        
        // Initialize managers
        lootBoxManager.initialize(application);
        
        // Load data
        refreshData();
    }

    /**
     * Refresh all data from storage
     */
    public void refreshData() {
        loading.setValue(true);
        
        // Load lutemons from storage
        lutemonStorage.loadSavedLutemons(application);
        
        // Make a deep copy of the lutemons list to ensure it's a new reference
        List<Lutemon> lutemonList = new ArrayList<>(lutemonStorage.getLutemons());
        lutemons.setValue(lutemonList);
        
        // Update currency display
        currency.setValue(lootBoxManager.getPlayerCurrency());
        
        // Load statistics
        statsManager.loadStats(application);
        
        loading.setValue(false);
    }

    public void saveData() {
        statsManager.saveStats(application);
    }

    public LiveData<List<Lutemon>> getLutemons() {
        return lutemons;
    }

    public LiveData<Integer> getCurrency() {
        return currency;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Purchase and open a loot box
     * @param type Loot box type
     * @return List of lutemons received, or null if purchase failed
     */
    public List<Lutemon> purchaseLootBox(LootBox.Type type) {
        if (!canAffordLootBox(type)) {
            errorMessage.setValue("Not enough currency to purchase this box!");
            return null;
        }
        
        loading.setValue(true);
        List<Lutemon> newLutemons = lootBoxManager.purchaseLootBox(application, type);
        
        if (newLutemons != null) {
            currency.setValue(lootBoxManager.getPlayerCurrency());
            
            lutemonStorage.saveLutemons(application);
            
            lutemons.setValue(lutemonStorage.getLutemons());
        } else {
            errorMessage.setValue("Failed to open loot box!");
        }
        
        loading.setValue(false);
        return newLutemons;
    }

    /**
     * Get a free loot box
     * @return List of obtained Lutemons
     */
    public List<Lutemon> getFreeLootBox() {
        loading.setValue(true);
        
        List<Lutemon> rewards = lootBoxManager.getFreeLootBox(application);
        refreshData();
        
        loading.setValue(false);
        return rewards;
    }

    /**
     * Get the cost of a loot box
     * @param type Loot box type
     * @return Cost in currency
     */
    public int getLootBoxCost(LootBox.Type type) {
        return lootBoxManager.getLootBoxCost(type);
    }

    /**
     * Check if the player can afford a loot box
     * @param type Loot box type
     * @return true if player has enough currency
     */
    public boolean canAffordLootBox(LootBox.Type type) {
        return lootBoxManager.canAffordLootBox(type);
    }

    /**
     * Remove a Lutemon from storage by its ID
     * @param lutemonId ID of the Lutemon to remove
     */
    public void removeLutemon(String lutemonId) {
        lutemonStorage.removeLutemonById(lutemonId);
        lutemonStorage.saveLutemons(application);
        
        List<Lutemon> updatedList = new ArrayList<>(lutemonStorage.getLutemons());
        lutemons.setValue(updatedList);
        
        currency.setValue(lootBoxManager.getPlayerCurrency());
    }
    
    /**
     * Add currency to the player's account
     * @param amount Amount of currency to add
     */
    public void addCurrency(int amount) {
        lootBoxManager.addCurrency(amount);
        lootBoxManager.savePlayerCurrency(application);
        currency.setValue(lootBoxManager.getPlayerCurrency());
    }
}