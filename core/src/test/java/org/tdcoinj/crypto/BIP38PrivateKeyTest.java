/*
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tdcoinj.crypto;

import org.tdcoinj.core.AddressFormatException;
import org.tdcoinj.core.Base58;
import org.tdcoinj.core.ECKey;
import org.tdcoinj.core.NetworkParameters;
import org.tdcoinj.core.Utils;
import org.tdcoinj.crypto.BIP38PrivateKey.BadPassphraseException;
import org.tdcoinj.params.MainNetParams;
import org.tdcoinj.params.TestNet3Params;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class BIP38PrivateKeyTest {
    private static final NetworkParameters MAINNET = MainNetParams.get();
    private static final NetworkParameters TESTNET = TestNet3Params.get();

    @Test
    public void bip38testvector_noCompression_noEcMultiply_test1() throws Exception {
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PRPsZ2Hfur4ik4Bbvtns8rYLKKYJuq4JHCTct6DeFDWitrwxEkoPwfpVg"); //TDCOIN

        ECKey key = encryptedKey.decrypt("TestingOneTwoThree");
        assertEquals("4bNrhYc9RjJwHVgX85H6urdgAETRKBsuzPGcG4exKVVsjwKRxrs", key.getPrivateKeyEncoded(MAINNET)
                .toString());//TDCOIN
    }

    @Test
    public void bip38testvector_noCompression_noEcMultiply_test2() throws Exception {
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PRQjvj7eQm4g1CGaei8rMMb5ZX8P99zTAiYoxm8Rq154sfTdTPRQWxFbK");
        ECKey key = encryptedKey.decrypt("Satoshi");
        assertEquals("4bd4aEqiA6tdJeVxPvhp42DRKZ4BpwC8KhqiUT1szzYfUmJzMRL", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_noCompression_noEcMultiply_test3() throws Exception {
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PRWSfi2UFo9SKvu1UrPBaSS43oa5o51wx7S6aau5hMULewyQ4dBU4hzmJ");
        StringBuilder passphrase = new StringBuilder();
        //passphrase.appendCodePoint(0x0021); // GREEK UPSILON WITH HOOK
        passphrase.appendCodePoint(0x03d2); // GREEK UPSILON WITH HOOK
        passphrase.appendCodePoint(0x0301); // COMBINING ACUTE ACCENT
        passphrase.appendCodePoint(0x0000); // NULL
        passphrase.appendCodePoint(0x010400); // DESERET CAPITAL LETTER LONG I
        passphrase.appendCodePoint(0x01f4a9); // PILE OF POO
        ECKey key = encryptedKey.decrypt(passphrase.toString());
        assertEquals("4c21GQvqwUwYnBkH8VpRLtmav9KimT7KmshS63rjrNt8rAGEa9S", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_compression_noEcMultiply_test1() throws Exception {
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PRKwu61LgFme437GPjwNixXdWeKL5LkGfNQ6tbwCePAHYUjnNHF3NJkBx");
        ECKey key = encryptedKey.decrypt("TestingOneTwoThree");
        assertEquals("4cW3ncTBGTjg1FQGUW8nVF3j3uWwzE2aKtSTaVbcajUCsEsD6Ny", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_compression_noEcMultiply_test2() throws Exception {
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PRKwu61MKBUYDYNfm8wQZhUjViyHTxLk4RSam8iCKhm1FypjSTNUrHBrr");
        ECKey key = encryptedKey.decrypt("Satoshi");
        assertEquals("4cW3ncTBGTjg1FQGUW8nVF3j3uWwzE2aKtSTaVbcajUCsEsD6Ny", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_ecMultiply_noCompression_noLotAndSequence_test1() throws Exception {
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PRNSj61vf5TM4qLWwgNinkkDYukE749MRYhPhjaQSRXkxTvEH1Z2XgVTD");//TDCOIN
        ECKey key = encryptedKey.decrypt("TestingOneTwoThree");
        assertEquals("4bJPYprT4gAQ6kXhgZMUS59Ar1mAAxeo5XipS8zs4ZkrmHHrEYp", key.getPrivateKeyEncoded(MAINNET)
                .toString());//TDCOIN
    }

    @Test
    public void bip38testvector_ecMultiply_noCompression_noLotAndSequence_test2() throws Exception {
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PRNSj61xN2rNBN7dfn2wpCN8Czmu9jMHcUwiUrZmzySDdT39oq9jD9B9o");//TDCOIN
        ECKey key = encryptedKey.decrypt("Satoshi");
        assertEquals("4bJPYprT4gAQ6kXhgZMUS59Ar1mAAxeo5XipS8zs4ZkrmHHrEYp", key.getPrivateKeyEncoded(MAINNET)
                .toString());//TDCOIN
    }

    @Test
    public void bip38testvector_ecMultiply_noCompression_lotAndSequence_test1() throws Exception {
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PRNSj61wGZGXuW8Q658DaVitN2fgrXTPxaLP6DUnF6qKmYbjYyq4TTEQx");//TDCOIN
        ECKey key = encryptedKey.decrypt("MOLON LABE");
        assertEquals("4bJPYprT4gAQ6kXhgZMUS59Ar1mAAxeo5XipS8zs4ZkrmHHrEYp", key.getPrivateKeyEncoded(MAINNET)
                .toString());//TDCOIN
    }

    @Test
    public void bip38testvector_ecMultiply_noCompression_lotAndSequence_test2() throws Exception {
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PRNSj61wMVxDvREYQ5whU3ck52XTeKactTKmcy1b9FhmwGLedksSRZvLj");//TDCOIN
        ECKey key = encryptedKey.decrypt("ΜΟΛΩΝ ΛΑΒΕ");
        assertEquals("4bJPYprT4gAQ6kXhgZMUS59Ar1mAAxeo5XipS8zs4ZkrmHHrEYp", key.getPrivateKeyEncoded(MAINNET)
                .toString());//TDCOIN
    }

    @Test
    public void tdcoinpaperwallet_testnet() throws Exception {
        // values taken from tdcoinpaperwallet.com
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(TESTNET,
                "6PRPhQhmtw6dQu6jD8E1KS4VphwJxBS9Eh9C8FQELcrwN3vPvskv9NKvuL");
        ECKey key = encryptedKey.decrypt("password");
        assertEquals("93MLfjbY6ugAsLeQfFY6zodDa8izgm1XAwA9cpMbUTwLkDitopg", key.getPrivateKeyEncoded(TESTNET)
                .toString());
    }

    @Test
    public void bitaddress_testnet() throws Exception {
        // values taken from bitaddress.org
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(TESTNET,
                "6PfMmVHn153N3x83Yiy4Nf76dHUkXufe2Adr9Fw5bewrunGNeaw2QCpifb");
        ECKey key = encryptedKey.decrypt("password");
        assertEquals("91tCpdaGr4Khv7UAuUxa6aMqeN5GcPVJxzLtNsnZHTCndxkRcz2", key.getPrivateKeyEncoded(TESTNET)
                .toString());
    }

    @Test(expected = BadPassphraseException.class)
    public void badPassphrase() throws Exception {
        BIP38PrivateKey encryptedKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PRVWUbkzzsbcVac2qwfssoUJAN1Xhrg6bNk8J7Nzm5H7kxEbn2Nh2ZoGg");
        encryptedKey.decrypt("BAD");
    }

    @Test(expected = AddressFormatException.InvalidDataLength.class)
    public void fromBase58_invalidLength() {
        String base58 = Base58.encodeChecked(1, new byte[16]);
        BIP38PrivateKey.fromBase58(null, base58);
    }

    @Test
    public void testJavaSerialization() throws Exception {
        BIP38PrivateKey testKey = BIP38PrivateKey.fromBase58(TESTNET,
                "6PfMmVHn153N3x83Yiy4Nf76dHUkXufe2Adr9Fw5bewrunGNeaw2QCpifb");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        new ObjectOutputStream(os).writeObject(testKey);
        BIP38PrivateKey testKeyCopy = (BIP38PrivateKey) new ObjectInputStream(
                new ByteArrayInputStream(os.toByteArray())).readObject();
        assertEquals(testKey, testKeyCopy);

        BIP38PrivateKey mainKey = BIP38PrivateKey.fromBase58(MAINNET,
                "6PfMmVHn153N3x83Yiy4Nf76dHUkXufe2Adr9Fw5bewrunGNeaw2QCpifb");
        os = new ByteArrayOutputStream();
        new ObjectOutputStream(os).writeObject(mainKey);
        BIP38PrivateKey mainKeyCopy = (BIP38PrivateKey) new ObjectInputStream(
                new ByteArrayInputStream(os.toByteArray())).readObject();
        assertEquals(mainKey, mainKeyCopy);
    }

    @Test
    public void cloning() throws Exception {
        BIP38PrivateKey a = BIP38PrivateKey.fromBase58(TESTNET, "6PfMmVHn153N3x83Yiy4Nf76dHUkXufe2Adr9Fw5bewrunGNeaw2QCpifb");
        // TODO: Consider overriding clone() in BIP38PrivateKey to narrow the type
        BIP38PrivateKey b = (BIP38PrivateKey) a.clone();

        assertEquals(a, b);
        assertNotSame(a, b);
    }

    @Test
    public void roundtripBase58() throws Exception {
        String base58 = "6PfMmVHn153N3x83Yiy4Nf76dHUkXufe2Adr9Fw5bewrunGNeaw2QCpifb";
        assertEquals(base58, BIP38PrivateKey.fromBase58(MAINNET, base58).toBase58());
    }
}
