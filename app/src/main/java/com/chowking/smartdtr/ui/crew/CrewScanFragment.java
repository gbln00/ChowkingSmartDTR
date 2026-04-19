package com.chowking.smartdtr.ui.crew;

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

public class CrewScanFragment extends Fragment {

    private AttendanceViewModel viewModel;
    private TextView tvScanResult;

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
        return inflater.inflate(R.layout.fragment_crew_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel    = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);
        tvScanResult = view.findViewById(R.id.tvScanResult);

        MaterialButton btnScan = view.findViewById(R.id.btnScan);
        btnScan.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan employee QR code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            qrLauncher.launch(options);
        });
    }

    private void handleScan(String scannedId) {
        viewModel.recordAttendance(scannedId).observe(getViewLifecycleOwner(), status -> {
            tvScanResult.setVisibility(View.VISIBLE);
            switch (status) {
                case "TIME_IN":
                    tvScanResult.setText("Timed IN successfully!");
                    tvScanResult.setTextColor(requireContext().getColor(R.color.color_present));
                    notifyDashboard();
                    break;
                case "TIME_OUT":
                    tvScanResult.setText("Timed OUT successfully!");
                    tvScanResult.setTextColor(requireContext().getColor(R.color.color_still_in));
                    notifyDashboard();
                    break;
                default:
                    tvScanResult.setText("Error recording attendance. Try again.");
                    tvScanResult.setTextColor(requireContext().getColor(R.color.color_absent));
            }
        });
    }

    private void notifyDashboard() {
        Fragment dashboard = getParentFragmentManager()
                .findFragmentByTag("f" + R.id.nav_crew_home);
        if (dashboard instanceof CrewDashboardFragment) {
            ((CrewDashboardFragment) dashboard).refreshStatus();
        }
    }
}