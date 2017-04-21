package br.edu.ffb.pedro.aulafacilaluno.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.arasthel.asyncjob.AsyncJob;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import br.edu.ffb.pedro.aulafacilaluno.R;
import br.edu.ffb.pedro.aulafacilaluno.Utils;
import br.edu.ffb.pedro.aulafacilaluno.adapters.ProfessorsListAdapter;
import br.edu.ffb.pedro.aulafacilaluno.events.MessageEvent;
import br.edu.ffb.pedro.aulafacilaluno.listeners.OnProfessorsListItemClickListener;
import br.edu.ffb.pedro.aulafacilaluno.payload.Quiz;

public class LoginActivity extends AppCompatActivity implements EasyP2pDataCallback,
        OnProfessorsListItemClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private Toolbar toolbar;
    private LinearLayout professorsContainer;
    private String studentInputName = "";

    public static EasyP2p network;

    private RecyclerView professorsList;
    private View mEmptyView;
    private ProfessorsListAdapter professorsListAdapter;

    private ProgressDialog connectingDialog;
    private boolean hasElectionResponse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);
        professorsContainer = (LinearLayout) findViewById(R.id.professorsContainer);
        professorsList = (RecyclerView) findViewById(R.id.professorsList);
        mEmptyView = findViewById(R.id.emptyView);
        showStudentInputNameDialog();
        EventBus.getDefault().register(LoginActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Deseja realmente sair?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (LoginActivity.network.thisDevice.isRegistered) {
                                    LoginActivity.network.unregisterClient(new EasyP2pCallback() {
                                        @Override
                                        public void call() {
                                            finish();
                                        }
                                    }, null, false);
                                }
                            }
                        })
                        .setNegativeButton("Não", null)
                        .create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("InflateParams")
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
                            toolbar.setVisibility(View.VISIBLE);
                            professorsContainer.setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.transparent));
                            setupEasyP2P();
                            alertDialogAndroid.dismiss();
                        }
                    }
                });
            }
        });

        alertDialogAndroid.show();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.message.equals(MessageEvent.EXIT_APP)) {
            finish();
        }
    }

    private void setupEasyP2P() {
        EasyP2pDataReceiver easyP2pDataReceiver = new EasyP2pDataReceiver(LoginActivity.this, LoginActivity.this);
        EasyP2pServiceData easyP2pServiceData = new EasyP2pServiceData("AulaFacilProfessor", 50489, studentInputName);

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

    private void checkAdapterIsEmpty() {
        if (professorsListAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            professorsList.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            professorsList.setVisibility(View.VISIBLE);
        }
    }

    private void setupProfessorsList() {
        professorsListAdapter = new ProfessorsListAdapter(network.foundDevices, LoginActivity.this);

        professorsListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });

        LinearLayoutManager professorsListLayoutManager = new LinearLayoutManager(LoginActivity.this,
                LinearLayoutManager.VERTICAL, false);

        professorsList.setLayoutManager(professorsListLayoutManager);
        professorsList.setHasFixedSize(true);
        professorsList.setAdapter(professorsListAdapter);
        checkAdapterIsEmpty();
    }

    @Override
    public void onDataReceived(Object data) {

        Log.d(TAG, "Received network data.");
        Payload payload;
        try {
            payload = LoganSquare.parse((String) data, Payload.class);
            switch (payload.type) {

                // O Líder receberá mensagens do tipo QUIZ
                case Quiz.TYPE:
                    final Quiz newQuiz = LoganSquare.parse((String) data, Quiz.class);
                    Log.d(TAG, "MENSAGEM GERAL: " + String.valueOf(newQuiz.isLeader));  //See you on the other side!
                    break;

                // BULLY ELECTION
                case BullyElection.TYPE:
                    BullyElection bullyElection = LoganSquare.parse((String) data, BullyElection.class);
                    switch (bullyElection.message) {
                        case BullyElection.START_ELECTION:
                            if (!hasElectionResponse) {

                                Log.d(TAG, "INICIANDO ELEIÇÃO");
                                Toast.makeText(LoginActivity.this, "INICIANDO ELEIÇÃO", Toast.LENGTH_LONG).show();

                                Log.d(TAG, "RESPONDENDO AO PEDIDO DE ELEIÇÃO");
                                network.repondElection(bullyElection.device,
                                        new EasyP2pCallback() {
                                            @Override
                                            public void call() {
                                                Log.d(TAG, "RESPOSTA ENVIADA COM SUCESSO");
                                                Log.d(TAG, "DEVICE ID: " + network.thisDevice.id);
                                                Log.d(TAG, "DEVICE NAME: " + network.thisDevice.readableName);

                                                // Checa se é o líder
                                                if (network.isLeader()) {
                                                    Log.d(TAG, "INFORMANDO LÍDER: " + network.thisDevice.readableName);
                                                    network.informLeader(null, null);
                                                } else {
                                                    network.startElection(null,
                                                            new EasyP2pDeviceCallback() {
                                                                @Override
                                                                public void call(final EasyP2pDevice device) {
                                                                    Log.d(TAG, "ERRO AO ENVIAR O PEDIDO DE ELEIÇÃO");

                                                                    // Se chegar nesse ponto significa que iniciou-se uma tentativa
                                                                    // de se conectar ao dispotivo de maior id, porém houve uma falha
                                                                    // de conexão, então o dispositivo aguardará um tempo definido em BuyllyElection
                                                                    // caso haja algum retorno a variável hasElectionResponse e não será efetuado nenhum
                                                                    // procedimento, caso contrário, esse dispositivo irá se declarar líder
                                                                    AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
                                                                        @Override
                                                                        public void doOnBackground() {
                                                                            try {
                                                                                Thread.sleep(BullyElection.TIMEOUT);
                                                                                // Se a variável não for alterada, significa que não houve resposta
                                                                                if (!hasElectionResponse) {
                                                                                    Log.d(TAG, "REMOVENDO A REFERÊNCIA DO DISPOSITIVO: " + device.readableName);
                                                                                    network.removeDeviceReference(device, null);
                                                                                    network.informRemoveDeviceReference(device, null);
                                                                                    network.informLeader(null, null);
                                                                                }
                                                                            } catch (InterruptedException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                }
                                            }
                                        }, new EasyP2pCallback() {
                                            @Override
                                            public void call() {
                                                Log.d(TAG, "FALHA AO ENVIAR A RESPOSTA");
                                            }
                                        }
                                );
                            }
                            break;
                        case BullyElection.RESPOND_OK:
                            hasElectionResponse = true;
                            break;
                        case BullyElection.INFORM_LEADER:
                            final EasyP2pDevice leader = bullyElection.device;
                            network.updateLeaderReference(leader, new EasyP2pCallback() {
                                @Override
                                public void call() {
                                    // O host passa a ser o líder
                                    network.registeredLeader = leader;
                                    hasElectionResponse = false;
                                }
                            });
                            break;
                    }
                    break;

                // DEVICE INFO
                case DeviceInfo.TYPE:
                    DeviceInfo deviceInfo = LoganSquare.parse((String) data, DeviceInfo.class);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(LoginActivity.this);
        if (LoginActivity.network.thisDevice.isRegistered) {
            LoginActivity.network.unregisterClient(null, null, false);
        }
    }
}
