package net.programmierecke.radiodroid2.core.domain.controllers;

import net.programmierecke.radiodroid2.core.domain.interfaces.IPlayerService;
import net.programmierecke.radiodroid2.station.DataRadioStation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class PlayerControllerTest {

    @Mock
    private IPlayerService mockPlayerService;

    private PlayerController playerController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        playerController = new PlayerController(mockPlayerService);
    }

    @Test
    public void testPlay() {
        DataRadioStation station = new DataRadioStation();
        station.Name = "Test Station";

        playerController.play(station);

        verify(mockPlayerService).play(station);
        assertEquals(1, playerController.getCommandHistory().size());
        assertTrue(playerController.getCommandHistory().get(0).contains("Test Station"));
    }

    @Test
    public void testPause() {
        playerController.pause();

        verify(mockPlayerService).pause();
        assertEquals(1, playerController.getCommandHistory().size());
        assertTrue(playerController.getCommandHistory().get(0).contains("Pause"));
    }

    @Test
    public void testStartRecording() {
        DataRadioStation station = new DataRadioStation();
        station.Name = "Test Station";

        playerController.startRecording(station);

        verify(mockPlayerService).startRecording();
        assertEquals(1, playerController.getCommandHistory().size());
    }

    @Test
    public void testStopRecording() {
        playerController.stopRecording();

        verify(mockPlayerService).stopRecording();
    }

    @Test
    public void testIsPlaying() {
        when(mockPlayerService.isPlaying()).thenReturn(true);

        assertTrue(playerController.isPlaying());
        verify(mockPlayerService).isPlaying();
    }

    @Test
    public void testIsRecording() {
        when(mockPlayerService.isRecording()).thenReturn(true);

        assertTrue(playerController.isRecording());
        verify(mockPlayerService).isRecording();
    }

    @Test
    public void testUndo() {
        // First play a station
        DataRadioStation station = new DataRadioStation();
        playerController.play(station);

        // Then undo
        playerController.undo();

        verify(mockPlayerService).pause();
    }

    @Test
    public void testClearHistory() {
        playerController.play(new DataRadioStation());
        playerController.pause();

        assertEquals(2, playerController.getCommandHistory().size());

        playerController.clearHistory();
        assertEquals(0, playerController.getCommandHistory().size());
    }
}

