package code.fortomorrow.messengerapp

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {
    var firebaseUser: FirebaseUser? = null


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
        setContentView(R.layout.activity_welcome)
        register_welcome_btn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
            finish()
        })
        login_welcome_btn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        })
    }

    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}
