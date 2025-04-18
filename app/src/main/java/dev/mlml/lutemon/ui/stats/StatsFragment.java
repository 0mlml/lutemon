package dev.mlml.lutemon.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dev.mlml.lutemon.databinding.FragmentStatsBinding;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        binding = FragmentStatsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupObservers();
        setupListeners();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshData();
    }

    private void setupObservers() {
        viewModel.getTotalBattles().observe(getViewLifecycleOwner(), 
            value -> binding.textTotalBattles.setText(String.valueOf(value)));
        
        viewModel.getBattlesWon().observe(getViewLifecycleOwner(), 
            value -> binding.textBattlesWon.setText(String.valueOf(value)));
        
        viewModel.getBattlesLost().observe(getViewLifecycleOwner(), 
            value -> binding.textBattlesLost.setText(String.valueOf(value)));
        
        viewModel.getWinRate().observe(getViewLifecycleOwner(), 
            value -> binding.textWinRate.setText(value + "%"));

        viewModel.getTotalTrainingSessions().observe(getViewLifecycleOwner(), 
            value -> binding.textTrainingSessions.setText(String.valueOf(value)));
        
        viewModel.getTotalTrainingClicks().observe(getViewLifecycleOwner(), 
            value -> binding.textTrainingClicks.setText(String.valueOf(value)));

        viewModel.getTotalLootBoxesOpened().observe(getViewLifecycleOwner(), 
            value -> binding.textLootBoxes.setText(String.valueOf(value)));
        
        viewModel.getTotalLutemonsCollected().observe(getViewLifecycleOwner(), 
            value -> binding.textLutemonsCollected.setText(String.valueOf(value)));
        
        viewModel.getHighestLevelReached().observe(getViewLifecycleOwner(), 
            value -> binding.textHighestLevel.setText(String.valueOf(value)));

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.buttonResetStats.setOnClickListener(v -> showResetConfirmation());
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Statistics")
                .setMessage("Are you sure you want to reset all statistics? This cannot be undone.")
                .setPositiveButton("Reset", (dialog, which) -> viewModel.resetStatistics())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 