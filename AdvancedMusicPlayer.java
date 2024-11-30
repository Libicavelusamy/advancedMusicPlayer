import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.sound.sampled.*;
import javax.swing.*;

public class AdvancedMusicPlayer extends Frame implements ActionListener, AdjustmentListener {

    private Button playButton, pauseButton, stopButton, loadButton, exitButton;
    private Label songLabel, statusLabel, timeLabel;
    private Scrollbar volumeSlider, seekSlider;
    private Clip audioClip;
    private boolean isPaused = false;
    private long pausePosition = 0;
    private Timer timer;

    public AdvancedMusicPlayer() {
        // Frame setup
        setTitle("Advanced Music Player");
        setSize(700, 500);
        setLayout(null);
        setBackground(new Color(248, 249, 250)); // Mild light gray background

        // Labels
        songLabel = new Label("No file loaded", Label.CENTER);
        songLabel.setBounds(50, 50, 600, 30);
        songLabel.setFont(new Font("Arial", Font.BOLD, 16));
        songLabel.setForeground(new Color(33, 150, 243)); // Blue text
        add(songLabel);

        statusLabel = new Label("Status: Idle", Label.CENTER);
        statusLabel.setBounds(50, 90, 600, 30);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setForeground(new Color(85, 139, 47)); // Green text
        add(statusLabel);

        timeLabel = new Label("00:00 / 00:00", Label.CENTER);
        timeLabel.setBounds(50, 130, 600, 30);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(timeLabel);

        // Buttons
        loadButton = new Button("Load");
        playButton = new Button("Play");
        pauseButton = new Button("Pause");
        stopButton = new Button("Stop");
        exitButton = new Button("Exit");

        // Button dimensions
        loadButton.setBounds(50, 190, 120, 50);
        playButton.setBounds(190, 190, 120, 50);
        pauseButton.setBounds(330, 190, 120, 50);
        stopButton.setBounds(470, 190, 120, 50);
        exitButton.setBounds(610, 190, 70, 50);

        // Button colors and fonts
        Color buttonColor = new Color(129, 212, 250); // Light blue
        loadButton.setBackground(buttonColor);
        playButton.setBackground(buttonColor);
        pauseButton.setBackground(buttonColor);
        stopButton.setBackground(buttonColor);
        exitButton.setBackground(new Color(255, 183, 77)); // Light orange

        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        loadButton.setFont(buttonFont);
        playButton.setFont(buttonFont);
        pauseButton.setFont(buttonFont);
        stopButton.setFont(buttonFont);
        exitButton.setFont(buttonFont);

        // Add buttons to frame
        add(loadButton);
        add(playButton);
        add(pauseButton);
        add(stopButton);
        add(exitButton);

        // Volume slider
        volumeSlider = new Scrollbar(Scrollbar.HORIZONTAL, 50, 1, 0, 101); // Default at 50%
        volumeSlider.setBounds(50, 270, 600, 30);
        volumeSlider.setBackground(new Color(229, 243, 255)); // Mild blue
        add(volumeSlider);

        Label volumeLabel = new Label("Volume", Label.CENTER);
        volumeLabel.setBounds(50, 310, 600, 30);
        volumeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(volumeLabel);

        // Seek slider
        seekSlider = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 0, 100);
        seekSlider.setBounds(50, 360, 600, 30);
        seekSlider.setBackground(new Color(229, 243, 255)); // Mild blue
        add(seekSlider);

        Label seekLabel = new Label("Seek", Label.CENTER);
        seekLabel.setBounds(50, 400, 600, 30);
        seekLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(seekLabel);

        // Button listeners
        loadButton.addActionListener(this);
        playButton.addActionListener(this);
        pauseButton.addActionListener(this);
        stopButton.addActionListener(this);
        exitButton.addActionListener(e -> System.exit(0)); // Exit button functionality

        // Slider listener
        volumeSlider.addAdjustmentListener(this);
        seekSlider.addAdjustmentListener(this);

        // Window close functionality
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (audioClip != null) {
                    audioClip.close();
                }
                System.exit(0);
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadButton) {
            loadAudioFile();
        } else if (e.getSource() == playButton) {
            playAudio();
        } else if (e.getSource() == pauseButton) {
            pauseAudio();
        } else if (e.getSource() == stopButton) {
            stopAudio();
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == volumeSlider && audioClip != null) {
            // Adjust volume based on slider value with more precision
            FloatControl volume = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);

            // Map the slider value to the volume range (-80.0 to 6.0 dB)
            float volumeValue = (float) volumeSlider.getValue();
            float minVolume = volume.getMinimum();
            float maxVolume = volume.getMaximum();

            // Scale the slider to the appropriate volume range
            float volumeInDb = minVolume + (volumeValue / 100.0f) * (maxVolume - minVolume);
            volume.setValue(volumeInDb);
        }

        if (e.getSource() == seekSlider && audioClip != null) {
            long seekPosition = (long) (seekSlider.getValue() / 100.0 * audioClip.getMicrosecondLength());
            audioClip.setMicrosecondPosition(seekPosition);
        }
    }

    private void loadAudioFile() {
        FileDialog fileDialog = new FileDialog(this, "Select an audio file", FileDialog.LOAD);
        fileDialog.setFile("*.wav");
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String fileName = fileDialog.getFile();

        if (fileName != null) {
            File audioFile = new File(directory, fileName);
            try {
                if (audioClip != null) {
                    audioClip.close();
                }
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                audioClip = AudioSystem.getClip();
                audioClip.open(audioStream);

                songLabel.setText("Loaded: " + fileName);
                statusLabel.setText("Status: File loaded successfully.");
                timeLabel.setText("00:00 / " + formatTime(audioClip.getMicrosecondLength() / 1_000_000));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void playAudio() {
        if (audioClip != null) {
            if (isPaused) {
                audioClip.setMicrosecondPosition(pausePosition);
                isPaused = false;
            }
            audioClip.start();
            statusLabel.setText("Status: Playing...");
            startTimer();
        }
    }

    private void pauseAudio() {
        if (audioClip != null && audioClip.isRunning()) {
            pausePosition = audioClip.getMicrosecondPosition();
            audioClip.stop();
            isPaused = true;
            statusLabel.setText("Status: Paused.");
            stopTimer();
        }
    }

    private void stopAudio() {
        if (audioClip != null) {
            audioClip.stop();
            audioClip.setMicrosecondPosition(0);
            isPaused = false;
            pausePosition = 0;
            statusLabel.setText("Status: Stopped.");
            stopTimer();
        }
    }

    private void startTimer() {
        timer = new Timer(1000, e -> {
            long currentTime = audioClip.getMicrosecondPosition() / 1_000_000;
            long totalTime = audioClip.getMicrosecondLength() / 1_000_000;
            timeLabel.setText(formatTime(currentTime) + " / " + formatTime(totalTime));
            seekSlider.setValue((int) (currentTime * 100.0 / totalTime));
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private String formatTime(long seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static void main(String[] args) {
        new AdvancedMusicPlayer();
    }
}