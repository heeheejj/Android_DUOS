// Generated by view binder compiler. Do not edit!
package com.example.duos.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentContainerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.duos.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivitySignupBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageView signupArrowIv;

  @NonNull
  public final FragmentContainerView signupFragmentContainerFc;

  @NonNull
  public final Button signupNextBtn;

  @NonNull
  public final TextView signupProcessTv;

  @NonNull
  public final TextView signupTitleTv;

  private ActivitySignupBinding(@NonNull ConstraintLayout rootView,
      @NonNull ImageView signupArrowIv, @NonNull FragmentContainerView signupFragmentContainerFc,
      @NonNull Button signupNextBtn, @NonNull TextView signupProcessTv,
      @NonNull TextView signupTitleTv) {
    this.rootView = rootView;
    this.signupArrowIv = signupArrowIv;
    this.signupFragmentContainerFc = signupFragmentContainerFc;
    this.signupNextBtn = signupNextBtn;
    this.signupProcessTv = signupProcessTv;
    this.signupTitleTv = signupTitleTv;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivitySignupBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivitySignupBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_signup, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivitySignupBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.signup_arrow_iv;
      ImageView signupArrowIv = ViewBindings.findChildViewById(rootView, id);
      if (signupArrowIv == null) {
        break missingId;
      }

      id = R.id.signup_fragment_container_fc;
      FragmentContainerView signupFragmentContainerFc = ViewBindings.findChildViewById(rootView, id);
      if (signupFragmentContainerFc == null) {
        break missingId;
      }

      id = R.id.signup_next_btn;
      Button signupNextBtn = ViewBindings.findChildViewById(rootView, id);
      if (signupNextBtn == null) {
        break missingId;
      }

      id = R.id.signup_process_tv;
      TextView signupProcessTv = ViewBindings.findChildViewById(rootView, id);
      if (signupProcessTv == null) {
        break missingId;
      }

      id = R.id.signup_title_tv;
      TextView signupTitleTv = ViewBindings.findChildViewById(rootView, id);
      if (signupTitleTv == null) {
        break missingId;
      }

      return new ActivitySignupBinding((ConstraintLayout) rootView, signupArrowIv,
          signupFragmentContainerFc, signupNextBtn, signupProcessTv, signupTitleTv);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
