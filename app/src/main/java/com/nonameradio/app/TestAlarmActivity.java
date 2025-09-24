package com.nonameradio.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.nonameradio.app.alarm.RadioAlarmManager;
import com.nonameradio.app.station.DataRadioStation;

import java.util.Calendar;

public class TestAlarmActivity extends Activity {
    private static final String TAG = "TestAlarmActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            NoNameRadioApp app = (NoNameRadioApp) getApplicationContext();
            RadioAlarmManager ram = app.getAlarmManager();

            // Создаем тестовую станцию
            DataRadioStation testStation = new DataRadioStation();
            testStation.Name = "Test Radio Station";
            testStation.StationUuid = "test-station-uuid";
            testStation.StreamUrl = "http://stream.example.com/test.mp3";
            testStation.Working = true;

            // Получаем текущее время + 2 минуты для тестирования
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 2);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Создаем будильник
            ram.add(testStation, hour, minute);

            // Активируем будильник
            int alarmId = ram.getList().length - 1;
            ram.setEnabled(alarmId, true);

            String message = String.format("Test alarm created for %02d:%02d (in 2 minutes)", hour, minute);
            Log.d(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Failed to create test alarm", e);
            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        finish();
    }
}
