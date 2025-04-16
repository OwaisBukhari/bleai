package com.example.bledevicesscanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bledevicesscanner.R;
import com.example.bledevicesscanner.model.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder> {

    private final List<BleDevice> devices = new ArrayList<>();
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(BleDevice device);
    }

    public DevicesAdapter(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BleDevice device = devices.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(BleDevice device) {
        if (!devices.contains(device)) {
            devices.add(device);
            notifyItemInserted(devices.size() - 1);
        } else {
            // Update RSSI for existing device
            int index = devices.indexOf(device);
            devices.get(index).setRssi(device.getRssi());
            notifyItemChanged(index);
        }
    }

    public void clearDevices() {
        devices.clear();
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return devices.isEmpty();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final TextView deviceNameTextView;
        private final TextView deviceAddressTextView;
        private final TextView rssiTextView;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.deviceNameTextView);
            deviceAddressTextView = itemView.findViewById(R.id.deviceAddressTextView);
            rssiTextView = itemView.findViewById(R.id.rssiTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeviceClick(devices.get(position));
                }
            });
        }

        void bind(BleDevice device) {
            deviceNameTextView.setText(device.getName());
            deviceAddressTextView.setText(device.getAddress());
            rssiTextView.setText(device.getRssi() + " dBm");
        }
    }
} 