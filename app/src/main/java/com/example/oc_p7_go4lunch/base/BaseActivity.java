package com.example.oc_p7_go4lunch.base;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.example.oc_p7_go4lunch.R;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {

    //----------
    // FOR DATA
    //----------
    abstract T getViewBinding();
    protected T binding;

    //---------------------------
    // ON-CREATE : BASE ACTIVITY
    //---------------------------
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBinding();
    }

    //--:: Initialize the binding object and the layout of the activity ::--
    private void initBinding(){
        binding = getViewBinding();
        View view = binding.getRoot();
        setContentView(view);
    }
}
