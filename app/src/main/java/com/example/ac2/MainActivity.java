package com.example.ac2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listaMedicamentos;
    private Button btnExcluir;
    private BancoHelper bancoHelper;
    private ArrayAdapter<String> adapter;
    private List<String> medicamentos = new ArrayList<>();
    private String medicamentoSelecionado;
    private int posicaoSelecionada = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listaMedicamentos = findViewById(R.id.listaMedicamentos);
        btnExcluir = findViewById(R.id.btnExcluir);
        bancoHelper = new BancoHelper(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, medicamentos);
        listaMedicamentos.setAdapter(adapter);

        carregarMedicamentos();
        Intent serviceIntent = new Intent(this,BackgroundService.class);
        startService(serviceIntent);


        listaMedicamentos.setOnItemLongClickListener((parent, view, position, id) -> {
            medicamentoSelecionado = medicamentos.get(position).split(" - ")[0];
            posicaoSelecionada = position;

            view.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

            btnExcluir.setVisibility(View.VISIBLE);

            return true;
        });

        listaMedicamentos.setOnItemClickListener((parent, view, position, id) -> {
            if (posicaoSelecionada == -1) {
                String nomeMedicamento = medicamentos.get(position).split(" - ")[0];
                boolean resultado = bancoHelper.marcarComoTomado(nomeMedicamento);
                if (resultado) {
                    Toast.makeText(MainActivity.this, "Medicamento marcado como tomado!", Toast.LENGTH_SHORT).show();
                    carregarMedicamentos();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao marcar medicamento como tomado.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnExcluir.setOnClickListener(v -> {
            if (posicaoSelecionada != -1) {
                excluirMedicamento();
            }
        });

        findViewById(R.id.btnCadastrar).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
        });

        carregarMedicamentos();
        agendarNotificacoes();
    }

    private void carregarMedicamentos() {
        medicamentos.clear();
        Cursor cursor = bancoHelper.listarMedicamentos();

        if (cursor.moveToFirst()) {
            do {
                String nome = cursor.getString(cursor.getColumnIndex(BancoHelper.COLUNA_NOME));
                int tomado = cursor.getInt(cursor.getColumnIndex(BancoHelper.COLUNA_TOMADO));
                String status = (tomado == 1) ? "Tomado" : "Não tomado";
                medicamentos.add(nome + " - " + status);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter.notifyDataSetChanged();
    }

    private void excluirMedicamento() {
        if (medicamentoSelecionado != null) {
            Cursor cursor = bancoHelper.listarMedicamentos();
            cursor.moveToPosition(posicaoSelecionada);
            int id = cursor.getInt(cursor.getColumnIndex(BancoHelper.COLUNA_ID));
            int resultado = bancoHelper.excluirMedicamento(id);
            cursor.close();

            if (resultado > 0) {
                Toast.makeText(MainActivity.this, "Medicamento excluído com sucesso!", Toast.LENGTH_SHORT).show();
                carregarMedicamentos();
                btnExcluir.setVisibility(View.INVISIBLE);
                posicaoSelecionada = -1;
            } else {
                Toast.makeText(MainActivity.this, "Erro ao excluir medicamento.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void agendarNotificacoes() {
        Cursor cursor = bancoHelper.listarHorariosMedicamentos();
        if (cursor.moveToFirst()) {
            do {
                String nome = cursor.getString(cursor.getColumnIndex(BancoHelper.COLUNA_NOME));
                String horario = cursor.getString(cursor.getColumnIndex(BancoHelper.COLUNA_HORARIO));

                try {
                    // Converter horário para Calendar
                    String[] partes = horario.split(":");
                    int hora = Integer.parseInt(partes[0]);
                    int minuto = Integer.parseInt(partes[1]);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hora);
                    calendar.set(Calendar.MINUTE, minuto - 5); // 5 min antes
                    calendar.set(Calendar.SECOND, 0);

                    // Se já passou o horário de hoje, agenda para amanhã
                    if (calendar.before(Calendar.getInstance())) {
                        calendar.add(Calendar.DATE, 1);
                    }

                    // Criar PendingIntent único para cada medicamento (usar ID diferente)
                    Intent intent = new Intent(MainActivity.this, NotificationReceiver.class);
                    intent.putExtra("nomeMedicamento", nome);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            MainActivity.this,
                            nome.hashCode(), // ID único baseado no nome
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

}
