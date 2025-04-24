package com.example.coffee4n.session

object OwnerSession {
    var ownerId: String = ""

    fun getReferencePath(model: String): String {
        return "owners/$ownerId/data/$model"
    }

    fun getMetadataPath(lastModelId: String): String {
        return "owners/$ownerId/data/metadata/$lastModelId"
    }
}