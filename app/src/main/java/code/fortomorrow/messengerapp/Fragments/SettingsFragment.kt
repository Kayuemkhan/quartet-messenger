package code.fortomorrow.messengerapp.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.TokenWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import code.fortomorrow.messengerapp.ModelClasses.Users

import code.fortomorrow.messengerapp.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {
    var usersReference:DatabaseReference? = null
    var firebaseUser:FirebaseUser? = null
    private val RequestCode = 435
    private var imageUri:Uri? = null
    private var storageRef: StorageReference? = null

    private var socialChecker:String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view =inflater.inflate(R.layout.fragment_settings, container, false)

        firebaseUser= FirebaseAuth.getInstance().currentUser
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")
        usersReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        usersReference!!.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val user:Users? = p0.getValue(Users::class.java)

                   if (context!=null){
                       view.username_settings.text = user!!.getUserName()
                       Picasso.get().load(user.getProfile()).placeholder(R.drawable.pp).into(view.profile_image_settings)
                       //Picasso.get().load(user.getCover()).placeholder(R.drawable.pp).into(view.cover_image_settings)
                   }
                }
            }

        })
        view.profile_image_settings.setOnClickListener{
            picImage()
        }
        view.set_facebook.setOnClickListener{
            socialChecker = "facebook"
            setSociallink()
        }
        view.set_instragram.setOnClickListener{
            socialChecker = "instragram"
            setSociallink()
        }
        view.set_website.setOnClickListener{
            socialChecker = "website"
            setSociallink()
        }



        return  view
    }

    private fun setSociallink()
    {
        val builder:AlertDialog.Builder= AlertDialog.Builder(context!!,R.style.Theme_AppCompat_Dialog_Alert)

        if (socialChecker =="website")
        {
            builder.setTitle("Write Url:")
        }
        else{
            builder.setTitle("Write Username:")
        }
        val editText = EditText(context)

        editText.hint= "e.g abdul123"

        builder.setView(editText)
        builder.setPositiveButton("Create", DialogInterface.OnClickListener{
            dialog, which ->
            val str = editText.text.toString()
            if (str ==""){
                Toast.makeText(context,"pleae write something...",Toast.LENGTH_LONG).show()
            }
            else{
                saveSocialLink(str)
            }
        })
        builder.setNegativeButton("No",DialogInterface.OnClickListener{
            dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun saveSocialLink(str: String)
    {
        val mapSocial=HashMap<String,Any>()
        when(socialChecker)
        {
            "facebook"->
            {
                mapSocial["facebook"] ="https://m.facebook.com/$str"
            }
            "instagram"->
            {
                mapSocial["instagram"] ="https://m.facebook.com/$str"
            }
            "website"->
            {
                mapSocial["webite"] ="https://$str"
            }
        }
        usersReference!!.updateChildren(mapSocial).addOnCompleteListener{
            task ->
            if (task.isComplete){
                Toast.makeText(context,"Update Successfully",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun picImage(){
        val intent = Intent()
        intent.type ="image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,RequestCode)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data!=null){
            imageUri = data.data
            Toast.makeText(context,"Uploading...",Toast.LENGTH_LONG).show()
            uploadImagetoDatabase()
        }
    }

    private fun uploadImagetoDatabase() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Image is uploading, please Wait...")
        progressBar.show()

        if(imageUri !=null){
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString()+".jpg")

            var uploadTask:StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask {task->
                if(!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@continueWithTask fileRef.downloadUrl
            }.addOnCompleteListener{task ->
                if (task.isSuccessful){
                    val downloadurl = task.result
                    val url =downloadurl.toString()

                    val mapProfileImg=HashMap<String,Any>()
                    mapProfileImg["profile"] = url
                    usersReference!!.updateChildren(mapProfileImg)

                    progressBar.dismiss()
                }

            }.addOnCanceledListener {
                progressBar.dismiss()
                Toast.makeText(context,"Error", Toast.LENGTH_LONG).show()
            }
        }
    }

}


