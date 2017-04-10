package br.edu.ffb.pedro.aulafacilaluno.activitiess;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.ffb.pedrosilveira.easyp2p.EasyP2p;
import com.ffb.pedrosilveira.easyp2p.EasyP2pDataReceiver;
import com.ffb.pedrosilveira.easyp2p.EasyP2pDevice;
import com.ffb.pedrosilveira.easyp2p.EasyP2pServiceData;
import com.ffb.pedrosilveira.easyp2p.callbacks.EasyP2pCallback;
import com.ffb.pedrosilveira.easyp2p.callbacks.EasyP2pDataCallback;
import com.ffb.pedrosilveira.easyp2p.callbacks.EasyP2pDeviceCallback;
import com.ffb.pedrosilveira.easyp2p.payloads.Payload;
import com.ffb.pedrosilveira.easyp2p.payloads.bully.BullyElection;
import com.ffb.pedrosilveira.easyp2p.payloads.device.DeviceInfo;

import java.io.IOException;

import br.edu.ffb.pedro.aulafacilaluno.R;
import br.edu.ffb.pedro.aulafacilaluno.Utils;
import br.edu.ffb.pedro.aulafacilaluno.adapters.ProfessorsListAdapter;
import br.edu.ffb.pedro.aulafacilaluno.listeners.OnProfessorsListItemClickListener;
import br.edu.ffb.pedro.aulafacilaluno.payload.Quiz;

public class LoginActivity extends AppCompatActivity implements EasyP2pDataCallback,
        OnProfessorsListItemClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private String studentInputName = "";

    private EasyP2pDataReceiver easyP2pDataReceiver;
    private EasyP2pServiceData easyP2pServiceData;
    public static EasyP2p network;

    private RecyclerView professorsList;
    private ProfessorsListAdapter professorsListAdapter;

    private ProgressDialog connectingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        showStudentInputNameDialog();
    }

    private void showStudentInputNameDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(LoginActivity.this);
        View mView = layoutInflaterAndroid.inflate(R.layout.student_input_name_dialog_box, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(LoginActivity.this);
        alertDialogBuilderUserInput.setView(mView);

        final EditText studentInputNameDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(R.string.send, null);

        final AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();

        alertDialogAndroid.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        studentInputName = studentInputNameDialogEditText.getText().toString();
                        if (studentInputName.isEmpty()) {
                            Toast.makeText(LoginActivity.this, R.string.please_insert_your_name,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            setupEasyP2P(studentInputName);
                            alertDialogAndroid.dismiss();
                        }
                    }
                });
            }
        });

        alertDialogAndroid.show();
    }

    private void setupEasyP2P(String studentName) {
        easyP2pDataReceiver = new EasyP2pDataReceiver(LoginActivity.this, LoginActivity.this);
        easyP2pServiceData = new EasyP2pServiceData("AulaFacilProfessor", 50489, studentName);

        network = new EasyP2p(easyP2pDataReceiver, easyP2pServiceData, new EasyP2pCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Desculpe, esse dispositivo não suporta WiFi Direct.");
            }
        });

        network.discoverNetworkServices(new EasyP2pDeviceCallback() {
            @Override
            public void call(EasyP2pDevice device) {
                Log.d(TAG, "NOVO DISPOSITIVO CONECTADO!" +
                        "\nREADABLE NAME: " + device.readableName +
                        "\nINSTANCE NAME: " + device.instanceName +
                        "\nSERVICE NAME: " + device.serviceName +
                        "\nDEVICE NAME: " + device.deviceName);

                professorsListAdapter.notifyDataSetChanged();
            }
        }, true);

        setupProfessorsList();
    }

    private void setupProfessorsList() {
        professorsList = (RecyclerView) findViewById(R.id.professorsList);
        professorsListAdapter = new ProfessorsListAdapter(network.foundDevices, LoginActivity.this);
        professorsList.setAdapter(professorsListAdapter);

        LinearLayoutManager professorsListLayoutManager = new LinearLayoutManager(LoginActivity.this,
                LinearLayoutManager.VERTICAL, false);

        professorsList.setLayoutManager(professorsListLayoutManager);
    }

    @Override
    public void onDataReceived(Object data) {
        // TODO: Tratar Bully Election e Device Info na biblioteca EasyP2P
        Log.d(TAG, "Received network data.");
        Payload payload = null;
        try {
            payload = LoganSquare.parse((String) data, Payload.class);


            switch (payload.type) {

                // QUIZ
                case Quiz.TYPE:
                    final Quiz newQuiz = LoganSquare.parse((String) data, Quiz.class);
                    Log.d(TAG, "MENSAGEM GERAL: " + String.valueOf(newQuiz.isLeader));  //See you on the other side!
                    break;

                // BULLY ELECTION
                case BullyElection.TYPE:
                    BullyElection bullyElection = LoganSquare.parse((String) data, BullyElection.class);
                    switch (bullyElection.message) {
                        case BullyElection.START_ELECTION:
                            Log.d(TAG, "INICIANDO ELEIÇÃO");
                            Toast.makeText(LoginActivity.this, "INICIANDO ELEIÇÃO", Toast.LENGTH_LONG).show();

                            Log.d(TAG, "RESPONDENDO AO PEDIDO DE ELEIÇÃO");
                            network.repondElection(bullyElection.device,
                                    new EasyP2pCallback() {
                                        @Override
                                        public void call() {
                                            Log.d(TAG, "RESPOSTA ENVIADA COM SUCESSO");
                                            network.startElection(null, null);
                                        }
                                    }, new EasyP2pCallback() {
                                        @Override
                                        public void call() {
                                            Log.d(TAG, "FALHA AO ENVIAR A RESPOSTA");
                                        }
                                    }
                            );
                            break;
                        case BullyElection.RESPOND_OK:
                            break;
                        case BullyElection.INFORM_LEADER:
                            break;
                    }
                    break;

                // DEVICE INFO
                case DeviceInfo.TYPE:
                    //DeviceInfo deviceInfo = LoganSquare.parse((String) data, DeviceInfo.class);
                    DeviceInfo deviceInfo = (DeviceInfo) payload;
                    switch (deviceInfo.message) {
                        case DeviceInfo.INFORM_DEVICE:
                            Log.d(TAG, "Informando device");
                            network.registeredClients.add(deviceInfo.device);
                            break;
                    }
                    break;
            }
        } catch (IOException e) {
            Log.d(TAG, "Falha ao receber os dados: " + e.getMessage());
        }
    }

    private void goMainActivity() {
        connectingDialog.hide();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onProfessorsListItemClick(EasyP2pDevice professorDevice) {
        connectingDialog = ProgressDialog
                .show(LoginActivity.this, "Conectando...",
                        "Conectando-se ao professor " + professorDevice.readableName);

        network.registerWithHost(professorDevice,
                new EasyP2pCallback() {
                    @Override
                    public void call() {
                        Log.d(TAG, "Registrado no servidor com sucesso!");
                        Utils.showToastShort(LoginActivity.this, "Registrado no servidor com sucesso!");
                        goMainActivity();
                    }
                }, new EasyP2pCallback() {
                    @Override
                    public void call() {
                        Log.d(TAG, "Falha ao se registrar no servidor!");
                        Utils.showToastShort(LoginActivity.this, "Falha ao se registrar no servidor!");
                    }
                });
    }
}
