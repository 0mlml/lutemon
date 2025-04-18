package dev.mlml.lutemon.ui.battle;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import dev.mlml.lutemon.game.lutemon.BattleManager;
import dev.mlml.lutemon.game.lutemon.LootBoxManager;
import dev.mlml.lutemon.game.lutemon.Lutemon;
import dev.mlml.lutemon.game.lutemon.LutemonStorage;
import dev.mlml.lutemon.game.lutemon.StatsManager;

public class BattleViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Lutemon>> availableLutemons = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Lutemon> selectedLutemon = new MutableLiveData<>();
    private final MutableLiveData<Lutemon> aiLutemon = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBattleActive = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isPlayerTurn = new MutableLiveData<>(true);
    private final MutableLiveData<String> battleLog = new MutableLiveData<>("");
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    
    private final LutemonStorage lutemonStorage;
    private final BattleManager battleManager;
    private final StatsManager statsManager;
    private final LootBoxManager lootBoxManager;
    private final Application application;
    
    public BattleViewModel(Application application) {
        super(application);
        this.application = application;
        this.lutemonStorage = LutemonStorage.getInstance();
        this.battleManager = BattleManager.getInstance();
        this.statsManager = StatsManager.getInstance();
        this.lootBoxManager = LootBoxManager.getInstance();
        
        refreshData();
    }
    
    /**
     * Refresh data from storage
     */
    public void refreshData() {
        lutemonStorage.loadSavedLutemons(application);
        statsManager.loadStats(application);
        
        availableLutemons.setValue(lutemonStorage.getLutemons());
        
        if (battleManager.loadBattleState(application)) {
            setupExistingBattle();
        }
    }
    
    /**
     * Setup the UI for an existing battle loaded from storage
     */
    private void setupExistingBattle() {
        Lutemon playerLutemon = battleManager.getPlayerLutemon();
        Lutemon aiOpponent = battleManager.getAiLutemon();
        
        selectedLutemon.setValue(playerLutemon);
        aiLutemon.setValue(aiOpponent);
        isBattleActive.setValue(true);
        isPlayerTurn.setValue(battleManager.isPlayerTurn());
        
        StringBuilder logBuilder = new StringBuilder();
        for (BattleManager.BattleAction action : battleManager.getBattleLog()) {
            logBuilder.append(action.getMessage()).append("\n\n");
        }
        battleLog.setValue(logBuilder.toString());
        
        if (battleManager.isPlayerTurn()) {
            statusMessage.setValue("Your turn! Choose an action.");
        } else {
            statusMessage.setValue("AI is thinking...");
        }
    }
    
    /**
     * Save changes to permanent storage
     */
    public void saveData() {
        lutemonStorage.saveLutemons(application);
        statsManager.saveStats(application);
        battleManager.saveBattleState(application);
        lootBoxManager.savePlayerCurrency(application);
    }
    
    /**
     * Set the Lutemon to battle with
     * @param position Position in the spinner
     */
    public void selectLutemon(int position) {
        List<Lutemon> lutemons = availableLutemons.getValue();
        if (lutemons != null && position >= 0 && position < lutemons.size()) {
            selectedLutemon.setValue(lutemons.get(position));
        }
    }
    
    /**
     * Generate a random AI opponent
     */
    public void generateRandomOpponent() {
        Lutemon playerLutemon = selectedLutemon.getValue();
        
        if (playerLutemon != null) {
            Lutemon opponent = battleManager.generateAiOpponent(
                    application, playerLutemon.getLevel());
            
            aiLutemon.setValue(opponent);
        }
    }

    /**
     * Start a battle with the selected Lutemon and AI opponent
     */
    public void startBattle() {
        Lutemon playerLutemon = selectedLutemon.getValue();
        Lutemon opponent = aiLutemon.getValue();
        
        if (playerLutemon != null && opponent != null) {
            if (battleManager.startBattle(playerLutemon, opponent)) {
                isBattleActive.setValue(true);
                isPlayerTurn.setValue(battleManager.isPlayerTurn());
                
                battleLog.setValue("");
                
                appendBattleLog();
                
                saveData();
                
                if (battleManager.isPlayerTurn()) {
                    statusMessage.setValue("Your turn! Choose an action.");
                } else {
                    statusMessage.setValue("AI goes first...");
                    new android.os.Handler().postDelayed(this::executeAiTurn, 1000);
                }
            } else {
                statusMessage.setValue("Failed to start battle!");
            }
        } else {
            statusMessage.setValue("Please select your Lutemon and generate an opponent!");
        }
    }
    
    /**
     * Execute player's attack action
     */
    public void playerAttack() {
        if (Boolean.TRUE.equals(isBattleActive.getValue()) && Boolean.TRUE.equals(isPlayerTurn.getValue())) {
            BattleManager.BattleResult result = battleManager.playerAttack();
            
            if (result.isSuccess()) {
                isPlayerTurn.setValue(battleManager.isPlayerTurn());
                appendBattleLog();
                saveData();
                
                if (result.isBattleEnded()) {
                    handleBattleEnd(result);
                } else {
                    statusMessage.setValue("AI is thinking...");
                    new android.os.Handler().postDelayed(this::executeAiTurn, 1000);
                }
            } else {
                statusMessage.setValue(result.getMessage());
            }
        }
    }
    
    /**
     * Execute player's defend action
     */
    public void playerDefend() {
        if (Boolean.TRUE.equals(isBattleActive.getValue()) && Boolean.TRUE.equals(isPlayerTurn.getValue())) {
            BattleManager.BattleResult result = battleManager.playerDefend();
            
            if (result.isSuccess()) {
                isPlayerTurn.setValue(battleManager.isPlayerTurn());
                appendBattleLog();
                saveData();
                
                if (result.isBattleEnded()) {
                    handleBattleEnd(result);
                } else {
                    statusMessage.setValue("AI is thinking...");
                    new android.os.Handler().postDelayed(this::executeAiTurn, 1000);
                }
            } else {
                statusMessage.setValue(result.getMessage());
            }
        }
    }
    
    /**
     * Execute AI's turn
     */
    private void executeAiTurn() {
        if (Boolean.TRUE.equals(isBattleActive.getValue()) && !Boolean.TRUE.equals(isPlayerTurn.getValue())) {
            BattleManager.BattleResult result = battleManager.executeAiTurn();
            
            if (result.isSuccess()) {
                isPlayerTurn.setValue(battleManager.isPlayerTurn());
                appendBattleLog();
                saveData();
                
                if (result.isBattleEnded()) {
                    handleBattleEnd(result);
                } else {
                    statusMessage.setValue("Your turn! Choose an action.");
                }
            } else {
                statusMessage.setValue(result.getMessage());
            }
        }
    }
    
    /**
     * Append latest battle log entries to the UI
     */
    private void appendBattleLog() {
        List<BattleManager.BattleAction> actions = battleManager.getBattleLog();
        StringBuilder logBuilder = new StringBuilder();
        
        for (BattleManager.BattleAction action : actions) {
            logBuilder.append(action.getMessage()).append("\n\n");
        }
        
        battleLog.setValue(logBuilder.toString());
        
        // Also update the Lutemon data
        selectedLutemon.setValue(battleManager.getPlayerLutemon());
        aiLutemon.setValue(battleManager.getAiLutemon());
    }
    
    /**
     * Handle battle end and cleanup
     * @param result Battle result with outcome data
     */
    private void handleBattleEnd(BattleManager.BattleResult result) {
        isBattleActive.setValue(false);
        
        appendBattleLog();
        statusMessage.setValue(result.getMessage());
        
        saveData();
        
        battleManager.clearBattleState(application);
    }
    
    /**
     * Manually end the battle (cancel)
     */
    public void endBattle() {
        isBattleActive.setValue(false);
        battleManager.clearBattleState(application);
        statusMessage.setValue("Battle ended.");
    }
    
    public LiveData<List<Lutemon>> getAvailableLutemons() {
        return availableLutemons;
    }
    
    public LiveData<Lutemon> getSelectedLutemon() {
        return selectedLutemon;
    }
    
    public LiveData<Lutemon> getAiLutemon() {
        return aiLutemon;
    }
    
    public LiveData<Boolean> isBattleActive() {
        return isBattleActive;
    }
    
    public LiveData<Boolean> isPlayerTurn() {
        return isPlayerTurn;
    }
    
    public LiveData<String> getBattleLog() {
        return battleLog;
    }
    
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
    
    public int getTotalBattles() {
        return statsManager.getTotalBattles();
    }
    
    public int getBattlesWon() {
        return statsManager.getBattlesWon();
    }
    
    public int getBattlesLost() {
        return statsManager.getBattlesLost();
    }
    
    public int getWinRate() {
        return statsManager.getWinRate();
    }
}