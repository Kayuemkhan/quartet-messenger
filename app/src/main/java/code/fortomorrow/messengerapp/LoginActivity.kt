package code.fortomorrow.messengerapp

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure? ")
        builder.setMessage("Do You want to Close the application")
        builder.setPositiveButton("Yes",{ dialogInterface: DialogInterface, i: Int ->
            finish()
        })
        builder.setNegativeButton("No",{ dialogInterface: DialogInterface, i: Int -> })
        builder.show()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        signupTextId.setOnClickListener{
            startActivity(Intent(this,RegisterActivity::class.java))
            finish()
        }
        login_btn.setOnClickListener{
            loginuser()
        }
    }

    private fun loginuser() {
        val email:String = email_login.text.toString()
        val password:String = password_login.text.toString()

        if(email.equals("")){
            email_login.setError("Email Field can't be Blank")
            return
        }
        else if(password.equals("")){
            password_login.setError("Password Field can't be Blank")
            return
        }
        else{
            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{task ->
                    if (task.isSuccessful){
                        val intent =Intent(this,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        Toast.makeText(this,"Error Message: "+task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }


}
