package org.groovy.util

import groovy.sql.Sql
import org.groovy.common.Commons

import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class RSAKeyStoreUtil {

    Sql db = new Sql(Commons.getDAO());

    def getKey(String alias, boolean isPubKey) {
        def row = db.firstRow("select k.* from ws_rsa_key_store k where k.key_alias=${alias}")
        if (!row) {
            return null
        }
        def factory = KeyFactory.getInstance('RSA')
        if (isPubKey) {
            if (!row.public_key) {
                return null
            }
            return factory.generatePublic(new X509EncodedKeySpec(row.public_key.decodeBase64()))
        } else {
            if (!row.private_key) {
                return null
            }
            return factory.generatePrivate(new PKCS8EncodedKeySpec(row.private_key.decodeBase64()))
        }
    }

    def setKey(String alias, Key key) {
        def isPubKey = false
        if (key instanceof PublicKey) {
            isPubKey = true
        }

        def keyData = key.encoded.encodeBase64() as String
        db.withTransaction {
            // lock
            db.firstRow("select k.id from ws_rsa_key_store k where k.key_alias=${alias} for update")
            def n = db.firstRow("select count(id) n from ws_rsa_key_store where key_alias=${alias}").n as long
            if (n > 0) {
                if (isPubKey) {
                    db.executeUpdate("update ws_rsa_key_store k set k.public_key=${keyData} where k.key_alias=${alias}")
                } else {
                    db.executeUpdate("update ws_rsa_key_store k set k.private_key=${keyData} where k.key_alias=${alias}")
                }
            } else {
                def row = [
                        id       : db.firstRow("select seq_wsrsakeystore.nextval n from dual").n as long,
                        key_alias: alias
                ]
                if (isPubKey) {
                    row.public_key = keyData
                } else {
                    row.private_key = keyData
                }
                db.dataSet('ws_rsa_key_store').add(row)
            }
        }
    }

    def genKeypair() {
        def gen = KeyPairGenerator.getInstance('RSA')
        gen.initialize(2048, new SecureRandom())
        def pair = gen.generateKeyPair()
        [privateKey: pair.private, publicKey: pair.public]
    }
}
