package com.chowking.smartdtr.database;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.utils.SessionManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncManager {
    private static final String CHANNEL_ID = "attendance_sync_channel";
    private final Context context;
    private final AppDatabase db;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;
    private final SessionManager session;

    public SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getInstance(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        this.session = new SessionManager(this.context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Attendance Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for employee clock in/out");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void syncFromCloud() {
        startRealtimeSync();
        
        executor.execute(() -> {
            Log.d("SyncManager", "Performing initial sync...");
            firestore.collection("users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    executor.execute(() -> {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            User localUser = db.userDao().getUserByEmployeeId(user.employeeId);
                            if (localUser == null) {
                                db.userDao().insertUser(user);
                            } else {
                                user.id = localUser.id;
                                db.userDao().updateUser(user);
                            }
                        }
                    });
                }
            });
        });
    }

    public void startRealtimeSync() {
        Log.d("SyncManager", "Starting real-time sync for attendance...");

        firestore.collection("attendance").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("SyncManager", "Listen failed.", error);
                return;
            }

            if (value != null) {
                executor.execute(() -> {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        AttendanceRecord record = dc.getDocument().toObject(AttendanceRecord.class);
                        AttendanceRecord existing = db.attendanceDao()
                                .getRecordSpecificSync(record.employeeId, record.date, record.timeIn);

                        switch (dc.getType()) {
                            case ADDED:
                                if (existing == null) {
                                    record.id = 0;
                                    db.attendanceDao().insertRecord(record);
                                    checkAndNotify(record, "Clocked In");
                                }
                                break;
                            case MODIFIED:
                                if (existing != null) {
                                    if (existing.timeOut == 0 && record.timeOut != 0) {
                                        checkAndNotify(record, "Clocked Out");
                                    }
                                    record.id = existing.id;
                                    db.attendanceDao().updateRecord(record);
                                } else {
                                    record.id = 0;
                                    db.attendanceDao().insertRecord(record);
                                }
                                break;
                            case REMOVED:
                                if (existing != null) {
                                    db.attendanceDao().deleteRecord(existing.id);
                                }
                                break;
                        }
                    }
                });
            }
        });
    }

    private void checkAndNotify(AttendanceRecord record, String action) {
        String currentRole = session.getRole();
        String currentEmpId = session.getEmployeeId();

        // Admin/Manager get notifications for EVERYONE
        if ("ADMIN".equals(currentRole) || "MANAGER".equals(currentRole)) {
            sendNotification(record, action, "Staff Alert: " + record.employeeId);
        } 
        // Crew only gets notifications for THEMSELVES (e.g., if clocked in by manager via scan)
        else if ("CREW".equals(currentRole) && record.employeeId.equals(currentEmpId)) {
            sendNotification(record, action, "Attendance Confirmed");
        }
    }

    private void sendNotification(AttendanceRecord record, String action, String title) {
        String message = (record.employeeId.equals(session.getEmployeeId()) ? "You have " : "Employee " + record.employeeId + " has ") 
                + action.toLowerCase() + " (" + record.date + ")";
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
