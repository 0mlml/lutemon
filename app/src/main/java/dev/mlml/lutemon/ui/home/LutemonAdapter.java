package dev.mlml.lutemon.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.mlml.lutemon.R;
import dev.mlml.lutemon.game.lutemon.LootBoxManager;
import dev.mlml.lutemon.game.lutemon.Lutemon;
import dev.mlml.lutemon.game.lutemon.LootBoxEffect;
import dev.mlml.lutemon.game.lutemon.LutemonStorage;

public class LutemonAdapter extends ListAdapter<Lutemon, LutemonAdapter.LutemonViewHolder> {

    private OnLutemonSoldListener onLutemonSoldListener;

    public interface OnLutemonSoldListener {
        void onLutemonSold(String lutemonId, int sellValue);
    }

    public LutemonAdapter() {
        super(new LutemonDiffCallback());
    }

    public void setOnLutemonSoldListener(OnLutemonSoldListener listener) {
        this.onLutemonSoldListener = listener;
    }

    @NonNull
    @Override
    public LutemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lutemon, parent, false);
        return new LutemonViewHolder(view, onLutemonSoldListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LutemonViewHolder holder, int position) {
        Lutemon lutemon = getItem(position);
        holder.bind(lutemon);
    }

    @Override
    public void onCurrentListChanged(@NonNull List<Lutemon> previousList, @NonNull List<Lutemon> currentList) {
        super.onCurrentListChanged(previousList, currentList);
        // This gets called after DiffUtil has calculated the differences and applied them
        // It provides a good spot for any additional cleanup or verification if needed
    }

    public static class LutemonViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final View colorView;
        private final TextView nameTextView;
        private final TextView healthTextView;
        private final TextView attackTextView;
        private final TextView defenseTextView;
        private final TextView speedTextView;
        private final TextView experienceTextView;
        private final Context context;
        private Lutemon lutemon;
        private final OnLutemonSoldListener listener;

        public LutemonViewHolder(@NonNull View itemView, OnLutemonSoldListener listener) {
            super(itemView);
            context = itemView.getContext();
            this.listener = listener;
            imageView = itemView.findViewById(R.id.img_lutemon);
            colorView = itemView.findViewById(R.id.view_color);
            nameTextView = itemView.findViewById(R.id.tv_lutemon_name);
            healthTextView = itemView.findViewById(R.id.tv_lutemon_health);
            attackTextView = itemView.findViewById(R.id.tv_lutemon_attack);
            defenseTextView = itemView.findViewById(R.id.tv_lutemon_defense);
            speedTextView = itemView.findViewById(R.id.tv_lutemon_speed);
            experienceTextView = itemView.findViewById(R.id.tv_lutemon_experience);

            itemView.setOnClickListener(v -> {
                if (lutemon != null) {
                    showLutemonDetails();
                }
            });
        }

        public void bind(Lutemon lutemon) {
            this.lutemon = lutemon;

            loadLutemonImage(lutemon.getSchemaId());
            
            colorView.setBackgroundColor(getColorForLutemon(lutemon.getColor()));

            LootBoxEffect.Rarity rarity = LootBoxEffect.calculateRarity(lutemon);
            int textColor = getTextColorForRarity(rarity);
            nameTextView.setTextColor(textColor);

            nameTextView.setText(lutemon.getName());
            healthTextView.setText(String.format("HP: %d/%d", lutemon.getCurrentHealth(), lutemon.getMaxHealth()));
            attackTextView.setText(String.format("ATK: %d", lutemon.getAttack()));
            defenseTextView.setText(String.format("DEF: %d", lutemon.getDefense()));
            speedTextView.setText(String.format("SPD: %d", lutemon.getSpeed()));
            experienceTextView.setText(String.format("XP: %d (Lvl %d)", lutemon.getExperience(), lutemon.getLevel()));
        }
        
        /**
         * Show a detailed dialog for the Lutemon with option to sell
         */
        private void showLutemonDetails() {
            LootBoxEffect.Rarity rarity = LootBoxEffect.calculateRarity(lutemon);
            int sellValue = calculateSellValue(rarity);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_lutemon_details, null);
            
            ImageView imageView = dialogView.findViewById(R.id.dialog_lutemon_image);
            TextView nameText = dialogView.findViewById(R.id.dialog_lutemon_name);
            TextView levelText = dialogView.findViewById(R.id.dialog_lutemon_level);
            TextView rarityText = dialogView.findViewById(R.id.dialog_lutemon_rarity);
            TextView healthText = dialogView.findViewById(R.id.dialog_lutemon_health);
            TextView attackText = dialogView.findViewById(R.id.dialog_lutemon_attack);
            TextView defenseText = dialogView.findViewById(R.id.dialog_lutemon_defense);
            TextView speedText = dialogView.findViewById(R.id.dialog_lutemon_speed);
            TextView descriptionText = dialogView.findViewById(R.id.dialog_lutemon_description);
            Button sellButton = dialogView.findViewById(R.id.dialog_sell_button);
            
            int imageResId = getImageResource(lutemon.getSchemaId());
            imageView.setImageResource(imageResId);
            
            nameText.setText(lutemon.getName());
            nameText.setTextColor(getTextColorForRarity(rarity));
            levelText.setText("Level " + lutemon.getLevel());
            rarityText.setText(rarity.name());
            rarityText.setTextColor(getTextColorForRarity(rarity));
            healthText.setText("HP: " + lutemon.getCurrentHealth() + "/" + lutemon.getMaxHealth());
            attackText.setText("Attack: " + lutemon.getAttack());
            defenseText.setText("Defense: " + lutemon.getDefense());
            speedText.setText("Speed: " + lutemon.getSpeed());
            
            String rarityDescription = LootBoxEffect.getRarityDescription(lutemon);
            descriptionText.setText(rarityDescription);
            
            // Initialize dialog before the click listener to avoid null reference
            AlertDialog dialog = builder.setView(dialogView).create();
            
            sellButton.setText("Sell for " + sellValue + " coins");
            sellButton.setOnClickListener(v -> {
                if (listener != null) {
                    // Dismiss dialog first to avoid RecyclerView inconsistency
                    dialog.dismiss();
                    
                    // Then notify the listener about the sold Lutemon
                    listener.onLutemonSold(lutemon.getId(), sellValue);
                }
            });
            
            dialog.show();
        }
        
        /**
         * Calculate the sell value of a Lutemon based on its rarity and stats
         */
        private int calculateSellValue(LootBoxEffect.Rarity rarity) {
            int baseValue;
            switch (rarity) {
                case LEGENDARY:
                    baseValue = 500;
                    break;
                case EPIC:
                    baseValue = 300;
                    break;
                case RARE:
                    baseValue = 150;
                    break;
                case COMMON:
                default:
                    baseValue = 50;
                    break;
            }
            
            int levelBonus = lutemon.getLevel() * 20;
            
            int statBonus = (int)(lutemon.getAttack() * 2 + 
                             lutemon.getDefense() * 1.5 + 
                             lutemon.getMaxHealth() * 0.5 + 
                             lutemon.getSpeed() * 1.5);
            
            return baseValue + levelBonus + statBonus;
        }

        @SuppressLint("DiscouragedApi")
        private void loadLutemonImage(String schemaId) {
            int imageResId = getImageResource(schemaId);
            imageView.setImageResource(imageResId);
        }
        
        @SuppressLint("DiscouragedApi")
        private int getImageResource(String schemaId) {
            int imageResId = R.drawable.ic_lutemon_unknown;
            
            if (schemaId != null && !schemaId.isEmpty() && !schemaId.equals("unknown")) {
                String resourceName = "ic_lutemon_" + schemaId.toLowerCase();
                int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
                
                if (resId != 0) {
                    imageResId = resId;
                } else {
                    if (schemaId.contains("_")) {
                        String baseType = schemaId.split("_")[0].toLowerCase();
                        String baseResourceName = "ic_lutemon_" + baseType + "_1";
                        int baseResId = context.getResources().getIdentifier(baseResourceName, "drawable", context.getPackageName());
                        
                        if (baseResId != 0) {
                            imageResId = baseResId;
                        }
                    }
                }
            }
            
            return imageResId;
        }

        private int getColorForLutemon(String colorName) {
            switch (colorName.toLowerCase()) {
                case "red":
                    return Color.rgb(255, 90, 90);
                case "green":
                    return Color.rgb(90, 255, 90);
                case "blue":
                    return Color.rgb(90, 90, 255);
                case "yellow":
                    return Color.rgb(255, 255, 90);
                case "purple":
                    return Color.rgb(200, 90, 255);
                default:
                    return Color.GRAY;
            }
        }

        private int getTextColorForRarity(LootBoxEffect.Rarity rarity) {
            switch (rarity) {
                case LEGENDARY:
                    return Color.rgb(255, 215, 0); // Gold
                case EPIC:
                    return Color.rgb(170, 0, 255); // Purple
                case RARE:
                    return Color.rgb(30, 144, 255); // Blue
                case COMMON:
                default:
                    return Color.BLACK;
            }
        }
    }

    private static class LutemonDiffCallback extends DiffUtil.ItemCallback<Lutemon> {
        @Override
        public boolean areItemsTheSame(@NonNull Lutemon oldItem, @NonNull Lutemon newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Lutemon oldItem, @NonNull Lutemon newItem) {
            // First compare IDs to make sure we're comparing the same Lutemon
            if (!oldItem.getId().equals(newItem.getId())) {
                return false;
            }
            
            // Then compare relevant properties
            return oldItem.getCurrentHealth() == newItem.getCurrentHealth() &&
                    oldItem.getAttack() == newItem.getAttack() &&
                    oldItem.getDefense() == newItem.getDefense() &&
                    oldItem.getSpeed() == newItem.getSpeed() &&
                    oldItem.getExperience() == newItem.getExperience();
        }
    }
} 