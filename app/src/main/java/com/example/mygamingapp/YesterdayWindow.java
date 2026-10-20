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

public class YesterdayWindow extends AppCompatActivity {
    private DatabaseHelper db;
    private MeuAdapter adapter;
    private List<ItemModel> listaItens;
    private RecyclerView recyclerView;
    private Button btnVoltar;
    private TextView txtTotalHorasOntem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_yesterday_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.yesterdayWind), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);

        db.verificarEResetarViradaDeDia();

        // Busca os dados consolidados e fechados da tabela_ontem
        listaItens = db.buscarItensOntem();

        recyclerView = findViewById(R.id.rvListagemYesterday);
        txtTotalHorasOntem = findViewById(R.id.txtTotalHorasOntem);

        // Calcula a soma total de tempo jogado no dia anterior e exibe no topo
        String tempoTotalOntem = db.calcularTempoTotalOntem();
        txtTotalHorasOntem.setText("Total Jogado Ontem: " + tempoTotalOntem);

        // Passamos 'false' no último parâmetro para bloquear TODA a edição (Apenas Leitura)
        adapter = new MeuAdapter(listaItens, this, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }
}