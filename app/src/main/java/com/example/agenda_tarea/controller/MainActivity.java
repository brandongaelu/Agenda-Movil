package com.example.agenda_tarea.controller;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.agenda_tarea.R;
import com.example.agenda_tarea.adapter.TaskAdapter;
import com.example.agenda_tarea.database.DatabaseHelper;
import com.example.agenda_tarea.model.Task;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerViewTasks;
    private FloatingActionButton fabAddTask;
    private TextView textViewEmpty;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        fabAddTask = findViewById(R.id.fabAddTask);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        databaseHelper = new DatabaseHelper(this);

        setupRecyclerView();

        setupFloatingActionButton();

        loadTasks();
    }

    private void setupRecyclerView() {
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, taskList, this);
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupFloatingActionButton() {
        fabAddTask.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    private void loadTasks() {
        taskList.clear();
        taskList.addAll(databaseHelper.getAllTasks());
        taskAdapter.notifyDataSetChanged();

        if (taskList.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadTasks();
        }
    }

    @Override
    public void onTaskClick(int position) {
    }

    @Override
    public void onEditClick(int position) {
        Task task = taskAdapter.getTaskAtPosition(position);
        Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        startActivityForResult(intent, 1);
    }

    @Override
    public void onDeleteClick(int position) {
        Task task = taskAdapter.getTaskAtPosition(position);

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Tarea")
                .setMessage("¿Estás seguro de eliminar: " + task.getTitulo() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    databaseHelper.deleteTask(task.getId());
                    loadTasks();
                    Toast.makeText(MainActivity.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onCheckboxClick(int position, boolean isChecked) {
        Task task = taskAdapter.getTaskAtPosition(position);
        databaseHelper.toggleTaskCompletion(task.getId(), isChecked);
        task.setCompletada(isChecked);
        taskAdapter.notifyItemChanged(position);

        String message = isChecked ? "Tarea completada" : "Tarea pendiente";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}