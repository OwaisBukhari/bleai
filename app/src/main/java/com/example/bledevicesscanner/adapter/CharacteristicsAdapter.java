package com.example.bledevicesscanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bledevicesscanner.R;
import com.example.bledevicesscanner.model.BleCharacteristic;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CharacteristicsAdapter extends RecyclerView.Adapter<CharacteristicsAdapter.CharacteristicViewHolder> {

    private final List<BleCharacteristic> characteristics = new ArrayList<>();
    private OnCharacteristicActionListener listener;

    public interface OnCharacteristicActionListener {
        void onReadCharacteristic(BleCharacteristic characteristic);
    }

    public CharacteristicsAdapter(OnCharacteristicActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CharacteristicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_characteristic, parent, false);
        return new CharacteristicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacteristicViewHolder holder, int position) {
        BleCharacteristic characteristic = characteristics.get(position);
        holder.bind(characteristic);
    }

    @Override
    public int getItemCount() {
        return characteristics.size();
    }

    public void setCharacteristics(List<BleCharacteristic> characteristics) {
        this.characteristics.clear();
        if (characteristics != null) {
            this.characteristics.addAll(characteristics);
        }
        notifyDataSetChanged();
    }

    public void updateCharacteristic(BleCharacteristic characteristic) {
        for (int i = 0; i < characteristics.size(); i++) {
            if (characteristics.get(i).getUuid().equals(characteristic.getUuid())) {
                characteristics.set(i, characteristic);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public boolean isEmpty() {
        return characteristics.isEmpty();
    }

    class CharacteristicViewHolder extends RecyclerView.ViewHolder {
        private final TextView characteristicUuidTextView;
        private final TextView characteristicPropertiesTextView;
        private final TextView characteristicValueTextView;
        private final MaterialButton readCharacteristicButton;

        CharacteristicViewHolder(@NonNull View itemView) {
            super(itemView);
            characteristicUuidTextView = itemView.findViewById(R.id.characteristicUuidTextView);
            characteristicPropertiesTextView = itemView.findViewById(R.id.characteristicPropertiesTextView);
            characteristicValueTextView = itemView.findViewById(R.id.characteristicValueTextView);
            readCharacteristicButton = itemView.findViewById(R.id.readCharacteristicButton);
        }

        void bind(BleCharacteristic characteristic) {
            characteristicUuidTextView.setText(characteristic.getUuid());
            characteristicPropertiesTextView.setText(characteristic.getPropertiesString());
            characteristicValueTextView.setText(characteristic.getValueAsString());

            if (characteristic.isReadable()) {
                readCharacteristicButton.setVisibility(View.VISIBLE);
                readCharacteristicButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onReadCharacteristic(characteristic);
                    }
                });
            } else {
                readCharacteristicButton.setVisibility(View.GONE);
            }
        }
    }
} 