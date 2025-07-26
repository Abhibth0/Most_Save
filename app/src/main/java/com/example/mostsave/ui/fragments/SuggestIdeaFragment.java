package com.example.mostsave.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mostsave.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SuggestIdeaFragment extends Fragment {

    private TextInputEditText etSuggestionMessage;
    private Button btnSubmitSuggestion;
    private MaterialButton btnAttachImage;
    private RecyclerView rvImagePreview;
    private List<Uri> attachedImageUris = new ArrayList<>();
    private ImagePreviewAdapter imagePreviewAdapter;
    private static final int REQUEST_IMAGE_PICK = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggest_idea, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        etSuggestionMessage = view.findViewById(R.id.etSuggestionMessage);
        btnSubmitSuggestion = view.findViewById(R.id.btnSubmitSuggestion);
        btnAttachImage = view.findViewById(R.id.btnAttachImage);
        rvImagePreview = view.findViewById(R.id.rvImagePreview);
    }

    private void setupRecyclerView() {
        imagePreviewAdapter = new ImagePreviewAdapter(attachedImageUris, uri -> {
            attachedImageUris.remove(uri);
            imagePreviewAdapter.notifyDataSetChanged();
            if (attachedImageUris.isEmpty()) {
                rvImagePreview.setVisibility(View.GONE);
            }
        });
        rvImagePreview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvImagePreview.setAdapter(imagePreviewAdapter);
    }

    private void setupClickListeners() {
        btnSubmitSuggestion.setOnClickListener(v -> sendSuggestion());
        btnAttachImage.setOnClickListener(v -> openImagePicker());
    }

    // Modern Activity Result API
    private final androidx.activity.result.ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == androidx.fragment.app.FragmentActivity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            attachedImageUris.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        attachedImageUris.add(data.getData());
                    }
                    imagePreviewAdapter.notifyDataSetChanged();
                    rvImagePreview.setVisibility(View.VISIBLE);
                }
            });

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    private void sendSuggestion() {
        String message = "";
        if (etSuggestionMessage.getText() != null) {
            message = etSuggestionMessage.getText().toString().trim();
        }
        if (message.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.suggest_idea_empty_message), Toast.LENGTH_SHORT).show();
            return;
        }
        String customMessage = "Hello Developer,\n\n" +
                "I am using your MostSave password manager app and I have a suggestion for improvement:\n\n" +
                "\"" + message + "\"\n\n" +
                "I hope this feedback helps you make the app even better!\n\n" +
                "Thank you for creating such a useful app.\n\n" +
                "Best regards,\n" +
                "A MostSave User";
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("image/*");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Abhishekpatelbth0@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "\uD83D\uDCA1 Suggestion for MostSave App - User Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, customMessage);
        if (!attachedImageUris.isEmpty()) {
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(attachedImageUris));
        }
        emailIntent.setPackage("com.google.android.gm");
        try {
            startActivity(emailIntent);
            Toast.makeText(getContext(), getString(R.string.suggest_idea_success), Toast.LENGTH_LONG).show();
            etSuggestionMessage.setText("");
            attachedImageUris.clear();
            imagePreviewAdapter.notifyDataSetChanged();
            rvImagePreview.setVisibility(View.GONE);
        } catch (Exception ex) {
            emailIntent.setPackage(null);
            try {
                startActivity(Intent.createChooser(emailIntent, "Send suggestion via..."));
                Toast.makeText(getContext(), getString(R.string.suggest_idea_success), Toast.LENGTH_LONG).show();
                etSuggestionMessage.setText("");
                attachedImageUris.clear();
                imagePreviewAdapter.notifyDataSetChanged();
                rvImagePreview.setVisibility(View.GONE);
            } catch (Exception e) {
                Toast.makeText(getContext(), getString(R.string.suggest_idea_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
