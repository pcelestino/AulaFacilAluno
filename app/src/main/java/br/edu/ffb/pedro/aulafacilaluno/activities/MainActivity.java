package br.edu.ffb.pedro.aulafacilaluno.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ffb.pedrosilveira.easyp2p.callbacks.EasyP2pCallback;
import com.ffb.pedrosilveira.easyp2p.payloads.bully.BullyElection;

import org.greenrobot.eventbus.EventBus;

import br.edu.ffb.pedro.aulafacilaluno.R;
import br.edu.ffb.pedro.aulafacilaluno.events.MessageEvent;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BullyElection election = new BullyElection();
                election.message = BullyElection.START_ELECTION;
                LoginActivity.network.sendToHost(election,
                        new EasyP2pCallback() {
                            @Override
                            public void call() {
                                Log.e(TAG, "Sucesso =].");
                            }
                        },
                        new EasyP2pCallback() {
                            @Override
                            public void call() {
                                Log.e(TAG, "Oh no! The data failed to send.");
                            }
                        });
            }
        });
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
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Deseja realmente sair?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (LoginActivity.network.thisDevice.isRegistered) {
                                    LoginActivity.network.unregisterClient(new EasyP2pCallback() {
                                        @Override
                                        public void call() {
                                            EventBus.getDefault().post(new MessageEvent(MessageEvent.EXIT_APP));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (LoginActivity.network.thisDevice.isRegistered) {
            LoginActivity.network.unregisterClient(null, null, false);
        }
    }
}
