package com.capstone.aiyam.presentation.auth.signin

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.capstone.aiyam.databinding.FragmentSigninBinding
import com.capstone.aiyam.domain.model.AuthenticationResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animate()
        button()
    }

    private fun button() { binding.apply {
        signInButton.setOnClickListener { lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.signInEmail(emailEditText.text.toString(), passwordEditText.text.toString()).collect {
                    handleOnAuth(it)
                }
            }
        }}

        googleSignInButton.setOnClickListener { lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.signInGoogle().collect {
                    handleOnAuth(it)
                }
            }
        }}

        signInTextView.setOnClickListener {
            SignInFragmentDirections.actionSigninFragmentToSignupFragment().let {
                findNavController().navigate(it)
            }
        }
    }}

    private fun handleOnAuth(result: AuthenticationResponse ) {
        when(result) {
            is AuthenticationResponse.Error -> {
                onLoading(false)
                showToast(result.message)
            }
            is AuthenticationResponse.Loading -> {
                onLoading(true)
            }
            is AuthenticationResponse.Success -> {
                onLoading(false)
                showToast("Successfully signed in")
                SignInFragmentDirections.actionSigninFragmentToHomeFragment().let {
                    findNavController().navigate(it)
                }
            }
        }
    }

    private fun onLoading(isLoading: Boolean) {
        binding.lpiLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun animate() { binding.apply {
        ObjectAnimator.ofFloat(imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(titleTextView, View.ALPHA, 1f).setDuration(100)
        val message = ObjectAnimator.ofFloat(messageTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout = ObjectAnimator.ofFloat(emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val signup = ObjectAnimator.ofFloat(signInTextView, View.ALPHA, 1f).setDuration(100)
        val login = ObjectAnimator.ofFloat(signInButton, View.ALPHA, 1f).setDuration(100)
        val divider = ObjectAnimator.ofFloat(signInWith, View.ALPHA, 1f).setDuration(100)
        val google = ObjectAnimator.ofFloat(googleSignInButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title, message,
                emailEditTextLayout,
                passwordEditTextLayout,
                signup, login,
                divider, google
            )
            startDelay = 100
        }.start()
    }}

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}