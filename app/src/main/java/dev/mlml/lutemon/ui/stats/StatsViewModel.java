package dev.mlml.lutemon.ui.stats;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import dev.mlml.lutemon.game.lutemon.LutemonStorage;
import dev.mlml.lutemon.game.lutemon.StatsManager;

public class StatsViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> totalBattles = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> battlesWon = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> battlesLost = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> winRate = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalTrainingSessions = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalTrainingClicks = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalLootBoxesOpened = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalLutemonsCollected = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> highestLevelReached = new MutableLiveData<>(1);
    
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    
    private final StatsManager statsManager;
    private final Application application;
    
    public StatsViewModel(Application application) {
        super(application);
        this.application = application;
        this.statsManager = StatsManager.getInstance();
        
        refreshData();
    }
    
    /**
     * Refresh statistics data from storage
     */
    public void refreshData() {
        statsManager.loadStats(application);
        
        totalBattles.setValue(statsManager.getTotalBattles());
        battlesWon.setValue(statsManager.getBattlesWon());
        battlesLost.setValue(statsManager.getBattlesLost());
        winRate.setValue(statsManager.getWinRate());
        totalTrainingSessions.setValue(statsManager.getTotalTrainingSessions());
        totalTrainingClicks.setValue(statsManager.getTotalTrainingClicks());
        totalLootBoxesOpened.setValue(statsManager.getTotalLootBoxesOpened());
        totalLutemonsCollected.setValue(statsManager.getTotalLutemonsCollected());
        highestLevelReached.setValue(statsManager.getHighestLevelReached());
    }
    
    /**
     * Reset all statistics
     */
    public void resetStatistics() {
        statsManager.resetStats(application);
        refreshData();
        statusMessage.setValue("Statistics have been reset.");
    }
    
    public LiveData<Integer> getTotalBattles() {
        return totalBattles;
    }
    
    public LiveData<Integer> getBattlesWon() {
        return battlesWon;
    }
    
    public LiveData<Integer> getBattlesLost() {
        return battlesLost;
    }
    
    public LiveData<Integer> getWinRate() {
        return winRate;
    }
    
    public LiveData<Integer> getTotalTrainingSessions() {
        return totalTrainingSessions;
    }
    
    public LiveData<Integer> getTotalTrainingClicks() {
        return totalTrainingClicks;
    }
    
    public LiveData<Integer> getTotalLootBoxesOpened() {
        return totalLootBoxesOpened;
    }
    
    public LiveData<Integer> getTotalLutemonsCollected() {
        return totalLutemonsCollected;
    }
    
    public LiveData<Integer> getHighestLevelReached() {
        return highestLevelReached;
    }
    
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
} 