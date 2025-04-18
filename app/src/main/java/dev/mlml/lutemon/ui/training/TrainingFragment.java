package dev.mlml.lutemon.ui.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import dev.mlml.lutemon.databinding.FragmentTrainingBinding;
import dev.mlml.lutemon.game.lutemon.Lutemon;

public class TrainingFragment extends Fragment {

    private FragmentTrainingBinding binding;
    private TrainingViewModel trainingViewModel;
    private List<Lutemon> lutemonList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        trainingViewModel = new ViewModelProvider(this).get(TrainingViewModel.class);

        binding = FragmentTrainingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupUI();
        setupObservers();
        setupListeners();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        trainingViewModel.refreshData();
    }

    private void setupUI() {
        updateUIVisibility(false);
    }

    private void setupObservers() {
        trainingViewModel.getAvailableLutemons().observe(getViewLifecycleOwner(), lutemons -> {
            lutemonList = lutemons;
            updateLutemonSpinner();
        });

        trainingViewModel.getSelectedLutemon().observe(getViewLifecycleOwner(), lutemon -> {
            if (lutemon != null) {
                updateLutemonStats(lutemon);
                binding.buttonStartTraining.setEnabled(true);
            } else {
                binding.buttonStartTraining.setEnabled(false);
            }
        });

        trainingViewModel.isTraining().observe(getViewLifecycleOwner(), isTraining -> {
            updateUIVisibility(isTraining);
            binding.buttonStartTraining.setEnabled(!isTraining);
            binding.spinnerLutemonSelector.setEnabled(!isTraining);
        });

        trainingViewModel.getExperienceGained().observe(getViewLifecycleOwner(), expGained -> {
            binding.textExpGained.setText("+" + expGained + " XP");
        });

        trainingViewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.spinnerLutemonSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trainingViewModel.selectLutemon(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.buttonStartTraining.setOnClickListener(v -> trainingViewModel.startTraining());

        binding.buttonTrain.setOnClickListener(v -> trainingViewModel.trainLutemon());

        binding.buttonReturnHome.setOnClickListener(v -> trainingViewModel.endTraining());
    }

    private void updateLutemonSpinner() {
        if (getContext() == null || lutemonList == null) return;

        List<String> lutemonNames = new ArrayList<>();
        for (Lutemon lutemon : lutemonList) {
            lutemonNames.add(lutemon.getName() + " (" + lutemon.getColor() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                lutemonNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLutemonSelector.setAdapter(adapter);
    }

    private void updateLutemonStats(Lutemon lutemon) {
        binding.textLutemonName.setText(lutemon.getName() + " (" + lutemon.getColor() + ")");
        binding.textLutemonLevel.setText("Level: " + lutemon.getLevel());

        binding.textCurrentExp.setText(String.valueOf(lutemon.getExperience()));
        int nextLevelExp = lutemon.getNextLevelExperience();
        binding.textNextLevelExp.setText(String.valueOf(nextLevelExp));

        int progress = (int) (lutemon.getLevelProgress() * 100);
        binding.progressExperience.setProgress(progress);

        binding.textAttack.setText(String.valueOf(lutemon.getAttack()));
        binding.textDefense.setText(String.valueOf(lutemon.getDefense()));
        binding.textHealth.setText(lutemon.getMaxHealth() + "/" + lutemon.getMaxHealth());
        binding.textSpeed.setText(String.valueOf(lutemon.getSpeed()));
    }

    private void updateUIVisibility(boolean isTraining) {
        binding.lutemonSelectorCard.setVisibility(isTraining ? View.GONE : View.VISIBLE);
        
        binding.lutemonStatsCard.setVisibility(View.VISIBLE);
        
        binding.trainingCard.setVisibility(isTraining ? View.VISIBLE : View.GONE);
        
        if (isTraining) {
            binding.buttonReturnHome.setVisibility(View.VISIBLE);
            binding.textTrainingInfo.setText("Tap the TRAIN button repeatedly to train your Lutemon!");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}