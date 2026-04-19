package com.chowking.smartdtr.ui.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.viewmodel.AttendanceViewModel;
import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ManagerScanFragment extends Fragment {

    private AttendanceViewModel viewModel;
    private TextView tvScanResult;
    private TextView tvLastScanned;

    private final ActivityResultLauncher<ScanOptions> qrLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    handleScan(result.getContents());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel     = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);
        tvScanResult  = view.findViewById(R.id.tvScanResult);
        tvLastScanned = view.findViewById(R.id.tvLastScanned);

        MaterialButton btnScan = view.findViewById(R.id.btnScan);
        btnScan.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan crew member QR code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(false);
            qrLauncher.launch(options);
        });
    }

    private void handleScan(String scannedId) {
        tvLastScanned.setVisibility(View.VISIBLE);
        tvLastScanned.setText("Employee ID: " + scannedId);

        viewModel.recordAttendance(scannedId).observe(getViewLifecycleOwner(), status -> {
            tvScanResult.setVisibility(View.VISIBLE);
            switch (status) {
                case "TIME_IN":
                    tvScanResult.setText("✓ " + scannedId + " — Timed IN successfully!");
                    tvScanResult.setTextColor(requireContext().getColor(R.color.color_present));
                    break;
                case "TIME_OUT":
                    tvScanResult.setText("✓ " + scannedId + " — Timed OUT successfully!");
                    tvScanResult.setTextColor(requireContext().getColor(R.color.color_still_in));
                    break;
                default:
                    tvScanResult.setText("✗ Error recording attendance for " + scannedId + ". Try again.");
                    tvScanResult.setTextColor(requireContext().getColor(R.color.color_absent));
            }
        });
    }
}