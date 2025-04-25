package com.example.coffee4n.repository

import com.example.coffee4n.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.coffee4n.session.Models
import com.example.coffee4n.session.OwnerSession

class CustomerRepository (
    private val firebaseDatabase: FirebaseDatabase
) {
    private val customerRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.User))

    fun getCustomersFlow(): Flow<List<User>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customers = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                trySend(customers).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        customerRef.addValueEventListener(listener)
        awaitClose { customerRef.removeEventListener(listener) }
    }
}