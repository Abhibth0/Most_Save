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

import com.example.mostsave.R;
import com.google.android.material.textfield.TextInputEditText;

public class SuggestIdeaFragment extends Fragment {

    private TextInputEditText etSuggestionMessage;
    private Button btnSubmitSuggestion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggest_idea, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        etSuggestionMessage = view.findViewById(R.id.etSuggestionMessage);
        btnSubmitSuggestion = view.findViewById(R.id.btnSubmitSuggestion);
    }

    private void setupClickListeners() {
        btnSubmitSuggestion.setOnClickListener(v -> sendSuggestion());
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

        // Create custom email message with user's suggestion
        String customMessage = "Hello Developer,\n\n" +
                "I am using your MostSave password manager app and I have a suggestion for improvement:\n\n" +
                "\"" + message + "\"\n\n" +
                "I hope this feedback helps you make the app even better!\n\n" +
                "Thank you for creating such a useful app.\n\n" +
                "Best regards,\n" +
                "A MostSave User";

        // Try multiple email intents for better compatibility
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Abhishekpatelbth0@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "💡 Suggestion for MostSave App - User Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, customMessage);

        try {
            if (emailIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(emailIntent);
                Toast.makeText(getContext(), getString(R.string.suggest_idea_success), Toast.LENGTH_LONG).show();
                // Clear the message after successful send
                etSuggestionMessage.setText("");
            } else {
                // Fallback to ACTION_SEND if ACTION_SENDTO doesn't work
                Intent fallbackIntent = new Intent(Intent.ACTION_SEND);
                fallbackIntent.setType("text/plain");
                fallbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Abhishekpatelbth0@gmail.com"});
                fallbackIntent.putExtra(Intent.EXTRA_SUBJECT, "💡 Suggestion for MostSave App - User Feedback");
                fallbackIntent.putExtra(Intent.EXTRA_TEXT, customMessage);

                startActivity(Intent.createChooser(fallbackIntent, "Send suggestion via..."));
                Toast.makeText(getContext(), getString(R.string.suggest_idea_success), Toast.LENGTH_LONG).show();
                etSuggestionMessage.setText("");
            }
        } catch (Exception ex) {
            Toast.makeText(getContext(), getString(R.string.suggest_idea_error), Toast.LENGTH_SHORT).show();
        }
    }
}
