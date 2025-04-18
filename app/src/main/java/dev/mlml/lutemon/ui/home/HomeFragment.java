package dev.mlml.lutemon.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.mlml.lutemon.databinding.FragmentHomeBinding;
import dev.mlml.lutemon.game.lutemon.LootBox;
import dev.mlml.lutemon.game.lutemon.Lutemon;
import dev.mlml.lutemon.game.lutemon.LootBoxEffect;

public class HomeFragment extends Fragment implements LutemonAdapter.OnLutemonSoldListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private LutemonAdapter lutemonAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        lutemonAdapter = new LutemonAdapter();
        lutemonAdapter.setOnLutemonSoldListener(this);
        binding.recyclerLutemons.setAdapter(lutemonAdapter);

        TextView currencyTextView = binding.textCurrency;
        RecyclerView lutemonRecyclerView = binding.recyclerLutemons;
        TextView emptyTextView = binding.textEmpty;
        ProgressBar progressBar = binding.progressBar;
        Button basicBoxButton = binding.btnBasicBox;
        Button premiumBoxButton = binding.btnPremiumBox;
        Button legendaryBoxButton = binding.btnLegendaryBox;

        homeViewModel.getLutemons().observe(getViewLifecycleOwner(), lutemons -> {
            lutemonAdapter.submitList(lutemons);
            
            if (lutemons == null || lutemons.isEmpty()) {
                emptyTextView.setVisibility(View.VISIBLE);
                lutemonRecyclerView.setVisibility(View.GONE);
            } else {
                emptyTextView.setVisibility(View.GONE);
                lutemonRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        homeViewModel.getCurrency().observe(getViewLifecycleOwner(), currency -> {
            currencyTextView.setText("Currency: " + currency);
            
            basicBoxButton.setEnabled(homeViewModel.canAffordLootBox(LootBox.Type.BASIC));
            premiumBoxButton.setEnabled(homeViewModel.canAffordLootBox(LootBox.Type.PREMIUM));
            legendaryBoxButton.setEnabled(homeViewModel.canAffordLootBox(LootBox.Type.LEGENDARY));
        });

        homeViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        basicBoxButton.setOnClickListener(v -> openLootBox(LootBox.Type.BASIC));
        premiumBoxButton.setOnClickListener(v -> openLootBox(LootBox.Type.PREMIUM));
        legendaryBoxButton.setOnClickListener(v -> openLootBox(LootBox.Type.LEGENDARY));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        homeViewModel.refreshData();
    }

    @Override
    public void onPause() {
        super.onPause();
        homeViewModel.saveData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Open a loot box and show the rewards
     * @param type Loot box type to open
     */
    private void openLootBox(LootBox.Type type) {
        List<Lutemon> rewards = homeViewModel.purchaseLootBox(type);
        if (rewards != null && !rewards.isEmpty()) {
            showRewards(rewards);
        }
    }

    /**
     * Display a toast with the rewards from a loot box
     * @param rewards List of Lutemons obtained
     */
    private void showRewards(List<Lutemon> rewards) {
        StringBuilder message = new StringBuilder("You got: ");
        for (int i = 0; i < rewards.size(); i++) {
            Lutemon lutemon = rewards.get(i);
            String rarity = LootBoxEffect.calculateRarity(lutemon).name();
            
            if (i > 0) message.append(", ");
            message.append(lutemon.getName())
                   .append(" (")
                   .append(rarity)
                   .append(")");
        }
        
        Toast.makeText(getContext(), message.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLutemonSold(String lutemonId, int sellValue) {
        // Remove the Lutemon from storage
        homeViewModel.removeLutemon(lutemonId);
        
        // Add the currency from selling
        homeViewModel.addCurrency(sellValue);
        
        // Show confirmation message
        Toast.makeText(getContext(), "Lutemon sold for " + sellValue + " coins", Toast.LENGTH_SHORT).show();
    }
}