package com.chowking.smartdtr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    // ── Listener interfaces ────────────────────────────────────────────────
    public interface OnResetPasswordListener { void onReset(User user); }
    public interface OnDeactivateListener    { void onDeactivate(User user); }
    public interface OnEditListener          { void onEdit(User user); }

    private List<User> users;
    private final OnResetPasswordListener resetListener;
    private final OnDeactivateListener    deactivateListener;
    private final OnEditListener          editListener;

    /**
     * Full constructor — pass null for editListener where Edit is not needed
     * (e.g. AdminDashboardFragment "recently added" preview list).
     */
    public UserAdapter(List<User> users,
                       OnResetPasswordListener resetListener,
                       OnDeactivateListener deactivateListener,
                       OnEditListener editListener) {
        this.users              = users;
        this.resetListener      = resetListener;
        this.deactivateListener = deactivateListener;
        this.editListener       = editListener;
    }

    /**
     * Backwards-compat constructor for places that don't need edit
     * (AdminDashboardFragment preview, AdminHomeActivity).
     */
    public UserAdapter(List<User> users,
                       OnResetPasswordListener resetListener,
                       OnDeactivateListener deactivateListener) {
        this(users, resetListener, deactivateListener, null);
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        User u = users.get(position);

        h.tvName.setText(u.fullName);
        h.tvId.setText(u.employeeId + " · " + u.role);
        h.tvPosition.setText(u.position != null ? u.position : "—");
        h.tvRate.setText(String.format(Locale.getDefault(), "₱%.2f/hr", u.hourlyRate));

        // Active / inactive badge
        if (u.isActive == 1) {
            h.chipStatus.setText("Active");
            h.chipStatus.setChipBackgroundColorResource(R.color.color_present);
            h.chipStatus.setTextColor(0xFFFFFFFF);
            h.btnToggle.setText("Deactivate");
        } else {
            h.chipStatus.setText("Inactive");
            h.chipStatus.setChipBackgroundColorResource(android.R.color.darker_gray);
            h.chipStatus.setTextColor(0xFFFFFFFF);
            h.btnToggle.setText("Reactivate");
        }

        // Edit button — show only when listener is provided
        if (editListener != null) {
            h.btnEdit.setVisibility(View.VISIBLE);
            h.btnEdit.setOnClickListener(v -> editListener.onEdit(u));
        } else {
            h.btnEdit.setVisibility(View.GONE);
        }

        h.btnReset.setOnClickListener(v -> resetListener.onReset(u));
        h.btnToggle.setOnClickListener(v -> deactivateListener.onDeactivate(u));
    }

    @Override
    public int getItemCount() { return users == null ? 0 : users.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvName, tvId, tvPosition, tvRate;
        Chip           chipStatus;
        MaterialButton btnEdit, btnReset, btnToggle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvUserName);
            tvId       = itemView.findViewById(R.id.tvUserId);
            tvPosition = itemView.findViewById(R.id.tvUserPosition);
            tvRate     = itemView.findViewById(R.id.tvUserRate);
            chipStatus = itemView.findViewById(R.id.chipUserStatus);
            btnEdit    = itemView.findViewById(R.id.btnEditUser);
            btnReset   = itemView.findViewById(R.id.btnResetPassword);
            btnToggle  = itemView.findViewById(R.id.btnToggleActive);
        }
    }
}