package dev.mlml.lutemon.ui.battle;

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

import dev.mlml.lutemon.databinding.FragmentBattleBinding;
import dev.mlml.lutemon.game.lutemon.Lutemon;

public class BattleFragment extends Fragment {

    private FragmentBattleBinding binding;
    private BattleViewModel battleViewModel;
    private List<Lutemon> lutemonList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        battleViewModel = new ViewModelProvider(this).get(BattleViewModel.class);

        binding = FragmentBattleBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupUI();
        setupObservers();
        setupListeners();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        battleViewModel.refreshData();
    }

    private void setupUI() {
        binding.battleCard.setVisibility(View.GONE);
        binding.lutemonSelectorCard.setVisibility(View.VISIBLE);
        
        binding.buttonStartBattle.setEnabled(false);
    }

    private void setupObservers() {
        battleViewModel.getAvailableLutemons().observe(getViewLifecycleOwner(), lutemons -> {
            lutemonList = lutemons;
            updateLutemonSpinner();
        });

        battleViewModel.getSelectedLutemon().observe(getViewLifecycleOwner(), lutemon -> {
            if (lutemon != null) {
                updatePlayerLutemonUI(lutemon);
            }
        });

        battleViewModel.getAiLutemon().observe(getViewLifecycleOwner(), lutemon -> {
            if (lutemon != null) {
                updateAILutemonUI(lutemon);
                binding.buttonStartBattle.setEnabled(true);
            } else {
                binding.buttonStartBattle.setEnabled(false);
            }
        });

        battleViewModel.isBattleActive().observe(getViewLifecycleOwner(), isActive -> {
            binding.battleCard.setVisibility(isActive ? View.VISIBLE : View.GONE);
            binding.lutemonSelectorCard.setVisibility(isActive ? View.GONE : View.VISIBLE);
            
            binding.buttonAttack.setEnabled(isActive);
            binding.buttonDefend.setEnabled(isActive);
        });

        battleViewModel.isPlayerTurn().observe(getViewLifecycleOwner(), isPlayerTurn -> {
            binding.textTurnIndicator.setText(isPlayerTurn ? "Your Turn!" : "AI's Turn!");
            
            binding.buttonAttack.setEnabled(isPlayerTurn);
            binding.buttonDefend.setEnabled(isPlayerTurn);
        });

        battleViewModel.getBattleLog().observe(getViewLifecycleOwner(), log -> {
            binding.textBattleLog.setText(log);
            
            binding.scrollBattleLog.post(() -> binding.scrollBattleLog.fullScroll(View.FOCUS_DOWN));
        });

        battleViewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                binding.textBattleStatus.setText(message);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.spinnerLutemonSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                battleViewModel.selectLutemon(position);
                battleViewModel.generateRandomOpponent();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.buttonFindRandomOpponent.setOnClickListener(v -> battleViewModel.generateRandomOpponent());

        binding.buttonStartBattle.setOnClickListener(v -> battleViewModel.startBattle());

        binding.buttonAttack.setOnClickListener(v -> battleViewModel.playerAttack());

        binding.buttonDefend.setOnClickListener(v -> battleViewModel.playerDefend());

        binding.buttonEndBattle.setOnClickListener(v -> battleViewModel.endBattle());
    }

    private void updateLutemonSpinner() {
        if (getContext() == null || lutemonList == null) return;

        List<String> lutemonNames = new ArrayList<>();
        for (Lutemon lutemon : lutemonList) {
            lutemonNames.add(lutemon.getName() + " (" + lutemon.getColor() + ") - LVL: " + lutemon.getLevel());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                lutemonNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLutemonSelector.setAdapter(adapter);
    }

    private void updatePlayerLutemonUI(Lutemon lutemon) {
        binding.textPlayerLutemonName.setText(
                lutemon.getName() + " (Lvl " + lutemon.getLevel() + ")");

        int maxHealth = lutemon.getMaxHealth();
        int currentHealth = lutemon.getCurrentHealth();
        int healthPercentage = (int) (((float) currentHealth / maxHealth) * 100);
        
        binding.progressPlayerHealth.setMax(maxHealth);
        binding.progressPlayerHealth.setProgress(currentHealth);
        binding.textPlayerHealth.setText("HP: " + currentHealth + "/" + maxHealth);

        binding.textPlayerStats.setText(
                "ATK: " + lutemon.getAttack() + " | DEF: " + lutemon.getDefense() +
                " | SPD: " + lutemon.getSpeed());
    }

    private void updateAILutemonUI(Lutemon lutemon) {
        binding.textAiLutemonName.setText(
                lutemon.getName() + " (Lvl " + lutemon.getLevel() + ")");

        int maxHealth = lutemon.getMaxHealth();
        int currentHealth = lutemon.getCurrentHealth();
        int healthPercentage = (int) (((float) currentHealth / maxHealth) * 100);
        
        binding.progressAiHealth.setMax(maxHealth);
        binding.progressAiHealth.setProgress(currentHealth);
        binding.textAiHealth.setText("HP: " + currentHealth + "/" + maxHealth);

        binding.textAiStats.setText(
                "ATK: " + lutemon.getAttack() + " | DEF: " + lutemon.getDefense() +
                " | SPD: " + lutemon.getSpeed());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}