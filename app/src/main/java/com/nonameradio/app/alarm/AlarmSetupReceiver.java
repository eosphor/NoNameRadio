package com.nonameradio.app.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.station.DataRadioStation;

public class AlarmSetupReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmSetup";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"com.nonameradio.app.CREATE_TEST_ALARM".equals(intent.getAction())) {
            return;
        }

        try {
            NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();
            RadioAlarmManager ram = app.getAlarmManager();

            // Создаем тестовую станцию
            DataRadioStation testStation = new DataRadioStation();
            testStation.Name = "Test Radio Station";
            testStation.StationUuid = "test-station-uuid";
            testStation.StreamUrl = "http://stream.example.com/test.mp3"; // Тестовый URL
            testStation.Working = true;

            // Получаем текущее время + 2 минуты для тестирования
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 2);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Создаем будильник
            ram.add(testStation, hour, minute);

            // Активируем будильник
            int alarmId = ram.getList().length - 1; // ID последнего добавленного будильника
            ram.setEnabled(alarmId, true);

            String message = String.format("Test alarm created for %02d:%02d (in 2 minutes)", hour, minute);
            Log.d(TAG, message);
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Failed to create test alarm", e);
            Toast.makeText(context, "Failed to create test alarm: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
