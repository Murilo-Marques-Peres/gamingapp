package com.example.mygamingapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TodayWindow extends AppCompatActivity {

    private DatabaseHelper db;
    private MeuAdapter adapter;
    private List<ItemModel> listaItens;
    private Button btnAdicionarMais, btnVerOntem;
    private RecyclerView recyclerView;
    private TextView txtTotalHorasHoje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_today_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.todayWind), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = new DatabaseHelper(this);
        db.verificarEResetarViradaDeDia();

        listaItens = db.buscarItensHoje();

        btnAdicionarMais = findViewById(R.id.btnAdicionarMais);
        recyclerView = findViewById(R.id.rvListagemToday);
        txtTotalHorasHoje = findViewById(R.id.txtTotalHorasHoje);

        adapter = new MeuAdapter(listaItens, this, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        atualizarContadorHoras();



        btnAdicionarMais.setOnClickListener(v -> {
            long idDoBanco = db.inserirNoBancoHoje();
            ItemModel novoItem = new ItemModel(idDoBanco, "00", "00", "00", "00");
            listaItens.add(novoItem);
            adapter.notifyItemInserted(listaItens.size() - 1);
            recyclerView.scrollToPosition(listaItens.size() - 1);

            atualizarContadorHoras();
        });
    }

    public void atualizarContadorHoras() {
        String tempoTotal = db.calcularTempoTotalHoje();
        txtTotalHorasHoje.setText("Tempo Total Jogado Hoje: " + tempoTotal);
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarContadorHoras();
    }
}