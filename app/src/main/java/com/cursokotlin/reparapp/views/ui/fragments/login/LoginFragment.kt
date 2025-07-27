package com.cursokotlin.reparapp.views.ui.fragments.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentLoginBinding
import com.cursokotlin.reparapp.views.ui.activities.PrincipalActivity
import com.cursokotlin.reparapp.viewmodel.LoginViewModel
import com.cursokotlin.reparapp.viewmodel.ValidationStateLogin
import com.cursokotlin.reparapp.views.ui.fragments.CustomDialogFragment
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.AccessToken
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.CallbackManager
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import java.security.MessageDigest
import java.util.Arrays

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel:LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding
    lateinit var dialog : CustomDialogFragment
    var loginProvider:String = ""


    companion object{
        val PROVIDER_GOOGLE = "GOOGLE"
        val PROVIDER_FACEBOOK = "FACEBOOK"
    }

    object Global{
        var callbackManager = CallbackManager.Factory.create()
    }

    // 🔹 One Tap Client y Launcher
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                val idToken = credential.googleIdToken?:""
                val nombre = credential.givenName ?: ""
                val apellido = credential.familyName ?: ""
                val email = credential.id ?: ""

                viewModel.signInWithGoogle(
                    idToken = idToken,
                    nombre = nombre,
                    apellido = apellido,
                    email = email
                )

            } else {
                Toast.makeText(requireContext(), "No se pudo obtener el token", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Inicio cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        setupGoogleSignIn()
        setupEventListeners()
        initDialog()

        return binding.root
    }

    private fun setupGoogleSignIn() {
        oneTapClient = Identity.getSignInClient(requireContext())

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFieldErrors()
        observeLoginState()
        mostrarKeyHash()
    }

    private fun mostrarKeyHash() {
        try {
            val info = activity?.packageManager?.getPackageInfo(
                requireActivity().packageName,
                PackageManager.GET_SIGNATURES
            )

            info?.signatures?.forEach { signature ->
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.e("KEYHASH", keyHash)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun observeFieldErrors() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fieldErrors.collect { errors ->
                    binding.etContenedorCorreo.error = errors?.emailError
                    binding.etContenedorClave.error = errors?.passwordError
                }
            }
        }
    }

    private fun initDialog(){
        dialog = CustomDialogFragment.newInstance(
            titulo = "Términos y Condiciones",
            contenido = getString(R.string.terminos_y_condiciones)
        )
    }

    private fun showDialog(){
        dialog.setDialogListener(object : CustomDialogFragment.DialogListener {
            override fun onConfirm() {
                // Acción al hacer clic en "Aceptar"
                if(loginProvider == PROVIDER_GOOGLE){
                    initSesionWithGoogle()
                }else if (loginProvider == PROVIDER_FACEBOOK){
                    login_facebook()
                }
            }

            override fun onCancel() {
                // Acción al hacer clic en "Declinar"
                Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        })

        dialog.show(parentFragmentManager, "CustomDialog")
    }

    private fun initSesionWithGoogle(){
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                googleSignInLauncher.launch(intentSenderRequest)
            }
            .addOnFailureListener { e ->
                Log.e("GoogleSignIn", "Fallo inicio sesión: ${e.message}", e)
                if (e is ApiException) {
                    Log.e("GoogleSignIn", "Status code: ${e.statusCode}")
                }
                Toast.makeText(requireContext(), "No se pudo iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validation.collect { state ->
                    when (state) {
                        is ValidationStateLogin.Loading -> showLoading(true)
                        is ValidationStateLogin.Verified -> {
                            showLoading(false)
                            Toast.makeText(requireContext(), "Bienvenido", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireContext(), PrincipalActivity::class.java).apply {
                                putExtra("userRole", state.userRole)
                            }
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        is ValidationStateLogin.Error -> {
                            showLoading(false)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }

                        is ValidationStateLogin.NotVerified -> {
                            showLoading(false)
                            val userRole = state.userRole
                            findNavController().navigate(R.id.action_loginFragment_to_verificacionEmailFragment)
                            Toast.makeText(requireContext(), "Verifica tu correo", Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupEventListeners() {
        binding.btnIniciarSesion.setOnClickListener {
            // Ocultar el teclado
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

            login()
        }

        binding.tvRegistrate.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.cardBtnGoogle.setOnClickListener {
            loginProvider = PROVIDER_GOOGLE
            showDialog()
        }

        binding.cardBtnFacebook.setOnClickListener {
            loginProvider = PROVIDER_FACEBOOK
            showDialog()
        }

        binding.etCorreo.doOnTextChanged { text, _, _, _ ->
            viewModel.onEmailChanged(text.toString())
            binding.etContenedorCorreo.error = null
        }

        binding.etClave.doOnTextChanged { text, _, _, _ ->
            viewModel.onPasswordChanged(text.toString())
            binding.etContenedorClave.error = null
        }

    }

    private fun login() {
        viewModel.signIn()
    }


    /**
     * Mostrar u ocultar el loader con fondo semitransparente
     */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(300).start()
            }
        } else {
            binding.loadingOverlay.animate()
                .alpha(0f).setDuration(300)
                .withEndAction { binding.loadingOverlay.visibility = View.GONE }
                .start()
        }
        // Opcional: deshabilitar botón mientras carga
        binding.btnIniciarSesion.isEnabled = !isLoading
    }

    fun login_facebook(){
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"));
        LoginManager.getInstance().registerCallback(
            Global.callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d("LOGIN_FACEBOOK_EXITO", "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d("LOGIN_FACEBOOK_CANCEL", "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.e("LOGIN_FACEEBOK_ERROR", "facebook:onError", error)
                    Toast.makeText(requireContext(), "ERROR DE FACEBOOK: ${error.message}", Toast.LENGTH_LONG).show()
                }
            },
        )
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val request = GraphRequest.newMeRequest(token) { obj, _ ->
            val nombre = obj?.optString("first_name") ?: ""
            val apellido = obj?.optString("last_name") ?: ""
            val email = obj?.optString("email") ?: ""

            viewModel.signInWithFacebook(token, nombre, apellido, email)
        }

        val parameters = Bundle()
        parameters.putString("fields", "first_name,last_name,email")
        request.parameters = parameters
        request.executeAsync()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        Global.callbackManager.onActivityResult(requestCode, resultCode, data)
    }

}
