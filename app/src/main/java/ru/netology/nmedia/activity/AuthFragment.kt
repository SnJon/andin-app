package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

@AndroidEntryPoint
class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null

    private val binding: FragmentAuthBinding
        get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        subscribeOnViewModel()
    }

    private fun bindViews() {
        binding.apply {
            authorizationContinueButton.setOnClickListener {
                findNavController().navigateUp()
            }

            authorizationSignInButton.setOnClickListener {
                val login = authorizationLoginEditText.text.toString()
                val password = authorizationPasswordEditText.text.toString()

                authorizationWrong.isVisible = false
                authorizationLoginObligatoryText.isVisible = false
                authorizationPasswordObligatoryText.isVisible = false

                if (login.isEmpty() || password.isEmpty()) {

                    if (login.isEmpty()) {
                        authorizationLoginObligatoryText.isVisible = true
                    }
                    if (password.isEmpty()) {
                        authorizationPasswordObligatoryText.isVisible = true
                    }
                    return@setOnClickListener
                }

                val credentials = Pair(login, password)

                viewModel.authorization(credentials)
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
    }

    private fun subscribeOnViewModel() {
        viewModel.autState.observe(viewLifecycleOwner) { state ->
            binding.authorizationProgressLinear.isVisible = state.loading
            binding.authorizationWrong.isVisible = state.error

            if (state.success) {
                findNavController().navigateUp()
                viewModel.restoreState()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
