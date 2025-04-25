import com.example.coffee4n.model.Owner
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class OwnerRepository {
    private val ownersRef = FirebaseDatabase.getInstance().getReference("owners")

    fun getOwnersFlow(): Flow<List<Owner>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val owners = snapshot.children.mapNotNull { ownerSnapshot ->
                    // Bỏ qua trường "data" bằng cách tạo Owner từ các field cụ thể
                    val ownerId = ownerSnapshot.child("ownerId").getValue(String::class.java) ?: return@mapNotNull null
                    val email = ownerSnapshot.child("email").getValue(String::class.java) ?: ""
                    val avatarUrl = ownerSnapshot.child("avatarUrl").getValue(String::class.java) ?: ""
                    val passCode = ownerSnapshot.child("passCode").getValue(String::class.java) ?: ""
                    val shopName = ownerSnapshot.child("shopName").getValue(String::class.java) ?: ""
                    val shopAddress = ownerSnapshot.child("shopAddress").getValue(String::class.java) ?: ""

                    Owner(
                        ownerId = ownerId,
                        email = email,
                        avatarUrl = avatarUrl,
                        passCode = passCode,
                        shopName = shopName,
                        shopAddress = shopAddress
                    )
                }
                trySend(owners).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ownersRef.addValueEventListener(listener)
        awaitClose { ownersRef.removeEventListener(listener) }
    }

    fun getOwner(ownerId: String): Flow<Owner?> = callbackFlow {
        val ownerRef = ownersRef.child(ownerId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val owner = snapshot.takeIf { it.exists() }?.let {
                    val id = it.child("ownerId").getValue(String::class.java) ?: return@let null
                    val email = it.child("email").getValue(String::class.java) ?: ""
                    val avatarUrl = it.child("avatarUrl").getValue(String::class.java) ?: ""
                    val passCode = it.child("passCode").getValue(String::class.java) ?: ""
                    val shopName = it.child("shopName").getValue(String::class.java) ?: ""
                    val shopAddress = it.child("shopAddress").getValue(String::class.java) ?: ""

                    Owner(
                        ownerId = id,
                        email = email,
                        avatarUrl = avatarUrl,
                        passCode = passCode,
                        shopName = shopName,
                        shopAddress = shopAddress
                    )
                }

                trySend(owner).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ownerRef.addValueEventListener(listener)
        awaitClose { ownerRef.removeEventListener(listener) }
    }


    fun updateOwner(owner: Owner) {
        val ownerRef = ownersRef.child(owner.ownerId)
        val updates = mapOf(
            "ownerId" to owner.ownerId,
            "email" to owner.email,
            "avatarUrl" to owner.avatarUrl,
            "passCode" to owner.passCode,
            "shopName" to owner.shopName,
            "shopAddress" to owner.shopAddress
        )
        ownerRef.updateChildren(updates)
    }
}
