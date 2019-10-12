package co.edu.udistrital.sintetizador;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import co.edu.udistrital.sintetizador.api.Nota;
import co.edu.udistrital.sintetizador.api.TipoNota;
import co.edu.udistrital.sintetizador.api.TipoTiempo;
import co.edu.udistrital.sintetizador.model.Cancion;
import co.edu.udistrital.sintetizador.model.NotaSeleccionada;

public class MainActivity extends AppCompatActivity {

    private RadioGroup notaRadioGroup;
    private RadioGroup tipoNotaRadioGroup;

    private Button agregarNotaBtn;
    private Button enviarCancionBtn;

    private Spinner tiempoSpinner;

    private Cancion cancion;

    private Toolbar toolBar;

    Handler bluetoothIn;

    // Cambiar a true cuando se vaya a trabajar con bluethoot
    boolean enableBluetooth = true;


    final int handlerState = 0;                        //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    //private static String address = "98:D3:21:F7:41:4F";
    private static String address = "00:11:11:29:04:55";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewComponents();
        //Coloqué la variable enableBluetooth para indicar si se desea trabajar intentando acceder al Bluethhot
        // Inicializar esta variable en true cuando se vaya a trabajar con el Bluetooth
        if (enableBluetooth) {
            btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
            checkBTState();
        }
    }

    private void initViewComponents() {
        this.cancion = new Cancion();
        this.notaRadioGroup = findViewById(R.id.notaRadioGroup);
        this.tipoNotaRadioGroup = findViewById(R.id.tipoNotaRadioGroup);
        this.agregarNotaBtn = findViewById(R.id.agregarNotaBtn);
        this.agregarNotaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agregarNota();
            }
        });
        this.tiempoSpinner = findViewById(R.id.tiempoSpinner);
        this.tiempoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cancion = new Cancion();
                notaRadioGroup.clearCheck();
                tipoNotaRadioGroup.clearCheck();

                Toast.makeText(getBaseContext(), "Se limpió el listado de notas", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                cancion = new Cancion();
                notaRadioGroup.clearCheck();
                tipoNotaRadioGroup.clearCheck();

                Toast.makeText(getBaseContext(), "Se limpió el listado de notas", Toast.LENGTH_SHORT).show();
            }
        });
        this.enviarCancionBtn = findViewById(R.id.enviarCancionBtn);
        this.enviarCancionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarCancion();
            }
        });

    }

    private boolean validoAgregarNota() {
        if (this.tiempoSpinner.getSelectedItem() == null || this.tiempoSpinner.getSelectedItem().toString().trim().isEmpty()) {
            Toast.makeText(getBaseContext(), "Debe seleccionar el tiempo", Toast.LENGTH_SHORT).show();
            return false;
        } else if (this.notaRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getBaseContext(), "Debe seleccionar una nota", Toast.LENGTH_SHORT).show();
            return false;
        } else if (this.tipoNotaRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getBaseContext(), "Debe seleccionar un tipo de nota", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private char getNotaSeleccionadaPorUsuario() {
        int indexRadio = this.notaRadioGroup.getCheckedRadioButtonId();
        RadioButton radio = findViewById(indexRadio);
        switch (radio.getText().toString()) {
            case "Do":
                return Nota.DO;
            case "Re":
                return Nota.RE;
            case "Mi":
                return Nota.MI;
            case "Fa":
                return Nota.FA;
            case "Sol":
                return Nota.SOL;
            case "La":
                return Nota.LA;
            case "Si":
                return Nota.SI;
            default:
                return 'z';
        }
    }

    private char getTipoNotaSeleccionadaPorUsuario() {
        int indexRadio = this.tipoNotaRadioGroup.getCheckedRadioButtonId();
        RadioButton radio = findViewById(indexRadio);
        switch (radio.getText().toString()) {
            case "Negra":
                return TipoNota.NEGRA;
            case "Blanca":
                return TipoNota.BLANCA;
            case "Redonda":
                return TipoNota.REDONDA;
            default:
                return 'z';
        }
    }

    private void agregarNota() {
        if (!validoAgregarNota())
            return;
        if (cancion == null)
            cancion = new Cancion();
        if (cancion.getList() == null)
            cancion.setList(new ArrayList<NotaSeleccionada>(1));
        char nota = getNotaSeleccionadaPorUsuario();
        char tipoNota = getTipoNotaSeleccionadaPorUsuario();
        cancion.getList().add(new NotaSeleccionada(nota, tipoNota));

        this.notaRadioGroup.clearCheck();
        this.tipoNotaRadioGroup.clearCheck();

        Toast.makeText(getBaseContext(), "Nota agregada", Toast.LENGTH_SHORT).show();
    }

    private boolean validoEnviarCancion() {
        if (this.cancion == null) {
            Toast.makeText(getBaseContext(), "No hay datos para procesar", Toast.LENGTH_SHORT).show();
            return false;
        } else if (this.tiempoSpinner.getSelectedItem() == null || this.tiempoSpinner.getSelectedItem().toString().isEmpty()) {
            Toast.makeText(getBaseContext(), "Debe seleccionar una opción para tiempo", Toast.LENGTH_SHORT).show();
            return false;
        } else if (this.cancion.getList() == null || this.cancion.getList().isEmpty()) {
            Toast.makeText(getBaseContext(), "Debe agregar al menos una nota para reproducir", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private int getTipoTiempo() {
        switch (this.tiempoSpinner.getSelectedItem().toString()) {
            case "30":
                return TipoTiempo.T_30_SG;
            case "60":
                return TipoTiempo.T_60_SG;
            case "120":
                return TipoTiempo.T_120_SG;
            default:
                return -1;
        }
    }

    private String construirArrayNotas(List<NotaSeleccionada> notaList) {
        if (notaList == null || notaList.isEmpty())
            return "";
        String result = "";
        for (NotaSeleccionada nota : notaList) {
            result += "[" + nota.getN() + "," + nota.getNt() + "]";
        }
        return result;
    }

    //Método que convierte toda la información de objetos a un arreglo de String en formato json para enviarlo por meiod de Bluethoot
    private void enviarCancion() {
        if (!validoEnviarCancion())
            return;
        int tipoTiempo = getTipoTiempo();
        this.cancion.setT(tipoTiempo);
        this.cancion.setNotas(construirArrayNotas(this.cancion.getList()));
        Gson gson = new Gson();
        //Se convierte el objeto cancion en su interpretacion json correspondiente
        String json = gson.toJson(this.cancion);
        System.out.println(json);

        String trama = "#" + json + "~";

        if (enableBluetooth) {
            this.mConnectedThread.write(trama);
        }
        this.cancion = null;
        this.cancion = new Cancion();
        this.notaRadioGroup.clearCheck();
        this.tipoNotaRadioGroup.clearCheck();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!enableBluetooth)
            return;
        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        //address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        //address = "98:D3:21:F7:41:4F";

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause() {
        if (this.enableBluetooth) {
            super.onPause();
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /*
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        */

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                android.os.SystemClock.sleep(150); //Esperar un tiempo entre el envio de datos
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}