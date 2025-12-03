package com.example.agenda_tarea.controller;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agenda_tarea.R;
import com.example.agenda_tarea.database.DatabaseHelper;
import com.example.agenda_tarea.model.Task;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextDescription, editTextDate;
    private Button buttonSave, buttonDatePicker;
    private DatabaseHelper databaseHelper;
    private Calendar calendar;
    private int taskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDatePicker = findViewById(R.id.buttonDatePicker);

        databaseHelper = new DatabaseHelper(this);

        calendar = Calendar.getInstance();

        if (getIntent().hasExtra("TASK_ID")) {
            taskId = getIntent().getIntExtra("TASK_ID", -1);
            loadTaskData();
            setTitle("Editar Tarea");
        } else {
            setTitle("Nueva Tarea");
            updateDateInView();
        }

        setupListeners();
    }

    private void loadTaskData() {
        if (taskId != -1) {
            Task task = databaseHelper.getTask(taskId);
            if (task != null) {
                editTextTitle.setText(task.getTitulo());
                editTextDescription.setText(task.getDescripcion());
                editTextDate.setText(task.getFecha());
            }
        }
    }

    private void setupListeners() {
        buttonDatePicker.setOnClickListener(v -> showDatePickerDialog());

        buttonSave.setOnClickListener(v -> saveTask());

        editTextDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateInView();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateInView() {
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        editTextDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("El título es obligatorio");
            editTextTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(date)) {
            editTextDate.setError("La fecha es obligatoria");
            editTextDate.requestFocus();
            return;
        }

        Task task = new Task();
        task.setTitulo(title);
        task.setDescripcion(description);
        task.setFecha(date);
        task.setCompletada(false);

        long result;
        String message;

        if (taskId == -1) {
            result = databaseHelper.addTask(task);
            message = (result != -1) ? "Tarea agregada" : "Error al agregar";
        } else {
            task.setId(taskId);
            result = databaseHelper.updateTask(task);
            message = (result > 0) ? "Tarea actualizada" : "Error al actualizar";
        }

        if (result != -1 && result > 0) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}