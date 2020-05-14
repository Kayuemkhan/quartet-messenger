package code.fortomorrow.messengerapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String =""

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
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()


        register_btn.setOnClickListener{
            registerUser()
        }
        signinTextId.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }


    private fun registerUser() {
        val username:String = username_register.text.toString()
        val email:String = email_register.text.toString()
        val password:String = password_register.text.toString()

        if(username.equals("")){
            username_register.setError("Name Field can't be Blank")
            return
        }
        else if(email.equals("")){
            email_register.setError("Email Field can't be Blank")
            return
        }
        else if(password.equals("")){
            password_register.setError("Password Field can't be Blank")
            return
        }
        else if((!email.contains("@")) or (!email.contains(".com"))){
            email_register.setError("Email is not corrected")
            return
        }
        else{
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task ->
                if (task.isSuccessful){
                    firebaseUserID = mAuth.currentUser!!.uid
                    refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                    val userHashMap = HashMap<String,Any>()
                    userHashMap["uid"] = firebaseUserID
                    userHashMap["username"] = username
                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/messenger-22f1f.appspot.com/o/pp.png?alt=media&token=8fb70735-e305-409c-8ce4-fb31f04c505f"
                    userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/messenger-22f1f.appspot.com/o/dp.jpg?alt=media&token=d079acc3-044f-4f59-a41b-790ec5785eef"
                    userHashMap["status"] = "offline"
                    userHashMap["search"] = username.toLowerCase()
                    userHashMap["facebook"] = "https://m.facebook.com"
                    userHashMap["instragram"] = "https://m.instragram.com"
                    userHashMap["website"] = "https://www.google.com"

                    refUsers.updateChildren(userHashMap)
                        .addOnCompleteListener{task->
                            if(task.isSuccessful){
                               val intent =Intent(this,LoginActivity::class.java)
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                }
                else{
                    Toast.makeText(this,"Error Message "+task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }

        }
    }
}
