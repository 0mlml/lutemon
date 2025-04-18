package dev.mlml.lutemon.ui.training;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import dev.mlml.lutemon.game.lutemon.Lutemon;
import dev.mlml.lutemon.game.lutemon.LutemonStorage;
import dev.mlml.lutemon.game.lutemon.StatsManager;
import dev.mlml.lutemon.game.lutemon.TrainingManager;

public class TrainingViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Lutemon>> availableLutemons = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Lutemon> selectedLutemon = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTraining = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> experienceGained = new MutableLiveData<>(0);
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    
    private final LutemonStorage lutemonStorage;
    private final TrainingManager trainingManager;
    private final StatsManager statsManager;
    private final Application application;

    public TrainingViewModel(Application application) {
        super(application);
        this.application = application;
        this.lutemonStorage = LutemonStorage.getInstance();
        this.trainingManager = TrainingManager.getInstance();
        this.statsManager = StatsManager.getInstance();
        
        refreshData();
    }
    
    /**
     * Refresh data from storage
     */
    public void refreshData() {
        lutemonStorage.loadSavedLutemons(application);
        trainingManager.loadTrainingState(application);
        statsManager.loadStats(application);
        
        availableLutemons.setValue(lutemonStorage.getLutemons());
        
        updateTrainingStatus();
    }
    
    /**
     * Check which Lutemons are in training and update status
     */
    private void updateTrainingStatus() {
        List<String> trainingIds = trainingManager.getTrainingLutemonIds();
        
        if (!trainingIds.isEmpty()) {
            String lutemonId = trainingIds.get(0);
            Lutemon lutemon = lutemonStorage.getLutemonById(lutemonId);
            
            if (lutemon != null) {
                isTraining.setValue(true);
                selectedLutemon.setValue(lutemon);
            } else {
                trainingManager.removeLutemonFromTraining(lutemonId);
                isTraining.setValue(false);
                selectedLutemon.setValue(null);
            }
        } else {
            isTraining.setValue(false);
            selectedLutemon.setValue(null);
        }
    }
    
    /**
     * Save changes to permanent storage
     */
    public void saveData() {
        lutemonStorage.saveLutemons(application);
        trainingManager.saveTrainingState(application);
        statsManager.saveStats(application);
    }
    
    /**
     * Set the Lutemon to be trained
     * @param position Position in the spinner
     */
    public void selectLutemon(int position) {
        List<Lutemon> lutemons = availableLutemons.getValue();
        if (lutemons != null && position >= 0 && position < lutemons.size()) {
            selectedLutemon.setValue(lutemons.get(position));
        }
    }
    
    /**
     * Start training the selected Lutemon
     */
    public void startTraining() {
        Lutemon lutemon = selectedLutemon.getValue();
        
        if (lutemon != null) {
            trainingManager.addLutemonToTraining(lutemon.getId());
            statsManager.incrementTotalTrainingSessions();
            isTraining.setValue(true);
            experienceGained.setValue(0);
            saveData();
            statusMessage.setValue(lutemon.getName() + " is now training!");
        } else {
            statusMessage.setValue("No Lutemon selected for training");
        }
    }
    
    /**
     * Perform a training click
     */
    public void trainLutemon() {
        Lutemon lutemon = selectedLutemon.getValue();
        
        if (lutemon != null && Boolean.TRUE.equals(isTraining.getValue())) {
            int clickCount = 1;
            TrainingManager.TrainingResult result = trainingManager.trainLutemon(lutemon, clickCount);
            statsManager.incrementTotalTrainingClicks();

            Integer currentExp = experienceGained.getValue();
            experienceGained.setValue((currentExp != null ? currentExp : 0) + result.experienceGained());
            
            saveData();
            
            selectedLutemon.setValue(lutemonStorage.getLutemonById(lutemon.getId()));

            if (result.levelsGained() > 0) {
                statusMessage.setValue(lutemon.getName() + " gained " + result.levelsGained() + " level(s)!");
            }
        }
    }
    
    /**
     * End training and return Lutemon home
     */
    public void endTraining() {
        Lutemon lutemon = selectedLutemon.getValue();
        
        if (lutemon != null) {
            trainingManager.removeLutemonFromTraining(lutemon.getId());
            isTraining.setValue(false);
            saveData();
            statusMessage.setValue(lutemon.getName() + " returned home with " + 
                    experienceGained.getValue() + " experience points!");
            experienceGained.setValue(0);
        }
    }
    
    public LiveData<List<Lutemon>> getAvailableLutemons() {
        return availableLutemons;
    }
    
    public LiveData<Lutemon> getSelectedLutemon() {
        return selectedLutemon;
    }
    
    public LiveData<Boolean> isTraining() {
        return isTraining;
    }
    
    public LiveData<Integer> getExperienceGained() {
        return experienceGained;
    }
    
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
}