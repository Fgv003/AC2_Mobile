package com.example.ac2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {

    private EditText edtNome, edtDescricao, edtHorario;
    private Button btnSalvar, btnVoltar;
    private BancoHelper bancoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        edtNome = findViewById(R.id.edtNome);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtHorario = findViewById(R.id.edtHorario);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnVoltar = findViewById(R.id.btnVoltar);
        bancoHelper = new BancoHelper(this);

        btnSalvar.setOnClickListener(v -> salvarMedicamento());

        btnVoltar.setOnClickListener(v -> voltarParaMenuPrincipal());
    }

    private void salvarMedicamento() {
        String nome = edtNome.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        String horario = edtHorario.getText().toString().trim();

        if (nome.isEmpty() || descricao.isEmpty() || horario.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = bancoHelper.inserirMedicamento(nome,horario);

        if (id != -1) {
            Toast.makeText(this, "Medicamento salvo com sucesso!", Toast.LENGTH_SHORT).show();
            limparCampos();
        } else {
            Toast.makeText(this, "Erro ao salvar medicamento.", Toast.LENGTH_SHORT).show();
        }
    }

    private void voltarParaMenuPrincipal() {
        Intent intent = new Intent(MainActivity2.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void limparCampos() {
        edtNome.setText("");
        edtDescricao.setText("");
        edtHorario.setText("");
    }
}
