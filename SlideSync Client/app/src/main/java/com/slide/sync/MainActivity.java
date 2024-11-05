package com.slide.sync;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private PrintWriter output;
    private ExecutorService executorService;

    private EditText edittextIP;
    private ImageButton connectButton, helpButton;
    private TextView connectionStatus;

    private String SERVER_IP;
    private int SERVER_PORT = 5010;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_IP_KEY = "server_ip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executorService = Executors.newSingleThreadExecutor();

        edittextIP = findViewById(R.id.edittextIP);
        connectButton = findViewById(R.id.buttonConnect);
        connectionStatus = findViewById(R.id.status);
        helpButton =findViewById(R.id.help);

        // Load saved IP from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SERVER_IP = prefs.getString(PREF_IP_KEY, "");
        edittextIP.setText(SERVER_IP);

//help
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(socket==null){
//                    connectionStatus.setText("Please, Connect to server first!");
//                }else {
                    Dialog d = new Dialog(MainActivity.this);
                    d.setTitle("help");

                    d.show();
//                }
            }
        });
        // Set up the connect button
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SERVER_IP = edittextIP.getText().toString().trim();
                if (SERVER_IP.isEmpty()) {
                    connectionStatus.setText("Please enter IP address");
                    return;
                }

                connectionStatus.setText("Connecting, please wait...");
                executorService.execute(new ClientThread(SERVER_IP));
            }
        });

        setUpControlButtons();
    }

    // Method to initialize control buttons
    private void setUpControlButtons() {
        findViewById(R.id.buttonUp).setOnClickListener(v -> sendCommand("up"));
        findViewById(R.id.buttonDown).setOnClickListener(v -> sendCommand("down"));
        findViewById(R.id.buttonLeft).setOnClickListener(v -> sendCommand("left"));
        findViewById(R.id.buttonRight).setOnClickListener(v -> sendCommand("right"));
        findViewById(R.id.buttonF5).setOnClickListener(v -> sendCommand("f5"));
        findViewById(R.id.buttonShftf5).setOnClickListener(v -> sendCommand("shiftf5"));
        findViewById(R.id.buttonHome).setOnClickListener(v -> sendCommand("home"));
        findViewById(R.id.buttonEnd).setOnClickListener(v -> sendCommand("end"));
        findViewById(R.id.buttonEsc).setOnClickListener(v -> sendCommand("esc"));
    }

    // Method to send commands to the server
    private void sendCommand(final String command) {
        executorService.execute(() -> {
            if (output != null) {
                output.print(command);
                output.flush();
            }
        });
        if(socket==null){
            connectionStatus.setText("Please, Connect to server first!");
        }
    }

    // Client thread to handle socket connection
    class ClientThread implements Runnable {
        private final String ip;

        ClientThread(String ip) {
            this.ip = ip;
        }

        @Override
        public void run() {
            try {
                // Establish socket connection to server
                socket = new Socket(ip, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream(), true);

                // Connection successful
                runOnUiThread(() -> {
                    connectionStatus.setText("Connected!");
                    Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_SHORT).show();
                    saveIpAddress(ip);
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> connectionStatus.setText("Connection Failed!\nMake sure your phone and computer are on the same network.\nand check server running or not on your PC."));
            }
        }
    }

    // Save IP address to SharedPreferences
    private void saveIpAddress(String ip) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(PREF_IP_KEY, ip);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            sendCommand("up");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            sendCommand("down");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

