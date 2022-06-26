package me.dio.simulador.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import me.dio.simulador.R;
import me.dio.simulador.data.MatchesAPI;
import  me.dio.simulador.databinding.*;
import me.dio.simulador.domain.Match;
import me.dio.simulador.ui.adapter.MatchesAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MatchesAPI matchesAPI;
    private MatchesAdapter matchesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setHttpClient();
        setupMatchesList();
        setupMatchesRefresh();
        setupFloatingActionButton();
    }

    private void setHttpClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wesleyalmont.github.io/matches-simulator-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

       matchesAPI=  retrofit.create(MatchesAPI.class);
    }

    private void setupFloatingActionButton() {
        binding.fabSimulate.setOnClickListener(view -> {
            view.animate().rotationBy(360).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Random random = new Random();
                   for (int i = 0; i < matchesAdapter.getItemCount(); i++){
                       Match match = matchesAdapter.getMatches().get(i);
                        match.getHomeTeam().setScore(random.nextInt(match.getHomeTeam().getStars() + 1));
                        match.getAwayTeam().setScore(random.nextInt(match.getAwayTeam().getStars() + 1));
                        matchesAdapter.notifyItemChanged(i);
                   }
                }
            });
        });
    }

    private void setupMatchesRefresh() {
       binding.srlMatcher.setOnRefreshListener(this::findMatchesFromAPI);
    }

    private void setupMatchesList() {
        binding.rvMatches.setHasFixedSize(true);
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        findMatchesFromAPI();
    }

    private void findMatchesFromAPI() {
        binding.srlMatcher.setRefreshing(true);
        matchesAPI.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(Call<List<Match>> call, Response<List<Match>> response) {
                 if(response.isSuccessful()){
                     List<Match> matches = response.body();
                     matchesAdapter = new MatchesAdapter(matches);
                     binding.rvMatches.setAdapter(matchesAdapter);
                 }else{
                     showErrorMessage();
                 }
                binding.srlMatcher.setRefreshing(false);
            }
 
            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
                 showErrorMessage();
                binding.srlMatcher.setRefreshing(false);
            }
        });
    }

    private void showErrorMessage() {
        Snackbar.make(binding.fabSimulate, R.string.error_api, Snackbar.LENGTH_LONG).show();
    }
}
;